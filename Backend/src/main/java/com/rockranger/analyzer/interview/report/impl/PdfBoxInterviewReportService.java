package com.rockranger.analyzer.interview.report.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.interview.entity.Interview;
import com.rockranger.analyzer.interview.entity.InterviewAnswer;
import com.rockranger.analyzer.interview.entity.InterviewQuestion;
import com.rockranger.analyzer.interview.entity.InterviewResult;
import com.rockranger.analyzer.interview.report.InterviewReportService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PdfBoxInterviewReportService implements InterviewReportService {

    private static final Logger logger = LoggerFactory.getLogger(PdfBoxInterviewReportService.class);

    private static final float MARGIN_X = 40f;
    private static final float MARGIN_Y = 40f;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    // Corporate Color Palette
    private static final Color COLOR_PRIMARY = new Color(24, 43, 73);      // Deep Navy
    private static final Color COLOR_ACCENT = new Color(37, 99, 235);      // Royal Blue
    private static final Color COLOR_BG_CARD = new Color(248, 250, 252);   // Light Gray Slate
    private static final Color COLOR_BORDER = new Color(226, 232, 240);    // Border Gray
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);    // Slate Dark
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139); // Slate Muted
    private static final Color COLOR_SUCCESS = new Color(22, 101, 52);     // Forest Green
    private static final Color COLOR_AMBER = new Color(180, 83, 9);        // Deep Amber
    private static final Color COLOR_WHITE = Color.WHITE;

    private final ObjectMapper objectMapper;
    private final PDType1Font fontRegular;
    private final PDType1Font fontBold;
    private final PDType1Font fontOblique;

    public PdfBoxInterviewReportService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        this.fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        this.fontOblique = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);
    }

    @Override
    public byte[] generatePdfReport(
            Interview interview,
            InterviewResult result,
            List<InterviewQuestion> questions,
            List<InterviewAnswer> answers,
            User user
    ) {
        logger.info("Generating PDF interview report for interview ID: {}, candidate: {}",
                interview.getId(), user.getEmail());

        try (PDDocument document = new PDDocument()) {
            ReportContext ctx = new ReportContext(document, PDRectangle.A4);

            // 1. Executive Title Banner
            drawHeaderBanner(ctx, interview);

            // 2. Candidate & Session Metadata Card
            drawCandidateDetailsCard(ctx, interview, user);

            // 3. Performance Scorecard Cards
            drawScorecards(ctx, result);

            // 4. Strengths & Improvements
            drawStrengthsAndImprovements(ctx, result);

            // 5. Question-by-Question Deep Dive
            drawQuestionAnalyses(ctx, questions, answers);

            // 6. Final AI Feedback
            drawFinalFeedback(ctx, result);

            // 7. Running Footers on all pages
            ctx.drawFooters(fontRegular, fontBold);

            ctx.close();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            logger.info("Successfully generated PDF report for interview ID: {} ({} pages, {} bytes)",
                    interview.getId(), document.getNumberOfPages(), outputStream.size());

            return outputStream.toByteArray();

        } catch (Exception e) {
            logger.error("Error creating PDF interview report", e);
            throw new RuntimeException("Failed to generate interview PDF report: " + e.getMessage(), e);
        }
    }

    private void drawHeaderBanner(ReportContext ctx, Interview interview) throws IOException {
        float bannerHeight = 70f;
        float y = ctx.y - bannerHeight;

        // Background dark bar
        ctx.fillRect(MARGIN_X, y, ctx.contentWidth, bannerHeight, COLOR_PRIMARY);

        // Title text
        ctx.drawText("AI MOCK INTERVIEW PERFORMANCE REPORT",
                MARGIN_X + 20f, y + 40f, fontBold, 16f, COLOR_WHITE);

        String subtitle = String.format("Interview ID: #%d | Track: %s | Questions: %d",
                interview.getId(),
                interview.getInterviewType() != null ? interview.getInterviewType() : "TECHNICAL",
                interview.getTotalQuestions());
        ctx.drawText(subtitle, MARGIN_X + 20f, y + 20f, fontRegular, 10f, new Color(203, 213, 225));

        ctx.y = y - 18f;
    }

    private void drawCandidateDetailsCard(ReportContext ctx, Interview interview, User user) throws IOException {
        float cardHeight = 65f;
        ctx.ensureSpace(cardHeight + 20f);

        float y = ctx.y - cardHeight;

        // Card container
        ctx.fillRect(MARGIN_X, y, ctx.contentWidth, cardHeight, COLOR_BG_CARD);
        ctx.drawRect(MARGIN_X, y, ctx.contentWidth, cardHeight, COLOR_BORDER, 1f);

        float col1X = MARGIN_X + 16f;
        float col2X = MARGIN_X + (ctx.contentWidth / 2f) + 10f;

        // Column 1: Candidate info
        ctx.drawText("Candidate Name:", col1X, y + 44f, fontBold, 9f, COLOR_TEXT_MUTED);
        ctx.drawText(sanitize(user.getFullName()), col1X + 85f, y + 44f, fontBold, 10f, COLOR_TEXT_DARK);

        ctx.drawText("Email Address:", col1X, y + 24f, fontBold, 9f, COLOR_TEXT_MUTED);
        ctx.drawText(sanitize(user.getEmail()), col1X + 85f, y + 24f, fontRegular, 10f, COLOR_TEXT_DARK);

        // Column 2: Session timing & Status
        ctx.drawText("Completed At:", col2X, y + 44f, fontBold, 9f, COLOR_TEXT_MUTED);
        String completedDate = interview.getCompletedAt() != null
                ? interview.getCompletedAt().format(DATE_FORMATTER)
                : "N/A";
        ctx.drawText(completedDate, col2X + 75f, y + 44f, fontRegular, 10f, COLOR_TEXT_DARK);

        ctx.drawText("Session Status:", col2X, y + 24f, fontBold, 9f, COLOR_TEXT_MUTED);
        ctx.drawText("COMPLETED", col2X + 75f, y + 24f, fontBold, 10f, COLOR_SUCCESS);

        ctx.y = y - 20f;
    }

    private void drawScorecards(ReportContext ctx, InterviewResult result) throws IOException {
        float cardHeight = 60f;
        ctx.ensureSpace(cardHeight + 35f);

        drawSectionTitle(ctx, "OVERALL EVALUATION SCORECARD");

        float gap = 10f;
        float cardWidth = (ctx.contentWidth - (gap * 3)) / 4f;
        float y = ctx.y - cardHeight;

        int overall = result != null && result.getOverallScore() != null ? result.getOverallScore() : 0;
        int tech = result != null && result.getTechnicalScore() != null ? result.getTechnicalScore() : 0;
        int comm = result != null && result.getCommunicationScore() != null ? result.getCommunicationScore() : 0;
        int problem = result != null && result.getProblemSolvingScore() != null ? result.getProblemSolvingScore() : 0;

        drawSingleScoreCard(ctx, MARGIN_X + 0 * (cardWidth + gap), y, cardWidth, cardHeight, "Overall Score", overall, COLOR_ACCENT);
        drawSingleScoreCard(ctx, MARGIN_X + 1 * (cardWidth + gap), y, cardWidth, cardHeight, "Technical Depth", tech, COLOR_PRIMARY);
        drawSingleScoreCard(ctx, MARGIN_X + 2 * (cardWidth + gap), y, cardWidth, cardHeight, "Communication", comm, COLOR_SUCCESS);
        drawSingleScoreCard(ctx, MARGIN_X + 3 * (cardWidth + gap), y, cardWidth, cardHeight, "Problem Solving", problem, COLOR_AMBER);

        ctx.y = y - 22f;
    }

    private void drawSingleScoreCard(
            ReportContext ctx,
            float x,
            float y,
            float width,
            float height,
            String label,
            int score,
            Color badgeColor
    ) throws IOException {
        ctx.fillRect(x, y, width, height, COLOR_BG_CARD);
        ctx.drawRect(x, y, width, height, COLOR_BORDER, 1f);

        // Score value with color
        String scoreText = score + "/100";
        ctx.drawText(scoreText, x + 12f, y + 34f, fontBold, 15f, badgeColor);

        // Score title
        ctx.drawText(label, x + 12f, y + 16f, fontRegular, 9f, COLOR_TEXT_MUTED);
    }

    private void drawStrengthsAndImprovements(ReportContext ctx, InterviewResult result) throws IOException {
        List<String> strengths = parseJsonList(result != null ? result.getStrengths() : null);
        List<String> improvements = parseJsonList(result != null ? result.getImprovements() : null);

        ctx.ensureSpace(120f);
        drawSectionTitle(ctx, "EXECUTIVE STRENGTHS & KEY RECOMMENDATIONS");

        float halfWidth = (ctx.contentWidth - 14f) / 2f;
        float startY = ctx.y;

        // Left Box: Strengths
        float leftHeight = calculateBulletBoxHeight(strengths, halfWidth - 20f);
        // Right Box: Improvements
        float rightHeight = calculateBulletBoxHeight(improvements, halfWidth - 20f);
        float boxHeight = Math.max(leftHeight, rightHeight);

        ctx.ensureSpace(boxHeight + 15f);
        startY = ctx.y;
        float boxY = startY - boxHeight;

        // Draw Left Box (Strengths)
        ctx.fillRect(MARGIN_X, boxY, halfWidth, boxHeight, COLOR_BG_CARD);
        ctx.drawRect(MARGIN_X, boxY, halfWidth, boxHeight, COLOR_BORDER, 1f);
        ctx.fillRect(MARGIN_X, startY - 24f, halfWidth, 24f, new Color(240, 253, 244));
        ctx.drawText("KEY STRENGTHS DEMONSTRATED", MARGIN_X + 10f, startY - 16f, fontBold, 9f, COLOR_SUCCESS);

        float itemY = startY - 38f;
        for (String item : strengths) {
            itemY = drawBulletText(ctx, MARGIN_X + 12f, itemY, halfWidth - 24f, "[+] " + sanitize(item), fontRegular, 8.5f, COLOR_TEXT_DARK);
            itemY -= 5f;
        }

        // Draw Right Box (Improvements)
        float rightX = MARGIN_X + halfWidth + 14f;
        ctx.fillRect(rightX, boxY, halfWidth, boxHeight, COLOR_BG_CARD);
        ctx.drawRect(rightX, boxY, halfWidth, boxHeight, COLOR_BORDER, 1f);
        ctx.fillRect(rightX, startY - 24f, halfWidth, 24f, new Color(254, 243, 199));
        ctx.drawText("AREAS FOR IMPROVEMENT", rightX + 10f, startY - 16f, fontBold, 9f, COLOR_AMBER);

        itemY = startY - 38f;
        for (String item : improvements) {
            itemY = drawBulletText(ctx, rightX + 12f, itemY, halfWidth - 24f, "[-] " + sanitize(item), fontRegular, 8.5f, COLOR_TEXT_DARK);
            itemY -= 5f;
        }

        ctx.y = boxY - 20f;
    }

    private float calculateBulletBoxHeight(List<String> items, float width) throws IOException {
        float h = 32f; // header + padding
        if (items.isEmpty()) {
            return h + 25f;
        }
        for (String item : items) {
            List<String> lines = wrapText("[+] " + sanitize(item), fontRegular, 8.5f, width);
            h += (lines.size() * 11f) + 5f;
        }
        return Math.max(h + 10f, 75f);
    }

    private void drawQuestionAnalyses(
            ReportContext ctx,
            List<InterviewQuestion> questions,
            List<InterviewAnswer> answers
    ) throws IOException {
        ctx.ensureSpace(60f);
        drawSectionTitle(ctx, "QUESTION-BY-QUESTION EVALUATION ANALYSIS");

        Map<Long, InterviewAnswer> answerMap = answers.stream()
                .filter(a -> a.getQuestion() != null && a.getQuestion().getId() != null)
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (k1, k2) -> k1));

        for (InterviewQuestion q : questions) {
            InterviewAnswer a = answerMap.get(q.getId());
            drawSingleQuestionAnalysis(ctx, q, a);
        }
    }

    private void drawSingleQuestionAnalysis(ReportContext ctx, InterviewQuestion q, InterviewAnswer a) throws IOException {
        ctx.ensureSpace(120f);

        // Header line for question
        float headerY = ctx.y - 18f;
        String qHeader = String.format("Question #%d  [%s | %s]",
                q.getQuestionNumber(),
                q.getCategory() != null ? q.getCategory() : "TECHNICAL",
                q.getDifficulty() != null ? q.getDifficulty() : "MEDIUM");

        ctx.fillRect(MARGIN_X, headerY, ctx.contentWidth, 18f, new Color(241, 245, 249));
        ctx.drawText(qHeader, MARGIN_X + 8f, headerY + 5f, fontBold, 9.5f, COLOR_PRIMARY);

        if (a != null && a.getScore() != null) {
            String scoreStr = "Score: " + a.getScore() + "/100";
            ctx.drawText(scoreStr, MARGIN_X + ctx.contentWidth - 85f, headerY + 5f, fontBold, 9.5f, COLOR_ACCENT);
        }

        ctx.y = headerY - 10f;

        // 1. Question Prompt
        ctx.drawText("Prompt:", MARGIN_X + 8f, ctx.y, fontBold, 9f, COLOR_TEXT_MUTED);
        ctx.y = drawWrappedBlock(ctx, MARGIN_X + 50f, ctx.y, ctx.contentWidth - 60f, sanitize(q.getQuestion()), fontBold, 9f, COLOR_TEXT_DARK);
        ctx.y -= 6f;

        // 2. Candidate's Answer
        ctx.ensureSpace(40f);
        ctx.drawText("Answer:", MARGIN_X + 8f, ctx.y, fontBold, 9f, COLOR_TEXT_MUTED);
        String ansText = (a != null && a.getAnswerText() != null && !a.getAnswerText().isBlank())
                ? a.getAnswerText()
                : "No answer submitted.";
        ctx.y = drawWrappedBlock(ctx, MARGIN_X + 50f, ctx.y, ctx.contentWidth - 60f, sanitize(ansText), fontRegular, 8.5f, COLOR_TEXT_DARK);
        ctx.y -= 6f;

        // 3. Sub-scores bar if available
        if (a != null && (a.getTechnicalAccuracy() != null || a.getCommunicationScore() != null)) {
            ctx.ensureSpace(20f);
            String scoresSummary = String.format("Technical Accuracy: %s/10   |   Communication: %s/10   |   Relevance: %s/10",
                    a.getTechnicalAccuracy() != null ? a.getTechnicalAccuracy() : "-",
                    a.getCommunicationScore() != null ? a.getCommunicationScore() : "-",
                    a.getRelevanceScore() != null ? a.getRelevanceScore() : "-");
            ctx.drawText(scoresSummary, MARGIN_X + 50f, ctx.y, fontRegular, 8f, COLOR_TEXT_MUTED);
            ctx.y -= 12f;
        }

        // 4. AI Feedback
        if (a != null && a.getFeedback() != null && !a.getFeedback().isBlank()) {
            ctx.ensureSpace(35f);
            ctx.drawText("Feedback:", MARGIN_X + 8f, ctx.y, fontBold, 9f, COLOR_AMBER);
            ctx.y = drawWrappedBlock(ctx, MARGIN_X + 60f, ctx.y, ctx.contentWidth - 70f, sanitize(a.getFeedback()), fontRegular, 8.5f, COLOR_TEXT_DARK);
            ctx.y -= 6f;
        }

        // 5. Better Model Answer
        if (a != null && a.getBetterAnswer() != null && !a.getBetterAnswer().isBlank()) {
            ctx.ensureSpace(35f);
            ctx.drawText("Exemplary:", MARGIN_X + 8f, ctx.y, fontBold, 9f, COLOR_SUCCESS);
            ctx.y = drawWrappedBlock(ctx, MARGIN_X + 60f, ctx.y, ctx.contentWidth - 70f, sanitize(a.getBetterAnswer()), fontOblique, 8.5f, new Color(47, 79, 79));
            ctx.y -= 6f;
        }

        // Divider
        ctx.drawLine(MARGIN_X, ctx.y - 4f, MARGIN_X + ctx.contentWidth, ctx.y - 4f, COLOR_BORDER, 0.8f);
        ctx.y -= 14f;
    }

    private void drawFinalFeedback(ReportContext ctx, InterviewResult result) throws IOException {
        if (result == null || result.getFinalFeedback() == null || result.getFinalFeedback().isBlank()) {
            return;
        }

        ctx.ensureSpace(100f);
        drawSectionTitle(ctx, "HIRING COMMITTEE FINAL ASSESSMENT");

        ctx.y = drawWrappedBlock(ctx, MARGIN_X, ctx.y, ctx.contentWidth, sanitize(result.getFinalFeedback()), fontRegular, 9f, COLOR_TEXT_DARK);
        ctx.y -= 20f;
    }

    private void drawSectionTitle(ReportContext ctx, String title) throws IOException {
        ctx.ensureSpace(28f);
        ctx.drawText(title, MARGIN_X, ctx.y, fontBold, 11f, COLOR_PRIMARY);
        ctx.drawLine(MARGIN_X, ctx.y - 4f, MARGIN_X + ctx.contentWidth, ctx.y - 4f, COLOR_ACCENT, 1.5f);
        ctx.y -= 16f;
    }

    private float drawWrappedBlock(
            ReportContext ctx,
            float x,
            float startY,
            float width,
            String text,
            PDType1Font font,
            float fontSize,
            Color color
    ) throws IOException {
        List<String> lines = wrapText(text, font, fontSize, width);
        float currentY = startY;
        float lineHeight = fontSize + 3f;

        for (String line : lines) {
            if (currentY - lineHeight < MARGIN_Y + 15f) {
                ctx.triggerPageBreak();
                currentY = ctx.y;
            }
            ctx.drawText(line, x, currentY, font, fontSize, color);
            currentY -= lineHeight;
        }

        return currentY;
    }

    private float drawBulletText(
            ReportContext ctx,
            float x,
            float startY,
            float width,
            String text,
            PDType1Font font,
            float fontSize,
            Color color
    ) throws IOException {
        List<String> lines = wrapText(text, font, fontSize, width);
        float currentY = startY;
        float lineHeight = fontSize + 2.5f;

        for (String line : lines) {
            ctx.drawText(line, x, currentY, font, fontSize, color);
            currentY -= lineHeight;
        }

        return currentY;
    }

    private List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return lines;
        }

        String[] paragraphs = text.split("\n");
        for (String paragraph : paragraphs) {
            if (paragraph.isBlank()) {
                lines.add("");
                continue;
            }

            String[] words = paragraph.split("\\s+");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if (word.isBlank()) {
                    continue;
                }
                String candidate = currentLine.length() == 0 ? word : currentLine + " " + word;
                float width = font.getStringWidth(candidate) / 1000f * fontSize;

                if (width <= maxWidth) {
                    currentLine = new StringBuilder(candidate);
                } else {
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                    }
                    currentLine = new StringBuilder(word);
                }
            }

            if (currentLine.length() > 0) {
                lines.add(currentLine.toString());
            }
        }

        return lines;
    }

    private List<String> parseJsonList(String jsonString) {
        if (jsonString == null || jsonString.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // Fallback for comma separated or single string
            return Arrays.stream(jsonString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toList());
        }
    }

    private String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("✓", "[+]")
                .replace("✔", "[+]")
                .replace("•", "*")
                .replace("–", "-")
                .replace("—", "-")
                .replace("“", "\"")
                .replace("”", "\"")
                .replace("‘", "'")
                .replace("’", "'")
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[^\\x00-\\x7F]", " ");
    }

    /**
     * Helper context class managing pages, content streams, and coordinate state.
     */
    private static class ReportContext {
        final PDDocument document;
        final PDRectangle pageSize;
        final float contentWidth;
        final List<PDPage> pages = new ArrayList<>();

        PDPage currentPage;
        PDPageContentStream stream;
        float y;

        ReportContext(PDDocument document, PDRectangle pageSize) throws IOException {
            this.document = document;
            this.pageSize = pageSize;
            this.contentWidth = pageSize.getWidth() - (MARGIN_X * 2);
            addNewPage();
        }

        void addNewPage() throws IOException {
            if (stream != null) {
                stream.close();
            }
            currentPage = new PDPage(pageSize);
            document.addPage(currentPage);
            pages.add(currentPage);
            stream = new PDPageContentStream(document, currentPage);
            y = pageSize.getHeight() - MARGIN_Y;
        }

        void ensureSpace(float requiredHeight) throws IOException {
            if (y - requiredHeight < MARGIN_Y + 15f) {
                triggerPageBreak();
            }
        }

        void triggerPageBreak() throws IOException {
            addNewPage();
            // Running top header line on subsequent pages
            drawLine(MARGIN_X, y, MARGIN_X + contentWidth, y, COLOR_BORDER, 0.8f);
            y -= 15f;
        }

        void drawText(String text, float x, float yCoord, PDType1Font font, float size, Color color) throws IOException {
            stream.beginText();
            stream.setFont(font, size);
            stream.setNonStrokingColor(color);
            stream.newLineAtOffset(x, yCoord);
            stream.showText(text);
            stream.endText();
        }

        void fillRect(float x, float yCoord, float width, float height, Color color) throws IOException {
            stream.setNonStrokingColor(color);
            stream.addRect(x, yCoord, width, height);
            stream.fill();
        }

        void drawRect(float x, float yCoord, float width, float height, Color color, float lineWidth) throws IOException {
            stream.setStrokingColor(color);
            stream.setLineWidth(lineWidth);
            stream.addRect(x, yCoord, width, height);
            stream.stroke();
        }

        void drawLine(float x1, float y1, float x2, float y2, Color color, float lineWidth) throws IOException {
            stream.setStrokingColor(color);
            stream.setLineWidth(lineWidth);
            stream.moveTo(x1, y1);
            stream.lineTo(x2, y2);
            stream.stroke();
        }

        void drawFooters(PDType1Font fontRegular, PDType1Font fontBold) throws IOException {
            int totalPages = pages.size();
            for (int i = 0; i < totalPages; i++) {
                PDPage page = pages.get(i);
                try (PDPageContentStream footerStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                    float footerY = MARGIN_Y - 15f;
                    // Footer line
                    footerStream.setStrokingColor(COLOR_BORDER);
                    footerStream.setLineWidth(0.8f);
                    footerStream.moveTo(MARGIN_X, footerY + 10f);
                    footerStream.lineTo(MARGIN_X + contentWidth, footerY + 10f);
                    footerStream.stroke();

                    // Left footer text
                    footerStream.beginText();
                    footerStream.setFont(fontRegular, 8f);
                    footerStream.setNonStrokingColor(COLOR_TEXT_MUTED);
                    footerStream.newLineAtOffset(MARGIN_X, footerY);
                    footerStream.showText("AI Mock Interview Platform | Confidential Career Assessment");
                    footerStream.endText();

                    // Right footer page number
                    String pageText = String.format("Page %d of %d", i + 1, totalPages);
                    footerStream.beginText();
                    footerStream.setFont(fontBold, 8f);
                    footerStream.setNonStrokingColor(COLOR_TEXT_MUTED);
                    footerStream.newLineAtOffset(MARGIN_X + contentWidth - 45f, footerY);
                    footerStream.showText(pageText);
                    footerStream.endText();
                }
            }
        }

        void close() throws IOException {
            if (stream != null) {
                stream.close();
            }
        }
    }
}
