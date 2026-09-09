package com.rockranger.analyzer.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewQuestionAiResponse {

    private Integer questionNumber;
    private String question;
    private String category;
    private String difficulty;

    public InterviewQuestionAiResponse() {
    }

    public InterviewQuestionAiResponse(Integer questionNumber, String question, String category, String difficulty) {
        this.questionNumber = questionNumber;
        this.question = question;
        this.category = category;
        this.difficulty = difficulty;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}
