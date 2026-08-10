package com.example.demo.repository;

import com.example.demo.entity.InterviewSession;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByUserOrderByCreatedAtDesc(User user);
    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(Long userId);
}
