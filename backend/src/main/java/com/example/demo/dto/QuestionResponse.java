package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    private Long sessionId;
    private int questionNumber;
    private String questionText;
    private String status; // "IN_PROGRESS" or "COMPLETED"
}
