package com.rockranger.analyzer.voice.service;

public interface TextToSpeechService {

    /**
     * Synthesizes speech using the default configured voice.
     *
     * @param text Text to synthesize
     * @return Audio byte array in WAV format
     */
    byte[] synthesizeSpeech(String text);

    /**
     * Synthesizes speech using a specific voice persona.
     *
     * @param text  Text to synthesize
     * @param voice Voice name (e.g. hannah, troy, austin, autumn, diana, daniel)
     * @return Audio byte array in WAV format
     */
    byte[] synthesizeSpeech(String text, String voice);
}
