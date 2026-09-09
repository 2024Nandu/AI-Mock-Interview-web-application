package com.rockranger.analyzer.interview.dto.response;

public class AnswerEvaluationResponse {

    private Long questionId;
    private Integer questionNumber;
    private String answerText;
    private Integer score;
    private Integer technicalAccuracy;
    private Integer communicationScore;
    private Integer relevanceScore;
    private String feedback;
    private String betterAnswer;

    public AnswerEvaluationResponse() {
    }

    public AnswerEvaluationResponse(Long questionId, Integer questionNumber, String answerText, Integer score, Integer technicalAccuracy, Integer communicationScore, Integer relevanceScore, String feedback, String betterAnswer) {
        this.questionId = questionId;
        this.questionNumber = questionNumber;
        this.answerText = answerText;
        this.score = score;
        this.technicalAccuracy = technicalAccuracy;
        this.communicationScore = communicationScore;
        this.relevanceScore = relevanceScore;
        this.feedback = feedback;
        this.betterAnswer = betterAnswer;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
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
