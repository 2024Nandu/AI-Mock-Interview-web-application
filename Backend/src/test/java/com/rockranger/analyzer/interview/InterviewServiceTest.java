package com.rockranger.analyzer.interview;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.analyzer.ai.dto.AnswerEvaluationAiResponse;
import com.rockranger.analyzer.ai.dto.FinalFeedbackAiResponse;
import com.rockranger.analyzer.ai.dto.InterviewQuestionAiResponse;
import com.rockranger.analyzer.ai.service.AiService;
import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.interview.dto.request.StartInterviewRequest;
import com.rockranger.analyzer.interview.dto.request.SubmitAnswerRequest;
import com.rockranger.analyzer.interview.dto.response.AnswerEvaluationResponse;
import com.rockranger.analyzer.interview.dto.response.InterviewResponse;
import com.rockranger.analyzer.interview.dto.response.InterviewResultResponse;
import com.rockranger.analyzer.interview.dto.response.QuestionResponse;
import com.rockranger.analyzer.interview.entity.*;
import com.rockranger.analyzer.interview.exception.InterviewAlreadyCompletedException;
import com.rockranger.analyzer.interview.exception.InterviewNotFoundException;
import com.rockranger.analyzer.interview.exception.QuestionNotFoundException;
import com.rockranger.analyzer.interview.repository.InterviewAnswerRepository;
import com.rockranger.analyzer.interview.repository.InterviewQuestionRepository;
import com.rockranger.analyzer.interview.repository.InterviewRepository;
import com.rockranger.analyzer.interview.repository.InterviewResultRepository;
import com.rockranger.analyzer.interview.service.impl.InterviewServiceImpl;
import com.rockranger.analyzer.resume.entity.Resume;
import com.rockranger.analyzer.resume.entity.ResumeAnalysis;
import com.rockranger.analyzer.resume.exception.ResumeAnalysisNotFoundException;
import com.rockranger.analyzer.resume.exception.ResumeNotFoundException;
import com.rockranger.analyzer.resume.repository.ResumeAnalysisRepository;
import com.rockranger.analyzer.resume.repository.ResumeRepository;
import com.rockranger.analyzer.interview.report.InterviewReportService;
import com.rockranger.analyzer.voice.dto.response.SpeechToTextResponse;
import com.rockranger.analyzer.voice.service.SpeechToTextService;
import com.rockranger.analyzer.voice.service.TextToSpeechService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private InterviewQuestionRepository interviewQuestionRepository;

    @Mock
    private InterviewAnswerRepository interviewAnswerRepository;

    @Mock
    private InterviewResultRepository interviewResultRepository;

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private ResumeAnalysisRepository resumeAnalysisRepository;

    @Mock
    private AiService aiService;

    @Mock
    private SpeechToTextService speechToTextService;

    @Mock
    private TextToSpeechService textToSpeechService;

    @Mock
    private InterviewReportService interviewReportService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private InterviewServiceImpl interviewService;

    private User testUser;
    private Resume testResume;
    private ResumeAnalysis testAnalysis;
    private Interview testInterview;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("developer@example.com");

        testResume = new Resume();
        testResume.setId(10L);
        testResume.setUser(testUser);

        testAnalysis = new ResumeAnalysis();
        testAnalysis.setId(20L);
        testAnalysis.setResume(testResume);
        testAnalysis.setStructuredResumeJson("{\"candidate\":{\"name\":\"John Doe\"}}");

        testInterview = new Interview();
        testInterview.setId(100L);
        testInterview.setUser(testUser);
        testInterview.setResume(testResume);
        testInterview.setTotalQuestions(2);
        testInterview.setCurrentQuestion(1);
        testInterview.setStatus(InterviewStatus.IN_PROGRESS);
        testInterview.setInterviewType("TECHNICAL");
    }

    @Test
    void startInterview_success() {
        StartInterviewRequest request = new StartInterviewRequest(10L, 2, "TECHNICAL");

        when(resumeRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(testResume));
        when(resumeAnalysisRepository.findByResumeIdAndResumeUser(10L, testUser)).thenReturn(Optional.of(testAnalysis));

        List<InterviewQuestionAiResponse> mockQuestions = List.of(
                new InterviewQuestionAiResponse(1, "What is Spring Boot autoconfiguration?", "TECHNICAL", "EASY"),
                new InterviewQuestionAiResponse(2, "Explain PostgreSQL MVCC.", "TECHNICAL", "MEDIUM")
        );
        when(aiService.generateInterviewQuestions(testAnalysis.getStructuredResumeJson(), 2, "TECHNICAL"))
                .thenReturn(mockQuestions);

        when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> {
            Interview i = invocation.getArgument(0);
            i.setId(100L);
            return i;
        });

        InterviewResponse response = interviewService.startInterview(request, testUser);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(10L, response.getResumeId());
        assertEquals(2, response.getTotalQuestions());
        assertEquals(1, response.getCurrentQuestion());
        assertEquals("IN_PROGRESS", response.getStatus());

        verify(aiService).generateInterviewQuestions(testAnalysis.getStructuredResumeJson(), 2, "TECHNICAL");
        verify(interviewRepository).save(any(Interview.class));
    }

    @Test
    void startInterview_resumeNotFound_throwsException() {
        StartInterviewRequest request = new StartInterviewRequest(999L, 5, "TECHNICAL");
        when(resumeRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());

        assertThrows(ResumeNotFoundException.class, () -> interviewService.startInterview(request, testUser));
    }

    @Test
    void startInterview_analysisNotFound_throwsException() {
        StartInterviewRequest request = new StartInterviewRequest(10L, 5, "TECHNICAL");
        when(resumeRepository.findByIdAndUser(10L, testUser)).thenReturn(Optional.of(testResume));
        when(resumeAnalysisRepository.findByResumeIdAndResumeUser(10L, testUser)).thenReturn(Optional.empty());

        assertThrows(ResumeAnalysisNotFoundException.class, () -> interviewService.startInterview(request, testUser));
    }

    @Test
    void getCurrentQuestion_success() {
        when(interviewRepository.findByIdAndUser(100L, testUser)).thenReturn(Optional.of(testInterview));

        InterviewQuestion question = new InterviewQuestion();
        question.setId(501L);
        question.setInterview(testInterview);
        question.setQuestionNumber(1);
        question.setQuestion("Explain Dependency Injection");
        question.setCategory("TECHNICAL");
        question.setDifficulty("EASY");

        when(interviewQuestionRepository.findByInterviewIdAndQuestionNumber(100L, 1)).thenReturn(Optional.of(question));
        when(interviewAnswerRepository.findByQuestionId(501L)).thenReturn(Optional.empty());

        QuestionResponse response = interviewService.getCurrentQuestion(100L, testUser);

        assertNotNull(response);
        assertEquals(501L, response.getQuestionId());
        assertEquals(1, response.getQuestionNumber());
        assertEquals("Explain Dependency Injection", response.getQuestion());
        assertFalse(response.isAlreadyAnswered());
    }

    @Test
    void submitAnswer_success() {
        SubmitAnswerRequest request = new SubmitAnswerRequest(501L, "Dependency injection is an IoC design pattern...");

        when(interviewRepository.findByIdAndUser(100L, testUser)).thenReturn(Optional.of(testInterview));

        InterviewQuestion question = new InterviewQuestion();
        question.setId(501L);
        question.setInterview(testInterview);
        question.setQuestionNumber(1);
        question.setQuestion("Explain Dependency Injection");

        when(interviewQuestionRepository.findById(501L)).thenReturn(Optional.of(question));
        when(resumeAnalysisRepository.findByResume(testResume)).thenReturn(Optional.of(testAnalysis));

        AnswerEvaluationAiResponse eval = new AnswerEvaluationAiResponse(
                90, 9, 9, 10, "Clear and accurate response.", "Exemplary answer details."
        );
        when(aiService.evaluateAnswer(testAnalysis.getStructuredResumeJson(), question.getQuestion(), request.getAnswer()))
                .thenReturn(eval);
        when(interviewAnswerRepository.findByQuestionId(501L)).thenReturn(Optional.empty());

        AnswerEvaluationResponse response = interviewService.submitAnswer(100L, request, testUser);

        assertNotNull(response);
        assertEquals(501L, response.getQuestionId());
        assertEquals(90, response.getScore());
        assertEquals(9, response.getTechnicalAccuracy());
        assertEquals("Clear and accurate response.", response.getFeedback());

        verify(interviewAnswerRepository).save(any(InterviewAnswer.class));
    }

    @Test
    void submitAnswer_alreadyCompleted_throwsException() {
        testInterview.setStatus(InterviewStatus.COMPLETED);
        when(interviewRepository.findByIdAndUser(100L, testUser)).thenReturn(Optional.of(testInterview));

        SubmitAnswerRequest request = new SubmitAnswerRequest(501L, "Some answer");
        assertThrows(InterviewAlreadyCompletedException.class,
                () -> interviewService.submitAnswer(100L, request, testUser));
    }

    @Test
    void nextQuestion_advancesQuestionNumber() {
        testInterview.setCurrentQuestion(1);
        testInterview.setTotalQuestions(2);
        when(interviewRepository.findByIdAndUser(100L, testUser)).thenReturn(Optional.of(testInterview));

        InterviewQuestion q2 = new InterviewQuestion();
        q2.setId(502L);
        q2.setInterview(testInterview);
        q2.setQuestionNumber(2);
        q2.setQuestion("Second Question");

        when(interviewQuestionRepository.findByInterviewIdAndQuestionNumber(100L, 2)).thenReturn(Optional.of(q2));
        when(interviewAnswerRepository.findByQuestionId(502L)).thenReturn(Optional.empty());

        QuestionResponse response = interviewService.nextQuestion(100L, testUser);

        assertEquals(2, testInterview.getCurrentQuestion());
        assertEquals(502L, response.getQuestionId());
        verify(interviewRepository).save(testInterview);
    }

    @Test
    void completeInterview_success() {
        when(interviewRepository.findByIdAndUser(100L, testUser)).thenReturn(Optional.of(testInterview));

        InterviewQuestion q1 = new InterviewQuestion();
        q1.setId(501L);
        q1.setInterview(testInterview);
        q1.setQuestionNumber(1);
        q1.setQuestion("Question 1");

        InterviewAnswer a1 = new InterviewAnswer();
        a1.setQuestion(q1);
        a1.setAnswerText("Answer 1");
        a1.setScore(85);

        when(interviewQuestionRepository.findByInterviewIdOrderByQuestionNumber(100L)).thenReturn(List.of(q1));
        when(interviewAnswerRepository.findByQuestionInterviewIdOrderByQuestionQuestionNumber(100L)).thenReturn(List.of(a1));
        when(resumeAnalysisRepository.findByResume(testResume)).thenReturn(Optional.of(testAnalysis));

        FinalFeedbackAiResponse mockFeedback = new FinalFeedbackAiResponse(
                85, 88, 82, 85,
                List.of("Strong Java foundation"),
                List.of("Elaborate on edge cases"),
                "Great performance overall."
        );
        when(aiService.generateFinalFeedback(eq(testAnalysis.getStructuredResumeJson()), anyList()))
                .thenReturn(mockFeedback);

        InterviewResultResponse response = interviewService.completeInterview(100L, testUser);

        assertNotNull(response);
        assertEquals(100L, response.getInterviewId());
        assertEquals(85, response.getOverallScore());
        assertEquals(88, response.getTechnicalScore());
        assertEquals(List.of("Strong Java foundation"), response.getStrengths());
        assertEquals("COMPLETED", testInterview.getStatus().name());

        verify(interviewResultRepository).save(any(InterviewResult.class));
        verify(interviewRepository).save(testInterview);
    }

    @Test
    void submitVoiceAnswer_Success() {
        InterviewQuestion question = new InterviewQuestion();
        question.setId(50L);
        question.setQuestionNumber(1);
        question.setQuestion("Explain Dependency Injection");
        question.setInterview(testInterview);

        MockMultipartFile audioFile = new MockMultipartFile(
                "audio",
                "answer.wav",
                "audio/wav",
                "RIFFmockwavdata".getBytes()
        );

        when(speechToTextService.transcribe(audioFile))
                .thenReturn(new SpeechToTextResponse("Dependency injection is an IoC pattern."));

        when(interviewRepository.findByIdAndUser(100L, testUser))
                .thenReturn(Optional.of(testInterview));
        when(interviewQuestionRepository.findById(50L))
                .thenReturn(Optional.of(question));
        when(resumeAnalysisRepository.findByResume(testResume))
                .thenReturn(Optional.of(testAnalysis));

        AnswerEvaluationAiResponse aiEval = new AnswerEvaluationAiResponse(
                88, 9, 8, 9,
                "Strong explanation.",
                "Better answer example"
        );
        when(aiService.evaluateAnswer(anyString(), anyString(), anyString()))
                .thenReturn(aiEval);

        InterviewAnswer savedAnswer = new InterviewAnswer();
        savedAnswer.setQuestion(question);
        when(interviewAnswerRepository.findByQuestionId(50L))
                .thenReturn(Optional.of(savedAnswer));

        AnswerEvaluationResponse response = interviewService.submitVoiceAnswer(100L, 50L, audioFile, testUser);

        assertNotNull(response);
        assertEquals(88, response.getScore());
        assertEquals("Dependency injection is an IoC pattern.", response.getAnswerText());
        assertEquals("VOICE", savedAnswer.getAnswerType());
        verify(speechToTextService).transcribe(audioFile);
    }

    @Test
    void speakCurrentQuestion_Success() {
        InterviewQuestion question = new InterviewQuestion();
        question.setId(50L);
        question.setQuestionNumber(1);
        question.setQuestion("Explain Dependency Injection");
        question.setInterview(testInterview);

        when(interviewRepository.findByIdAndUser(100L, testUser))
                .thenReturn(Optional.of(testInterview));
        when(interviewQuestionRepository.findByInterviewIdAndQuestionNumber(100L, 1))
                .thenReturn(Optional.of(question));
        when(interviewAnswerRepository.findByQuestionId(50L))
                .thenReturn(Optional.empty());

        byte[] fakeWav = "RIFFwavheader".getBytes();
        when(textToSpeechService.synthesizeSpeech("Explain Dependency Injection"))
                .thenReturn(fakeWav);

        byte[] audioResult = interviewService.speakCurrentQuestion(100L, testUser);

        assertNotNull(audioResult);
        assertArrayEquals(fakeWav, audioResult);
        verify(textToSpeechService).synthesizeSpeech("Explain Dependency Injection");
    }

    @Test
    void generateInterviewReportPdf_Success() {
        InterviewResult result = new InterviewResult();
        result.setId(200L);
        result.setInterview(testInterview);
        result.setOverallScore(90);

        when(interviewRepository.findByIdAndUser(100L, testUser))
                .thenReturn(Optional.of(testInterview));
        when(interviewResultRepository.findByInterviewId(100L))
                .thenReturn(Optional.of(result));
        when(interviewQuestionRepository.findByInterviewIdOrderByQuestionNumber(100L))
                .thenReturn(List.of());
        when(interviewAnswerRepository.findByQuestionInterviewIdOrderByQuestionQuestionNumber(100L))
                .thenReturn(List.of());

        byte[] mockPdf = "%PDF-1.4 mock".getBytes();
        when(interviewReportService.generatePdfReport(eq(testInterview), eq(result), anyList(), anyList(), eq(testUser)))
                .thenReturn(mockPdf);

        byte[] pdfResult = interviewService.generateInterviewReportPdf(100L, testUser);

        assertNotNull(pdfResult);
        assertArrayEquals(mockPdf, pdfResult);
        verify(interviewReportService).generatePdfReport(eq(testInterview), eq(result), anyList(), anyList(), eq(testUser));
    }
}
