package com.rockranger.analyzer.interview.report;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.interview.entity.Interview;
import com.rockranger.analyzer.interview.entity.InterviewAnswer;
import com.rockranger.analyzer.interview.entity.InterviewQuestion;
import com.rockranger.analyzer.interview.entity.InterviewResult;
import com.rockranger.analyzer.interview.report.impl.PdfBoxInterviewReportService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfBoxInterviewReportServiceTest {

    private PdfBoxInterviewReportService reportService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        reportService = new PdfBoxInterviewReportService(objectMapper);
    }

    @Test
    void generatePdfReport_Success() throws IOException {
        User user = new User();
        user.setId(1L);
        user.setFullName("Mohan Kumar");
        user.setEmail("mohan@example.com");

        Interview interview = new Interview();
        interview.setId(101L);
        interview.setUser(user);
        interview.setInterviewType("TECHNICAL");
        interview.setTotalQuestions(2);
        interview.setCompletedAt(LocalDateTime.now());

        InterviewResult result = new InterviewResult();
        result.setInterview(interview);
        result.setOverallScore(85);
        result.setTechnicalScore(88);
        result.setCommunicationScore(82);
        result.setProblemSolvingScore(84);
        result.setStrengths("[\"Strong Java fundamentals\", \"Good Spring Boot knowledge\"]");
        result.setImprovements("[\"Improve database indexing explanation\", \"Use STAR method\"]");
        result.setFinalFeedback("Candidate demonstrates great technical understanding with strong articulation.");

        InterviewQuestion q1 = new InterviewQuestion();
        q1.setId(1L);
        q1.setQuestionNumber(1);
        q1.setCategory("TECHNICAL");
        q1.setDifficulty("MEDIUM");
        q1.setQuestion("Explain Dependency Injection in Spring Boot.");

        InterviewAnswer a1 = new InterviewAnswer();
        a1.setQuestion(q1);
        a1.setAnswerText("Dependency injection is a pattern where objects receive their dependencies from an external container.");
        a1.setScore(85);
        a1.setTechnicalAccuracy(9);
        a1.setCommunicationScore(8);
        a1.setRelevanceScore(9);
        a1.setFeedback("Clear explanation of IoC and DI.");
        a1.setBetterAnswer("Dependency Injection is a software design pattern implementing Inversion of Control...");

        InterviewQuestion q2 = new InterviewQuestion();
        q2.setId(2L);
        q2.setQuestionNumber(2);
        q2.setCategory("PROJECT");
        q2.setDifficulty("HARD");
        q2.setQuestion("How did you handle transactions and concurrency in your backend project?");

        InterviewAnswer a2 = new InterviewAnswer();
        a2.setQuestion(q2);
        a2.setAnswerText("I used @Transactional and optimistic locking with @Version.");
        a2.setScore(90);
        a2.setTechnicalAccuracy(9);
        a2.setCommunicationScore(9);
        a2.setRelevanceScore(9);
        a2.setFeedback("Good practical knowledge demonstrated.");
        a2.setBetterAnswer("In our microservices architecture, we utilized declarative transactions...");

        byte[] pdfBytes = reportService.generatePdfReport(
                interview,
                result,
                List.of(q1, q2),
                List.of(a1, a2),
                user
        );

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000, "PDF size should be substantial");

        // Verify PDF magic header
        String pdfHeader = new String(pdfBytes, 0, 5);
        assertEquals("%PDF-", pdfHeader);

        // Verify valid PDF structure with PDFBox Loader
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            assertTrue(document.getNumberOfPages() >= 1, "PDF should have at least 1 page");
        }
    }

    @Test
    void generatePdfReport_HandlesSpecialAndUnicodeCharacters() throws IOException {
        User user = new User();
        user.setId(2L);
        user.setFullName("Mohan Kumar — Candidate “Rock”");
        user.setEmail("mohan.special@example.com");

        Interview interview = new Interview();
        interview.setId(102L);
        interview.setUser(user);
        interview.setInterviewType("SYSTEM_DESIGN");
        interview.setTotalQuestions(1);
        interview.setCompletedAt(LocalDateTime.now());

        InterviewResult result = new InterviewResult();
        result.setInterview(interview);
        result.setOverallScore(92);
        result.setTechnicalScore(95);
        result.setCommunicationScore(90);
        result.setProblemSolvingScore(91);
        // Includes Unicode symbols ✓, •, —, “”, ‘’
        result.setStrengths("[\"✓ Excellent microservice design\", \"• High scalability\"]");
        result.setImprovements("[\"– Focus on trade-offs\", \"‘Explain CAP theorem’\"]");
        result.setFinalFeedback("“Outstanding performance” across all metrics — strongly recommend hiring!");

        InterviewQuestion q1 = new InterviewQuestion();
        q1.setId(10L);
        q1.setQuestionNumber(1);
        q1.setCategory("SYSTEM_DESIGN");
        q1.setDifficulty("HARD");
        q1.setQuestion("Design a URL shortener like TinyURL — handle 100M daily writes.");

        InterviewAnswer a1 = new InterviewAnswer();
        a1.setQuestion(q1);
        a1.setAnswerText("We use Base62 encoding • distributed ID generator • Redis cache.");
        a1.setScore(95);
        a1.setFeedback("✓ Accurate architecture proposal with solid caching.");
        a1.setBetterAnswer("A robust distributed URL shortening architecture requires...");

        byte[] pdfBytes = reportService.generatePdfReport(
                interview,
                result,
                List.of(q1),
                List.of(a1),
                user
        );

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000);

        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            assertTrue(document.getNumberOfPages() >= 1);
        }
    }
}
