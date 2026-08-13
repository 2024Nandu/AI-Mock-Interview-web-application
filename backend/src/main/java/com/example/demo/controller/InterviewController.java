package com.example.demo.controller;

import com.example.demo.dto.QuestionResponse;
import com.example.demo.dto.StartInterviewRequest;
import com.example.demo.dto.SubmitAnswerRequest;
import com.example.demo.entity.FinalReport;
import com.example.demo.entity.InterviewSession;
import com.example.demo.entity.QaPair;
import com.example.demo.repository.FinalReportRepository;
import com.example.demo.repository.InterviewSessionRepository;
import com.example.demo.repository.QaPairRepository;
import com.example.demo.service.DeepgramTtsService;
import com.example.demo.service.InterviewService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {
    private final InterviewService interviewService;
    private final InterviewSessionRepository sessionRepository;
    private final FinalReportRepository finalReportRepository;
    private final QaPairRepository qaPairRepository;
    private final DeepgramTtsService ttsService;

    public InterviewController(InterviewService interviewService,
                               InterviewSessionRepository sessionRepository,
                               FinalReportRepository finalReportRepository,
                               QaPairRepository qaPairRepository,
                               DeepgramTtsService ttsService) {
        this.interviewService = interviewService;
        this.sessionRepository = sessionRepository;
        this.finalReportRepository = finalReportRepository;
        this.qaPairRepository = qaPairRepository;
        this.ttsService = ttsService;
    }

    @PostMapping("/start")
    public ResponseEntity<?> startInterview(@RequestBody StartInterviewRequest request,
                                            @RequestAttribute("userId") Long userId) {
        try {
            QuestionResponse response = interviewService.startInterview(userId, request.getRoleId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/answer")
    public ResponseEntity<?> submitAnswer(@PathVariable Long sessionId,
                                          @RequestBody SubmitAnswerRequest request) {
        try {
            QuestionResponse response = interviewService.submitAnswer(sessionId, request.getAnswerText());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestAttribute("userId") Long userId) {
        List<InterviewSession> sessions = sessionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<Map<String, Object>> response = new ArrayList<>();
        
        for (InterviewSession session : sessions) {
            Map<String, Object> map = new HashMap<>();
            map.put("sessionId", session.getId());
            map.put("roleName", session.getRole().getDisplayName());
            map.put("roleKey", session.getRole().getRoleKey());
            map.put("status", session.getStatus().toString());
            map.put("createdAt", session.getCreatedAt());
            
            if (session.getStatus() == InterviewSession.SessionStatus.COMPLETED) {
                finalReportRepository.findBySessionId(session.getId()).ifPresent(report -> {
                    map.put("overallScore", report.getOverallScore());
                });
            }
            response.add(map);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/report")
    public ResponseEntity<?> getReport(@PathVariable Long sessionId) {
        return finalReportRepository.findBySessionId(sessionId)
                .map(report -> {
                    List<QaPair> qaPairs = qaPairRepository.findBySessionIdOrderByQuestionNumberAsc(sessionId);
                    
                    // Filter out circular references or map to basic structure
                    List<Map<String, Object>> qaList = new ArrayList<>();
                    for (QaPair qa : qaPairs) {
                        Map<String, Object> qaMap = new HashMap<>();
                        qaMap.put("id", qa.getId());
                        qaMap.put("questionNumber", qa.getQuestionNumber());
                        qaMap.put("questionText", qa.getQuestionText());
                        qaMap.put("answerText", qa.getAnswerText());
                        qaMap.put("score", qa.getScore());
                        qaMap.put("scoreJustification", qa.getScoreJustification());
                        qaList.add(qaMap);
                    }

                    Map<String, Object> response = new HashMap<>();
                    response.put("report", report);
                    response.put("qaPairs", qaList);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/speak")
    public ResponseEntity<byte[]> speak(@RequestParam String text) {
        byte[] audioBytes = ttsService.generateSpeech(text);
        if (audioBytes == null || audioBytes.length == 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("audio/wav"));
        headers.setContentLength(audioBytes.length);
        headers.setCacheControl("max-age=86400");
        
        return new ResponseEntity<>(audioBytes, headers, HttpStatus.OK);
    }
}
