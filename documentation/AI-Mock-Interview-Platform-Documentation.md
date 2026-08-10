# AI Mock Interview Platform — Technical Documentation

**Version:** 1.0
**Backend:** Java 21, Spring Boot
**Frontend:** React + Tailwind CSS
**Database:** MySQL
**Email/OTP Service:** Brevo (formerly Sendinblue)
**AI Services:** LLM API (Claude/OpenAI), Speech-to-Text, Text-to-Speech

---

## 1. Project Overview

An AI-powered mock interview platform where users:
1. Register/login with email verification via OTP (Brevo)
2. Upload their resume, which is parsed and structured using an LLM
3. Select an interview track/role (Fresher, Frontend, Backend, Full-Stack, Android, Data Analyst, DevOps, Professional, etc.)
4. Undergo a voice-based AI interview of 5 personalized questions
5. Receive a detailed evaluation report: per-question scores, strengths, weaknesses, a model answer for the weakest response, and a preparation roadmap

The system is split into three functional phases that map directly to development milestones:

| Phase | Capability |
|---|---|
| Phase 1 | Authentication (Register, OTP verification, Login) |
| Phase 2 | Resume upload, parsing, role selection |
| Phase 3 | Voice-based AI interview + evaluation report |

---

## 2. High-Level Architecture

```
┌─────────────────────┐        REST API (JSON, HTTPS)       ┌──────────────────────────┐
│   React + Tailwind   │ ───────────────────────────────────▶│      Spring Boot API     │
│   (SPA, Vite/CRA)     │◀─────────────────────────────────── │  (Java 21, Layered arch) │
└─────────────────────┘                                       └──────────────────────────┘
                                                                        │
                        ┌───────────────────────────────────────────────┼─────────────────────────┐
                        ▼                                               ▼                          ▼
                ┌───────────────┐                             ┌──────────────────┐       ┌───────────────────┐
                │  MySQL DB      │                             │   Brevo API        │       │   LLM API (Claude/ │
                │ (users, resumes,│                             │  (OTP email send)  │       │   OpenAI)           │
                │  sessions, etc) │                             └──────────────────┘       └───────────────────┘
                └───────────────┘                                                                    │
                                                                                              ┌────────┴─────────┐
                                                                                              ▼                  ▼
                                                                                        STT Service       TTS Service
                                                                                     (Web Speech API /   (Web Speech API /
                                                                                      Whisper API)         ElevenLabs API)
```

**Design principle:** Voice (STT/TTS) is treated as an I/O layer at the frontend edge. The backend interview state machine works purely in text — this keeps the core logic simple and swappable regardless of which voice vendor you pick later.

---

## 3. Tech Stack Summary

| Layer | Technology |
|---|---|
| Frontend | React 18+, Tailwind CSS, Axios/Fetch, React Router |
| Backend | Java 21, Spring Boot 3.x, Spring Web, Spring Security, Spring Data JPA |
| Database | MySQL 8.x |
| Auth | JWT (access token), BCrypt password hashing |
| OTP/Email | Brevo Transactional Email API |
| File Parsing | Apache PDFBox (PDF), Apache POI (DOCX) |
| AI/LLM | Claude or OpenAI API (resume parsing, question generation, evaluation) |
| Voice | Web Speech API (MVP) → Whisper (STT) / ElevenLabs (TTS) (upgrade path) |
| Build Tools | Maven/Gradle (backend), Vite (frontend) |

---

## 4. Database Schema (MySQL)

### 4.1 `users`
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4.2 `otp_verifications`
```sql
CREATE TABLE otp_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    purpose ENUM('REGISTRATION','PASSWORD_RESET') DEFAULT 'REGISTRATION',
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 4.3 `resumes`
```sql
CREATE TABLE resumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    original_file_path VARCHAR(500),
    raw_text LONGTEXT,
    parsed_json JSON,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 4.4 `interview_roles`
```sql
CREATE TABLE interview_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_key VARCHAR(50) UNIQUE NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    prompt_template TEXT NOT NULL
);
```

