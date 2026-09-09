import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  ArrowLeft,
  Award,
  TrendingUp,
  TrendingDown,
  CheckCircle,
  XCircle,
  Download,
  Brain,
  BarChart3,
  AlertCircle,
  Loader2,
  FileText
} from 'lucide-react';
import { interviewService, type InterviewResult } from '../../services/interviewService';

const ResultDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [result, setResult] = useState<InterviewResult | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) return;
    fetchResult();
  }, [id]);

  const fetchResult = async () => {
    if (!id) return;
    try {
      const data = await interviewService.getInterviewResult(parseInt(id));
      console.log('📊 Result data:', data);
      setResult(data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to load result');
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadReport = async () => {
    if (!id) return;
    try {
      const blob = await interviewService.downloadReport(parseInt(id));
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `interview-report-${id}.pdf`;
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
          <Loader2 className="w-12 h-12 text-green-600 animate-spin mx-auto" />
          <p className="text-gray-500 mt-4">Loading result...</p>
        </div>
      </div>
    );
  }

  if (error || !result) {
    return (
      <div className="text-center py-16">
        <AlertCircle className="w-16 h-16 text-red-400 mx-auto mb-4" />
        <h3 className="text-xl font-semibold text-gray-900 mb-2">Result Not Found</h3>
        <p className="text-gray-500">{error || 'No result available for this interview.'}</p>
        <button
          onClick={() => navigate('/dashboard/results')}
          className="mt-4 px-6 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-colors"
        >
          Back to Results
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate('/dashboard/results')}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ArrowLeft className="w-6 h-6" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Interview Results</h1>
            <p className="text-gray-500">Detailed performance breakdown</p>
          </div>
        </div>
        <button
          onClick={handleDownloadReport}
          className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors flex items-center gap-2"
        >
          <Download className="w-5 h-5" />
          Download Report
        </button>
      </div>

      {/* Overall Score */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8"
      >
        <div className="flex items-center gap-8">
          <div className="relative">
            <div className={`w-32 h-32 rounded-full ${getScoreBgColor(result.overallScore)} flex items-center justify-center`}>
              <span className={`text-4xl font-bold ${getScoreColor(result.overallScore)}`}>
                {result.overallScore}%
              </span>
            </div>
            <div className="absolute -bottom-2 -right-2 bg-purple-600 text-white p-2 rounded-full">
              <Award className="w-6 h-6" />
            </div>
          </div>
          <div>
            <h2 className="text-2xl font-bold text-gray-900">Overall Performance</h2>
            <p className="text-gray-600 mt-1">
              {result.overallScore >= 80 
                ? 'Excellent performance! You demonstrated strong skills across all areas.'
                : result.overallScore >= 60
                ? 'Good performance! There\'s room for improvement in some areas.'
                : 'Keep practicing! Focus on the areas mentioned below to improve.'}
            </p>
          </div>
        </div>
      </motion.div>

      {/* Score Breakdown - Only if scores exist */}
      {result.scores && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6"
        >
          <h3 className="font-semibold text-gray-900 mb-4">Score Breakdown</h3>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {Object.entries(result.scores).map(([key, value]) => (
              <div key={key} className="text-center p-4 bg-gray-50 rounded-xl">
                <p className="text-sm text-gray-500 capitalize">{key.replace(/([A-Z])/g, ' $1').trim()}</p>
                <p className={`text-2xl font-bold ${getScoreColor(value)}`}>{value}%</p>
              </div>
            ))}
          </div>
        </motion.div>
      )}

      {/* Strengths & Improvements */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.2 }}
          className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6"
        >
          <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-green-600" />
            Strengths
          </h3>
          {result.strengths && result.strengths.length > 0 ? (
            <ul className="space-y-2">
              {result.strengths.map((strength, index) => (
                <li key={index} className="flex items-start gap-2">
                  <CheckCircle className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
                  <span className="text-gray-700">{strength}</span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-gray-500 text-sm">No strengths listed</p>
          )}
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6"
        >
          <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <TrendingDown className="w-5 h-5 text-orange-600" />
            Areas for Improvement
          </h3>
          {result.improvements && result.improvements.length > 0 ? (
            <ul className="space-y-2">
              {result.improvements.map((improvement, index) => (
                <li key={index} className="flex items-start gap-2">
                  <XCircle className="w-5 h-5 text-orange-600 flex-shrink-0 mt-0.5" />
                  <span className="text-gray-700">{improvement}</span>
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-gray-500 text-sm">No improvements listed</p>
          )}
        </motion.div>
      </div>

      {/* Summary */}
      {result.summary && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6"
        >
          <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <Brain className="w-5 h-5 text-purple-600" />
            Executive Summary
          </h3>
          <p className="text-gray-700 leading-relaxed">{result.summary}</p>
        </motion.div>
      )}

      {/* Question Breakdown */}
      {result.questionsAndAnswers && result.questionsAndAnswers.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6"
        >
          <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <BarChart3 className="w-5 h-5 text-blue-600" />
            Question Breakdown
          </h3>
          <div className="space-y-4">
            {result.questionsAndAnswers.map((qa, index) => (
              <div key={index} className="border-b border-gray-100 last:border-0 pb-4 last:pb-0">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-sm font-medium text-gray-500">Q{index + 1}</span>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
                        qa.score >= 80 ? 'bg-green-100 text-green-700' :
                        qa.score >= 60 ? 'bg-yellow-100 text-yellow-700' :
                        'bg-red-100 text-red-700'
                      }`}>
                        {qa.score}%
                      </span>
                    </div>
                    <p className="text-sm font-medium text-gray-900">{qa.question}</p>
                    <p className="text-sm text-gray-600 mt-1">{qa.answer}</p>
                    <p className="text-sm text-gray-500 mt-1">{qa.feedback}</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </motion.div>
      )}

      {/* Action Buttons */}
      <div className="flex flex-wrap gap-4">
        <button
          onClick={() => navigate('/dashboard/interview/new')}
          className="px-6 py-3 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-colors flex items-center gap-2"
        >
          <Brain className="w-5 h-5" />
          Take Another Interview
        </button>
        <button
          onClick={() => navigate('/dashboard/resumes')}
          className="px-6 py-3 bg-gray-100 text-gray-700 rounded-xl hover:bg-gray-200 transition-colors flex items-center gap-2"
        >
          <FileText className="w-5 h-5" />
          View Resumes
        </button>
      </div>
    </div>
  );
};

export default ResultDetail;