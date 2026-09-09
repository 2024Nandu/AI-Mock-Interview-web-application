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
  AlertCircle,
  User,
  Mail,
  Phone,
  MapPin,
  Briefcase,
  GraduationCap,
  Code,
  Loader2,
  BarChart3
} from 'lucide-react';
import { resumeService } from '../../services/resumeService';
import type { ResumeAnalysisResponse } from '../../services/resumeService';

const ResumeAnalysisPage = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [analysis, setAnalysis] = useState<ResumeAnalysisResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!id) return;
    fetchAnalysis();
  }, [id]);

  const fetchAnalysis = async () => {
    if (!id) return;
    setLoading(true);
    setError('');
    
    try {
      const data = await resumeService.getResumeAnalysis(parseInt(id));
      console.log('✅ Analysis data received:', data);
      setAnalysis(data);
    } catch (err: any) {
      console.error('❌ Error fetching analysis:', err);
      if (err.response?.status === 404) {
        setError('Resume analysis not found. Please analyze the resume first.');
      } else {
        setError(err.response?.data?.message || 'Failed to load analysis');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleAnalyze = async () => {
    if (!id) return;
    setAnalyzing(true);
    setError('');
    
    try {
      console.log('🔍 Triggering analysis for resume:', id);
      await resumeService.analyzeResume(parseInt(id));
      
      // Wait and fetch the analysis
      let attempts = 0;
      const maxAttempts = 10;
      
      while (attempts < maxAttempts) {
        await new Promise(resolve => setTimeout(resolve, 1500));
        attempts++;
        console.log(`⏳ Checking analysis (attempt ${attempts}/${maxAttempts})...`);
        
        try {
          const data = await resumeService.getResumeAnalysis(parseInt(id));
          if (data && data.overallScore !== undefined) {
            console.log('✅ Analysis ready!');
            setAnalysis(data);
            setError('');
            return;
          }
        } catch (err: any) {
          if (err.response?.status !== 404) {
            throw err;
          }
          console.log('⏳ Analysis not ready yet...');
        }
      }
      
      setError('Analysis is taking longer than expected. Please try again later.');
    } catch (err: any) {
      console.error('❌ Error analyzing resume:', err);
      setError(err.response?.data?.message || 'Failed to analyze resume. Please try again.');
    } finally {
      setAnalyzing(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="text-center">
          <Loader2 className="w-12 h-12 text-green-600 animate-spin mx-auto" />
          <p className="text-gray-500 mt-4">Loading analysis...</p>
        </div>
      </div>
    );
  }

  // Show analyze button if analysis not found
  if (error && error.includes('not found')) {
    return (
      <div className="text-center py-16 max-w-md mx-auto">
        <BarChart3 className="w-16 h-16 text-gray-400 mx-auto mb-4" />
        <h3 className="text-xl font-semibold text-gray-900 mb-2">Resume Not Analyzed</h3>
        <p className="text-gray-500 mb-6">
          This resume has not been analyzed yet. Click the button below to start the analysis.
        </p>
        <button
          onClick={handleAnalyze}
          disabled={analyzing}
          className="px-6 py-3 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-colors disabled:opacity-50 flex items-center gap-2 mx-auto"
        >
          {analyzing ? (
            <>
              <Loader2 className="w-5 h-5 animate-spin" />
              Analyzing...
            </>
          ) : (
            <>
              <BarChart3 className="w-5 h-5" />
              Analyze Resume
            </>
          )}
        </button>
        <button
          onClick={() => navigate('/dashboard/resumes')}
          className="block mt-3 text-gray-500 hover:text-gray-700 transition-colors"
        >
          Back to Resumes
        </button>
      </div>
    );
  }

  if (error || !analysis) {
    return (
      <div className="text-center py-16">
        <AlertCircle className="w-16 h-16 text-red-400 mx-auto mb-4" />
        <h3 className="text-xl font-semibold text-gray-900 mb-2">Error Loading Analysis</h3>
        <p className="text-gray-500">{error || 'Failed to load analysis'}</p>
        <div className="mt-4 flex flex-col sm:flex-row gap-3 justify-center">
          <button
            onClick={handleAnalyze}
            disabled={analyzing}
            className="px-6 py-2 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-colors disabled:opacity-50 flex items-center gap-2 mx-auto"
          >
            {analyzing ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                Retrying...
              </>
            ) : (
              <>
                <BarChart3 className="w-4 h-4" />
                Try Again
              </>
            )}
          </button>
          <button
            onClick={() => navigate('/dashboard/resumes')}
            className="px-6 py-2 bg-gray-100 text-gray-700 rounded-xl hover:bg-gray-200 transition-colors"
          >
            Back to Resumes
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate('/dashboard/resumes')}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ArrowLeft className="w-6 h-6" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Resume Analysis</h1>
            <p className="text-gray-500">Detailed ATS score and resume insights</p>
          </div>
        </div>
        <button
          onClick={handleAnalyze}
          disabled={analyzing}
          className="px-4 py-2 text-sm bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition-colors disabled:opacity-50 flex items-center gap-2"
        >
          {analyzing ? (
            <>
              <Loader2 className="w-4 h-4 animate-spin" />
              Re-analyzing...
            </>
          ) : (
            <>
              <BarChart3 className="w-4 h-4" />
              Re-analyze
            </>
          )}
        </button>
      </div>

      {/* Overall Score Card */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8"
      >
        <div className="flex items-center gap-6">
          <div className="relative">
            <div className="w-32 h-32 rounded-full bg-green-100 flex items-center justify-center">
              <span className="text-4xl font-bold text-green-700">
                {analysis.overallScore || 0}%
              </span>
            </div>
            <div className="absolute -bottom-2 -right-2 bg-green-600 text-white p-2 rounded-full">
              <Award className="w-6 h-6" />
            </div>
          </div>
          <div>
            <h2 className="text-2xl font-bold text-gray-900">ATS Score</h2>
            <p className="text-gray-600 mt-1">
              {(analysis.overallScore || 0) >= 80 
                ? 'Excellent! Your resume is well-optimized for ATS systems.'
                : (analysis.overallScore || 0) >= 60
                ? 'Good score! There\'s room for improvement in some areas.'
                : 'Your resume needs optimization to pass ATS screening.'}
            </p>
          </div>
        </div>
      </motion.div>

      {/* Section Scores */}
      {analysis.sectionScores && analysis.sectionScores.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6"
        >
          <h3 className="font-semibold text-gray-900 mb-4">Section Scores</h3>
          <div className="space-y-4">
            {analysis.sectionScores.map((section, index) => (
              <div key={index}>
                <div className="flex justify-between text-sm mb-1">
                  <span className="text-gray-700">{section.sectionName}</span>
                  <span className="font-medium text-gray-900">
                    {section.score}/{section.maxScore}
                  </span>
                </div>
                <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
                  <div
                    className={`h-full rounded-full ${
                      section.score / section.maxScore >= 0.8
                        ? 'bg-green-600'
                        : section.score / section.maxScore >= 0.6
                        ? 'bg-yellow-500'
                        : 'bg-red-500'
                    }`}
                    style={{ width: `${(section.score / section.maxScore) * 100}%` }}
                  />
                </div>
                <p className="text-sm text-gray-500 mt-1">{section.feedback}</p>
              </div>
            ))}
          </div>
        </motion.div>
      )}

      {/* Missing Keywords */}
      {analysis.missingKeywords && analysis.missingKeywords.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.15 }}
          className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6"
        >
          <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <AlertCircle className="w-5 h-5 text-yellow-600" />
            Missing Keywords
          </h3>
          <div className="flex flex-wrap gap-2">
            {analysis.missingKeywords.map((keyword, index) => (
              <span key={index} className="px-3 py-1 bg-yellow-50 text-yellow-700 rounded-full text-sm">
                {keyword}
              </span>
            ))}
          </div>
          <p className="text-sm text-gray-500 mt-2">
            Add these keywords to improve your ATS score.
          </p>
        </motion.div>
      )}

      {/* Strengths & Weaknesses */}
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
          <ul className="space-y-2">
            {analysis.strengths?.map((strength, index) => (
              <li key={index} className="flex items-start gap-2">
                <CheckCircle className="w-5 h-5 text-green-600 flex-shrink-0 mt-0.5" />
                <span className="text-gray-700">{strength}</span>
              </li>
            ))}
          </ul>
        </motion.div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6"
        >
          <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <TrendingDown className="w-5 h-5 text-red-600" />
            Areas for Improvement
          </h3>
          <ul className="space-y-2">
            {analysis.weaknesses?.map((weakness, index) => (
              <li key={index} className="flex items-start gap-2">
                <XCircle className="w-5 h-5 text-red-600 flex-shrink-0 mt-0.5" />
                <span className="text-gray-700">{weakness}</span>
              </li>
            ))}
          </ul>
        </motion.div>
      </div>

      {/* Recommendations */}
      {analysis.improvements && analysis.improvements.length > 0 && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.4 }}
          className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6"
        >
          <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <AlertCircle className="w-5 h-5 text-blue-600" />
            Recommendations
          </h3>
          <ul className="space-y-2">
            {analysis.improvements.map((improvement, index) => (
              <li key={index} className="flex items-start gap-2">
                <div className="w-5 h-5 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0 mt-0.5">
                  <span className="text-xs font-bold text-blue-600">{index + 1}</span>
                </div>
                <span className="text-gray-700">{improvement}</span>
              </li>
            ))}
          </ul>
        </motion.div>
      )}
    </div>
  );
};

export default ResumeAnalysisPage;