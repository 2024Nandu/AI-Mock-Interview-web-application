import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient';
import { Sparkles, Terminal, Code, Cpu, Database, Blocks, Smartphone, Layers, AlertCircle, RefreshCw } from 'lucide-react';

const iconMap = {
  fresher: Layers,
  frontend: Code,
  backend: Terminal,
  fullstack: Blocks,
  devops: Cpu,
  data_analyst: Database,
  android: Smartphone,
  professional: Sparkles
};

const RoleSelectPage = () => {
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [starting, setStarting] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchRoles = async () => {
      try {
        const response = await apiClient.get('/api/roles');
        setRoles(response.data);
      } catch (err) {
        console.error(err);
        setError('Failed to fetch interview tracks. Please try again later.');
      } finally {
        setLoading(false);
      }
    };
    fetchRoles();
  }, []);

  const handleStartInterview = async (roleId) => {
    if (starting) return;
    setError('');
    setStarting(true);

    try {
      const response = await apiClient.post('/api/interviews/start', { roleId });
      const { sessionId } = response.data;
      navigate(`/interview/${sessionId}`, { state: { initialQuestion: response.data } });
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.error || 'Failed to start interview. Please try again.');
    } finally {
      setStarting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-[#09090b] text-white flex items-center justify-center">
        <div className="flex flex-col items-center gap-4">
          <RefreshCw className="w-8 h-8 text-indigo-500 animate-spin" />
          <p className="text-zinc-500">Loading interview tracks...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#09090b] text-white relative px-6 py-12">
      <div className="absolute top-[5%] left-[5%] w-[400px] h-[400px] rounded-full bg-indigo-500/5 blur-[100px] pointer-events-none" />
      <div className="absolute bottom-[5%] right-[5%] w-[450px] h-[450px] rounded-full bg-violet-600/5 blur-[110px] pointer-events-none" />

      <div className="max-w-6xl mx-auto relative z-10">
        
        {/* Header */}
        <div className="mb-12 text-center">
          <h1 className="text-4xl font-extrabold tracking-tight bg-gradient-to-r from-white to-zinc-400 bg-clip-text text-transparent">
            Choose Interview Track
          </h1>
          <p className="text-zinc-500 mt-2">
            Select a specialized track. The AI agent will simulate a real technical round based on industry requirements.
          </p>
        </div>

        {error && (
          <div className="mb-8 p-4 rounded-xl border border-red-500/30 bg-red-500/5 text-red-400 text-sm flex items-start gap-3 max-w-2xl mx-auto">
            <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {starting && (
          <div className="mb-8 p-4 rounded-xl border border-indigo-500/30 bg-indigo-500/5 text-indigo-400 text-sm flex items-center justify-center gap-3 max-w-2xl mx-auto animate-pulse">
            <RefreshCw className="w-4 h-4 animate-spin" />
            <span>Generating personalized questions using your resume... Please wait.</span>
          </div>
        )}

        <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {roles.map((role) => {
            const Icon = iconMap[role.roleKey] || Code;
            return (
              <div 
                key={role.id}
                onClick={() => !starting && handleStartInterview(role.id)}
                className={`border border-zinc-800 bg-zinc-900/40 backdrop-blur-sm p-6 rounded-2xl hover:border-indigo-500/50 hover:bg-zinc-900/60 transition-all duration-300 hover:-translate-y-1 flex flex-col justify-between cursor-pointer group ${starting ? 'opacity-50 cursor-not-allowed' : ''}`}
              >
                <div>
                  <div className="w-12 h-12 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 mb-5 group-hover:bg-indigo-500/20 transition">
                    <Icon className="w-6 h-6" />
                  </div>
                  <h3 className="font-bold text-lg text-zinc-100 mb-2 group-hover:text-white transition">
                    {role.displayName}
                  </h3>
                  <p className="text-zinc-400 text-xs leading-relaxed mb-6">
                    {role.description}
                  </p>
                </div>

                <div className="text-xs font-semibold text-indigo-400 group-hover:text-indigo-300 transition flex items-center gap-1">
                  Start Practice Round <span className="transition-transform group-hover:translate-x-1">&rarr;</span>
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};

export default RoleSelectPage;
