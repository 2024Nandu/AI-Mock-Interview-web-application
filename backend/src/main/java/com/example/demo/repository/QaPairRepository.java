package com.example.demo.repository;

import com.example.demo.entity.QaPair;
import com.example.demo.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface QaPairRepository extends JpaRepository<QaPair, Long> {
    List<QaPair> findBySessionOrderByQuestionNumberAsc(InterviewSession session);
    List<QaPair> findBySessionIdOrderByQuestionNumberAsc(Long sessionId);
    Optional<QaPair> findBySessionIdAndQuestionNumber(Long sessionId, int questionNumber);
}
