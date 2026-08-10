package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "interview_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterviewRole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_key", nullable = false, unique = true, length = 50)
    private String roleKey;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "prompt_template", nullable = false, columnDefinition = "TEXT")
    private String promptTemplate;
}
