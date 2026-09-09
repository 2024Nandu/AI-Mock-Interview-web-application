package com.rockranger.analyzer.interview.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_answers")
public class InterviewAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interview_answers_id_seq")
    @SequenceGenerator(name = "interview_answers_id_seq", sequenceName = "interview_answers_id_seq", allocationSize = 1)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false, unique = true)
    private InterviewQuestion question;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "answer_type", length = 30)
    private String answerType = "TEXT";

    @Column(name = "audio_url", length = 1000)
    private String audioUrl;

    @Column(name = "score")
    private Integer score;

    @Column(name = "technical_accuracy")
    private Integer technicalAccuracy;

    @Column(name = "communication_score")
    private Integer communicationScore;

    @Column(name = "relevance_score")
    private Integer relevanceScore;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "better_answer", columnDefinition = "TEXT")
    private String betterAnswer;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    public InterviewAnswer() {
    }

    @PrePersist
    protected void onCreate() {
        if (answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InterviewQuestion getQuestion() {
        return question;
    }

    public void setQuestion(InterviewQuestion question) {
        this.question = question;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public String getAnswerType() {
        return answerType;
    }

    public void setAnswerType(String answerType) {
        this.answerType = answerType;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
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

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }
}
