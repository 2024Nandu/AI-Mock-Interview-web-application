package com.rockranger.analyzer.interview.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StartInterviewRequest {

    @NotNull(message = "Resume ID is required")
    private Long resumeId;

    @Min(value = 1, message = "Minimum number of questions is 1")
    @Max(value = 25, message = "Maximum number of questions is 25")
    private Integer numberOfQuestions = 10;

    private String interviewType = "TECHNICAL";

    public StartInterviewRequest() {
    }

    public StartInterviewRequest(Long resumeId, Integer numberOfQuestions, String interviewType) {
        this.resumeId = resumeId;
        this.numberOfQuestions = numberOfQuestions;
        this.interviewType = interviewType;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Integer getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(Integer numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public String getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(String interviewType) {
        this.interviewType = interviewType;
    }
}
