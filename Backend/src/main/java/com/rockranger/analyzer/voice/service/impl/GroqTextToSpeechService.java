package com.rockranger.analyzer.voice.service.impl;

import com.rockranger.analyzer.voice.exception.AudioProcessingException;
import com.rockranger.analyzer.voice.service.TextToSpeechService;
import com.rockranger.analyzer.voice.util.WavAudioMerger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GroqTextToSpeechService implements TextToSpeechService {

    private static final Logger logger = LoggerFactory.getLogger(GroqTextToSpeechService.class);
    private static final int MAX_CHUNK_LENGTH = 190; // Orpheus English model limit is ~200 chars

    private final RestClient restClient;
    private final String model;
    private final String defaultVoice;

    public GroqTextToSpeechService(
            @Value("${groq.tts-url:https://api.groq.com/openai/v1/audio/speech}") String ttsUrl,
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.tts-model:canopylabs/orpheus-v1-english}") String model,
            @Value("${groq.tts-voice:hannah}") String defaultVoice
    ) {
        this.model = model;
        this.defaultVoice = defaultVoice;
        this.restClient = RestClient.builder()
                .baseUrl(ttsUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public byte[] synthesizeSpeech(String text) {
        return synthesizeSpeech(text, defaultVoice);
    }

    @Override
    public byte[] synthesizeSpeech(String text, String voice) {
        if (text == null || text.isBlank()) {
            throw new AudioProcessingException("Cannot synthesize speech: Input text is empty.");
        }

        String effectiveVoice = (voice != null && !voice.isBlank()) ? voice.trim().toLowerCase() : defaultVoice;
        String cleanText = text.trim();

        try {
            if (cleanText.length() <= MAX_CHUNK_LENGTH) {
                logger.info("Synthesizing speech for single chunk ({} chars) with voice '{}'", cleanText.length(), effectiveVoice);
                return callGroqTtsApi(cleanText, effectiveVoice);
            }

            logger.info("Input text exceeds {} chars (total: {}). Splitting into natural chunks...", MAX_CHUNK_LENGTH, cleanText.length());
            List<String> chunks = splitTextIntoChunks(cleanText, MAX_CHUNK_LENGTH);
            logger.info("Split text into {} chunks for sequential synthesis", chunks.size());

            List<byte[]> audioSegments = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                logger.debug("Synthesizing chunk {}/{}: \"{}\"", i + 1, chunks.size(), chunk);
                byte[] segmentAudio = callGroqTtsApi(chunk, effectiveVoice);
                audioSegments.add(segmentAudio);
            }

            try {
                return WavAudioMerger.merge(audioSegments);
            } catch (Exception e) {
                logger.warn("WAV audio merging encountered an issue, falling back to first audio segment: {}", e.getMessage());
                return audioSegments.get(0);
            }

        } catch (AudioProcessingException ape) {
            throw ape;
        } catch (Exception e) {
            logger.error("Failed to synthesize speech with Groq TTS", e);
            String message = e.getMessage();
            if (message != null && (message.contains("model_terms_required") || message.contains("requires terms acceptance"))) {
                throw new AudioProcessingException("The TTS model '" + model + "' requires terms acceptance in the Groq Console. Please visit https://console.groq.com/playground?model=canopylabs%2Forpheus-v1-english with your Groq account and click 'Accept Terms'.", e);
            }
            throw new AudioProcessingException("Failed to synthesize speech: " + message, e);
        }
    }

    private byte[] callGroqTtsApi(String input, String voice) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "input", input,
                "voice", voice,
                "response_format", "wav"
        );

        byte[] audioBytes = restClient.post()
                .body(requestBody)
                .accept(MediaType.parseMediaType("audio/wav"), MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .body(byte[].class);

        if (audioBytes == null || audioBytes.length == 0) {
            throw new AudioProcessingException("Groq TTS returned an empty audio response for input: " + input);
        }

        return audioBytes;
    }

    /**
     * Splits text into natural sentence/clause chunks that do not exceed maxLen characters.
     */
    private List<String> splitTextIntoChunks(String text, int maxLen) {
        List<String> result = new ArrayList<>();
        // Split by sentences first
        String[] sentences = text.split("(?<=[.!?])\\s+");

        StringBuilder currentChunk = new StringBuilder();
        for (String sentence : sentences) {
            if (sentence.isBlank()) {
                continue;
            }

            // If a single sentence exceeds maxLen, split it further by clauses or words
            if (sentence.length() > maxLen) {
                if (currentChunk.length() > 0) {
                    result.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
                result.addAll(splitLongSentence(sentence, maxLen));
                continue;
            }

            if (currentChunk.length() + sentence.length() + 1 <= maxLen) {
                if (currentChunk.length() > 0) {
                    currentChunk.append(" ");
                }
                currentChunk.append(sentence);
            } else {
                if (currentChunk.length() > 0) {
                    result.add(currentChunk.toString().trim());
                }
                currentChunk = new StringBuilder(sentence);
            }
        }

        if (currentChunk.length() > 0) {
            result.add(currentChunk.toString().trim());
        }

        return result;
    }

    private List<String> splitLongSentence(String sentence, int maxLen) {
        List<String> chunks = new ArrayList<>();
        String[] words = sentence.split("\\s+");
        StringBuilder sb = new StringBuilder();

        for (String word : words) {
            if (sb.length() + word.length() + 1 <= maxLen) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(word);
            } else {
                if (sb.length() > 0) {
                    chunks.add(sb.toString().trim());
                }
                sb = new StringBuilder(word);
            }
        }

        if (sb.length() > 0) {
            chunks.add(sb.toString().trim());
        }

        return chunks;
    }
}
