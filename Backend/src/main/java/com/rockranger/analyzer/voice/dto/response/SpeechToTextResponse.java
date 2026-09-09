package com.rockranger.analyzer.voice.dto.response;

public class SpeechToTextResponse {

    private String text;

    public SpeechToTextResponse() {
    }

    public SpeechToTextResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