### 4.5 `interview_sessions`
```sql
CREATE TABLE interview_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    resume_id BIGINT,
    role_id BIGINT NOT NULL,
    status ENUM('IN_PROGRESS','COMPLETED') DEFAULT 'IN_PROGRESS',
    current_question_number INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (resume_id) REFERENCES resumes(id),
    FOREIGN KEY (role_id) REFERENCES interview_roles(id)
);
```

### 4.6 `qa_pairs`
```sql
CREATE TABLE qa_pairs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    question_number INT NOT NULL,
    question_text TEXT NOT NULL,
    answer_text TEXT,
    score DECIMAL(3,1),
    score_justification TEXT,
    FOREIGN KEY (session_id) REFERENCES interview_sessions(id)
);
```

### 4.7 `final_reports`
```sql
CREATE TABLE final_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT UNIQUE NOT NULL,
    overall_score DECIMAL(3,1),
    strength_1 TEXT, strength_2 TEXT,
    weakness_1 TEXT, weakness_2 TEXT,
    weakest_question_number INT,
    model_answer TEXT,
    preparation_suggestions JSON,
    closing_note TEXT,
    FOREIGN KEY (session_id) REFERENCES interview_sessions(id)
);
```

---

## 5. Phase 1 — Authentication with Brevo OTP Verification

### 5.1 Flow

```
Register:
  User submits {name, email, password}
    → backend validates + hashes password (BCrypt)
    → user row created with is_verified = FALSE
    → generate 6-digit OTP, store in otp_verifications with expiry (e.g. 10 min)
    → call Brevo API to email OTP to user
    → frontend shows "Enter OTP" screen

Verify OTP:
  User submits {email, otp}
    → backend checks otp_verifications: matches, not expired, not used
    → mark is_used = TRUE, set users.is_verified = TRUE
    → issue JWT, log user in

Login:
  User submits {email, password}
    → backend verifies password hash
    → check is_verified = TRUE (block login otherwise, offer OTP resend)
    → issue JWT
```

### 5.2 Brevo Integration (Theory)

Brevo provides a transactional email API. Your Spring Boot backend calls Brevo's REST endpoint server-side (never expose the Brevo API key to the frontend).

```java
@Service
public class BrevoEmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    public void sendOtpEmail(String toEmail, String otpCode) {
        // POST https://api.brevo.com/v3/smtp/email
        // Headers: api-key: brevoApiKey
        // Body: {
        //   sender: {name, email},
        //   to: [{email: toEmail}],
        //   subject: "Your OTP Code",
        //   htmlContent: "<p>Your OTP is <b>" + otpCode + "</b>. Valid for 10 minutes.</p>"
        // }
    }
}
```

### 5.3 REST Endpoints — Phase 1

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create unverified user, send OTP |
| POST | `/api/auth/verify-otp` | Verify OTP, activate account, return JWT |
| POST | `/api/auth/resend-otp` | Generate + send a new OTP |
| POST | `/api/auth/login` | Authenticate, return JWT |
| POST | `/api/auth/forgot-password` | Send OTP for password reset |
| POST | `/api/auth/reset-password` | Verify OTP + set new password |

### 5.4 Security Notes
- Passwords hashed with `BCryptPasswordEncoder`.
- OTPs: 6-digit numeric, 10-minute expiry, single-use, rate-limited (e.g. max 3 requests per 15 minutes per email) to prevent abuse of the Brevo quota.
- JWT signed with a strong secret (`HS256` minimum), short-lived access token (e.g. 1 hour) + optional refresh token.
- All protected endpoints validated via a `JwtAuthenticationFilter` in the Spring Security filter chain.

---

## 6. Phase 2 — Resume Upload, Parsing, Role Selection

### 6.1 Flow

```
Upload Resume (PDF/DOCX)
   → Spring Boot receives multipart file
   → Apache PDFBox (PDF) or Apache POI (DOCX) extracts raw text
   → raw text stored in resumes.raw_text
   → raw text sent to LLM API with a structuring prompt
   → LLM returns structured JSON: {skills, experience, projects, education, summary}
   → stored in resumes.parsed_json (MySQL JSON column)

Select Role
   → GET /api/roles returns list from interview_roles table
   → user selects one → stored as part of the upcoming interview_session
```

