package com.rockranger.analyzer.resume.repository;

import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.resume.entity.Resume;
import com.rockranger.analyzer.resume.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

    Optional<ResumeAnalysis> findByResume(Resume resume);

    Optional<ResumeAnalysis> findByResumeIdAndResumeUser(Long resumeId, User user);
}
