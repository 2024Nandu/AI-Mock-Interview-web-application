package com.rockranger.analyzer.interview.dto.response;

import java.time.LocalDateTime;

public class InterviewResponse {

    private Long id;
    private Long resumeId;
    private Integer totalQuestions;
    private Integer currentQuestion;
    private String interviewType;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public InterviewResponse() {
    }

    public InterviewResponse(Long id, Long resumeId, Integer totalQuestions, Integer currentQuestion, String interviewType, String status, LocalDateTime startedAt, LocalDateTime completedAt) {
        this.id = id;
        this.resumeId = resumeId;
        this.totalQuestions = totalQuestions;
        this.currentQuestion = currentQuestion;
        this.interviewType = interviewType;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
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

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(Integer currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public String getInterviewType() {
        return interviewType;
    }

    public void setInterviewType(String interviewType) {
        this.interviewType = interviewType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
