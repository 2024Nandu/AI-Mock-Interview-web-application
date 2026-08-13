package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class DeepgramTtsService {
    private static final Logger logger = LoggerFactory.getLogger(DeepgramTtsService.class);

    @Value("${app.deepgram.api-key}")
    private String apiKey;

    @Value("${app.deepgram.api-url}")
    private String apiUrl;

    public byte[] generateSpeech(String text) {
        if (apiKey == null || apiKey.trim().isEmpty() 
                || apiKey.equalsIgnoreCase("YOUR_DEEPGRAM_API_KEY") 
                || apiKey.startsWith("YOUR_") 
                || apiKey.startsWith("your-")) {
            logger.warn("Deepgram API Key is unconfigured or placeholder. Skipping backend TTS (Frontend Web Speech API fallback active).");
            return new byte[0];
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Token " + apiKey);

            Map<String, String> body = new HashMap<>();
            body.put("text", text);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            
            // Call Deepgram v1 speak endpoint
            // query parameters: model=aura-asteria-en, encoding=linear16, container=wav
            String url = apiUrl + "?model=aura-asteria-en&encoding=linear16&container=wav";
            
            logger.info("Sending text to Deepgram for speech generation: [{}]", text);
            byte[] audioBytes = restTemplate.postForObject(url, request, byte[].class);
            
            if (audioBytes != null && audioBytes.length > 0) {
                logger.info("Successfully generated speech audio, bytes received: {}", audioBytes.length);
                return audioBytes;
            }
            logger.error("Deepgram returned empty or null audio bytes.");
        } catch (Exception e) {
            logger.error("Failed to generate speech using Deepgram TTS API: {}", e.getMessage(), e);
        }
        return new byte[0];
    }
}
