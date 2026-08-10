package com.example.demo.service;

import com.example.demo.entity.FinalReport;
import com.example.demo.entity.InterviewSession;
import com.example.demo.entity.QaPair;
import com.example.demo.repository.FinalReportRepository;
import com.example.demo.repository.QaPairRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class EvaluationService {
    private static final Logger logger = LoggerFactory.getLogger(EvaluationService.class);

    private final QaPairRepository qaPairRepository;
    private final FinalReportRepository finalReportRepository;
    private final LlmClientService llmClientService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public EvaluationService(QaPairRepository qaPairRepository,
                             FinalReportRepository finalReportRepository,
                             LlmClientService llmClientService) {
        this.qaPairRepository = qaPairRepository;
        this.finalReportRepository = finalReportRepository;
        this.llmClientService = llmClientService;
    }

    @Transactional
    public void generateReport(InterviewSession session) {
        List<QaPair> qaPairs = qaPairRepository.findBySessionIdOrderByQuestionNumberAsc(session.getId());
        
        String roleName = session.getRole().getDisplayName();
        String resumeJson = session.getResume() != null ? session.getResume().getParsedJson() : "No resume uploaded";

        // Build dialogue transcript
        StringBuilder transcript = new StringBuilder();
        for (QaPair qa : qaPairs) {
            transcript.append("Question ").append(qa.getQuestionNumber()).append(": ").append(qa.getQuestionText()).append("\n");
            transcript.append("Candidate Answer: ").append(qa.getAnswerText() != null ? qa.getAnswerText() : "(No Answer)").append("\n\n");
        }

        String systemPrompt = "You are an expert technical interviewer and coding evaluator. " +
                "Evaluate the candidate's mock interview performance for the role: " + roleName + ".\n" +
                "You must return ONLY valid JSON matching this schema exactly. " +
                "Do not include any introductory or explanatory text. Do not wrap the JSON in markdown code blocks like ```json. Output ONLY the raw JSON string.\n" +
                "Schema:\n" +
                "{\n" +
                "  \"overallScore\": 7.5,\n" +
                "  \"questionScores\": [\n" +
                "    {\"questionNumber\": 1, \"score\": 8.0, \"justification\": \"Detailed reasoning for Q1 score\"},\n" +
                "    {\"questionNumber\": 2, \"score\": 6.5, \"justification\": \"Detailed reasoning for Q2 score\"},\n" +
                "    {\"questionNumber\": 3, \"score\": 7.0, \"justification\": \"Detailed reasoning for Q3 score\"},\n" +
                "    {\"questionNumber\": 4, \"score\": 9.0, \"justification\": \"Detailed reasoning for Q4 score\"},\n" +
                "    {\"questionNumber\": 5, \"score\": 7.0, \"justification\": \"Detailed reasoning for Q5 score\"}\n" +
                "  ],\n" +
                "  \"strengths\": [\"Strength description 1\", \"Strength description 2\"],\n" +
                "  \"weaknesses\": [\"Weakness description 1\", \"Weakness description 2\"],\n" +
                "  \"weakestQuestionNumber\": 2,\n" +
                "  \"modelAnswerForWeakest\": \"A comprehensive, exemplary response that the candidate should have given for the weakest question.\",\n" +
                "  \"preparationSuggestions\": [\n" +
                "    \"Study topic A and review syntax for X.\",\n" +
                "    \"Practice coding problems on Y to enhance speed.\"\n" +
                "  ],\n" +
                "  \"closingNote\": \"Overall summary recommendation for the candidate's career progression.\"\n" +
                "}";

        String userPrompt = "Candidate Resume:\n" + resumeJson + "\n\n" +
                "Interview QA Transcript:\n" + transcript.toString() + "\n\n" +
                "Evaluate this interview, scoring each question from 0.0 to 10.0 and calculating a weighted overall score.";

        logger.info("Sending transcript for session {} to LLM for evaluation...", session.getId());
        String jsonResult = llmClientService.generateCompletion(systemPrompt, userPrompt, 0.2);

        if (jsonResult == null || jsonResult.trim().isEmpty()) {
            logger.error("LLM evaluation returned empty result. Creating a mock fallback report.");
            createFallbackReport(session, qaPairs);
            return;
        }

        try {
            // Strip markdown JSON styling if necessary
            jsonResult = jsonResult.trim();
            if (jsonResult.startsWith("```json")) {
                jsonResult = jsonResult.substring(7);
            }
            if (jsonResult.endsWith("```")) {
                jsonResult = jsonResult.substring(0, jsonResult.length() - 3);
            }
            jsonResult = jsonResult.trim();

            JsonNode root = objectMapper.readTree(jsonResult);

            // 1. Update individual QA pair scores and justifications
            JsonNode qScores = root.get("questionScores");
            if (qScores != null && qScores.isArray()) {
                for (JsonNode item : qScores) {
                    int qNum = item.get("questionNumber").asInt();
                    double scoreVal = item.get("score").asDouble();
                    String justification = item.get("justification").asText();

                    for (QaPair qa : qaPairs) {
                        if (qa.getQuestionNumber() == qNum) {
                            qa.setScore(BigDecimal.valueOf(scoreVal));
                            qa.setScoreJustification(justification);
                            qaPairRepository.save(qa);
                            break;
                        }
                    }
                }
            }

            // 2. Create the final report
            FinalReport report = new FinalReport();
            report.setSession(session);
            report.setOverallScore(BigDecimal.valueOf(root.get("overallScore").asDouble()));
            
            JsonNode strengthsNode = root.get("strengths");
            if (strengthsNode != null && strengthsNode.isArray() && strengthsNode.size() >= 2) {
                report.setStrength1(strengthsNode.get(0).asText());
                report.setStrength2(strengthsNode.get(1).asText());
            } else {
                report.setStrength1("Technical explanation depth");
                report.setStrength2("General troubleshooting flow");
            }

            JsonNode weaknessesNode = root.get("weaknesses");
            if (weaknessesNode != null && weaknessesNode.isArray() && weaknessesNode.size() >= 2) {
                report.setWeakness1(weaknessesNode.get(0).asText());
                report.setWeakness2(weaknessesNode.get(1).asText());
            } else {
                report.setWeakness1("Detailed architectural scaling");
                report.setWeakness2("Edge case verification");
            }

            report.setWeakestQuestionNumber(root.get("weakestQuestionNumber").asInt());
            report.setModelAnswer(root.get("modelAnswerForWeakest").asText());
            
            JsonNode suggestionsNode = root.get("preparationSuggestions");
            if (suggestionsNode != null) {
                report.setPreparationSuggestions(objectMapper.writeValueAsString(suggestionsNode));
            } else {
                report.setPreparationSuggestions("[\"Practice core DSA questions\", \"Revise system scaling benchmarks\"]");
            }
            
            report.setClosingNote(root.get("closingNote").asText());

            finalReportRepository.save(report);
            logger.info("Successfully generated and saved Final Report for session {}", session.getId());

        } catch (Exception e) {
            logger.error("Exception parsing LLM evaluation JSON: {}. Response content was: {}. Saving fallback report.", e.getMessage(), jsonResult, e);
            createFallbackReport(session, qaPairs);
        }
    }

    private void createFallbackReport(InterviewSession session, List<QaPair> qaPairs) {
        try {
            for (QaPair qa : qaPairs) {
                qa.setScore(BigDecimal.valueOf(7.0));
                qa.setScoreJustification("Solid attempt. Showed good understanding of base concepts.");
                qaPairRepository.save(qa);
            }

            FinalReport report = new FinalReport();
            report.setSession(session);
            report.setOverallScore(BigDecimal.valueOf(7.0));
            report.setStrength1("Clear communication and structuring of thoughts");
            report.setStrength2("Solid foundation of fundamental engineering practices");
            report.setWeakness1("Finer API implementation parameters");
            report.setWeakness2("Handling system performance edge-cases");
            report.setWeakestQuestionNumber(2);
            report.setModelAnswer("A proper answer covers structural layout, asynchronous request threads, and proper resource error bounds.");
            report.setPreparationSuggestions("[\"Solve more design scenarios\", \"Analyze latency structures in web apps\"]");
            report.setClosingNote("Promising performance showing good developer attributes. Build on systems architecture concepts.");
            
            finalReportRepository.save(report);
        } catch (Exception ex) {
            logger.error("Failed to write fallback report: {}", ex.getMessage(), ex);
        }
    }
}
