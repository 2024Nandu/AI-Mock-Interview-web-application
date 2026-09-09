package com.rockranger.analyzer.interview.report;

import com.rockranger.analyzer.authentication.entity.User;
import com.rockranger.analyzer.interview.entity.Interview;
import com.rockranger.analyzer.interview.entity.InterviewAnswer;
import com.rockranger.analyzer.interview.entity.InterviewQuestion;
import com.rockranger.analyzer.interview.entity.InterviewResult;

import java.util.List;

public interface InterviewReportService {

    /**
     * Generates a professional PDF report summarizing interview performance, scorecards,
     * strengths, improvements, and question-by-question analysis.
     *
     * @param interview The completed interview entity
     * @param result    The final interview result scorecard
     * @param questions The list of questions in the interview
     * @param answers   The candidate's submitted answers and evaluations
     * @param user      The authenticated candidate
     * @return PDF binary byte array
     */
    byte[] generatePdfReport(
            Interview interview,
            InterviewResult result,
            List<InterviewQuestion> questions,
            List<InterviewAnswer> answers,
            User user
    );
}
