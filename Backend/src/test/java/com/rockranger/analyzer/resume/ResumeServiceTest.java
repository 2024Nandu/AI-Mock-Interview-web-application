package com.rockranger.analyzer.resume;

import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.resume.dto.response.ResumeResponse;
import com.rockranger.analyzer.resume.entity.Resume;
import com.rockranger.analyzer.resume.exception.InvalidFileException;
import com.rockranger.analyzer.resume.exception.ResumeNotFoundException;
import com.rockranger.analyzer.resume.repository.ResumeAnalysisRepository;
import com.rockranger.analyzer.resume.repository.ResumeRepository;
import com.rockranger.analyzer.resume.service.CloudinaryService;
import com.rockranger.analyzer.resume.service.impl.ResumeServiceImpl;
import com.rockranger.analyzer.resume.util.ResumeTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ResumeAnalysisRepository resumeAnalysisRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private ResumeTextExtractor resumeTextExtractor;

    @Mock
    private com.rockranger.analyzer.ai.service.AiService aiService;

    @org.mockito.Spy
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @InjectMocks
    private ResumeServiceImpl resumeService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setFullName("Test User");
    }

    @Test
    void uploadResume_success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "sample_resume.pdf",
                "application/pdf",
                "Sample PDF Content".getBytes()
        );

        when(resumeTextExtractor.extractText(file)).thenReturn("Extracted Resume Text");
        when(cloudinaryService.uploadFile(file, "resumes")).thenReturn(Map.of(
                "public_id", "resumes/sample_123",
                "secure_url", "https://res.cloudinary.com/demo/image/upload/sample.pdf"
        ));

        Resume savedResume = new Resume();
        savedResume.setId(10L);
        savedResume.setFileName("sample_resume.pdf");
        savedResume.setFileType("application/pdf");
        savedResume.setCloudinaryPublicId("resumes/sample_123");
        savedResume.setFileUrl("https://res.cloudinary.com/demo/image/upload/sample.pdf");
        savedResume.setExtractedText("Extracted Resume Text");
        savedResume.setUser(testUser);

        when(resumeRepository.save(any(Resume.class))).thenReturn(savedResume);

        ResumeResponse response = resumeService.uploadResume(file, testUser);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("sample_resume.pdf", response.getFileName());
        assertEquals("resumes/sample_123", response.getCloudinaryPublicId());
        assertEquals("Extracted Resume Text", response.getExtractedText());
        verify(cloudinaryService).uploadFile(file, "resumes");
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void uploadResume_invalidExtension_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malicious.exe",
                "application/octet-stream",
                "binary content".getBytes()
        );

        assertThrows(InvalidFileException.class, () -> resumeService.uploadResume(file, testUser));
        verifyNoInteractions(cloudinaryService);
        verifyNoInteractions(resumeRepository);
    }

    @Test
    void getResumeById_notFound_throwsException() {
        when(resumeRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        assertThrows(ResumeNotFoundException.class, () -> resumeService.getResumeById(999L, testUser));
    }

    @Test
    void deleteResume_success() {
        Resume resume = new Resume();
        resume.setId(5L);
        resume.setCloudinaryPublicId("resumes/delete_me");
        resume.setUser(testUser);

        when(resumeRepository.findByIdAndUser(5L, testUser)).thenReturn(Optional.of(resume));

        resumeService.deleteResume(5L, testUser);

        verify(cloudinaryService).deleteFile("resumes/delete_me");
        verify(resumeRepository).delete(resume);
    }

    @Test
    void analyzeResume_success() {
        Resume resume = new Resume();
        resume.setId(10L);
        resume.setExtractedText("John Doe\nJava Developer\nSpring Boot, PostgreSQL");
        resume.setUser(testUser);

        when(resumeRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(resume));

        com.rockranger.analyzer.ai.dto.ResumeAiResponse mockAiResponse = new com.rockranger.analyzer.ai.dto.ResumeAiResponse();
        mockAiResponse.getStructuredResume().getCandidate().setName("John Doe");
        mockAiResponse.getStructuredResume().getCandidate().setRole("Java Developer");
        mockAiResponse.getAnalysis().setOverallScore(88);
        mockAiResponse.getAnalysis().setStrengths(List.of("Strong Java background"));
        mockAiResponse.getAnalysis().setWeaknesses(List.of("Missing Docker"));

        when(aiService.analyzeResume(resume.getExtractedText())).thenReturn(mockAiResponse);
        when(resumeAnalysisRepository.findByResume(resume)).thenReturn(Optional.empty());

        com.rockranger.analyzer.resume.entity.ResumeAnalysis savedAnalysis = new com.rockranger.analyzer.resume.entity.ResumeAnalysis();
        savedAnalysis.setId(100L);
        savedAnalysis.setResume(resume);
        savedAnalysis.setOverallScore(88);
        savedAnalysis.setStructuredResumeJson("{\"candidate\":{\"name\":\"John Doe\",\"role\":\"Java Developer\"}}");
        savedAnalysis.setStrengths("[\"Strong Java background\"]");
        savedAnalysis.setWeaknesses("[\"Missing Docker\"]");

        when(resumeAnalysisRepository.save(any(com.rockranger.analyzer.resume.entity.ResumeAnalysis.class))).thenReturn(savedAnalysis);

        com.rockranger.analyzer.resume.dto.response.ResumeAnalysisResponse response = resumeService.analyzeResume(10L, testUser);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(10L, response.getResumeId());
        assertEquals(88, response.getOverallScore());
        assertEquals("John Doe", response.getStructuredResume().getCandidate().getName());
        verify(aiService).analyzeResume(resume.getExtractedText());
        verify(resumeAnalysisRepository).save(any(com.rockranger.analyzer.resume.entity.ResumeAnalysis.class));
    }

    @Test
    void analyzeResume_emptyExtractedText_throwsException() {
        Resume resume = new Resume();
        resume.setId(10L);
        resume.setExtractedText("");
        resume.setUser(testUser);

        when(resumeRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(resume));

        assertThrows(InvalidFileException.class, () -> resumeService.analyzeResume(10L, testUser));
        verifyNoInteractions(aiService);
    }

    @Test
    void getResumeAnalysis_notFound_throwsException() {
        Resume resume = new Resume();
        resume.setId(10L);
        resume.setUser(testUser);

        when(resumeRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(resume));
        when(resumeAnalysisRepository.findByResume(resume)).thenReturn(Optional.empty());

        assertThrows(com.rockranger.analyzer.resume.exception.ResumeAnalysisNotFoundException.class,
                () -> resumeService.getResumeAnalysis(10L, testUser));
    }
}
