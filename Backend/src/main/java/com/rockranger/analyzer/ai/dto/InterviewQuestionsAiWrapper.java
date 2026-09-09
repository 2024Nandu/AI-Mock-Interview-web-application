package com.rockranger.analyzer.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterviewQuestionsAiWrapper {

    private List<InterviewQuestionAiResponse> questions;

    public InterviewQuestionsAiWrapper() {
    }

    public InterviewQuestionsAiWrapper(List<InterviewQuestionAiResponse> questions) {
        this.questions = questions;
    }

    public List<InterviewQuestionAiResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<InterviewQuestionAiResponse> questions) {
        this.questions = questions;
    }
}
