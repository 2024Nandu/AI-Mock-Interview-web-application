package com.rockranger.analyzer.resume.repository;

import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserOrderByUploadedAtDesc(User user);

    Optional<Resume> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);
}
