import React from 'react';
import { Link } from 'react-router-dom';
import { Sparkles, ShieldCheck, AudioLines, FileSpreadsheet, ArrowRight, UserCheck } from 'lucide-react';

const LandingPage = () => {
  return (
    <div className="min-h-screen bg-[#09090b] text-white overflow-hidden relative font-sans">
      {/* Decorative gradient glowing spheres */}
      <div className="absolute top-[-20%] left-[-10%] w-[500px] h-[500px] rounded-full bg-indigo-500/10 blur-[100px] pointer-events-none" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[600px] h-[600px] rounded-full bg-violet-600/10 blur-[120px] pointer-events-none" />

      {/* Navigation Header */}
      <header className="border-b border-zinc-800/80 bg-zinc-950/60 backdrop-blur-md sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-indigo-500 to-violet-600 flex items-center justify-center shadow-lg shadow-indigo-500/20">
              <Sparkles className="w-5 h-5 text-white" />
            </div>
            <span className="font-bold text-xl tracking-tight bg-gradient-to-r from-indigo-200 to-white bg-clip-text text-transparent">
              MockAI
            </span>
          </div>
          <div className="flex items-center gap-4">
            <Link to="/login" className="text-zinc-400 hover:text-white transition px-4 py-2 text-sm font-medium">
              Log In
            </Link>
            <Link 
              to="/register" 
              className="bg-gradient-to-r from-indigo-500 to-violet-600 hover:from-indigo-600 hover:to-violet-700 text-white px-5 py-2.5 rounded-xl text-sm font-semibold transition-all duration-300 shadow-md shadow-indigo-500/10 hover:shadow-indigo-500/25"
            >
              Sign Up Free
            </Link>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <main className="max-w-7xl mx-auto px-6 py-20 lg:py-32 relative z-10 flex flex-col items-center text-center">
        {/* Banner Tag */}
        <div className="inline-flex items-center gap-2 px-3 py-1.5 rounded-full border border-indigo-500/30 bg-indigo-500/5 text-indigo-300 text-xs font-semibold uppercase tracking-wider mb-8 animate-pulse">
          <Sparkles className="w-3.5 h-3.5" /> Powered by Groq & Deepgram
        </div>

        <h1 className="text-5xl lg:text-7xl font-extrabold tracking-tight max-w-4xl leading-[1.15] mb-6">
          Nail Your Next Interview with <br />
          <span className="bg-gradient-to-r from-indigo-400 via-violet-400 to-purple-500 bg-clip-text text-transparent">
            Conversational AI
          </span>
        </h1>

        <p className="text-zinc-400 text-lg lg:text-xl max-w-2xl mb-10 leading-relaxed">
          Upload your resume, select your desired career track, and engage in a real-time, voice-interactive technical mock interview. Get instant, comprehensive reports and roadmaps.
        </p>

        <div className="flex flex-col sm:flex-row gap-4 mb-20">
          <Link
            to="/register"
            className="flex items-center justify-center gap-2 bg-gradient-to-r from-indigo-500 to-violet-600 hover:from-indigo-600 hover:to-violet-700 text-white px-8 py-4 rounded-xl font-semibold transition-all duration-300 shadow-lg shadow-indigo-500/25 hover:-translate-y-0.5 group"
          >
            Start Mock Interview <ArrowRight className="w-5 h-5 transition group-hover:translate-x-1" />
          </Link>
          <a
            href="#features"
            className="flex items-center justify-center border border-zinc-800 bg-zinc-900/50 hover:bg-zinc-800/80 text-zinc-300 hover:text-white px-8 py-4 rounded-xl font-semibold transition"
          >
            Learn More
          </a>
        </div>

        {/* Feature Highlights Grid */}
        <div id="features" className="grid md:grid-cols-2 lg:grid-cols-4 gap-6 w-full text-left">
          
          {/* Card 1 */}
          <div className="border border-zinc-800 bg-zinc-900/40 backdrop-blur-sm p-6 rounded-2xl hover:border-zinc-700/80 transition duration-300 hover:-translate-y-1">
            <div className="w-12 h-12 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400 mb-5">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-lg mb-2">Secure Authentication</h3>
            <p className="text-zinc-400 text-sm leading-relaxed">
              Verify your identity seamlessly with OTP codes. Your profile, resume data, and performance history are kept private.
            </p>
          </div>

          {/* Card 2 */}
          <div className="border border-zinc-800 bg-zinc-900/40 backdrop-blur-sm p-6 rounded-2xl hover:border-zinc-700/80 transition duration-300 hover:-translate-y-1">
            <div className="w-12 h-12 rounded-xl bg-violet-500/10 border border-violet-500/20 flex items-center justify-center text-violet-400 mb-5">
              <FileSpreadsheet className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-lg mb-2">Resume Parsing</h3>
            <p className="text-zinc-400 text-sm leading-relaxed">
              Drop your PDF resume. Our LLM agent extracts your experience, skills, and highlights to customize interview questions.
            </p>
          </div>

          {/* Card 3 */}
          <div className="border border-zinc-800 bg-zinc-900/40 backdrop-blur-sm p-6 rounded-2xl hover:border-zinc-700/80 transition duration-300 hover:-translate-y-1">
            <div className="w-12 h-12 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center text-purple-400 mb-5">
              <AudioLines className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-lg mb-2">Voice Simulation</h3>
            <p className="text-zinc-400 text-sm leading-relaxed">
              Speak your answers. Powered by Deepgram's natural text-to-speech engine and browser voice transcription APIs.
            </p>
          </div>

          {/* Card 4 */}
          <div className="border border-zinc-800 bg-zinc-900/40 backdrop-blur-sm p-6 rounded-2xl hover:border-zinc-700/80 transition duration-300 hover:-translate-y-1">
            <div className="w-12 h-12 rounded-xl bg-fuchsia-500/10 border border-fuchsia-500/20 flex items-center justify-center text-fuchsia-400 mb-5">
              <UserCheck className="w-6 h-6" />
            </div>
            <h3 className="font-bold text-lg mb-2">Evaluation Reports</h3>
            <p className="text-zinc-400 text-sm leading-relaxed">
              Get scorecards, weakness analyses, custom model answers for weak replies, and structured prep roadmaps.
            </p>
          </div>

        </div>
      </main>

      {/* Footer */}
      <footer className="border-t border-zinc-900 bg-[#09090b] py-8 text-zinc-500 text-center text-sm z-10 relative">
        <p>&copy; {new Date().getFullYear()} MockAI Platform. Built with Spring Boot, React, and Tailwind CSS.</p>
      </footer>
    </div>
  );
};

export default LandingPage;
