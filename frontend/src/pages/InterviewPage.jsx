import React, { useState, useEffect, useRef } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient';
import useSpeechRecognition from '../hooks/useSpeechRecognition';
import { Mic, MicOff, Send, Volume2, Sparkles, RefreshCw, ChevronRight, AlertCircle, HelpCircle, Terminal } from 'lucide-react';

const InterviewPage = () => {
  const { sessionId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  const [questionNo, setQuestionNo] = useState(1);
  const [questionText, setQuestionText] = useState('');
  const [inputText, setInputText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [isPlayingAudio, setIsPlayingAudio] = useState(false);
  const [error, setError] = useState('');

  const audioRef = useRef(null);

  const {
    isListening,
    transcript,
    setTranscript,
    startListening,
    stopListening,
    resetTranscript,
    isSupported: isSttSupported
  } = useSpeechRecognition();

  // Read initial question from navigation state, or fallback
  useEffect(() => {
    if (location.state?.initialQuestion) {
      const initial = location.state.initialQuestion;
      setQuestionNo(initial.questionNumber);
      setQuestionText(initial.questionText);
      speakQuestion(initial.questionText);
    } else {
      // If refreshed, start from basic state or alert
      setError('Unable to restore exact session state. Refreshing is not supported on active mock rounds.');
      setQuestionText('Please restart the interview track from the dashboard.');
    }

    return () => {
      if (audioRef.current) {
        audioRef.current.pause();
      }
    };
  }, [location, sessionId]);

  // Synchronize captured speech transcript with inputs
  useEffect(() => {
    if (transcript) {
      setInputText(transcript);
    }
  }, [transcript]);

  const speakQuestion = (text) => {
    if (!text) return;
    setIsPlayingAudio(true);
    setError('');

    if (audioRef.current) {
      audioRef.current.pause();
    }

    // Stop any ongoing browser speech synthesis
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }

    const backendBaseUrl = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';
    const ttsUrl = `${backendBaseUrl}/api/interviews/speak?text=${encodeURIComponent(text)}`;
    const audio = new Audio(ttsUrl);

    let fallbackTriggered = false;

    const playBrowserFallback = () => {
      if (fallbackTriggered) return;
      fallbackTriggered = true;

      if ('speechSynthesis' in window) {
        window.speechSynthesis.cancel();
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.rate = 0.95;
        utterance.pitch = 1.0;
        
        // Select a natural English voice if available
        const voices = window.speechSynthesis.getVoices();
        const preferredVoice = voices.find(v => (v.lang.startsWith('en') && (v.name.includes('Natural') || v.name.includes('Google') || v.name.includes('Samantha') || v.name.includes('Daniel') || v.name.includes('Jenny')))) || voices.find(v => v.lang.startsWith('en'));
        if (preferredVoice) {
          utterance.voice = preferredVoice;
        }

        utterance.onend = () => {
          setIsPlayingAudio(false);
        };
        utterance.onerror = () => {
          setIsPlayingAudio(false);
        };

        window.speechSynthesis.speak(utterance);
      } else {
        setIsPlayingAudio(false);
      }
    };

    audio.onended = () => {
      setIsPlayingAudio(false);
    };

    audio.onerror = () => {
      console.warn('Backend audio unavailable or unconfigured. Falling back to Browser Web Speech API.');
      playBrowserFallback();
    };

    audioRef.current = audio;
    
    // Play with fallback handling
    audio.play().catch((err) => {
      console.warn('Backend audio play blocked or unavailable. Falling back to Web Speech API.', err);
      playBrowserFallback();
    });
  };

  const handleMicToggle = () => {
    if (isListening) {
      stopListening();
    } else {
      resetTranscript();
      setInputText('');
      startListening();
    }
  };

  const handleSubmit = async (e) => {
    if (e) e.preventDefault();
    if (!inputText.trim()) {
      setError('Your answer cannot be blank. Speak or type a response.');
      return;
    }

    setError('');
    setSubmitting(true);
    stopListening();

    try {
      const response = await apiClient.post(`/api/interviews/${sessionId}/answer`, {
        answerText: inputText,
      });

      const nextData = response.data;
      
      if (nextData.status === 'COMPLETED') {
        // Successful mock evaluation
        setQuestionText('Evaluation completed! Processing report...');
        setTimeout(() => {
          navigate(`/report/${sessionId}`);
        }, 1500);
      } else {
        // Move to next question
        setQuestionNo(nextData.questionNumber);
        setQuestionText(nextData.questionText);
        setInputText('');
        resetTranscript();
        speakQuestion(nextData.questionText);
      }
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.error || 'Failed to submit answer. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#09090b] text-white flex flex-col justify-between relative px-6 py-6 font-sans">
      <div className="absolute top-[10%] left-[10%] w-[350px] h-[350px] rounded-full bg-indigo-500/5 blur-[90px] pointer-events-none" />
      <div className="absolute bottom-[10%] right-[10%] w-[400px] h-[400px] rounded-full bg-violet-600/5 blur-[100px] pointer-events-none" />

      {/* Header bar */}
      <header className="max-w-5xl mx-auto w-full flex items-center justify-between border-b border-zinc-800 pb-4 z-10">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-tr from-indigo-500 to-violet-600 flex items-center justify-center">
            <Sparkles className="w-4 h-4 text-white" />
          </div>
          <span className="font-bold tracking-tight text-sm text-zinc-300">MockAI Practice Session</span>
        </div>

        <div className="flex items-center gap-4">
          <span className="text-zinc-500 text-xs">Session: #{sessionId}</span>
          <button 
            onClick={() => navigate('/upload-resume')}
            className="text-zinc-400 hover:text-white transition text-xs font-semibold bg-zinc-900 border border-zinc-800 px-3 py-1.5 rounded-lg"
          >
            Exit Round
          </button>
        </div>
      </header>

      {/* Active Panel */}
      <main className="max-w-3xl mx-auto w-full my-auto py-8 z-10 flex flex-col gap-6">
        
        {/* Progress bar */}
        <div className="flex flex-col gap-2">
          <div className="flex justify-between items-center text-xs text-zinc-400 font-semibold uppercase tracking-wider">
            <span>Progress</span>
            <span>Question {questionNo} of 5</span>
          </div>
          <div className="h-2 w-full bg-zinc-900 border border-zinc-800 rounded-full overflow-hidden">
            <div 
              className="h-full bg-gradient-to-r from-indigo-500 to-violet-600 rounded-full transition-all duration-500" 
              style={{ width: `${(questionNo / 5) * 100}%` }}
            />
          </div>
        </div>

        {error && (
          <div className="p-4 rounded-xl border border-red-500/30 bg-red-500/5 text-red-400 text-sm flex items-start gap-3">
            <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {/* Question Panel */}
        <div className="bg-zinc-900/40 border border-zinc-800 p-8 rounded-3xl backdrop-blur-md shadow-xl flex flex-col gap-5 relative overflow-hidden">
          <div className="absolute top-0 right-0 p-3 bg-zinc-800/20 text-zinc-500 rounded-bl-2xl">
            <HelpCircle className="w-5 h-5" />
          </div>

          <div className="flex items-center gap-2 text-indigo-400 text-xs font-bold uppercase tracking-wider">
            <Terminal className="w-4 h-4" /> AI Interviewer
          </div>

          <p className="text-xl md:text-2xl font-semibold leading-relaxed text-zinc-100">
            {questionText}
          </p>

          <div className="flex items-center gap-2 mt-2">
            <button
              onClick={() => speakQuestion(questionText)}
              disabled={isPlayingAudio}
              className={`p-2 rounded-lg border border-zinc-800 bg-zinc-900 text-indigo-400 hover:text-indigo-300 hover:bg-zinc-800 transition flex items-center gap-2 text-xs font-semibold ${isPlayingAudio ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}`}
            >
              <Volume2 className={`w-4 h-4 ${isPlayingAudio ? 'animate-bounce' : ''}`} />
              {isPlayingAudio ? 'Speaking...' : 'Listen Question'}
            </button>
            {isPlayingAudio && (
              <span className="flex items-center gap-1">
                <span className="w-1.5 h-1.5 rounded-full bg-indigo-500 animate-ping" />
                <span className="text-[10px] text-zinc-500 uppercase tracking-widest font-bold">Audio playing</span>
              </span>
            )}
          </div>
        </div>

        {/* Answer input */}
        <div className="flex flex-col gap-4">
          <div className="flex justify-between items-center text-xs text-zinc-400 font-semibold uppercase tracking-wider px-1">
            <span>Your Answer</span>
            <span className="text-zinc-600">You can speak or type to edit</span>
          </div>

          <div className="relative">
            <textarea
              rows={5}
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder={isListening ? 'Listening... start speaking your answer now.' : 'Type your answer here, or click the microphone to speak...'}
              className="w-full p-5 bg-zinc-950/80 border border-zinc-800 rounded-2xl text-zinc-200 placeholder-zinc-700 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition duration-200 text-sm md:text-base resize-none"
            />

            {/* Glowing audio visualizer during recording */}
            {isListening && (
              <div className="absolute top-4 right-4 flex items-center gap-1.5 px-3 py-1.5 bg-indigo-500/10 border border-indigo-500/30 text-indigo-300 text-xs font-bold rounded-lg animate-pulse">
                <span className="flex gap-0.5">
                  <span className="w-1 h-3 rounded-full bg-indigo-400 animate-[bounce_0.8s_infinite_100ms]" />
                  <span className="w-1 h-4 rounded-full bg-indigo-400 animate-[bounce_0.8s_infinite_300ms]" />
                  <span className="w-1 h-2 rounded-full bg-indigo-400 animate-[bounce_0.8s_infinite_500ms]" />
                </span>
                Recording
              </div>
            )}
          </div>

          {/* Interactive controls */}
          <div className="flex justify-between items-center gap-4">
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={handleMicToggle}
                className={`w-14 h-14 rounded-2xl flex items-center justify-center shadow-lg transition-all duration-300 cursor-pointer ${
                  isListening 
                    ? 'bg-red-500 hover:bg-red-600 text-white shadow-red-500/20' 
                    : 'bg-zinc-900 hover:bg-zinc-800 border border-zinc-800 text-indigo-400 hover:text-indigo-300 shadow-indigo-500/5'
                }`}
                title={isSttSupported ? 'Toggle Microphone' : 'Speech recognition not supported'}
              >
                {isListening ? <MicOff className="w-6 h-6 animate-pulse" /> : <Mic className="w-6 h-6" />}
              </button>

              {!isSttSupported && (
                <span className="text-[10px] text-zinc-600 max-w-[150px] leading-tight italic">
                  * Voice input not supported in your browser. Typing is enabled.
                </span>
              )}
            </div>

            <button
              onClick={() => !submitting && handleSubmit()}
              disabled={submitting || !inputText.trim()}
              className="flex-1 py-4 bg-gradient-to-r from-indigo-500 to-violet-600 hover:from-indigo-600 hover:to-violet-700 disabled:from-zinc-900 disabled:to-zinc-900 disabled:text-zinc-600 text-white rounded-2xl font-semibold transition-all duration-300 shadow-lg shadow-indigo-500/10 disabled:shadow-none flex items-center justify-center gap-2 group cursor-pointer border disabled:border-zinc-800 border-transparent"
            >
              {submitting ? (
                <span className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <>
                  Submit Response
                  <ChevronRight className="w-4 h-4 transition group-hover:translate-x-0.5" />
                </>
              )}
            </button>
          </div>
        </div>
      </main>

      {/* Footer info */}
      <footer className="max-w-5xl mx-auto w-full text-center text-xs text-zinc-600 z-10">
        <p>Ensure your microphone access is granted. Speak clearly for best transcription accuracy.</p>
      </footer>
    </div>
  );
};

export default InterviewPage;
