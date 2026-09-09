package com.rockranger.analyzer.interview.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.analyzer.ai.dto.AnswerEvaluationAiResponse;
import com.rockranger.analyzer.ai.dto.FinalFeedbackAiResponse;
import com.rockranger.analyzer.ai.dto.InterviewQuestionAiResponse;
import com.rockranger.analyzer.ai.service.AiService;
import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.interview.dto.request.StartInterviewRequest;
import com.rockranger.analyzer.interview.dto.request.SubmitAnswerRequest;
import com.rockranger.analyzer.interview.dto.response.AnswerEvaluationResponse;
import com.rockranger.analyzer.interview.dto.response.InterviewResponse;
import com.rockranger.analyzer.interview.dto.response.InterviewResultResponse;
import com.rockranger.analyzer.interview.dto.response.QuestionResponse;
import com.rockranger.analyzer.interview.entity.*;
import com.rockranger.analyzer.interview.exception.InterviewAlreadyCompletedException;
import com.rockranger.analyzer.interview.exception.InterviewNotFoundException;
import com.rockranger.analyzer.interview.exception.QuestionNotFoundException;
import com.rockranger.analyzer.interview.repository.InterviewAnswerRepository;
import com.rockranger.analyzer.interview.repository.InterviewQuestionRepository;
import com.rockranger.analyzer.interview.repository.InterviewRepository;
import com.rockranger.analyzer.interview.repository.InterviewResultRepository;
import com.rockranger.analyzer.interview.service.InterviewService;
import com.rockranger.analyzer.resume.entity.Resume;
import com.rockranger.analyzer.resume.entity.ResumeAnalysis;
import com.rockranger.analyzer.resume.exception.ResumeAnalysisNotFoundException;
import com.rockranger.analyzer.resume.exception.ResumeNotFoundException;
import com.rockranger.analyzer.resume.repository.ResumeAnalysisRepository;
import com.rockranger.analyzer.resume.repository.ResumeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterviewServiceImpl implements InterviewService {

    private static final Logger logger = LoggerFactory.getLogger(InterviewServiceImpl.class);

    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final InterviewAnswerRepository interviewAnswerRepository;
    private final InterviewResultRepository interviewResultRepository;
    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public InterviewServiceImpl(
            InterviewRepository interviewRepository,
            InterviewQuestionRepository interviewQuestionRepository,
            InterviewAnswerRepository interviewAnswerRepository,
            InterviewResultRepository interviewResultRepository,
            ResumeRepository resumeRepository,
            ResumeAnalysisRepository resumeAnalysisRepository,
            AiService aiService,
            ObjectMapper objectMapper
    ) {
        this.interviewRepository = interviewRepository;
        this.interviewQuestionRepository = interviewQuestionRepository;
        this.interviewAnswerRepository = interviewAnswerRepository;
        this.interviewResultRepository = interviewResultRepository;
        this.resumeRepository = resumeRepository;
        this.resumeAnalysisRepository = resumeAnalysisRepository;
        this.aiService = aiService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public InterviewResponse startInterview(StartInterviewRequest request, User user) {
        logger.info("Starting new interview for user: {} with resume ID: {}", user.getEmail(), request.getResumeId());

        Resume resume = resumeRepository.findByIdAndUser(request.getResumeId(), user)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found with id: " + request.getResumeId()));

        ResumeAnalysis analysis = resumeAnalysisRepository.findByResumeIdAndResumeUser(request.getResumeId(), user)
                .orElseThrow(() -> new ResumeAnalysisNotFoundException("Resume analysis not found for resume ID: " + request.getResumeId() + ". Please analyze the resume first before starting an interview."));

        if (analysis.getStructuredResumeJson() == null || analysis.getStructuredResumeJson().isBlank()) {
            throw new RuntimeException("Structured resume data is missing. Please re-analyze your resume first.");
        }

        List<InterviewQuestionAiResponse> generatedQuestions = aiService.generateInterviewQuestions(
                analysis.getStructuredResumeJson(),
                request.getNumberOfQuestions(),
                request.getInterviewType()
        );

        if (generatedQuestions == null || generatedQuestions.isEmpty()) {
            throw new RuntimeException("Failed to generate questions for the interview. Please try again.");
        }

        Interview interview = new Interview();
        interview.setUser(user);
        interview.setResume(resume);
        interview.setTotalQuestions(generatedQuestions.size());
        interview.setCurrentQuestion(1);
        interview.setInterviewType(request.getInterviewType() != null && !request.getInterviewType().isBlank() ? request.getInterviewType() : "TECHNICAL");
        interview.setStatus(InterviewStatus.IN_PROGRESS);
        interview.setStartedAt(LocalDateTime.now());

        List<InterviewQuestion> questions = new ArrayList<>();
        int index = 1;
        for (InterviewQuestionAiResponse qAi : generatedQuestions) {
            InterviewQuestion question = new InterviewQuestion();
            question.setInterview(interview);
            question.setQuestionNumber(qAi.getQuestionNumber() != null ? qAi.getQuestionNumber() : index);
            question.setQuestion(qAi.getQuestion());
            question.setCategory(qAi.getCategory() != null ? qAi.getCategory() : "TECHNICAL");
            question.setDifficulty(qAi.getDifficulty() != null ? qAi.getDifficulty() : "MEDIUM");
            questions.add(question);
            index++;
        }

        interview.setQuestions(questions);
        Interview savedInterview = interviewRepository.save(interview);
        logger.info("Created interview session with ID: {} and {} questions", savedInterview.getId(), savedInterview.getTotalQuestions());

        return mapToInterviewResponse(savedInterview);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getCurrentQuestion(Long interviewId, User user) {
        Interview interview = interviewRepository.findByIdAndUser(interviewId, user)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + interviewId));

        InterviewQuestion question = interviewQuestionRepository
                .findByInterviewIdAndQuestionNumber(interviewId, interview.getCurrentQuestion())
                .orElseThrow(() -> new QuestionNotFoundException("Question not found for interview " + interviewId + " at position " + interview.getCurrentQuestion()));

        boolean alreadyAnswered = interviewAnswerRepository.findByQuestionId(question.getId()).isPresent();

        return new QuestionResponse(
                question.getId(),
                question.getQuestionNumber(),
                interview.getTotalQuestions(),
                question.getQuestion(),
                question.getCategory(),
                question.getDifficulty(),
                alreadyAnswered
        );
    }

    @Override
    @Transactional
    public AnswerEvaluationResponse submitAnswer(Long interviewId, SubmitAnswerRequest request, User user) {
        Interview interview = interviewRepository.findByIdAndUser(interviewId, user)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + interviewId));

        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new InterviewAlreadyCompletedException("Interview " + interviewId + " has already been completed.");
        }

        InterviewQuestion question = interviewQuestionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new QuestionNotFoundException("Question not found with id: " + request.getQuestionId()));

        if (!question.getInterview().getId().equals(interviewId)) {
            throw new QuestionNotFoundException("Question with id " + request.getQuestionId() + " does not belong to interview " + interviewId);
        }

        ResumeAnalysis analysis = resumeAnalysisRepository.findByResume(interview.getResume()).orElse(null);
        String structuredResume = analysis != null ? analysis.getStructuredResumeJson() : "{}";

        AnswerEvaluationAiResponse eval = aiService.evaluateAnswer(
                structuredResume,
                question.getQuestion(),
                request.getAnswer()
        );

        InterviewAnswer answer = interviewAnswerRepository.findByQuestionId(question.getId())
                .orElse(new InterviewAnswer());

        answer.setQuestion(question);
        answer.setAnswerText(request.getAnswer());
        answer.setAnswerType("TEXT");
        answer.setScore(eval.getScore());
        answer.setTechnicalAccuracy(eval.getTechnicalAccuracy());
        answer.setCommunicationScore(eval.getCommunicationScore());
        answer.setRelevanceScore(eval.getRelevanceScore());
        answer.setFeedback(eval.getFeedback());
        answer.setBetterAnswer(eval.getBetterAnswer());
        answer.setAnsweredAt(LocalDateTime.now());

        interviewAnswerRepository.save(answer);

        return new AnswerEvaluationResponse(
                question.getId(),
                question.getQuestionNumber(),
                answer.getAnswerText(),
                answer.getScore(),
                answer.getTechnicalAccuracy(),
                answer.getCommunicationScore(),
                answer.getRelevanceScore(),
                answer.getFeedback(),
                answer.getBetterAnswer()
        );
    }

    @Override
    @Transactional
    public QuestionResponse nextQuestion(Long interviewId, User user) {
        Interview interview = interviewRepository.findByIdAndUser(interviewId, user)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + interviewId));

        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new InterviewAlreadyCompletedException("Interview " + interviewId + " has already been completed.");
        }

        if (interview.getCurrentQuestion() < interview.getTotalQuestions()) {
            interview.setCurrentQuestion(interview.getCurrentQuestion() + 1);
            interviewRepository.save(interview);
        }

        return getCurrentQuestion(interviewId, user);
    }

    @Override
    @Transactional
    public InterviewResultResponse completeInterview(Long interviewId, User user) {
        Interview interview = interviewRepository.findByIdAndUser(interviewId, user)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + interviewId));

        if (interview.getStatus() == InterviewStatus.COMPLETED) {
            return getInterviewResult(interviewId, user);
        }

        List<InterviewQuestion> questions = interviewQuestionRepository.findByInterviewIdOrderByQuestionNumber(interviewId);
        List<InterviewAnswer> answers = interviewAnswerRepository.findByQuestionInterviewIdOrderByQuestionQuestionNumber(interviewId);

        Map<Long, InterviewAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a));

        List<Map<String, Object>> qaList = new ArrayList<>();
        for (InterviewQuestion q : questions) {
            InterviewAnswer a = answerMap.get(q.getId());
            Map<String, Object> qa = new HashMap<>();
            qa.put("questionNumber", q.getQuestionNumber());
            qa.put("category", q.getCategory());
            qa.put("question", q.getQuestion());
            if (a != null) {
                qa.put("answer", a.getAnswerText());
                qa.put("score", a.getScore());
                qa.put("technicalAccuracy", a.getTechnicalAccuracy());
                qa.put("communicationScore", a.getCommunicationScore());
                qa.put("relevanceScore", a.getRelevanceScore());
                qa.put("feedback", a.getFeedback());
            } else {
                qa.put("answer", "Not answered");
                qa.put("score", 0);
            }
            qaList.add(qa);
        }

        ResumeAnalysis analysis = resumeAnalysisRepository.findByResume(interview.getResume()).orElse(null);
        String structuredResume = analysis != null ? analysis.getStructuredResumeJson() : "{}";

        FinalFeedbackAiResponse finalAi = aiService.generateFinalFeedback(structuredResume, qaList);

        InterviewResult result = new InterviewResult();
        result.setInterview(interview);
        result.setOverallScore(finalAi.getOverallScore());
        result.setTechnicalScore(finalAi.getTechnicalScore());
        result.setCommunicationScore(finalAi.getCommunicationScore());
        result.setProblemSolvingScore(finalAi.getProblemSolvingScore());

        try {
            result.setStrengths(objectMapper.writeValueAsString(finalAi.getStrengths()));
            result.setImprovements(objectMapper.writeValueAsString(finalAi.getImprovements()));
        } catch (Exception e) {
            logger.warn("Could not serialize strengths/improvements as JSON array, using string representation", e);
            result.setStrengths(String.valueOf(finalAi.getStrengths()));
            result.setImprovements(String.valueOf(finalAi.getImprovements()));
        }

        result.setFinalFeedback(finalAi.getFinalFeedback());
        result.setCompletedAt(LocalDateTime.now());
        interviewResultRepository.save(result);

        interview.setStatus(InterviewStatus.COMPLETED);
        interview.setCompletedAt(LocalDateTime.now());
        interviewRepository.save(interview);

        return buildInterviewResultResponse(result, answers);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewResultResponse getInterviewResult(Long interviewId, User user) {
        Interview interview = interviewRepository.findByIdAndUser(interviewId, user)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + interviewId));

        InterviewResult result = interviewResultRepository.findByInterviewId(interviewId)
                .orElseThrow(() -> new RuntimeException("Interview result not found for interview " + interviewId + ". Please complete the interview first."));

        List<InterviewAnswer> answers = interviewAnswerRepository.findByQuestionInterviewIdOrderByQuestionQuestionNumber(interviewId);
        return buildInterviewResultResponse(result, answers);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewResponse> getAllInterviews(User user) {
        return interviewRepository.findByUserOrderByStartedAtDesc(user)
                .stream()
                .map(this::mapToInterviewResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(Long interviewId, User user) {
        Interview interview = interviewRepository.findByIdAndUser(interviewId, user)
                .orElseThrow(() -> new InterviewNotFoundException("Interview not found with id: " + interviewId));
        return mapToInterviewResponse(interview);
    }

    private InterviewResponse mapToInterviewResponse(Interview interview) {
        return new InterviewResponse(
                interview.getId(),
                interview.getResume().getId(),
                interview.getTotalQuestions(),
                interview.getCurrentQuestion(),
                interview.getInterviewType(),
                interview.getStatus().name(),
                interview.getStartedAt(),
                interview.getCompletedAt()
        );
    }

    private InterviewResultResponse buildInterviewResultResponse(InterviewResult result, List<InterviewAnswer> answers) {
        List<String> strengthsList = new ArrayList<>();
        if (result.getStrengths() != null) {
            try {
                strengthsList = objectMapper.readValue(result.getStrengths(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                strengthsList = List.of(result.getStrengths());
            }
        }

        List<String> improvementsList = new ArrayList<>();
        if (result.getImprovements() != null) {
            try {
                improvementsList = objectMapper.readValue(result.getImprovements(), new TypeReference<List<String>>() {});
            } catch (Exception e) {
                improvementsList = List.of(result.getImprovements());
            }
        }

        List<AnswerEvaluationResponse> answerResponses = answers.stream()
                .map(a -> new AnswerEvaluationResponse(
                        a.getQuestion().getId(),
                        a.getQuestion().getQuestionNumber(),
                        a.getAnswerText(),
                        a.getScore(),
                        a.getTechnicalAccuracy(),
                        a.getCommunicationScore(),
                        a.getRelevanceScore(),
                        a.getFeedback(),
                        a.getBetterAnswer()
                ))
                .collect(Collectors.toList());

        return new InterviewResultResponse(
                result.getInterview().getId(),
                result.getOverallScore(),
                result.getTechnicalScore(),
                result.getCommunicationScore(),
                result.getProblemSolvingScore(),
                strengthsList,
                improvementsList,
                result.getFinalFeedback(),
                answerResponses,
                result.getCompletedAt()
        );
    }
}
