import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient';
import { Upload, Sparkles, FileText, CheckCircle, ArrowRight, RefreshCw, AlertCircle } from 'lucide-react';

const ResumeUploadPage = () => {
  const [file, setFile] = useState(null);
  const [parsedData, setParsedData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [fetchingLatest, setFetchingLatest] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    // Check if user already has a parsed resume
    const fetchLatestResume = async () => {
      try {
        const response = await apiClient.get('/api/resumes/latest');
        if (response.data?.parsedResume) {
          try {
            setParsedData(JSON.parse(response.data.parsedResume));
          } catch (e) {
            console.error('Failed to parse cached JSON', e);
          }
        }
      } catch (err) {
        console.error('Failed to fetch latest resume', err);
      } finally {
        setFetchingLatest(false);
      }
    };
    fetchLatestResume();
  }, []);

  const handleFileChange = (e) => {
    const selected = e.target.files[0];
    if (selected) {
      setFile(selected);
      setError('');
    }
  };

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file) {
      setError('Please select a PDF or DOCX file first.');
      return;
    }
    setError('');
    setLoading(true);

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await apiClient.post('/api/resumes/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      });
      
      const parsedResume = response.data?.parsedResume;
      if (parsedResume) {
        setParsedData(JSON.parse(parsedResume));
      }
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.error || 'Failed to parse resume. Please ensure it is a text-based PDF/DOCX and try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleProceed = () => {
    navigate('/select-role');
  };

  if (fetchingLatest) {
    return (
      <div className="min-h-screen bg-[#09090b] text-white flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <RefreshCw className="w-8 h-8 text-indigo-500 animate-spin" />
          <p className="text-zinc-500">Checking your profile...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#09090b] text-white relative px-6 py-12">
      <div className="absolute top-[5%] left-[5%] w-[400px] h-[400px] rounded-full bg-indigo-500/5 blur-[100px] pointer-events-none" />
      <div className="absolute bottom-[5%] right-[5%] w-[450px] h-[450px] rounded-full bg-violet-600/5 blur-[110px] pointer-events-none" />

      <div className="max-w-4xl mx-auto relative z-10">
        
        {/* Header */}
        <div className="mb-10 text-center">
          <h1 className="text-4xl font-extrabold tracking-tight bg-gradient-to-r from-white to-zinc-400 bg-clip-text text-transparent">
            Resume Customization
          </h1>
          <p className="text-zinc-500 mt-2">
            Upload your professional resume so our AI agent can tailor interview questions to your background.
          </p>
        </div>

        {error && (
          <div className="mb-8 p-4 rounded-xl border border-red-500/30 bg-red-500/5 text-red-400 text-sm flex items-start gap-3">
            <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        <div className="grid lg:grid-cols-12 gap-8 items-start">
          {/* Left Column: Upload Box */}
          <div className={`${parsedData ? 'lg:col-span-5' : 'lg:col-span-12'} transition-all duration-300`}>
            <div className="bg-zinc-900/40 border border-zinc-800 p-8 rounded-3xl backdrop-blur-md shadow-xl text-center">
              <div className="flex flex-col items-center justify-center border-2 border-dashed border-zinc-800 hover:border-indigo-500/50 rounded-2xl p-8 cursor-pointer relative transition duration-300 mb-6 bg-zinc-950/20">
                <input
                  type="file"
                  accept=".pdf,.docx,.txt"
                  onChange={handleFileChange}
                  className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                />
                <div className="w-12 h-12 rounded-xl bg-indigo-500/10 flex items-center justify-center text-indigo-400 mb-4">
                  <Upload className="w-6 h-6" />
                </div>
                <p className="font-semibold text-zinc-300">
                  {file ? file.name : 'Select file'}
                </p>
                <p className="text-xs text-zinc-600 mt-2">
                  Supports PDF, DOCX or TXT (Max 10MB)
                </p>
              </div>

              <div className="space-y-4">
                <button
                  onClick={handleUpload}
                  disabled={loading || !file}
                  className="w-full py-3 bg-indigo-500 hover:bg-indigo-600 disabled:bg-zinc-800 disabled:text-zinc-600 text-white rounded-xl font-semibold transition flex items-center justify-center gap-2 cursor-pointer shadow-lg shadow-indigo-500/10"
                >
                  {loading ? (
                    <span className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  ) : (
                    <>
                      <Sparkles className="w-4 h-4" />
                      Parse Resume with AI
                    </>
                  )}
                </button>

                {parsedData && (
                  <button
                    onClick={handleProceed}
                    className="w-full py-3 bg-gradient-to-r from-indigo-500 to-violet-600 hover:from-indigo-600 hover:to-violet-700 text-white rounded-xl font-semibold transition-all duration-300 shadow-md shadow-indigo-500/15 flex items-center justify-center gap-2 group cursor-pointer"
                  >
                    Proceed to Role Selection
                    <ArrowRight className="w-4 h-4 transition group-hover:translate-x-0.5" />
                  </button>
                )}
              </div>
            </div>
          </div>

          {/* Right Column: Parsed Preview */}
          {parsedData && (
            <div className="lg:col-span-7 bg-zinc-900/40 border border-zinc-800 p-8 rounded-3xl backdrop-blur-md shadow-xl max-h-[650px] overflow-y-auto">
              <div className="flex items-center justify-between gap-2 mb-6 border-b border-zinc-800/80 pb-4">
                <div className="flex items-center gap-2 text-emerald-400 font-bold">
                  <CheckCircle className="w-5 h-5" /> Structured Resume Profile
                </div>
                {parsedData.atsScore !== undefined && (
                  <span className="px-3 py-1 text-xs font-semibold rounded-lg border border-indigo-500/30 bg-indigo-500/10 text-indigo-300 animate-pulse">
                    ATS Score: {parsedData.atsScore}%
                  </span>
                )}
              </div>

              {/* ATS SCORE PANEL */}
              {parsedData.atsScore !== undefined && (
                <div className="mb-6 p-5 bg-gradient-to-r from-indigo-950/20 to-violet-950/20 border border-indigo-500/20 rounded-2xl flex flex-col md:flex-row items-center gap-6">
                  {/* Radial Gauge representation */}
                  <div className="relative w-20 h-20 rounded-full bg-gradient-to-tr from-indigo-500 to-violet-600 p-0.5 shrink-0 shadow-lg shadow-indigo-500/10">
                    <div className="w-full h-full rounded-full bg-zinc-950 flex flex-col items-center justify-center">
                      <span className="text-xl font-bold text-zinc-100">{parsedData.atsScore}%</span>
                    </div>
                  </div>
                  <div>
                    <h4 className="font-bold text-sm text-zinc-200 flex items-center gap-1.5">
                      <Sparkles className="w-4 h-4 text-indigo-400" /> AI ATS Optimization Audit
                    </h4>
                    <p className="text-xs text-zinc-400 mt-1 leading-relaxed">
                      Your resume has been parsed and scored. Review the recommendations below to increase formatting match and structural relevancy.
                    </p>
                  </div>
                </div>
              )}

              {/* ACTIONABLE IMPROVEMENTS */}
              {parsedData.improvements && parsedData.improvements.length > 0 && (
                <div className="mb-6 p-5 border border-amber-500/10 bg-amber-500/5 rounded-2xl">
                  <h4 className="font-bold text-sm text-amber-400 flex items-center gap-1.5 mb-3">
                    <AlertCircle className="w-4 h-4 shrink-0" /> Recommended Improvements
                  </h4>
                  <ul className="space-y-2 text-xs text-zinc-300">
                    {parsedData.improvements.map((imp, idx) => (
                      <li key={idx} className="flex items-start gap-2">
                        <span className="text-amber-500 select-none mt-0.5">&bull;</span>
                        <span>{imp}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              <div className="space-y-6">
                <div>
                  <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-1">Name</h3>
                  <p className="text-lg font-bold text-zinc-100">{parsedData.name || 'Not provided'}</p>
                </div>

                {parsedData.summary && (
                  <div>
                    <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-1">Professional Summary</h3>
                    <p className="text-sm text-zinc-400 leading-relaxed">{parsedData.summary}</p>
                  </div>
                )}

                {parsedData.skills && parsedData.skills.length > 0 && (
                  <div>
                    <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-2">Extracted Skills</h3>
                    <div className="flex flex-wrap gap-2">
                      {parsedData.skills.map((skill, i) => (
                        <span key={i} className="px-3 py-1 text-xs rounded-full border border-indigo-500/20 bg-indigo-500/5 text-indigo-300 font-medium">
                          {skill}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {parsedData.experience && parsedData.experience.length > 0 && (
                  <div>
                    <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-3">Work History</h3>
                    <div className="space-y-4">
                      {parsedData.experience.map((exp, i) => (
                        <div key={i} className="border-l-2 border-zinc-800 pl-4 py-1">
                          <h4 className="font-bold text-sm text-zinc-200">{exp.role}</h4>
                          <p className="text-xs text-zinc-500">{exp.company} | {exp.duration}</p>
                          {exp.highlights && exp.highlights.length > 0 && (
                            <ul className="list-disc pl-4 text-xs text-zinc-400 mt-2 space-y-1">
                              {exp.highlights.map((hl, j) => <li key={j}>{hl}</li>)}
                            </ul>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {parsedData.projects && parsedData.projects.length > 0 && (
                  <div>
                    <h3 className="text-xs font-semibold text-zinc-500 uppercase tracking-wider mb-3">Projects</h3>
                    <div className="space-y-4">
                      {parsedData.projects.map((proj, i) => (
                        <div key={i} className="border-l-2 border-zinc-800 pl-4 py-1">
                          <h4 className="font-bold text-sm text-zinc-200">{proj.title}</h4>
                          <p className="text-xs text-zinc-400 mt-1">{proj.description}</p>
                          {proj.techStack && proj.techStack.length > 0 && (
                            <div className="flex flex-wrap gap-1.5 mt-2">
                              {proj.techStack.map((tech, j) => (
                                <span key={j} className="px-2 py-0.5 text-[10px] rounded border border-zinc-800 bg-zinc-900 text-zinc-400">
                                  {tech}
                                </span>
                              ))}
                            </div>
                          )}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default ResumeUploadPage;
