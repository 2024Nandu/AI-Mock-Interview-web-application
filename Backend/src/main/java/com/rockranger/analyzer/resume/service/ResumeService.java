package com.rockranger.analyzer.resume.service;

import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.resume.dto.response.ResumeResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {

    ResumeResponse uploadResume(MultipartFile file, User user);

    List<ResumeResponse> getAllResumes(User user);

    ResumeResponse getResumeById(Long id, User user);

    void deleteResume(Long id, User user);

    com.rockranger.analyzer.resume.dto.response.ResumeAnalysisResponse analyzeResume(Long resumeId, User user);

    com.rockranger.analyzer.resume.dto.response.ResumeAnalysisResponse getResumeAnalysis(Long resumeId, User user);
}
