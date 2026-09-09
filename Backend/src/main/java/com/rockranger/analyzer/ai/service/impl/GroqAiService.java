package com.rockranger.analyzer.ai.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.analyzer.ai.dto.*;
import com.rockranger.analyzer.ai.exception.AiServiceException;
import com.rockranger.analyzer.ai.service.AiService;
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
public class GroqAiService implements AiService {

    private static final Logger logger = LoggerFactory.getLogger(GroqAiService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;

    public GroqAiService(
            @Value("${groq.api-url:https://api.groq.com/openai/v1/chat/completions}") String apiUrl,
            @Value("${groq.api-key}") String apiKey,
            @Value("${groq.api-model:openai/gpt-oss-120b}") String model,
            ObjectMapper objectMapper
    ) {
        this.model = model;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public ResumeAiResponse analyzeResume(String resumeText) {
        if (resumeText == null || resumeText.isBlank()) {
            throw new AiServiceException("Cannot analyze resume: Extracted text is empty.");
        }

        try {
            String systemPrompt = """
                    You are an expert ATS (Applicant Tracking System) and Technical Career Consultant.
                    Analyze the candidate's resume text and return a SINGLE valid JSON object with the following schema:
                    {
                      "structuredResume": {
                        "candidate": {
                          "name": "Candidate Full Name or empty string",
                          "email": "Email address or empty string",
                          "phone": "Phone number or empty string",
                          "role": "Primary professional title or targeted role"
                        },
                        "summary": "Professional summary or objective",
                        "skills": {
                          "programmingLanguages": ["Java", "Python"],
                          "backend": ["Spring Boot", "REST APIs"],
                          "frontend": ["React", "HTML"],
                          "database": ["PostgreSQL"],
                          "tools": ["Git", "Docker"]
                        },
                        "experience": [
                          {
                            "company": "Company Name",
                            "role": "Job Title",
                            "duration": "Start - End Date",
                            "responsibilities": ["Responsibility or accomplishment 1"]
                          }
                        ],
                        "projects": [
                          {
                            "name": "Project Name",
                            "technologies": ["Tech 1", "Tech 2"],
                            "description": "Project overview and outcomes"
                          }
                        ],
                        "education": [
                          {
                            "degree": "Degree Title",
                            "college": "Institution Name",
                            "cgpa": "Grade/CGPA/Percentage or empty string",
                            "year": "Graduation Year or empty string"
                          }
                        ]
                      },
                      "analysis": {
                        "strengths": ["Clear strength 1", "Clear strength 2"],
                        "weaknesses": ["Area for growth 1", "Area for growth 2"],
                        "improvements": ["Actionable recommendation 1"],
                        "missingKeywords": ["Industry keyword 1", "Skill keyword 2"],
                        "sectionScores": {
                          "summary": 8,
                          "skills": 9,
                          "experience": 7,
                          "projects": 8,
                          "education": 8
                        },
                        "overallScore": 82
                      }
                    }
                    Provide realistic, constructive section scores (1-10) and overallScore (1-100).
                    Return ONLY the JSON object.
                    """;

            String jsonContent = callGroq(systemPrompt, "Resume Text:\n\n" + resumeText, 0.2);
            return objectMapper.readValue(jsonContent, ResumeAiResponse.class);

        } catch (Exception e) {
            logger.error("Failed to analyze resume with Groq AI", e);
            throw new AiServiceException("Failed to analyze resume with Groq AI: " + e.getMessage(), e);
        }
    }

    @Override
    public List<InterviewQuestionAiResponse> generateInterviewQuestions(String structuredResume, Integer numberOfQuestions, String interviewType) {
        int count = (numberOfQuestions != null && numberOfQuestions > 0) ? numberOfQuestions : 10;
        String type = (interviewType != null && !interviewType.isBlank()) ? interviewType : "TECHNICAL";

        try {
            String systemPrompt = String.format("""
                    You are an expert technical interviewer and hiring manager conducting a realistic job interview.
                    Based on the candidate's structured resume JSON provided below, generate an interview plan with exactly %d interview questions tailored for a %s interview.
                    Ground the questions directly in the candidate's projects, work experience, technical skills, and background.
                    Include a balanced variety:
                    - Deep dives into projects they built and technologies they used
                    - Technical concepts, design principles, and problem-solving relevant to their tech stack
                    - Behavioral or situational questions based on their past experience
                    - System architecture or design appropriate to their experience level

                    Return a SINGLE valid JSON object with the following schema:
                    {
                      "questions": [
                        {
                          "questionNumber": 1,
                          "question": "Detailed interview question text",
                          "category": "TECHNICAL",
                          "difficulty": "MEDIUM"
                        }
                      ]
                    }

                    Valid categories: "TECHNICAL", "BEHAVIORAL", "SYSTEM_DESIGN", "PROJECT".
                    Valid difficulties: "EASY", "MEDIUM", "HARD".
                    Return ONLY the JSON object.
                    """, count, type);

            String userPrompt = "Structured Resume:\n\n" + (structuredResume != null ? structuredResume : "{}");
            String jsonContent = callGroq(systemPrompt, userPrompt, 0.3);

            JsonNode contentNode = objectMapper.readTree(jsonContent);
            if (contentNode.has("questions") && contentNode.get("questions").isArray()) {
                InterviewQuestionsAiWrapper wrapper = objectMapper.treeToValue(contentNode, InterviewQuestionsAiWrapper.class);
                return wrapper.getQuestions();
            } else if (contentNode.isArray()) {
                return objectMapper.readValue(jsonContent, new TypeReference<List<InterviewQuestionAiResponse>>() {});
            } else {
                throw new AiServiceException("Unexpected JSON format received for interview questions: " + jsonContent);
            }

        } catch (Exception e) {
            logger.error("Failed to generate interview questions with Groq AI", e);
            throw new AiServiceException("Failed to generate interview questions with Groq AI: " + e.getMessage(), e);
        }
    }

    @Override
    public AnswerEvaluationAiResponse evaluateAnswer(String structuredResume, String question, String answer) {
        try {
            String systemPrompt = """
                    You are an expert technical interviewer evaluating a candidate's answer during a job interview.
                    Evaluate the candidate's answer to the interview question objectively, considering the candidate's background and resume context if relevant.
                    Score the answer across:
                    1. score (1-100): Overall percentage score for this specific answer.
                    2. technicalAccuracy (1-10): How technically accurate, correct, and sound the answer is.
                    3. communicationScore (1-10): Clarity, structure, articulation, and concise delivery.
                    4. relevanceScore (1-10): Direct relevance to the specific question asked.

                    Provide constructive, actionable feedback and an exemplary model answer ('betterAnswer') demonstrating how a top candidate would answer.

                    Return a SINGLE valid JSON object with the following schema:
                    {
                      "score": 85,
                      "technicalAccuracy": 9,
                      "communicationScore": 8,
                      "relevanceScore": 9,
                      "feedback": "Constructive feedback highlighting what was done well and specific areas missing or needing improvement.",
                      "betterAnswer": "Exemplary model answer with ideal technical depth, clarity, and structure."
                    }
                    Return ONLY the JSON object.
                    """;

            String userPrompt = String.format("""
                    Candidate Resume Context:
                    %s

                    Interview Question:
                    %s

                    Candidate Answer:
                    %s
                    """,
                    structuredResume != null ? structuredResume : "N/A",
                    question != null ? question : "N/A",
                    answer != null ? answer : "No answer provided."
            );

            String jsonContent = callGroq(systemPrompt, userPrompt, 0.2);
            return objectMapper.readValue(jsonContent, AnswerEvaluationAiResponse.class);

        } catch (Exception e) {
            logger.error("Failed to evaluate answer with Groq AI", e);
            throw new AiServiceException("Failed to evaluate answer with Groq AI: " + e.getMessage(), e);
        }
    }

    @Override
    public FinalFeedbackAiResponse generateFinalFeedback(String structuredResume, List<Map<String, Object>> qaList) {
        try {
            String systemPrompt = """
                    You are an executive hiring committee chair and senior technical leader evaluating an entire interview session.
                    Review the candidate's resume context, the questions asked during the interview, the candidate's answers, and individual scores.
                    Synthesize a comprehensive final evaluation and scorecard:
                    1. overallScore (1-100): Weighted overall interview score.
                    2. technicalScore (1-100): Aggregate technical depth and accuracy score.
                    3. communicationScore (1-100): Aggregate communication clarity and articulation score.
                    4. problemSolvingScore (1-100): Aggregate problem solving, analytical thinking, and architecture score.
                    5. strengths: Array of 3 to 5 distinct, specific strengths demonstrated during the interview.
                    6. improvements: Array of 3 to 5 actionable, specific recommendations for improvement.
                    7. finalFeedback: Detailed overall assessment summary and career advice.

                    Return a SINGLE valid JSON object with the following schema:
                    {
                      "overallScore": 84,
                      "technicalScore": 86,
                      "communicationScore": 82,
                      "problemSolvingScore": 85,
                      "strengths": ["Clear explanation of concurrency...", "Strong understanding of Spring Boot"],
                      "improvements": ["Elaborate more on trade-offs...", "Structure answers using the STAR method"],
                      "finalFeedback": "Detailed summary and hiring committee assessment..."
                    }
                    Return ONLY the JSON object.
                    """;

            String qaJson = objectMapper.writeValueAsString(qaList != null ? qaList : new ArrayList<>());
            String userPrompt = String.format("""
                    Candidate Resume Context:
                    %s

                    Interview Questions and Answers Session:
                    %s
                    """,
                    structuredResume != null ? structuredResume : "N/A",
                    qaJson
            );

            String jsonContent = callGroq(systemPrompt, userPrompt, 0.2);
            return objectMapper.readValue(jsonContent, FinalFeedbackAiResponse.class);

        } catch (Exception e) {
            logger.error("Failed to generate final interview feedback with Groq AI", e);
            throw new AiServiceException("Failed to generate final interview feedback with Groq AI: " + e.getMessage(), e);
        }
    }

    private String callGroq(String systemPrompt, String userPrompt, double temperature) throws Exception {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "response_format", Map.of("type", "json_object"),
                "temperature", temperature
        );

        logger.info("Sending request to Groq using model: {}", model);

        String responseBody = restClient.post()
                .body(requestBody)
                .retrieve()
                .body(String.class);

        JsonNode rootNode = objectMapper.readTree(responseBody);
        String content = rootNode.path("choices").get(0).path("message").path("content").asText();
        logger.debug("Received Groq response: {}", content);
        return content;
    }
}