### 6.2 Resume Text Extraction (Spring Boot)

```java
@Service
public class ResumeParsingService {

    public String extractText(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename.endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                return new PDFTextStripper().getText(doc);
            }
        } else if (filename.endsWith(".docx")) {
            try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
                return new XWPFWordExtractor(doc).getText();
            }
        }
        throw new UnsupportedFileTypeException(filename);
    }
}
```

### 6.3 LLM Structuring Prompt (Concept)

```
System: You are a resume parser. Given raw resume text, extract and return
ONLY valid JSON with this structure, no other text:
{
  "name": "",
  "skills": [],
  "experience": [{"company": "", "role": "", "duration": "", "highlights": []}],
  "projects": [{"title": "", "description": "", "techStack": []}],
  "education": [{"institution": "", "degree": "", "year": ""}]
}

User: <raw resume text>
```

### 6.4 REST Endpoints — Phase 2

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/resumes/upload` | Multipart upload, triggers extraction + LLM structuring |
| GET | `/api/resumes/{userId}/latest` | Fetch the most recent parsed resume |
| GET | `/api/roles` | List all available interview roles |

### 6.5 Role Presets (seeded into `interview_roles`)

Fresher, Frontend Developer, Backend Developer, Full-Stack Developer, Android Developer, Data Analyst, DevOps Engineer, Professional (Experienced Generalist) — each with its own `prompt_template` describing topic focus, similar to the SDE Intern/Data Analyst/Frontend presets discussed earlier.

---

## 7. Phase 3 — Voice-Based AI Interview + Evaluation

### 7.1 Voice Pipeline (Concept)

```
LLM generates question (text)
   → TTS converts text to audio → played to candidate
Candidate speaks answer
   → STT converts audio to text → sent to backend as the answer
```

**MVP approach:** Use the browser-native **Web Speech API** (`SpeechRecognition` for STT, `SpeechSynthesis` for TTS) — zero backend audio handling, works in-browser (best support in Chrome).

**Upgrade path:** Swap to Whisper API (STT) and ElevenLabs/OpenAI TTS (server-side) for higher accuracy and more natural voice, without touching the interview state machine.

### 7.2 Interview State Machine

The backend is the single source of truth for question count — never rely on the LLM to self-track progress.

```java
@Service
public class InterviewService {

    public QuestionResponse startInterview(Long userId, Long roleId) {
        // fetch latest resume.parsed_json + role.prompt_template
        // build system prompt combining both
        // create interview_session row (current_question_number = 0)
        // call LLM -> Question 1
        // save qa_pairs row (question_number=1)
        // increment current_question_number
        // return question text to frontend
    }

    public QuestionResponse submitAnswer(Long sessionId, String answerText) {
        InterviewSession session = repo.findById(sessionId)...;
        // save answerText into current qa_pairs row

        if (session.getCurrentQuestionNumber() < 5) {
            // call LLM for next question using full Q&A history so far
            // save new qa_pairs row, increment counter
            return nextQuestionResponse;
        } else {
            session.setStatus(COMPLETED);
            FinalReport report = evaluationService.generateReport(session);
            return finalReportResponse;
        }
    }
}
```

### 7.3 Evaluation Prompt (Concept)

```
System: You are an interview evaluator. Given the role, resume context,
and all 5 question-answer pairs, return ONLY valid JSON:
{
  "overallScore": 0.0,
  "questionScores": [{"questionNumber": 1, "score": 0.0, "justification": ""}],
  "strengths": ["", ""],
  "weaknesses": ["", ""],
  "weakestQuestionNumber": 0,
  "modelAnswerForWeakest": "",
  "preparationSuggestions": ["", ""],
  "closingNote": ""
}
```

This maps directly into the `final_reports` table.

### 7.4 REST Endpoints — Phase 3

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/interviews/start` | body: `{roleId}` → creates session, returns Q1 |
| POST | `/api/interviews/{sessionId}/answer` | body: `{answerText}` → next question OR final report |
| GET | `/api/interviews/{sessionId}` | Fetch session state (page-refresh recovery) |
| GET | `/api/interviews/history/{userId}` | List past interview sessions |
| GET | `/api/interviews/{sessionId}/report` | Fetch the final report |

