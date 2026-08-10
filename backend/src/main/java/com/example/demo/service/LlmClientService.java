package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LlmClientService {
    private static final Logger logger = LoggerFactory.getLogger(LlmClientService.class);

    @Value("${app.groq.api-key}")
    private String apiKey;

    @Value("${app.groq.api-url}")
    private String apiUrl;

    @Value("${app.groq.model}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateCompletion(String systemPrompt, String userPrompt, double temperature) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            List<Map<String, String>> messages = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                messages.add(Map.of("role", "system", "content", systemPrompt));
            }
            messages.add(Map.of("role", "user", "content", userPrompt));

            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", messages);
            body.put("temperature", temperature);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode choices = rootNode.get("choices");
                if (choices != null && choices.isArray() && !choices.isEmpty()) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null && message.has("content")) {
                        return message.get("content").asText();
                    }
                }
            }
            logger.error("Failed to parse LLM completion response. Status: {}", response.getStatusCode());
        } catch (Exception e) {
            logger.error("Exception calling Groq LLM API: {}", e.getMessage(), e);
        }
        return null;
    }
}
