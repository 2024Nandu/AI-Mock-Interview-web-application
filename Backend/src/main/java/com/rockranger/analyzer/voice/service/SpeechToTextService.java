package com.rockranger.analyzer.voice.service;

import com.rockranger.analyzer.voice.dto.response.SpeechToTextResponse;
import org.springframework.web.multipart.MultipartFile;

public interface SpeechToTextService {

    /**
     * Transcribes an audio file into text using Groq Whisper.
     *
     * @param audioFile MultipartFile containing candidate speech audio
     * @return SpeechToTextResponse with transcribed text
     */
    SpeechToTextResponse transcribe(MultipartFile audioFile);
}
