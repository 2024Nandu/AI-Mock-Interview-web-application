package com.rockranger.analyzer.interview.repository;

import com.rockranger.analyzer.interview.entity.InterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {

    List<InterviewQuestion> findByInterviewIdOrderByQuestionNumber(Long interviewId);

    Optional<InterviewQuestion> findByInterviewIdAndQuestionNumber(Long interviewId, Integer questionNumber);
}
