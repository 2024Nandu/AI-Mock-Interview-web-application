import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient';
import { CheckCircle2, AlertTriangle, Target, Award, ArrowLeft, ChevronDown, ChevronUp, Sparkles, RefreshCw } from 'lucide-react';

const ReportPage = () => {
  const { sessionId } = useParams();
  const navigate = useNavigate();

  const [report, setReport] = useState(null);
  const [qaPairs, setQaPairs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [expandedQa, setExpandedQa] = useState({});

  useEffect(() => {
    const fetchReport = async () => {
      try {
        const response = await apiClient.get(`/api/interviews/${sessionId}/report`);
        setReport(response.data.report);
        setQaPairs(response.data.qaPairs);
        
        // Auto-expand the weakest question
        const weakestNum = response.data.report.weakestQuestionNumber;
        if (weakestNum) {
          setExpandedQa({ [weakestNum]: true });
        }
      } catch (err) {
        console.error(err);
        setError('Failed to load your evaluation report. It may still be generating in the background.');
      } finally {
        setLoading(false);
      }
    };
    fetchReport();
  }, [sessionId]);

  const toggleExpand = (num) => {
    setExpandedQa((prev) => ({ ...prev, [num]: !prev[num] }));
  };

  const getSuggestions = () => {
    if (!report?.preparationSuggestions) return [];
    try {
      return JSON.parse(report.preparationSuggestions);
    } catch (e) {
      return [report.preparationSuggestions];
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#09090b] text-white flex items-center justify-center">
        <div className="flex flex-col items-center gap-4 text-center">
          <RefreshCw className="w-10 h-10 text-indigo-500 animate-spin" />
          <h2 className="text-xl font-bold">Assembling Report...</h2>
          <p className="text-zinc-500 max-w-xs text-sm">Our AI is scoring your answers and writing customized model answers.</p>
        </div>
      </div>
    );
  }

  if (error || !report) {
    return (
      <div className="min-h-screen bg-[#09090b] text-white flex items-center justify-center px-6">
        <div className="bg-zinc-900/40 border border-zinc-800 p-8 rounded-3xl max-w-md text-center flex flex-col items-center gap-4">
          <AlertTriangle className="w-12 h-12 text-yellow-500" />
          <h2 className="text-xl font-bold">Report Generating</h2>
          <p className="text-zinc-400 text-sm">
            AI is completing final calculations. If you just finished, please wait a moment and reload the page.
          </p>
          <button 
            onClick={() => window.location.reload()}
            className="w-full py-3 bg-indigo-500 hover:bg-indigo-600 text-white rounded-xl font-semibold transition"
          >
            Reload Report
          </button>
          <button 
            onClick={() => navigate('/upload-resume')}
            className="text-sm text-zinc-500 hover:text-zinc-300 transition"
          >
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  const overallScoreVal = Number(report.overallScore).toFixed(1);

  return (
    <div className="min-h-screen bg-[#09090b] text-white relative px-6 py-12 font-sans">
      <div className="absolute top-[2%] left-[2%] w-[450px] h-[450px] rounded-full bg-indigo-500/5 blur-[100px] pointer-events-none" />
      <div className="absolute bottom-[2%] right-[2%] w-[500px] h-[500px] rounded-full bg-violet-600/5 blur-[120px] pointer-events-none" />

      <div className="max-w-5xl mx-auto relative z-10">
        
        {/* Navigation back and Print buttons */}
        <div className="flex items-center justify-between mb-8 print:hidden">
          <button
            onClick={() => navigate('/upload-resume')}
            className="inline-flex items-center gap-2 text-zinc-400 hover:text-white font-medium text-sm transition cursor-pointer"
          >
            <ArrowLeft className="w-4 h-4" /> Back to Dashboard
          </button>
          
          <button
            onClick={() => window.print()}
            className="inline-flex items-center gap-2 bg-zinc-900 hover:bg-zinc-800 border border-zinc-800 text-indigo-400 hover:text-indigo-300 px-4 py-2 rounded-xl text-xs font-semibold transition cursor-pointer"
          >
            Download / Print Report
          </button>
        </div>

        {/* Score and Hero */}
        <div className="bg-zinc-900/40 border border-zinc-800 p-8 rounded-3xl backdrop-blur-md shadow-xl flex flex-col md:flex-row items-center gap-8 mb-8 print:border-zinc-700">
          {/* Radial score display (Percentage formatted) */}
          <div className="relative w-36 h-36 rounded-full bg-gradient-to-tr from-indigo-500 to-violet-600 p-1 flex items-center justify-center shadow-lg shadow-indigo-500/20 shrink-0">
            <div className="w-full h-full rounded-full bg-zinc-950 flex flex-col items-center justify-center">
              <span className="text-4xl font-extrabold bg-gradient-to-r from-indigo-300 to-violet-400 bg-clip-text text-transparent">
                {(Number(report.overallScore) * 10).toFixed(0)}%
              </span>
              <span className="text-[10px] text-zinc-500 font-bold uppercase tracking-wider mt-1">Score: {overallScoreVal}/10</span>
            </div>
          </div>

          <div className="flex-1 text-center md:text-left space-y-3">
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full border border-indigo-500/30 bg-indigo-500/5 text-indigo-300 text-xs font-semibold print:border-zinc-700">
              <Award className="w-3.5 h-3.5" /> Performance Certificate
            </div>
            <h1 className="text-3xl font-extrabold tracking-tight">Technical Review Completed</h1>
            <p className="text-zinc-400 text-sm leading-relaxed max-w-2xl">
              {report.closingNote || 'Mock evaluation generated successfully. Review below to address strengths, weaknesses, and roadmap preparation.'}
            </p>
          </div>
        </div>

        {/* Strengths and Weaknesses Grid */}
        <div className="grid md:grid-cols-2 gap-6 mb-8">
          {/* Strengths */}
          <div className="border border-emerald-500/20 bg-emerald-500/5 p-6 rounded-2xl">
            <h3 className="font-bold text-lg text-emerald-400 flex items-center gap-2 mb-4">
              <CheckCircle2 className="w-5 h-5 shrink-0" /> Evaluated Strengths
            </h3>
            <ul className="space-y-3 text-sm text-zinc-300">
              {report.strength1 && <li className="flex items-start gap-2.5">&bull; {report.strength1}</li>}
              {report.strength2 && <li className="flex items-start gap-2.5">&bull; {report.strength2}</li>}
            </ul>
          </div>

          {/* Weaknesses */}
          <div className="border border-red-500/20 bg-red-500/5 p-6 rounded-2xl">
            <h3 className="font-bold text-lg text-red-400 flex items-center gap-2 mb-4">
              <AlertTriangle className="w-5 h-5 shrink-0" /> Target Weaknesses
            </h3>
            <ul className="space-y-3 text-sm text-zinc-300">
              {report.weakness1 && <li className="flex items-start gap-2.5">&bull; {report.weakness1}</li>}
              {report.weakness2 && <li className="flex items-start gap-2.5">&bull; {report.weakness2}</li>}
            </ul>
          </div>
        </div>

        {/* Preparation Suggestions Timeline */}
        <div className="bg-zinc-900/40 border border-zinc-800 p-8 rounded-3xl backdrop-blur-md shadow-xl mb-8">
          <h2 className="text-xl font-bold mb-6 flex items-center gap-2 text-indigo-400">
            <Target className="w-5 h-5" /> Personalized Preparation Roadmap
          </h2>

          <div className="space-y-6">
            {getSuggestions().map((step, i) => (
              <div key={i} className="flex gap-4 relative">
                {i < getSuggestions().length - 1 && (
                  <div className="absolute left-6 top-10 bottom-[-20px] w-[2px] bg-zinc-800" />
                )}
                <div className="w-12 h-12 rounded-xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-300 font-extrabold flex items-center justify-center shrink-0">
                  {i + 1}
                </div>
                <div className="pt-2">
                  <p className="text-zinc-300 text-sm md:text-base leading-relaxed">{step}</p>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Weakest Question Spotlight */}
        {report.weakestQuestionNumber > 0 && (
          <div className="border border-indigo-500/30 bg-indigo-500/5 p-8 rounded-3xl shadow-xl mb-8">
            <h3 className="font-bold text-lg text-indigo-400 flex items-center gap-2 mb-3">
              <Sparkles className="w-5 h-5 shrink-0 animate-pulse" /> Weakest Response Spotlight (Q{report.weakestQuestionNumber})
            </h3>
            <p className="text-xs text-indigo-300/80 mb-4 uppercase tracking-wider font-semibold">
              Improvement model answer constructed by AI:
            </p>
            <div className="p-5 bg-zinc-950/80 border border-zinc-800 rounded-2xl text-zinc-300 text-sm leading-relaxed italic">
              "{report.modelAnswer}"
            </div>
          </div>
        )}

        {/* Collapsible Questionnaire Section */}
        <div className="bg-zinc-900/40 border border-zinc-800 p-8 rounded-3xl backdrop-blur-md shadow-xl">
          <h2 className="text-xl font-bold mb-6">Detailed Response Breakdown</h2>

          <div className="space-y-4">
            {qaPairs.map((qa) => {
              const isOpen = !!expandedQa[qa.questionNumber];
              const scoreVal = qa.score ? Number(qa.score).toFixed(1) : 'N/A';
              
              return (
                <div key={qa.id} className="border border-zinc-800 rounded-2xl overflow-hidden bg-zinc-950/20">
                  <button
                    onClick={() => toggleExpand(qa.questionNumber)}
                    className="w-full px-6 py-4 flex items-center justify-between text-left hover:bg-zinc-900/20 transition cursor-pointer"
                  >
                    <div className="flex items-center gap-3 pr-4">
                      <span className="w-7 h-7 rounded-lg bg-zinc-900 border border-zinc-800 flex items-center justify-center text-xs font-bold text-zinc-400">
                        {qa.questionNumber}
                      </span>
                      <p className="font-bold text-sm md:text-base text-zinc-200 line-clamp-1">
                        {qa.questionText}
                      </p>
                    </div>

                    <div className="flex items-center gap-4 shrink-0">
                      <span className="px-2.5 py-1 text-xs font-semibold rounded-lg border border-indigo-500/20 bg-indigo-500/10 text-indigo-300">
                        Score: {scoreVal}/10
                      </span>
                      {isOpen ? <ChevronUp className="w-5 h-5 text-zinc-500" /> : <ChevronDown className="w-5 h-5 text-zinc-500" />}
                    </div>
                  </button>

                  {isOpen && (
                    <div className="px-6 pb-6 pt-2 border-t border-zinc-900 space-y-4 text-sm">
                      <div>
                        <h4 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-1.5">Question</h4>
                        <p className="text-zinc-200 leading-relaxed">{qa.questionText}</p>
                      </div>

                      <div>
                        <h4 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-1.5">Your Response</h4>
                        <p className="text-zinc-300 leading-relaxed italic bg-zinc-950/55 p-4 rounded-xl border border-zinc-900">
                          "{qa.answerText || '(No response captured)'}"
                        </p>
                      </div>

                      <div>
                        <h4 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-1.5">Evaluation & Justification</h4>
                        <p className="text-zinc-400 leading-relaxed">{qa.scoreJustification || 'No justification provided.'}</p>
                      </div>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>

      </div>
    </div>
  );
};

export default ReportPage;
