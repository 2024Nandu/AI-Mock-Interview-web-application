package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "final_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private InterviewSession session;

    @Column(name = "overall_score", precision = 3, scale = 1)
    private BigDecimal overallScore;

    @Column(name = "strength_1", columnDefinition = "TEXT")
    private String strength1;

    @Column(name = "strength_2", columnDefinition = "TEXT")
    private String strength2;

    @Column(name = "weakness_1", columnDefinition = "TEXT")
    private String weakness1;

    @Column(name = "weakness_2", columnDefinition = "TEXT")
    private String weakness2;

    @Column(name = "weakest_question_number")
    private int weakestQuestionNumber;

    @Column(name = "model_answer", columnDefinition = "TEXT")
    private String modelAnswer;

    @Lob
    @Column(name = "preparation_suggestions", columnDefinition = "TEXT")
    private String preparationSuggestions; // JSON Array stored as text

    @Column(name = "closing_note", columnDefinition = "TEXT")
    private String closingNote;
}
