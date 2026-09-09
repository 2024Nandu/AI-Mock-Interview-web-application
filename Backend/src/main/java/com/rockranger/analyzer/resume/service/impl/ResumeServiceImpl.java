package com.rockranger.analyzer.resume.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.analyzer.ai.dto.ResumeAiResponse;
import com.rockranger.analyzer.ai.service.AiService;
import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.resume.dto.response.ResumeAnalysisResponse;
import com.rockranger.analyzer.resume.dto.response.ResumeResponse;
import com.rockranger.analyzer.resume.entity.Resume;
import com.rockranger.analyzer.resume.entity.ResumeAnalysis;
import com.rockranger.analyzer.resume.exception.InvalidFileException;
import com.rockranger.analyzer.resume.exception.ResumeAnalysisNotFoundException;
import com.rockranger.analyzer.resume.exception.ResumeNotFoundException;
import com.rockranger.analyzer.resume.repository.ResumeAnalysisRepository;
import com.rockranger.analyzer.resume.repository.ResumeRepository;
import com.rockranger.analyzer.resume.service.CloudinaryService;
import com.rockranger.analyzer.resume.service.ResumeService;
import com.rockranger.analyzer.resume.util.ResumeTextExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResumeServiceImpl implements ResumeService {

    private static final Logger logger = LoggerFactory.getLogger(ResumeServiceImpl.class);
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "docx", "doc", "txt");

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final CloudinaryService cloudinaryService;
    private final ResumeTextExtractor resumeTextExtractor;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public ResumeServiceImpl(ResumeRepository resumeRepository,
                             ResumeAnalysisRepository resumeAnalysisRepository,
                             CloudinaryService cloudinaryService,
                             ResumeTextExtractor resumeTextExtractor,
                             AiService aiService,
                             ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.cloudinaryService = cloudinaryService;
        this.resumeTextExtractor = resumeTextExtractor;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public ResumeResponse uploadResume(MultipartFile file, User user) {
        validateFile(file);

        // Extract text from resume
        String extractedText = resumeTextExtractor.extractText(file);

        // Upload to Cloudinary under resumes folder
        Map uploadResult = cloudinaryService.uploadFile(file, "resumes");
        String publicId = (String) uploadResult.get("public_id");
        String secureUrl = (String) uploadResult.get("secure_url");
        if (secureUrl == null) {
            secureUrl = (String) uploadResult.get("url");
        }

        // Save Resume entity
        Resume resume = new Resume();
        resume.setFileName(file.getOriginalFilename());
        resume.setFileType(file.getContentType());
        resume.setCloudinaryPublicId(publicId);
        resume.setFileUrl(secureUrl);
        resume.setExtractedText(extractedText);
        resume.setUser(user);

        Resume savedResume = resumeRepository.save(resume);
        logger.info("Resume successfully uploaded and saved with ID: {} for user: {}", savedResume.getId(), user.getEmail());

        return ResumeResponse.fromEntity(savedResume);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResumeResponse> getAllResumes(User user) {
        return resumeRepository.findByUserOrderByUploadedAtDesc(user)
                .stream()
                .map(ResumeResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeResponse getResumeById(Long id, User user) {
        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found with id: " + id));
        return ResumeResponse.fromEntity(resume);
    }

    @Override
    @Transactional
    public void deleteResume(Long id, User user) {
        Resume resume = resumeRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found with id: " + id));

        // Delete from Cloudinary
        cloudinaryService.deleteFile(resume.getCloudinaryPublicId());

        // Delete from database
        resumeRepository.delete(resume);
        logger.info("Resume with ID: {} deleted for user: {}", id, user.getEmail());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Please select a valid resume file to upload.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new InvalidFileException("File must have a valid extension (.pdf, .docx, .doc, or .txt).");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException("Unsupported file type. Only PDF, DOCX, DOC, and TXT files are allowed.");
        }
    }

    @Override
    @Transactional
    public ResumeAnalysisResponse analyzeResume(Long resumeId, User user) {
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found with id: " + resumeId));

        if (resume.getExtractedText() == null || resume.getExtractedText().isBlank()) {
            throw new InvalidFileException("Cannot analyze resume: No text extracted from document.");
        }

        logger.info("Starting AI analysis for resume ID: {} belonging to user: {}", resumeId, user.getEmail());

        ResumeAiResponse aiResponse = aiService.analyzeResume(resume.getExtractedText());

        // Check if an analysis already exists for this resume to update or create
        ResumeAnalysis analysis = resumeAnalysisRepository.findByResume(resume)
                .orElseGet(() -> {
                    ResumeAnalysis newAnalysis = new ResumeAnalysis();
                    newAnalysis.setResume(resume);
                    return newAnalysis;
                });

        try {
            analysis.setStructuredResumeJson(objectMapper.writeValueAsString(aiResponse.getStructuredResume()));
            analysis.setStrengths(objectMapper.writeValueAsString(aiResponse.getAnalysis().getStrengths()));
            analysis.setWeaknesses(objectMapper.writeValueAsString(aiResponse.getAnalysis().getWeaknesses()));
            analysis.setImprovements(objectMapper.writeValueAsString(aiResponse.getAnalysis().getImprovements()));
            analysis.setMissingKeywords(objectMapper.writeValueAsString(aiResponse.getAnalysis().getMissingKeywords()));
            analysis.setSectionScores(objectMapper.writeValueAsString(aiResponse.getAnalysis().getSectionScores()));
            analysis.setOverallScore(aiResponse.getAnalysis().getOverallScore());
        } catch (Exception e) {
            logger.error("Failed to serialize AI analysis data to JSON", e);
            throw new RuntimeException("Failed to serialize AI analysis results: " + e.getMessage(), e);
        }

        ResumeAnalysis savedAnalysis = resumeAnalysisRepository.save(analysis);
        resume.setAnalysis(savedAnalysis);

        logger.info("Resume analysis saved successfully for resume ID: {} with overall score: {}", resumeId, savedAnalysis.getOverallScore());

        return ResumeAnalysisResponse.fromEntity(savedAnalysis, objectMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public ResumeAnalysisResponse getResumeAnalysis(Long resumeId, User user) {
        Resume resume = resumeRepository.findByIdAndUser(resumeId, user)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found with id: " + resumeId));

        ResumeAnalysis analysis = resumeAnalysisRepository.findByResume(resume)
                .orElseThrow(() -> new ResumeAnalysisNotFoundException("Resume analysis not found for resume id: " + resumeId + ". Please analyze the resume first."));

        return ResumeAnalysisResponse.fromEntity(analysis, objectMapper);
    }
}
