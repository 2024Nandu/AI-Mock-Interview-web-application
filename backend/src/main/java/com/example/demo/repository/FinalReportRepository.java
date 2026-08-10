package com.example.demo.repository;

import com.example.demo.entity.FinalReport;
import com.example.demo.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FinalReportRepository extends JpaRepository<FinalReport, Long> {
    Optional<FinalReport> findBySession(InterviewSession session);
    Optional<FinalReport> findBySessionId(Long sessionId);
}
