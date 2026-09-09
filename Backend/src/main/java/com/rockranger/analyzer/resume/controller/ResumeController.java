package com.rockranger.analyzer.resume.controller;

import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.resume.dto.response.ResumeResponse;
import com.rockranger.analyzer.resume.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/resumes", "/api/resumes"})
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user
    ) {
        ResumeResponse response = resumeService.uploadResume(file, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getAllResumes(@AuthenticationPrincipal User user) {
        List<ResumeResponse> resumes = resumeService.getAllResumes(user);
        return ResponseEntity.ok(resumes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResumeById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        ResumeResponse resume = resumeService.getResumeById(id, user);
        return ResponseEntity.ok(resume);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteResume(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        resumeService.deleteResume(id, user);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Resume deleted successfully."
        ));
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<com.rockranger.analyzer.resume.dto.response.ResumeAnalysisResponse> analyzeResume(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        com.rockranger.analyzer.resume.dto.response.ResumeAnalysisResponse response = resumeService.analyzeResume(id, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/analysis")
    public ResponseEntity<com.rockranger.analyzer.resume.dto.response.ResumeAnalysisResponse> getResumeAnalysis(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        com.rockranger.analyzer.resume.dto.response.ResumeAnalysisResponse response = resumeService.getResumeAnalysis(id, user);
        return ResponseEntity.ok(response);
    }
}
