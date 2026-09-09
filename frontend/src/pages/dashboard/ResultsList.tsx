import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import {
  BarChart3,
  TrendingUp,
  TrendingDown,
  CheckCircle,
  Clock,
  AlertCircle,
  Download,
  Eye,
  Calendar,
  Brain,
  FileText
} from 'lucide-react';
import { interviewService, type Interview, type InterviewResult } from '../../services/interviewService';

interface InterviewWithResult extends Interview {
  result?: InterviewResult;
}

const ResultsList = () => {
  const [interviews, setInterviews] = useState<InterviewWithResult[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchResults();
  }, []);

  const fetchResults = async () => {
    try {
      const data = await interviewService.getAllInterviews();
      // Only show completed interviews
      const completed = data.filter(i => i.status === 'COMPLETED');
      
      // Fetch results for each completed interview
      const withResults = await Promise.all(
        completed.map(async (interview) => {
          try {
            const result = await interviewService.getInterviewResult(interview.id);
            return { ...interview, result };
          } catch {
            return { ...interview };
          }
        })
      );
      
      setInterviews(withResults);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load results');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadReport = async (interviewId: number) => {
    try {
      const blob = await interviewService.downloadReport(interviewId);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `interview-report-${interviewId}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      window.URL.revokeObjectURL(url);
    } catch (err) {
      console.error('Failed to download report:', err);
    }
  };

  const getScoreColor = (score: number) => {
    if (score >= 80) return 'text-green-600';
    if (score >= 60) return 'text-yellow-600';
    return 'text-red-600';
  };

  const getScoreBgColor = (score: number) => {
    if (score >= 80) return 'bg-green-100';
    if (score >= 60) return 'bg-yellow-100';
    return 'bg-red-100';
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="text-center">
          <div className="w-12 h-12 border-4 border-green-600 border-t-transparent rounded-full animate-spin mx-auto" />
          <p className="text-gray-500 mt-4">Loading results...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-16">
        <AlertCircle className="w-16 h-16 text-red-400 mx-auto mb-4" />
        <h3 className="text-xl font-semibold text-gray-900 mb-2">Error Loading Results</h3>
        <p className="text-gray-500">{error}</p>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Interview Results</h1>
          <p className="text-gray-600 mt-2">
            View your performance across all completed interviews.
          </p>
        </div>
        <Link
          to="/dashboard/interview/new"
          className="px-6 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-colors flex items-center gap-2"
        >
          <Brain className="w-5 h-5" />
          New Interview
        </Link>
      </div>

      {interviews.length === 0 ? (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-12 text-center">
          <BarChart3 className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h3 className="text-xl font-semibold text-gray-900 mb-2">No Results Yet</h3>
          <p className="text-gray-500 mb-6">
            Complete an interview to see your results here.
          </p>
          <Link
            to="/dashboard/interview/new"
            className="inline-flex items-center gap-2 px-6 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-colors"
          >
            <Brain className="w-5 h-5" />
            Start Interview
          </Link>
        </div>
      ) : (
        <div className="space-y-6">
          {interviews.map((interview, index) => (
            <motion.div
              key={interview.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: index * 0.05 }}
              className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 bg-purple-50 rounded-xl flex items-center justify-center">
                    <BarChart3 className="w-6 h-6 text-purple-600" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900">
                      {interview.interviewType} Interview
                    </h3>
                    <div className="flex items-center gap-3 mt-1">
                      <span className="text-sm text-gray-500 flex items-center gap-1">
                        <Calendar className="w-3 h-3" />
                        {new Date(interview.completedAt || interview.startedAt).toLocaleDateString()}
                      </span>
                      <span className="text-sm text-gray-500">
                        {interview.totalQuestions} questions
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-4 w-full md:w-auto">
                  {interview.result ? (
                    <>
                      <div className={`px-4 py-2 rounded-xl text-center ${getScoreBgColor(interview.result.overallScore)}`}>
                        <p className={`text-2xl font-bold ${getScoreColor(interview.result.overallScore)}`}>
                          {interview.result.overallScore}%
                        </p>
                        <p className="text-xs text-gray-500">Overall Score</p>
                      </div>
                      <div className="flex gap-2">
                        <Link
                          to={`/dashboard/result/${interview.id}`}
                          className="px-4 py-2 text-sm bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center gap-2"
                        >
                          <Eye className="w-4 h-4" />
                          Details
                        </Link>
                        <button
                          onClick={() => handleDownloadReport(interview.id)}
                          className="px-4 py-2 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors flex items-center gap-2"
                        >
                          <Download className="w-4 h-4" />
                          Report
                        </button>
                      </div>
                    </>
                  ) : (
                    <div className="flex items-center gap-2 text-yellow-600">
                      <Clock className="w-5 h-5" />
                      <span className="text-sm">Processing result...</span>
                    </div>
                  )}
                </div>
              </div>

              {/* Score Breakdown - Only show if result exists */}
              {interview.result && interview.result.scores && (
                <div className="mt-4 pt-4 border-t border-gray-100">
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
                    <div className="text-center">
                      <p className="text-xs text-gray-500">Technical</p>
                      <p className="text-sm font-semibold text-gray-900">
                        {interview.result.scores.technical || 0}%
                      </p>
                    </div>
                    <div className="text-center">
                      <p className="text-xs text-gray-500">Communication</p>
                      <p className="text-sm font-semibold text-gray-900">
                        {interview.result.scores.communication || 0}%
                      </p>
                    </div>
                    <div className="text-center">
                      <p className="text-xs text-gray-500">Problem Solving</p>
                      <p className="text-sm font-semibold text-gray-900">
                        {interview.result.scores.problemSolving || 0}%
                      </p>
                    </div>
                    <div className="text-center">
                      <p className="text-xs text-gray-500">Experience</p>
                      <p className="text-sm font-semibold text-gray-900">
                        {interview.result.scores.experience || 0}%
                      </p>
                    </div>
                  </div>
                </div>
              )}
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ResultsList;