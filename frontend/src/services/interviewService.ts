import api from './api';

export interface Interview {
  id: number;
  resumeId: number;
  totalQuestions: number;
  currentQuestion: number;
  interviewType: 'TECHNICAL' | 'HR' | 'BEHAVIORAL';
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';
  startedAt: string;
  completedAt: string | null;
}

export interface Question {
  questionId: number;  // ✅ Changed from 'id' to 'questionId'
  questionNumber: number;
  totalQuestions: number;
  question: string;
  category: string;
  difficulty: string;
}

export interface AnswerResponse {
  questionId: number;
  answer: string;
  score: number;
  feedback: string;
  strengths: string[];
  improvements: string[];
}

export interface InterviewResult {
  id: number;
  interviewId: number;
  overallScore: number;
  scores: {
    technical: number;
    communication: number;
    problemSolving: number;
    experience: number;
  };
  strengths: string[];
  improvements: string[];
  summary: string;
  questionsAndAnswers: {
    question: string;
    answer: string;
    score: number;
    feedback: string;
  }[];
}

export const interviewService = {
  // Start Interview
  startInterview: async (data: {
    resumeId: number;
    numberOfQuestions?: number;
    interviewType?: 'TECHNICAL' | 'HR' | 'BEHAVIORAL';
  }): Promise<Interview> => {
    const response = await api.post('/interviews', {
      resumeId: data.resumeId,
      numberOfQuestions: data.numberOfQuestions || 10,
      interviewType: data.interviewType || 'TECHNICAL',
    });
    console.log('🎯 Start interview response:', response.data);
    return response.data;
  },

  // Get Current Question
  getCurrentQuestion: async (interviewId: number): Promise<Question> => {
    const response = await api.get(`/interviews/${interviewId}/current-question`);
    console.log('❓ Current question response:', response.data);
    return response.data;
  },

  // Submit Answer
  submitAnswer: async (interviewId: number, questionId: number, answer: string): Promise<AnswerResponse> => {
    console.log('📤 Submitting with questionId:', questionId);
    const response = await api.post(`/interviews/${interviewId}/answers`, {
      questionId,
      answer,
    });
    return response.data;
  },

  // Submit Voice Answer
  submitVoiceAnswer: async (interviewId: number, questionId: number, file: File): Promise<AnswerResponse> => {
    const formData = new FormData();
    formData.append('questionId', questionId.toString());
    formData.append('file', file);
    const response = await api.post(`/interviews/${interviewId}/answers/voice`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Next Question
  nextQuestion: async (interviewId: number): Promise<Question> => {
    const response = await api.post(`/interviews/${interviewId}/next`);
    return response.data;
  },

  // Complete Interview
  completeInterview: async (interviewId: number): Promise<Interview> => {
    const response = await api.post(`/interviews/${interviewId}/complete`);
    return response.data;
  },

  // Get Interview Result
  getInterviewResult: async (interviewId: number): Promise<InterviewResult> => {
    const response = await api.get(`/interviews/${interviewId}/result`);
    return response.data;
  },

  // Get All User Interviews
  getAllInterviews: async (): Promise<Interview[]> => {
    const response = await api.get('/interviews');
    console.log('📋 All interviews:', response.data);
    return response.data;
  },

  // Get Interview by ID
  getInterviewById: async (id: number): Promise<Interview> => {
    const response = await api.get(`/interviews/${id}`);
    console.log('📄 Interview by ID:', response.data);
    return response.data;
  },

  // Speak Question (Get Audio)
  speakQuestion: async (interviewId: number): Promise<Blob> => {
    const response = await api.get(`/interviews/${interviewId}/current-question/speak`, {
      responseType: 'blob',
    });
    return response.data;
  },

  // Download PDF Report
  downloadReport: async (interviewId: number): Promise<Blob> => {
    const response = await api.get(`/interviews/${interviewId}/report`, {
      responseType: 'blob',
    });
    return response.data;
  },
};

export default interviewService;