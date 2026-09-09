package com.rockranger.analyzer.voice;

import com.rockranger.analyzer.voice.util.WavAudioMerger;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WavAudioMergerTest {

    @Test
    void merge_EmptyList_ReturnsEmptyArray() {
        byte[] result = WavAudioMerger.merge(List.of());
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void merge_SingleFile_ReturnsSameBytes() {
        byte[] wav = createSyntheticPcmWav(50);
        byte[] result = WavAudioMerger.merge(List.of(wav));
        assertArrayEquals(wav, result);
    }

    @Test
    void merge_MultipleFiles_SuccessfullyMergesAudioData() {
        byte[] wav1 = createSyntheticPcmWav(100);
        byte[] wav2 = createSyntheticPcmWav(150);

        byte[] merged = WavAudioMerger.merge(List.of(wav1, wav2));

        assertNotNull(merged);
        // Header (44 bytes) + combined data (100 + 150 = 250 bytes)
        assertEquals(44 + 250, merged.length);

        // Verify RIFF and WAVE headers
        assertEquals("RIFF", new String(merged, 0, 4));
        assertEquals("WAVE", new String(merged, 8, 4));
        assertEquals("fmt ", new String(merged, 12, 4));
        assertEquals("data", new String(merged, 36, 4));

        // Verify updated RIFF total size: 36 + 250 = 286
        int riffSize = ByteBuffer.wrap(merged, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        assertEquals(286, riffSize);

        // Verify updated data chunk size: 250
        int dataSize = ByteBuffer.wrap(merged, 40, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        assertEquals(250, dataSize);
    }

    @Test
    void merge_StreamingWavWithUnknownDataLength_SuccessfullyMerges() {
        // Simulates Groq/OpenAI streaming WAV where data chunk has 0xFFFFFFFF (-1) and offset is 78
        byte[] streamingWav1 = createStreamingPcmWav(78, 200);
        byte[] streamingWav2 = createStreamingPcmWav(78, 300);

        byte[] merged = WavAudioMerger.merge(List.of(streamingWav1, streamingWav2));

        assertNotNull(merged);
        // Header (78 bytes) + combined data (200 + 300 = 500 bytes)
        assertEquals(78 + 500, merged.length);

        assertEquals("RIFF", new String(merged, 0, 4));
        assertEquals("WAVE", new String(merged, 8, 4));
        assertEquals("data", new String(merged, 70, 4));

        int dataSize = ByteBuffer.wrap(merged, 74, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
        assertEquals(500, dataSize);
    }

    private byte[] createStreamingPcmWav(int headerSize, int pcmDataLength) {
        byte[] wav = new byte[headerSize + pcmDataLength];
        ByteBuffer buffer = ByteBuffer.wrap(wav).order(ByteOrder.LITTLE_ENDIAN);

        buffer.put("RIFF".getBytes());
        buffer.putInt(-1); // 0xFFFFFFFF streaming RIFF size
        buffer.put("WAVE".getBytes());

        // Pad with arbitrary header chunks up to (headerSize - 8)
        int paddingLen = headerSize - 8 - 12; // 12 bytes already written
        for (int i = 0; i < paddingLen; i++) {
            buffer.put((byte) 0);
        }

        // data chunk at (headerSize - 8)
        buffer.put("data".getBytes());
        buffer.putInt(-1); // 0xFFFFFFFF streaming data size

        // Audio payload
        for (int i = 0; i < pcmDataLength; i++) {
            buffer.put((byte) 42);
        }

        return wav;
    }

    private byte[] createSyntheticPcmWav(int pcmDataLength) {
        byte[] wav = new byte[44 + pcmDataLength];
        ByteBuffer buffer = ByteBuffer.wrap(wav).order(ByteOrder.LITTLE_ENDIAN);

        // RIFF header
        buffer.put("RIFF".getBytes());
        buffer.putInt(36 + pcmDataLength);
        buffer.put("WAVE".getBytes());

        // fmt chunk
        buffer.put("fmt ".getBytes());
        buffer.putInt(16);          // Subchunk1Size for PCM
        buffer.putShort((short) 1);  // AudioFormat = 1 (PCM)
        buffer.putShort((short) 1);  // NumChannels = 1 (Mono)
        buffer.putInt(16000);        // SampleRate = 16kHz
        buffer.putInt(32000);        // ByteRate = 16000 * 1 * 2
        buffer.putShort((short) 2);  // BlockAlign = 2
        buffer.putShort((short) 16); // BitsPerSample = 16

        // data chunk
        buffer.put("data".getBytes());
        buffer.putInt(pcmDataLength);

        // Synthetic PCM audio samples
        for (int i = 0; i < pcmDataLength; i++) {
            buffer.put((byte) (i % 128));
        }

        return wav;
    }
}
