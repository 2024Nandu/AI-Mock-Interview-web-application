package com.rockranger.analyzer.interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SubmitAnswerRequest {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotBlank(message = "Answer text cannot be blank")
    private String answer;

    public SubmitAnswerRequest() {
    }

    public SubmitAnswerRequest(Long questionId, String answer) {
        this.questionId = questionId;
        this.answer = answer;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