---

## 8. Frontend Structure (React + Tailwind)

```
src/
 ├── api/
 │    ├── authApi.js
 │    ├── resumeApi.js
 │    └── interviewApi.js
 ├── components/
 │    ├── auth/ (RegisterForm, OtpForm, LoginForm)
 │    ├── resume/ (ResumeUpload, RoleSelectCard)
 │    ├── interview/ (QuestionDisplay, MicButton, ProgressBar)
 │    └── report/ (ScoreCard, StrengthsWeaknesses, ModelAnswer)
 ├── pages/
 │    ├── RegisterPage.jsx
 │    ├── OtpVerifyPage.jsx
 │    ├── LoginPage.jsx
 │    ├── ResumeUploadPage.jsx
 │    ├── RoleSelectPage.jsx
 │    ├── InterviewPage.jsx
 │    └── ReportPage.jsx
 ├── hooks/
 │    ├── useSpeechRecognition.js
 │    └── useSpeechSynthesis.js
 ├── context/
 │    └── AuthContext.jsx
 └── App.jsx
```

**Routing (React Router):**
```
/register → /verify-otp → /login → /upload-resume → /select-role → /interview/:sessionId → /report/:sessionId
```

**State management:** `useState`/`useReducer` + `AuthContext` for JWT/user state is sufficient at this scale — no need for Redux.

---

## 9. Backend Structure (Spring Boot)

```
src/main/java/com/mockinterview/
 ├── config/          (SecurityConfig, JwtConfig, CorsConfig)
 ├── controller/       (AuthController, ResumeController, RoleController,
 │                       InterviewController)
 ├── service/          (AuthService, BrevoEmailService, ResumeParsingService,
 │                       LlmClientService, InterviewService, EvaluationService)
 ├── repository/       (UserRepository, ResumeRepository, InterviewSessionRepository,
 │                       QaPairRepository, FinalReportRepository, RoleRepository)
 ├── entity/           (User, Resume, InterviewRole, InterviewSession, QaPair, FinalReport, OtpVerification)
 ├── dto/              (RegisterRequest, LoginRequest, QuestionResponse, FinalReportDto, etc.)
 ├── security/         (JwtAuthenticationFilter, JwtUtil)
 └── exception/        (GlobalExceptionHandler)
```

---

## 10. Build Order / Milestones

1. **Phase 1:** Users table + Spring Security + JWT + Brevo OTP email → test register/verify/login end-to-end.
2. **Phase 2a:** File upload + PDFBox/POI text extraction (no LLM yet) → confirm raw text extraction works.
3. **Phase 2b:** Wire LLM structuring call → confirm parsed_json populates correctly.
4. **Phase 2c:** Seed `interview_roles`, build role selection UI.
5. **Phase 3a:** Build interview state machine in **text-only mode** first (no voice) → validate 5-question loop + evaluation JSON parsing.
6. **Phase 3b:** Add Web Speech API (STT/TTS) on the frontend as an I/O wrapper around the already-working text flow.
7. **Polish:** History dashboard, error handling, loading states, rate limiting on OTP and LLM calls.

**Why this order:** voice and LLM integrations are the most failure-prone parts (network/API/browser-support issues). Validating the state machine and data flow in plain text first isolates those risks before adding audio complexity on top.

---

## 11. Environment Variables (Backend)

```
DB_URL=jdbc:mysql://localhost:3306/mock_interview_db
DB_USERNAME=...
DB_PASSWORD=...
JWT_SECRET=...
JWT_EXPIRATION_MS=3600000
BREVO_API_KEY=...
BREVO_SENDER_EMAIL=...
LLM_API_KEY=...
LLM_API_URL=...
```

Never commit these to source control — use `application.yml` + environment-specific profiles, or a secrets manager in production.

---

## 12. Summary

This architecture keeps each phase loosely coupled: Auth doesn't know about resumes, resume parsing doesn't know about interviews, and the interview state machine doesn't know whether input came from text or voice. This separation makes the system easier to build incrementally, test in isolation, and extend later (e.g. adding new roles, swapping LLM/voice providers, or adding analytics) without rewriting core logic.
