package com.rockranger.analyzer.interview.service;

import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.interview.dto.request.StartInterviewRequest;
import com.rockranger.analyzer.interview.dto.request.SubmitAnswerRequest;
import com.rockranger.analyzer.interview.dto.response.AnswerEvaluationResponse;
import com.rockranger.analyzer.interview.dto.response.InterviewResponse;
import com.rockranger.analyzer.interview.dto.response.InterviewResultResponse;
import com.rockranger.analyzer.interview.dto.response.QuestionResponse;

import java.util.List;

public interface InterviewService {

    InterviewResponse startInterview(StartInterviewRequest request, User user);

    QuestionResponse getCurrentQuestion(Long interviewId, User user);

    AnswerEvaluationResponse submitAnswer(Long interviewId, SubmitAnswerRequest request, User user);

    QuestionResponse nextQuestion(Long interviewId, User user);

    InterviewResultResponse completeInterview(Long interviewId, User user);

    InterviewResultResponse getInterviewResult(Long interviewId, User user);

    List<InterviewResponse> getAllInterviews(User user);

    InterviewResponse getInterviewById(Long interviewId, User user);

    AnswerEvaluationResponse submitVoiceAnswer(Long interviewId, Long questionId, org.springframework.web.multipart.MultipartFile audioFile, User user);

    byte[] speakCurrentQuestion(Long interviewId, User user);

    byte[] generateInterviewReportPdf(Long interviewId, User user);
}
