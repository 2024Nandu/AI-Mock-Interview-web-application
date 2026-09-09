package com.rockranger.analyzer.interview.dto.response;

public class QuestionResponse {

    private Long questionId;
    private Integer questionNumber;
    private Integer totalQuestions;
    private String question;
    private String category;
    private String difficulty;
    private boolean alreadyAnswered;

    public QuestionResponse() {
    }

    public QuestionResponse(Long questionId, Integer questionNumber, Integer totalQuestions, String question, String category, String difficulty, boolean alreadyAnswered) {
        this.questionId = questionId;
        this.questionNumber = questionNumber;
        this.totalQuestions = totalQuestions;
        this.question = question;
        this.category = category;
        this.difficulty = difficulty;
        this.alreadyAnswered = alreadyAnswered;
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

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
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

    public boolean isAlreadyAnswered() {
        return alreadyAnswered;
    }

    public void setAlreadyAnswered(boolean alreadyAnswered) {
        this.alreadyAnswered = alreadyAnswered;
    }
}
