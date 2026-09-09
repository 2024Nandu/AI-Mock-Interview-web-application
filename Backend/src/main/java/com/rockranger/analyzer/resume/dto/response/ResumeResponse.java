package com.rockranger.analyzer.resume.dto.response;

import com.rockranger.analyzer.resume.entity.Resume;
import java.time.LocalDateTime;

public class ResumeResponse {

    private Long id;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private String cloudinaryPublicId;
    private String extractedText;
    private int extractedTextLength;
    private LocalDateTime uploadedAt;
    private String status;  // ✅ ADD THIS FIELD

    public ResumeResponse() {
    }

    public static ResumeResponse fromEntity(Resume resume) {
        ResumeResponse response = new ResumeResponse();
        response.setId(resume.getId());
        response.setFileName(resume.getFileName());
        response.setFileType(resume.getFileType());
        response.setFileUrl(resume.getFileUrl());
        response.setCloudinaryPublicId(resume.getCloudinaryPublicId());
        response.setExtractedText(resume.getExtractedText());
        response.setExtractedTextLength(resume.getExtractedText() != null ? resume.getExtractedText().length() : 0);
        response.setUploadedAt(resume.getUploadedAt());
        // ✅ SET STATUS BASED ON WHETHER ANALYSIS EXISTS
        response.setStatus(resume.getAnalysis() != null ? "ANALYZED" : "PENDING");
        return response;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getCloudinaryPublicId() {
        return cloudinaryPublicId;
    }

    public void setCloudinaryPublicId(String cloudinaryPublicId) {
        this.cloudinaryPublicId = cloudinaryPublicId;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public int getExtractedTextLength() {
        return extractedTextLength;
    }

    public void setExtractedTextLength(int extractedTextLength) {
        this.extractedTextLength = extractedTextLength;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    // ✅ ADD GETTER AND SETTER FOR STATUS
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}