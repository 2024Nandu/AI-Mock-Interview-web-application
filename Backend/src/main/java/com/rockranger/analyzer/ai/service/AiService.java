package com.rockranger.analyzer.ai.service;

import com.rockranger.analyzer.ai.dto.AnswerEvaluationAiResponse;
import com.rockranger.analyzer.ai.dto.FinalFeedbackAiResponse;
import com.rockranger.analyzer.ai.dto.InterviewQuestionAiResponse;
import com.rockranger.analyzer.ai.dto.ResumeAiResponse;

import java.util.List;
import java.util.Map;

public interface AiService {

    ResumeAiResponse analyzeResume(String resumeText);

    List<InterviewQuestionAiResponse> generateInterviewQuestions(String structuredResume, Integer numberOfQuestions, String interviewType);

    AnswerEvaluationAiResponse evaluateAnswer(String structuredResume, String question, String answer);

    FinalFeedbackAiResponse generateFinalFeedback(String structuredResume, List<Map<String, Object>> qaList);
}
