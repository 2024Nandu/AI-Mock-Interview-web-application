package com.rockranger.analyzer.interview.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class InterviewResultResponse {

    private Long interviewId;
    private Integer overallScore;
    private Integer technicalScore;
    private Integer communicationScore;
    private Integer problemSolvingScore;
    private List<String> strengths;
    private List<String> improvements;
    private String finalFeedback;
    private List<AnswerEvaluationResponse> answers;
    private LocalDateTime completedAt;

    public InterviewResultResponse() {
    }

    public InterviewResultResponse(Long interviewId, Integer overallScore, Integer technicalScore, Integer communicationScore, Integer problemSolvingScore, List<String> strengths, List<String> improvements, String finalFeedback, List<AnswerEvaluationResponse> answers, LocalDateTime completedAt) {
        this.interviewId = interviewId;
        this.overallScore = overallScore;
        this.technicalScore = technicalScore;
        this.communicationScore = communicationScore;
        this.problemSolvingScore = problemSolvingScore;
        this.strengths = strengths;
        this.improvements = improvements;
        this.finalFeedback = finalFeedback;
        this.answers = answers;
        this.completedAt = completedAt;
    }

    public Long getInterviewId() {
        return interviewId;
    }

    public void setInterviewId(Long interviewId) {
        this.interviewId = interviewId;
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

    public List<AnswerEvaluationResponse> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerEvaluationResponse> answers) {
        this.answers = answers;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
