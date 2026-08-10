package com.example.demo.repository;

import com.example.demo.entity.InterviewRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InterviewRoleRepository extends JpaRepository<InterviewRole, Long> {
    Optional<InterviewRole> findByRoleKey(String roleKey);
}
