package com.rockranger.analyzer.interview.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "interview_questions")
public class InterviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "interview_questions_id_seq")
    @SequenceGenerator(name = "interview_questions_id_seq", sequenceName = "interview_questions_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interview_id", nullable = false)
    private Interview interview;

    @Column(name = "question_number", nullable = false)
    private Integer questionNumber;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(length = 50)
    private String category;

    @Column(length = 30)
    private String difficulty;

    @OneToOne(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private InterviewAnswer answer;

    public InterviewQuestion() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Interview getInterview() {
        return interview;
    }

    public void setInterview(Interview interview) {
        this.interview = interview;
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

    public InterviewAnswer getAnswer() {
        return answer;
    }

    public void setAnswer(InterviewAnswer answer) {
        this.answer = answer;
    }
}
