package com.rockranger.analyzer.resume.dto.response;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.analyzer.ai.dto.ResumeAiResponse;
import com.rockranger.analyzer.resume.entity.ResumeAnalysis;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResumeAnalysisResponse {

    private Long id;
    private Long resumeId;
    private ResumeAiResponse.StructuredResume structuredResume;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> improvements;
    private List<String> missingKeywords;
    private Map<String, Integer> sectionScores;
    private Integer overallScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ResumeAnalysisResponse() {
    }

    public static ResumeAnalysisResponse fromEntity(ResumeAnalysis entity, ObjectMapper objectMapper) {
        ResumeAnalysisResponse response = new ResumeAnalysisResponse();
        response.setId(entity.getId());
        response.setResumeId(entity.getResume() != null ? entity.getResume().getId() : null);
        response.setOverallScore(entity.getOverallScore());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        try {
            if (entity.getStructuredResumeJson() != null && !entity.getStructuredResumeJson().isBlank()) {
                response.setStructuredResume(objectMapper.readValue(entity.getStructuredResumeJson(), ResumeAiResponse.StructuredResume.class));
            } else {
                response.setStructuredResume(new ResumeAiResponse.StructuredResume());
            }

            if (entity.getStrengths() != null && !entity.getStrengths().isBlank()) {
                response.setStrengths(objectMapper.readValue(entity.getStrengths(), new TypeReference<List<String>>() {}));
            } else {
                response.setStrengths(new ArrayList<>());
            }

            if (entity.getWeaknesses() != null && !entity.getWeaknesses().isBlank()) {
                response.setWeaknesses(objectMapper.readValue(entity.getWeaknesses(), new TypeReference<List<String>>() {}));
            } else {
                response.setWeaknesses(new ArrayList<>());
            }

            if (entity.getImprovements() != null && !entity.getImprovements().isBlank()) {
                response.setImprovements(objectMapper.readValue(entity.getImprovements(), new TypeReference<List<String>>() {}));
            } else {
                response.setImprovements(new ArrayList<>());
            }

            if (entity.getMissingKeywords() != null && !entity.getMissingKeywords().isBlank()) {
                response.setMissingKeywords(objectMapper.readValue(entity.getMissingKeywords(), new TypeReference<List<String>>() {}));
            } else {
                response.setMissingKeywords(new ArrayList<>());
            }

            if (entity.getSectionScores() != null && !entity.getSectionScores().isBlank()) {
                response.setSectionScores(objectMapper.readValue(entity.getSectionScores(), new TypeReference<Map<String, Integer>>() {}));
            } else {
                response.setSectionScores(new HashMap<>());
            }
        } catch (Exception e) {
            response.setStructuredResume(new ResumeAiResponse.StructuredResume());
            response.setStrengths(new ArrayList<>());
            response.setWeaknesses(new ArrayList<>());
            response.setImprovements(new ArrayList<>());
            response.setMissingKeywords(new ArrayList<>());
            response.setSectionScores(new HashMap<>());
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public ResumeAiResponse.StructuredResume getStructuredResume() {
        return structuredResume;
    }

    public void setStructuredResume(ResumeAiResponse.StructuredResume structuredResume) {
        this.structuredResume = structuredResume;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(List<String> weaknesses) {
        this.weaknesses = weaknesses;
    }

    public List<String> getImprovements() {
        return improvements;
    }

    public void setImprovements(List<String> improvements) {
        this.improvements = improvements;
    }

    public List<String> getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(List<String> missingKeywords) {
        this.missingKeywords = missingKeywords;
    }

    public Map<String, Integer> getSectionScores() {
        return sectionScores;
    }

    public void setSectionScores(Map<String, Integer> sectionScores) {
        this.sectionScores = sectionScores;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
