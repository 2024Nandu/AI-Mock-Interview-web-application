package com.rockranger.analyzer.interview.controller;

import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.interview.dto.request.StartInterviewRequest;
import com.rockranger.analyzer.interview.dto.request.SubmitAnswerRequest;
import com.rockranger.analyzer.interview.dto.response.AnswerEvaluationResponse;
import com.rockranger.analyzer.interview.dto.response.InterviewResponse;
import com.rockranger.analyzer.interview.dto.response.InterviewResultResponse;
import com.rockranger.analyzer.interview.dto.response.QuestionResponse;
import com.rockranger.analyzer.interview.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/interviews", "/api/interviews"})
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> startInterview(
            @Valid @RequestBody StartInterviewRequest request,
            @AuthenticationPrincipal User user
    ) {
        InterviewResponse response = interviewService.startInterview(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/current-question")
    public ResponseEntity<QuestionResponse> getCurrentQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        QuestionResponse response = interviewService.getCurrentQuestion(id, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/answers")
    public ResponseEntity<AnswerEvaluationResponse> submitAnswer(
            @PathVariable Long id,
            @Valid @RequestBody SubmitAnswerRequest request,
            @AuthenticationPrincipal User user
    ) {
        AnswerEvaluationResponse response = interviewService.submitAnswer(id, request, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/next")
    public ResponseEntity<QuestionResponse> nextQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        QuestionResponse response = interviewService.nextQuestion(id, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<InterviewResultResponse> completeInterview(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        InterviewResultResponse response = interviewService.completeInterview(id, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/result")
    public ResponseEntity<InterviewResultResponse> getInterviewResult(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        InterviewResultResponse response = interviewService.getInterviewResult(id, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{id}/answers/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnswerEvaluationResponse> submitVoiceAnswer(
            @PathVariable Long id,
            @RequestParam("questionId") Long questionId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "audio", required = false) MultipartFile audio,
            @AuthenticationPrincipal User user
    ) {
        MultipartFile targetFile = file != null ? file : audio;
        AnswerEvaluationResponse response = interviewService.submitVoiceAnswer(id, questionId, targetFile, user);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{id}/current-question/speak", produces = "audio/wav")
    public ResponseEntity<byte[]> speakCurrentQuestion(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        byte[] audio = interviewService.speakCurrentQuestion(id, user);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"question-" + id + ".wav\"")
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(audio);
    }

    @GetMapping(value = "/{id}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getInterviewReport(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        byte[] pdfReport = interviewService.generateInterviewReportPdf(id, user);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"mock-interview-report-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfReport);
    }

    @GetMapping
    public ResponseEntity<List<InterviewResponse>> getAllInterviews(@AuthenticationPrincipal User user) {
        List<InterviewResponse> list = interviewService.getAllInterviews(user);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InterviewResponse> getInterviewById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) {
        InterviewResponse response = interviewService.getInterviewById(id, user);
        return ResponseEntity.ok(response);
    }
}
