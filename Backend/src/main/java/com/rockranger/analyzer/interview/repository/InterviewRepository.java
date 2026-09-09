package com.rockranger.analyzer.interview.repository;

import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.interview.entity.Interview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByUserOrderByStartedAtDesc(User user);

    Optional<Interview> findByIdAndUser(Long id, User user);
}
