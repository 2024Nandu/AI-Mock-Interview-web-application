package com.rockranger.analyzer.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AnswerEvaluationAiResponse {

    private Integer score;
    private Integer technicalAccuracy;
    private Integer communicationScore;
    private Integer relevanceScore;
    private String feedback;
    private String betterAnswer;

    public AnswerEvaluationAiResponse() {
    }

    public AnswerEvaluationAiResponse(Integer score, Integer technicalAccuracy, Integer communicationScore, Integer relevanceScore, String feedback, String betterAnswer) {
        this.score = score;
        this.technicalAccuracy = technicalAccuracy;
        this.communicationScore = communicationScore;
        this.relevanceScore = relevanceScore;
        this.feedback = feedback;
        this.betterAnswer = betterAnswer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTechnicalAccuracy() {
        return technicalAccuracy;
    }

    public void setTechnicalAccuracy(Integer technicalAccuracy) {
        this.technicalAccuracy = technicalAccuracy;
    }

    public Integer getCommunicationScore() {
        return communicationScore;
    }

    public void setCommunicationScore(Integer communicationScore) {
        this.communicationScore = communicationScore;
    }

    public Integer getRelevanceScore() {
        return relevanceScore;
    }

    public void setRelevanceScore(Integer relevanceScore) {
        this.relevanceScore = relevanceScore;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getBetterAnswer() {
        return betterAnswer;
    }

    public void setBetterAnswer(String betterAnswer) {
        this.betterAnswer = betterAnswer;
    }
}
