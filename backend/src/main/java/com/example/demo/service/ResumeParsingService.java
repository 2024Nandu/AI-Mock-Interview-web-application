package com.example.demo.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ResumeParsingService {
    private static final Logger logger = LoggerFactory.getLogger(ResumeParsingService.class);
    private final LlmClientService llmClientService;

    public ResumeParsingService(LlmClientService llmClientService) {
        this.llmClientService = llmClientService;
    }

    public String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        try (InputStream is = file.getInputStream()) {
            if (filename.toLowerCase().endsWith(".pdf")) {
                try (PDDocument doc = PDDocument.load(is)) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    return stripper.getText(doc);
                }
            } else if (filename.toLowerCase().endsWith(".docx")) {
                try (XWPFDocument doc = new XWPFDocument(is)) {
                    try (XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
                        return extractor.getText();
                    }
                }
            } else if (filename.toLowerCase().endsWith(".txt")) {
                return new String(is.readAllBytes());
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + filename);
            }
        }
    }

    public String parseResumeToJson(String rawText) {
        String systemPrompt = "You are an expert ATS (Applicant Tracking System) resume parser. " +
                "Your task is to analyze the raw resume text, extract structured data, evaluate its formatting, density, and impact, and output ONLY valid JSON matching this schema exactly. " +
                "Calculate an 'atsScore' (an integer between 0 and 100 based on standard ATS parameters: skill density, impact metrics, action verbs, clear structuring). " +
                "Compile a list of 'improvements' containing specific, actionable text recommendations for the candidate to improve their resume's ATS score.\n" +
                "Do not write any introductory or explanatory text. Do not wrap the JSON in markdown code blocks like ```json. Output ONLY the raw JSON string.\n" +
                "Schema:\n" +
                "{\n" +
                "  \"name\": \"Candidate Full Name\",\n" +
                "  \"email\": \"Candidate Email\",\n" +
                "  \"phone\": \"Candidate Phone\",\n" +
                "  \"atsScore\": 85,\n" +
                "  \"improvements\": [\n" +
                "    \"Add quantifiable metrics to experience details (e.g., speed increases, percentages).\",\n" +
                "    \"List key tools like Docker or AWS explicitly in the Skills section.\"\n" +
                "  ],\n" +
                "  \"skills\": [\"Skill 1\", \"Skill 2\"],\n" +
                "  \"experience\": [\n" +
                "    {\n" +
                "      \"company\": \"Company Name\",\n" +
                "      \"role\": \"Job Title\",\n" +
                "      \"duration\": \"Start Date - End Date\",\n" +
                "      \"highlights\": [\"Achievement 1\", \"Achievement 2\"]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"projects\": [\n" +
                "    {\n" +
                "      \"title\": \"Project Name\",\n" +
                "      \"description\": \"Brief description\",\n" +
                "      \"techStack\": [\"Tech 1\", \"Tech 2\"]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"education\": [\n" +
                "    {\n" +
                "      \"institution\": \"University Name\",\n" +
                "      \"degree\": \"Degree earned\",\n" +
                "      \"year\": \"Graduation Year\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"summary\": \"Brief profile summary\"\n" +
                "}";

        logger.info("Sending resume raw text to LLM for parsing...");
        String jsonResult = llmClientService.generateCompletion(systemPrompt, rawText, 0.2);
        
        if (jsonResult != null) {
            // strip markdown formatting blocks if the LLM output contains them despite the system prompt
            jsonResult = jsonResult.trim();
            if (jsonResult.startsWith("```json")) {
                jsonResult = jsonResult.substring(7);
            }
            if (jsonResult.endsWith("```")) {
                jsonResult = jsonResult.substring(0, jsonResult.length() - 3);
            }
            jsonResult = jsonResult.trim();
        }
        
        return jsonResult;
    }
}
