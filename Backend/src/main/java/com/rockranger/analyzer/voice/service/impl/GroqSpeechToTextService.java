package com.rockranger.analyzer.voice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.analyzer.voice.dto.response.SpeechToTextResponse;
import com.rockranger.analyzer.voice.exception.AudioProcessingException;
import com.rockranger.analyzer.voice.service.SpeechToTextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

@Service
public class GroqSpeechToTextService implements SpeechToTextService {

    private static final Logger logger = LoggerFactory.getLogger(GroqSpeechToTextService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public GroqSpeechToTextService(
            @Value("${groq.stt-url:https://api.groq.com/openai/v1/audio/transcriptions}") String sttUrl,
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.stt-model:whisper-large-v3-turbo}") String model,
            ObjectMapper objectMapper
    ) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(sttUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    @Override
    public SpeechToTextResponse transcribe(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new AudioProcessingException("Cannot transcribe: Audio file is missing or empty.");
        }

        try {
            String originalFilename = audioFile.getOriginalFilename();
            String effectiveFilename = (originalFilename != null && !originalFilename.isBlank())
                    ? originalFilename
                    : "audio.wav";

            logger.info("Transcribing audio file '{}' (size: {} bytes) using Groq Whisper model '{}'",
                    effectiveFilename, audioFile.getSize(), model);

            ByteArrayResource resource = new ByteArrayResource(audioFile.getBytes()) {
                @Override
                public String getFilename() {
                    return effectiveFilename;
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            body.add("model", model);
            body.add("response_format", "json");

            String responseBody = restClient.post()
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(responseBody);
            String transcribedText = rootNode.path("text").asText("");

            logger.info("Successfully transcribed audio. Result length: {} characters", transcribedText.length());
            return new SpeechToTextResponse(transcribedText.trim());

        } catch (Exception e) {
            logger.error("Failed to transcribe audio with Groq Whisper", e);
            throw new AudioProcessingException("Failed to transcribe audio with Groq Whisper: " + e.getMessage(), e);
        }
    }
}
