package com.rockranger.analyzer.voice.util;

import com.rockranger.analyzer.voice.exception.AudioProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class WavAudioMerger {

    private static final Logger logger = LoggerFactory.getLogger(WavAudioMerger.class);

    private WavAudioMerger() {
    }

    /**
     * Merges multiple WAV byte arrays with identical PCM audio format into a single valid WAV file.
     *
     * @param wavFiles List of WAV file byte arrays
     * @return Merged WAV audio byte array
     */
    public static byte[] merge(List<byte[]> wavFiles) {
        if (wavFiles == null || wavFiles.isEmpty()) {
            return new byte[0];
        }
        if (wavFiles.size() == 1) {
            return wavFiles.get(0);
        }

        try {
            byte[] firstWav = wavFiles.get(0);
            DataChunkInfo firstDataInfo = findDataChunk(firstWav);

            ByteArrayOutputStream combinedDataStream = new ByteArrayOutputStream();

            // Write first audio payload
            combinedDataStream.write(firstWav, firstDataInfo.dataOffset, firstDataInfo.dataSize);

            // Write subsequent audio payloads
            for (int i = 1; i < wavFiles.size(); i++) {
                byte[] currentWav = wavFiles.get(i);
                DataChunkInfo currentInfo = findDataChunk(currentWav);
                combinedDataStream.write(currentWav, currentInfo.dataOffset, currentInfo.dataSize);
            }

            byte[] combinedAudioData = combinedDataStream.toByteArray();
            int totalDataSize = combinedAudioData.length;
            int totalRiffSize = (firstDataInfo.dataOffset - 8) + totalDataSize;

            byte[] header = new byte[firstDataInfo.dataOffset];
            System.arraycopy(firstWav, 0, header, 0, firstDataInfo.dataOffset);

            // Update RIFF total size at offset 4 (little-endian 32-bit integer)
            ByteBuffer.wrap(header, 4, 4)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(totalRiffSize);

            // Update data chunk size right before dataOffset (little-endian 32-bit integer)
            ByteBuffer.wrap(header, firstDataInfo.dataOffset - 4, 4)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(totalDataSize);

            ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
            mergedOutput.write(header);
            mergedOutput.write(combinedAudioData);

            logger.info("Successfully merged {} WAV chunks into single WAV file (total size: {} bytes)",
                    wavFiles.size(), mergedOutput.size());

            return mergedOutput.toByteArray();

        } catch (Exception e) {
            logger.error("Failed to merge WAV audio chunks", e);
            throw new AudioProcessingException("Failed to concatenate audio chunks: " + e.getMessage(), e);
        }
    }

    private static DataChunkInfo findDataChunk(byte[] wav) {
        if (wav == null || wav.length < 44) {
            throw new AudioProcessingException("Invalid WAV file: Length is less than 44 bytes");
        }

        String riff = new String(wav, 0, 4, java.nio.charset.StandardCharsets.US_ASCII);
        String wave = new String(wav, 8, 4, java.nio.charset.StandardCharsets.US_ASCII);
        if (!"RIFF".equals(riff) || !"WAVE".equals(wave)) {
            throw new AudioProcessingException("Invalid WAV file: Missing RIFF/WAVE header marker");
        }

        // Scan for "data" chunk identifier
        for (int i = 12; i <= wav.length - 8; i++) {
            if (wav[i] == 'd' && wav[i + 1] == 'a' && wav[i + 2] == 't' && wav[i + 3] == 'a') {
                int dataOffset = i + 8;
                long declaredChunkSize = Integer.toUnsignedLong(
                        ByteBuffer.wrap(wav, i + 4, 4)
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .getInt()
                );

                int actualDataSize;
                // If declaredChunkSize is 0xFFFFFFFF (streaming placeholder -1) or exceeds available length:
                if (declaredChunkSize == 0xFFFFFFFFL || declaredChunkSize == 0 || dataOffset + declaredChunkSize > wav.length) {
                    actualDataSize = Math.max(0, wav.length - dataOffset);
                } else {
                    actualDataSize = (int) declaredChunkSize;
                }

                return new DataChunkInfo(dataOffset, actualDataSize);
            }
        }

        // Fallback: Default to standard 44-byte PCM header if no explicit "data" chunk found
        return new DataChunkInfo(44, Math.max(0, wav.length - 44));
    }

    private static class DataChunkInfo {
        final int dataOffset;
        final int dataSize;

        DataChunkInfo(int dataOffset, int dataSize) {
            this.dataOffset = dataOffset;
            this.dataSize = dataSize;
        }
    }
}
