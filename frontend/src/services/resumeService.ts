import api from './api';

export interface Resume {
  id: number;
  fileName: string;
  fileUrl: string;
  fileType: string;
  extractedText: string;
  uploadedAt: string;
  status: 'PENDING' | 'PROCESSING' | 'ANALYZED' | 'ERROR';
}

export interface ResumeAnalysisResponse {
  id: number;
  createdAt: string;
  overallScore: number;
  sectionScores: {
    sectionName: string;
    score: number;
    maxScore: number;
    feedback: string;
  }[];
  strengths: string[];
  weaknesses: string[];
  improvements: string[];
  missingKeywords: string[];
}

export interface ResumeAnalysis {
  id: number;
  resumeId: number;
  candidateInfo: {
    fullName: string;
    email: string;
    phone: string;
    location: string;
    summary: string;
  };
  skills: {
    technical: string[];
    soft: string[];
  };
  experience: {
    company: string;
    title: string;
    startDate: string;
    endDate: string;
    description: string;
  }[];
  education: {
    institution: string;
    degree: string;
    field: string;
    graduationYear: string;
  }[];
  projects: {
    name: string;
    description: string;
    technologies: string[];
  }[];
  atsScore: {
    overall: number;
    sections: {
      name: string;
      score: number;
      maxScore: number;
      feedback: string;
    }[];
    strengths: string[];
    weaknesses: string[];
    improvements: string[];
  };
}

export const resumeService = {
  // Upload Resume
  uploadResume: async (file: File): Promise<Resume> => {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post('/resumes/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  // Get All Resumes
  getAllResumes: async (): Promise<Resume[]> => {
    const response = await api.get('/resumes');
    return response.data;
  },

  // Get Resume by ID
  getResumeById: async (id: number): Promise<Resume> => {
    const response = await api.get(`/resumes/${id}`);
    return response.data;
  },

  // Delete Resume
  deleteResume: async (id: number): Promise<void> => {
    await api.delete(`/resumes/${id}`);
  },

  // Analyze Resume
  analyzeResume: async (id: number): Promise<{ message: string }> => {
    const response = await api.post(`/resumes/${id}/analyze`);
    return response.data;
  },

  // Get Resume Analysis
  getResumeAnalysis: async (id: number): Promise<ResumeAnalysisResponse> => {
    const response = await api.get(`/resumes/${id}/analysis`);
    console.log('📊 Analysis response:', response.data);
    return response.data;
  },
};

export default resumeService;