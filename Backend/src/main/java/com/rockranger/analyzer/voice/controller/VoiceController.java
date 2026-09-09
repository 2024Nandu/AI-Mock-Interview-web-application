package com.rockranger.analyzer.voice.controller;

import com.rockranger.analyzer.voice.dto.request.TextToSpeechRequest;
import com.rockranger.analyzer.voice.dto.response.SpeechToTextResponse;
import com.rockranger.analyzer.voice.exception.AudioProcessingException;
import com.rockranger.analyzer.voice.service.SpeechToTextService;
import com.rockranger.analyzer.voice.service.TextToSpeechService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping({"/api/v1/voice", "/api/voice"})
public class VoiceController {

    private final SpeechToTextService speechToTextService;
    private final TextToSpeechService textToSpeechService;

    public VoiceController(SpeechToTextService speechToTextService, TextToSpeechService textToSpeechService) {
        this.speechToTextService = speechToTextService;
        this.textToSpeechService = textToSpeechService;
    }

    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SpeechToTextResponse> transcribe(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "audio", required = false) MultipartFile audio
    ) {
        MultipartFile effectiveFile = file != null ? file : audio;
        if (effectiveFile == null || effectiveFile.isEmpty()) {
            throw new AudioProcessingException("Audio file is required for transcription. Please provide 'file' or 'audio' multipart field.");
        }

        SpeechToTextResponse response = speechToTextService.transcribe(effectiveFile);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/speak", produces = "audio/wav")
    public ResponseEntity<byte[]> speak(@Valid @RequestBody TextToSpeechRequest request) {
        byte[] audioData = textToSpeechService.synthesizeSpeech(request.getText(), request.getVoice());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"speech.wav\"")
                .contentType(MediaType.parseMediaType("audio/wav"))
                .body(audioData);
    }
}
