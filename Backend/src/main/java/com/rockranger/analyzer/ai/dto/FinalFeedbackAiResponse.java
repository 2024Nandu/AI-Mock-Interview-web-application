package com.rockranger.analyzer.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FinalFeedbackAiResponse {

    private Integer overallScore;
    private Integer technicalScore;
    private Integer communicationScore;
    private Integer problemSolvingScore;
    private List<String> strengths;
    private List<String> improvements;
    private String finalFeedback;

    public FinalFeedbackAiResponse() {
    }

    public FinalFeedbackAiResponse(Integer overallScore, Integer technicalScore, Integer communicationScore, Integer problemSolvingScore, List<String> strengths, List<String> improvements, String finalFeedback) {
        this.overallScore = overallScore;
        this.technicalScore = technicalScore;
        this.communicationScore = communicationScore;
        this.problemSolvingScore = problemSolvingScore;
        this.strengths = strengths;
        this.improvements = improvements;
        this.finalFeedback = finalFeedback;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public Integer getTechnicalScore() {
        return technicalScore;
    }

    public void setTechnicalScore(Integer technicalScore) {
        this.technicalScore = technicalScore;
    }

    public Integer getCommunicationScore() {
        return communicationScore;
    }

    public void setCommunicationScore(Integer communicationScore) {
        this.communicationScore = communicationScore;
    }

    public Integer getProblemSolvingScore() {
        return problemSolvingScore;
    }

    public void setProblemSolvingScore(Integer problemSolvingScore) {
        this.problemSolvingScore = problemSolvingScore;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getImprovements() {
        return improvements;
    }

    public void setImprovements(List<String> improvements) {
        this.improvements = improvements;
    }

    public String getFinalFeedback() {
        return finalFeedback;
    }

    public void setFinalFeedback(String finalFeedback) {
        this.finalFeedback = finalFeedback;
    }
}
