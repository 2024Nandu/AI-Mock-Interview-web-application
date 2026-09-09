import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import {
  Mic,
  MicOff,
  Send,
  Loader2,
  ArrowLeft,
  CheckCircle,
  AlertCircle,
  Volume2,
  BarChart3
} from 'lucide-react';
import { interviewService, type Question, type AnswerResponse } from '../../services/interviewService';

const InterviewSession = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [question, setQuestion] = useState<Question | null>(null);
  const [answer, setAnswer] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isRecording, setIsRecording] = useState(false);
  const [error, setError] = useState('');
  const [feedback, setFeedback] = useState<AnswerResponse | null>(null);
  const [questionIndex, setQuestionIndex] = useState(0);
  const [totalQuestions, setTotalQuestions] = useState(0);
  const [isComplete, setIsComplete] = useState(false);
  const [isSpeaking, setIsSpeaking] = useState(false);
  const [audioUrl, setAudioUrl] = useState<string | null>(null);
  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const audioChunksRef = useRef<Blob[]>([]);
  const audioRef = useRef<HTMLAudioElement | null>(null);

  useEffect(() => {
    if (!id) return;
    fetchCurrentQuestion();
  }, [id]);

  const fetchCurrentQuestion = async () => {
    if (!id) return;
    try {
      const data = await interviewService.getCurrentQuestion(parseInt(id));
      console.log('📝 Question data received:', data);
      console.log('📝 Question ID:', data.questionId);
      setQuestion(data);
      
      // Also get interview details to know total questions
      const interview = await interviewService.getInterviewById(parseInt(id));
      setTotalQuestions(interview.totalQuestions);
      setQuestionIndex(interview.currentQuestion || 0);
      if (interview.status === 'COMPLETED') {
        setIsComplete(true);
      }
    } catch (err: any) {
      if (err.response?.status === 404) {
        setIsComplete(true);
      } else {
        setError(err.response?.data?.message || 'Failed to load question');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitAnswer = async () => {
    // Trim the answer and check if it's empty
    const trimmedAnswer = answer.trim();
    
    if (!trimmedAnswer || !id || !question) {
      setError('Please enter an answer before submitting.');
      return;
    }

    // Make sure we have a valid questionId
    if (!question.questionId) {
      setError('Error: Question ID is missing. Please refresh and try again.');
      console.error('❌ Question ID is undefined:', question);
      return;
    }

    setIsSubmitting(true);
    setError('');

    // Log the request data for debugging
    console.log('📤 Submitting answer:', {
      interviewId: parseInt(id),
      questionId: question.questionId,
      answer: trimmedAnswer
    });

    try {
      const response = await interviewService.submitAnswer(
        parseInt(id),
        question.questionId,
        trimmedAnswer
      );
      console.log('✅ Answer submitted successfully:', response);
      setFeedback(response);
      
      // Auto advance after showing feedback
      setTimeout(() => {
        handleNextQuestion();
      }, 3000);
    } catch (err: any) {
      console.error('❌ Failed to submit answer:', err);
      console.error('Error response:', err.response?.data);
      console.error('Error status:', err.response?.status);
      
      // Show the error message from the backend
      const errorMessage = err.response?.data?.message || err.response?.data?.error || 'Failed to submit answer. Please try again.';
      setError(errorMessage);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleNextQuestion = async () => {
    if (!id) return;
    setFeedback(null);
    setAnswer('');
    
    try {
      const nextQuestion = await interviewService.nextQuestion(parseInt(id));
      setQuestion(nextQuestion);
      setQuestionIndex(prev => prev + 1);
    } catch (err: any) {
      if (err.response?.status === 404) {
        // No more questions - interview complete
        setIsComplete(true);
        // Navigate to results after delay
        setTimeout(() => {
          navigate(`/dashboard/result/${id}`);
        }, 2000);
      } else {
        setError(err.response?.data?.message || 'Failed to load next question');
      }
    }
  };

  const handleCompleteInterview = async () => {
    if (!id) return;
    setIsSubmitting(true);
    try {
      await interviewService.completeInterview(parseInt(id));
      navigate(`/dashboard/result/${id}`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to complete interview');
    } finally {
      setIsSubmitting(false);
    }
  };

  const startRecording = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const mediaRecorder = new MediaRecorder(stream);
      mediaRecorderRef.current = mediaRecorder;
      audioChunksRef.current = [];

      mediaRecorder.ondataavailable = (event) => {
        audioChunksRef.current.push(event.data);
      };

      mediaRecorder.onstop = async () => {
        const audioBlob = new Blob(audioChunksRef.current, { type: 'audio/wav' });
        const audioFile = new File([audioBlob], 'voice-answer.wav', { type: 'audio/wav' });
        await handleVoiceSubmit(audioFile);
      };

      mediaRecorder.start();
      setIsRecording(true);
    } catch (err) {
      setError('Failed to access microphone. Please check permissions.');
    }
  };

  const stopRecording = () => {
    if (mediaRecorderRef.current && isRecording) {
      mediaRecorderRef.current.stop();
      mediaRecorderRef.current.stream.getTracks().forEach(track => track.stop());
      setIsRecording(false);
    }
  };

  const handleVoiceSubmit = async (audioFile: File) => {
    if (!id || !question) return;

    // Make sure we have a valid questionId
    if (!question.questionId) {
      setError('Error: Question ID is missing. Please refresh and try again.');
      return;
    }

    setIsSubmitting(true);
    setError('');

    try {
      const response = await interviewService.submitVoiceAnswer(
        parseInt(id),
        question.questionId,
        audioFile
      );
      setFeedback(response);
      
      setTimeout(() => {
        handleNextQuestion();
      }, 3000);
    } catch (err: any) {
      console.error('❌ Failed to submit voice answer:', err);
      setError(err.response?.data?.message || 'Failed to submit voice answer');
    } finally {
      setIsSubmitting(false);
    }
  };

  const speakQuestion = async () => {
    if (!id || isSpeaking) return;
    
    setIsSpeaking(true);
    try {
      const audioData = await interviewService.speakQuestion(parseInt(id));
      const url = URL.createObjectURL(audioData);
      setAudioUrl(url);
      
      if (audioRef.current) {
        audioRef.current.src = url;
        await audioRef.current.play();
        audioRef.current.onended = () => {
          setIsSpeaking(false);
          URL.revokeObjectURL(url);
          setAudioUrl(null);
        };
      }
    } catch (err) {
      console.error('Failed to speak question:', err);
      setError('Failed to play audio');
      setIsSpeaking(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="text-center">
          <Loader2 className="w-12 h-12 text-green-600 animate-spin mx-auto" />
          <p className="text-gray-500 mt-4">Loading interview...</p>
        </div>
      </div>
    );
  }

  if (isComplete) {
    return (
      <div className="text-center py-16">
        <CheckCircle className="w-16 h-16 text-green-600 mx-auto mb-4" />
        <h3 className="text-2xl font-bold text-gray-900 mb-2">Interview Complete!</h3>
        <p className="text-gray-500">Your interview has been completed successfully.</p>
        <p className="text-gray-500 mt-2">Redirecting to results...</p>
        <div className="mt-4 w-8 h-8 border-4 border-green-600 border-t-transparent rounded-full animate-spin mx-auto" />
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-4">
          <button
            onClick={() => navigate('/dashboard/interviews')}
            className="p-2 hover:bg-gray-100 rounded-lg transition-colors"
          >
            <ArrowLeft className="w-6 h-6" />
          </button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Interview Session</h1>
            <p className="text-gray-500">
              Question {questionIndex + 1} of {totalQuestions}
            </p>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-sm text-gray-500">Progress</span>
          <div className="w-32 h-2 bg-gray-200 rounded-full overflow-hidden">
            <div
              className="h-full bg-green-600 rounded-full transition-all"
              style={{ width: `${((questionIndex) / totalQuestions) * 100}%` }}
            />
          </div>
        </div>
      </div>

      {/* Error */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-4 flex items-center gap-3 mb-6">
          <AlertCircle className="w-5 h-5 text-red-600" />
          <p className="text-sm text-red-700">{error}</p>
          <button
            onClick={() => setError('')}
            className="ml-auto text-red-600 hover:text-red-800"
          >
            ×
          </button>
        </div>
      )}

      {/* Question Card */}
      <motion.div
        key={question?.questionId}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 mb-6"
      >
        <div className="flex items-start justify-between gap-4">
          <div className="flex-1">
            <div className="flex items-center gap-3 mb-3">
              <span className="px-3 py-1 bg-green-50 text-green-700 rounded-full text-sm font-medium">
                Question {questionIndex + 1}
              </span>
              {question?.category && (
                <span className="px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm">
                  {question.category}
                </span>
              )}
              {question?.difficulty && (
                <span className={`px-3 py-1 rounded-full text-sm ${
                  question.difficulty === 'EASY' ? 'bg-green-50 text-green-700' :
                  question.difficulty === 'MEDIUM' ? 'bg-yellow-50 text-yellow-700' :
                  'bg-red-50 text-red-700'
                }`}>
                  {question.difficulty}
                </span>
              )}
            </div>
            <h3 className="text-xl font-semibold text-gray-900 mb-4">
              {question?.question}
            </h3>
            
            {/* Speak Button */}
            <button
              onClick={speakQuestion}
              disabled={isSpeaking}
              className="flex items-center gap-2 text-gray-600 hover:text-green-600 transition-colors disabled:opacity-50"
            >
              {isSpeaking ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Speaking...
                </>
              ) : (
                <>
                  <Volume2 className="w-5 h-5" />
                  Listen to Question
                </>
              )}
            </button>
            <audio ref={audioRef} className="hidden" />
          </div>
        </div>
      </motion.div>

      {/* Answer Section */}
      {!feedback && (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Your Answer
            </label>
            <textarea
              value={answer}
              onChange={(e) => setAnswer(e.target.value)}
              placeholder="Type your answer here..."
              rows={6}
              className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent resize-none"
              disabled={isSubmitting}
            />
            <p className="text-xs text-gray-500 mt-1">
              {answer.trim().length > 0 ? `${answer.trim().length} characters` : 'Enter your answer'}
            </p>
          </div>

          <div className="flex flex-wrap gap-3">
            <button
              onClick={handleSubmitAnswer}
              disabled={isSubmitting || !answer.trim()}
              className="flex-1 px-6 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Submitting...
                </>
              ) : (
                <>
                  <Send className="w-5 h-5" />
                  Submit Answer
                </>
              )}
            </button>

            <button
              onClick={isRecording ? stopRecording : startRecording}
              disabled={isSubmitting}
              className={`px-6 py-2 rounded-xl transition-colors flex items-center gap-2 ${
                isRecording
                  ? 'bg-red-600 hover:bg-red-700 text-white'
                  : 'bg-gray-100 hover:bg-gray-200 text-gray-700'
              }`}
            >
              {isRecording ? (
                <>
                  <MicOff className="w-5 h-5" />
                  Stop Recording
                </>
              ) : (
                <>
                  <Mic className="w-5 h-5" />
                  Voice Answer
                </>
              )}
            </button>
          </div>

          {isRecording && (
            <div className="flex items-center gap-3 mt-4 p-3 bg-red-50 rounded-xl">
              <div className="w-3 h-3 bg-red-600 rounded-full animate-pulse" />
              <p className="text-sm text-red-700">Recording... Speak your answer</p>
            </div>
          )}
        </div>
      )}

      {/* Feedback */}
      {feedback && (
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8"
        >
          <div className="flex items-center gap-3 mb-4">
            <CheckCircle className="w-6 h-6 text-green-600" />
            <h3 className="text-lg font-semibold text-gray-900">Answer Feedback</h3>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div className="bg-green-50 rounded-xl p-4 text-center">
              <p className="text-sm text-gray-500">Score</p>
              <p className="text-3xl font-bold text-green-700">{feedback.score}/100</p>
            </div>
            <div className="bg-blue-50 rounded-xl p-4">
              <p className="text-sm text-gray-500 mb-1">Strengths</p>
              <ul className="text-sm text-blue-700">
                {feedback.strengths?.map((s, i) => (
                  <li key={i} className="flex items-start gap-1">
                    <span>•</span> {s}
                  </li>
                ))}
              </ul>
            </div>
            <div className="bg-orange-50 rounded-xl p-4">
              <p className="text-sm text-gray-500 mb-1">Improvements</p>
              <ul className="text-sm text-orange-700">
                {feedback.improvements?.map((s, i) => (
                  <li key={i} className="flex items-start gap-1">
                    <span>•</span> {s}
                  </li>
                ))}
              </ul>
            </div>
          </div>
          
          <div className="bg-gray-50 rounded-xl p-4">
            <p className="text-sm text-gray-500 mb-1">Detailed Feedback</p>
            <p className="text-gray-700">{feedback.feedback}</p>
          </div>

          <div className="mt-4 text-center text-sm text-gray-500">
            Moving to next question...
          </div>
        </motion.div>
      )}

      {/* Complete Interview Button */}
      {question && questionIndex >= totalQuestions - 1 && !feedback && (
        <button
          onClick={handleCompleteInterview}
          disabled={isSubmitting}
          className="w-full mt-6 py-3 bg-purple-600 text-white rounded-xl font-medium hover:bg-purple-700 transition-colors disabled:opacity-50 flex items-center justify-center gap-2"
        >
          {isSubmitting ? (
            <>
              <Loader2 className="w-5 h-5 animate-spin" />
              Completing...
            </>
          ) : (
            <>
              <BarChart3 className="w-5 h-5" />
              Complete Interview & View Results
            </>
          )}
        </button>
      )}
    </div>
  );
};

export default InterviewSession;