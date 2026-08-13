package com.example.demo.controller;

import com.example.demo.entity.Resume;
import com.example.demo.entity.User;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ResumeParsingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {
    private final ResumeParsingService resumeParsingService;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public ResumeController(ResumeParsingService resumeParsingService,
                            ResumeRepository resumeRepository,
                            UserRepository userRepository) {
        this.resumeParsingService = resumeParsingService;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
                                          @RequestAttribute("userId") Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            String rawText = resumeParsingService.extractText(file);
            String parsedJson = resumeParsingService.parseResumeToJson(rawText);

            if (parsedJson == null || parsedJson.isEmpty()) {
                parsedJson = resumeParsingService.parseResumeTextFallback(rawText);
            }

            Resume resume = new Resume();
            resume.setUser(user);
            resume.setOriginalFilePath(file.getOriginalFilename());
            resume.setRawText(rawText);
            resume.setParsedJson(parsedJson);
            resume.setUploadedAt(LocalDateTime.now());
            resumeRepository.save(resume);

            return ResponseEntity.ok(Map.of(
                    "message", "Resume uploaded and parsed successfully",
                    "parsedResume", parsedJson
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to parse file: " + e.getMessage()));
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestResume(@RequestAttribute("userId") Long userId) {
        return resumeRepository.findFirstByUserIdOrderByUploadedAtDesc(userId)
                .map(resume -> ResponseEntity.ok(Map.of("parsedResume", resume.getParsedJson())))
                .orElse(ResponseEntity.ok(Map.of("parsedResume", "")));
    }
}
