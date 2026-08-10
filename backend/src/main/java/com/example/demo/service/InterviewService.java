package com.example.demo.service;

import com.example.demo.dto.QuestionResponse;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InterviewService {
    private static final Logger logger = LoggerFactory.getLogger(InterviewService.class);

    private final InterviewSessionRepository sessionRepository;
    private final QaPairRepository qaPairRepository;
    private final UserRepository userRepository;
    private final InterviewRoleRepository roleRepository;
    private final ResumeRepository resumeRepository;
    private final LlmClientService llmClientService;
    private final EvaluationService evaluationService;

    public InterviewService(InterviewSessionRepository sessionRepository,
                            QaPairRepository qaPairRepository,
                            UserRepository userRepository,
                            InterviewRoleRepository roleRepository,
                            ResumeRepository resumeRepository,
                            LlmClientService llmClientService,
                            EvaluationService evaluationService) {
        this.sessionRepository = sessionRepository;
        this.qaPairRepository = qaPairRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.resumeRepository = resumeRepository;
        this.llmClientService = llmClientService;
        this.evaluationService = evaluationService;
    }

    @Transactional
    public QuestionResponse startInterview(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        InterviewRole role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Interview Track not found"));
        
        Optional<Resume> resumeOpt = resumeRepository.findFirstByUserIdOrderByUploadedAtDesc(userId);
        
        InterviewSession session = new InterviewSession();
        session.setUser(user);
        resumeOpt.ifPresent(session::setResume);
        session.setRole(role);
        session.setStatus(InterviewSession.SessionStatus.IN_PROGRESS);
        session.setCurrentQuestionNumber(1);
        session = sessionRepository.save(session);

        logger.info("Initializing interview session {} for user {} on track {}", session.getId(), user.getEmail(), role.getRoleKey());

        // Generate the first question
        String questionText = generateQuestion(session, null);
        
        QaPair qaPair = new QaPair();
        qaPair.setSession(session);
        qaPair.setQuestionNumber(1);
        qaPair.setQuestionText(questionText);
        qaPairRepository.save(qaPair);

        return new QuestionResponse(session.getId(), 1, questionText, "IN_PROGRESS");
    }

    @Transactional
    public QuestionResponse submitAnswer(Long sessionId, String answerText) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        
        if (session.getStatus() == InterviewSession.SessionStatus.COMPLETED) {
            throw new IllegalStateException("Interview session is already completed");
        }

        int currentQuestionNo = session.getCurrentQuestionNumber();
        
        // Save user's answer to the current QA pair
        QaPair currentQa = qaPairRepository.findBySessionIdAndQuestionNumber(sessionId, currentQuestionNo)
                .orElseThrow(() -> new IllegalStateException("QA pair not found for question " + currentQuestionNo));
        currentQa.setAnswerText(answerText);
        qaPairRepository.save(currentQa);

        logger.info("Session {} - Saved answer for Question {}", sessionId, currentQuestionNo);

        if (currentQuestionNo < 5) {
            // Move to next question
            int nextQuestionNo = currentQuestionNo + 1;
            session.setCurrentQuestionNumber(nextQuestionNo);
            sessionRepository.save(session);

            List<QaPair> history = qaPairRepository.findBySessionIdOrderByQuestionNumberAsc(sessionId);
            String questionText = generateQuestion(session, history);

            QaPair nextQa = new QaPair();
            nextQa.setSession(session);
            nextQa.setQuestionNumber(nextQuestionNo);
            nextQa.setQuestionText(questionText);
            qaPairRepository.save(nextQa);

            return new QuestionResponse(sessionId, nextQuestionNo, questionText, "IN_PROGRESS");
        } else {
            // Wrap up interview and generate report
            session.setStatus(InterviewSession.SessionStatus.COMPLETED);
            session.setCompletedAt(LocalDateTime.now());
            sessionRepository.save(session);

            logger.info("Session {} - 5-question limit reached. Evaluating and generating final report...", sessionId);
            evaluationService.generateReport(session);

            return new QuestionResponse(sessionId, 5, "Interview Completed. Generating your evaluation report...", "COMPLETED");
        }
    }

    private String generateQuestion(InterviewSession session, List<QaPair> history) {
        String roleTemplate = session.getRole().getPromptTemplate();
        String resumeJson = session.getResume() != null ? session.getResume().getParsedJson() : "No resume uploaded";

        String systemPrompt = "You are a professional AI interviewer for the role: " + session.getRole().getDisplayName() + ".\n" +
                "You conduct natural, technical mock interviews. You ask exactly one focused technical question at a time. " +
                "Keep your questions concise, clear, and relevant. Do not include any filler, greeting, or introductory text. Ask the question directly.\n" +
                "Candidate Resume Info (in JSON):\n" +
                resumeJson + "\n\n" +
                "Interview track requirements/instructions:\n" +
                roleTemplate;

        StringBuilder userPrompt = new StringBuilder();
        if (history == null || history.isEmpty()) {
            userPrompt.append("This is the beginning of the interview. Generate the very first question tailored to the candidate's background and the target role.");
        } else {
            userPrompt.append("Here is the dialogue history of the interview so far:\n\n");
            for (QaPair qa : history) {
                userPrompt.append("Interviewer: ").append(qa.getQuestionText()).append("\n");
                userPrompt.append("Candidate: ").append(qa.getAnswerText() != null ? qa.getAnswerText() : "(No Answer)").append("\n\n");
            }
            userPrompt.append("Generate the next logical technical question (Question ").append(session.getCurrentQuestionNumber()).append(") to test the candidate. Focus on core technical skills. Ask only one question directly.");
        }

        String question = llmClientService.generateCompletion(systemPrompt, userPrompt.toString(), 0.6);
        if (question == null || question.trim().isEmpty()) {
            question = "Can you elaborate on your technical experience and how you handle complex code structure and debugging?";
        }
        return question.trim();
    }
}
