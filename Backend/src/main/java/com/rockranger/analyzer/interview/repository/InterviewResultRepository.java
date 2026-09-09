package com.rockranger.analyzer.interview.repository;

import com.rockranger.analyzer.interview.entity.InterviewResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterviewResultRepository extends JpaRepository<InterviewResult, Long> {

    Optional<InterviewResult> findByInterviewId(Long interviewId);
}
