package com.rockranger.analyzer.interview.repository;

import com.rockranger.analyzer.interview.entity.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {

    Optional<InterviewAnswer> findByQuestionId(Long questionId);

    List<InterviewAnswer> findByQuestionInterviewIdOrderByQuestionQuestionNumber(Long interviewId);
}
