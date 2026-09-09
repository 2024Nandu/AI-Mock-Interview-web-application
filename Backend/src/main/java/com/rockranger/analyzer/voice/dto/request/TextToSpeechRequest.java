package com.rockranger.analyzer.voice.dto.request;

import jakarta.validation.constraints.NotBlank;

public class TextToSpeechRequest {

    @NotBlank(message = "Text cannot be blank")
    private String text;

    private String voice;

    public TextToSpeechRequest() {
    }

    public TextToSpeechRequest(String text) {
        this.text = text;
    }

    public TextToSpeechRequest(String text, String voice) {
        this.text = text;
        this.voice = voice;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }
}
