import { useEffect, useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { 
  FileText, 
  Settings, 
  ChevronRight,
  Loader2,
  CheckCircle,
  AlertCircle
} from 'lucide-react';
import { resumeService, type Resume } from '../../services/resumeService';
import { interviewService } from '../../services/interviewService';

const NewInterview = () => {
  const navigate = useNavigate();
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [allResumes, setAllResumes] = useState<Resume[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedResumeId, setSelectedResumeId] = useState<number | null>(null);
  const [interviewType, setInterviewType] = useState<'TECHNICAL' | 'HR' | 'BEHAVIORAL'>('TECHNICAL');
  const [numberOfQuestions, setNumberOfQuestions] = useState(10);
  const [isStarting, setIsStarting] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchResumes = async () => {
      try {
        const data = await resumeService.getAllResumes();
        console.log('📄 All resumes:', data);
        
        // Log each resume's status to see what values we have
        data.forEach((resume, index) => {
          console.log(`📄 Resume ${index + 1}: ${resume.fileName} - Status: "${resume.status}"`);
        });
        
        setAllResumes(data);
        
        // Only show resumes with status 'ANALYZED' for interview
        const readyResumes = data.filter(r => r.status === 'ANALYZED');
        
        console.log('✅ Ready resumes (ANALYZED):', readyResumes);
        setResumes(readyResumes);
        
        if (readyResumes.length > 0) {
          setSelectedResumeId(readyResumes[0].id);
        }
      } catch (error) {
        console.error('Error fetching resumes:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchResumes();
  }, []);

  const handleStartInterview = async () => {
    if (!selectedResumeId) {
      setError('Please select a resume to start the interview');
      return;
    }

    setIsStarting(true);
    setError('');

    try {
      const response = await interviewService.startInterview({
        resumeId: selectedResumeId,
        numberOfQuestions,
        interviewType,
      });
      navigate(`/dashboard/interview/${response.id}`);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to start interview. Please try again.');
    } finally {
      setIsStarting(false);
    }
  };

  const interviewTypes = [
    { value: 'TECHNICAL', label: 'Technical', description: 'Focus on technical skills and problem-solving', icon: '💻' },
    { value: 'HR', label: 'HR', description: 'Focus on behavioral and cultural fit questions', icon: '🤝' },
    { value: 'BEHAVIORAL', label: 'Behavioral', description: 'Focus on past experiences and behavior patterns', icon: '🧠' },
  ];

  if (loading) {
    return (
      <div className="flex items-center justify-center py-16">
        <div className="text-center">
          <Loader2 className="w-12 h-12 text-green-600 animate-spin mx-auto" />
          <p className="text-gray-500 mt-4">Loading your resumes...</p>
        </div>
      </div>
    );
  }

  // Count resumes by status for display
  const statusCounts = allResumes.reduce((acc, resume) => {
    const status = resume.status || 'UNKNOWN';
    acc[status] = (acc[status] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Start New Interview</h1>
        <p className="text-gray-600 mt-2">
          Practice with our AI interviewer based on your resume.
        </p>
      </div>

      <div className="space-y-6">
        {/* Resume Selection */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
            <FileText className="w-5 h-5 text-green-600" />
            Select Resume
          </h3>
          {resumes.length === 0 ? (
            <div className="text-center py-8">
              <AlertCircle className="w-12 h-12 text-yellow-500 mx-auto mb-3" />
              <p className="text-gray-600">No analyzed resumes found.</p>
              <p className="text-sm text-gray-500 mt-1">
                You need to upload and analyze a resume first before starting an interview.
              </p>
              
              {/* Show status breakdown */}
              {allResumes.length > 0 && (
                <div className="mt-4 text-sm text-gray-500">
                  <p>Your resumes:</p>
                  <div className="mt-2 flex flex-wrap justify-center gap-2">
                    {Object.entries(statusCounts).map(([status, count]) => (
                      <span key={status} className={`px-3 py-1 rounded-full text-xs font-medium ${
                        status === 'ANALYZED' ? 'bg-green-100 text-green-700' :
                        status === 'PENDING' ? 'bg-yellow-100 text-yellow-700' :
                        status === 'PROCESSING' ? 'bg-blue-100 text-blue-700' :
                        status === 'ERROR' ? 'bg-red-100 text-red-700' :
                        'bg-gray-100 text-gray-700'
                      }`}>
                        {status}: {count}
                      </span>
                    ))}
                  </div>
                </div>
              )}
              
              <div className="mt-6 flex flex-col sm:flex-row gap-3 justify-center">
                <Link
                  to="/dashboard/resume/upload"
                  className="px-6 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-colors"
                >
                  Upload New Resume
                </Link>
                {allResumes.some(r => r.status === 'PENDING' || r.status === 'PROCESSING') && (
                  <Link
                    to="/dashboard/resumes"
                    className="px-6 py-2 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-colors"
                  >
                    Analyze Pending Resumes
                  </Link>
                )}
              </div>
            </div>
          ) : (
            <div className="space-y-2">
              {resumes.map((resume) => (
                <button
                  key={resume.id}
                  onClick={() => setSelectedResumeId(resume.id)}
                  className={`w-full text-left p-3 rounded-xl transition-colors flex items-center justify-between ${
                    selectedResumeId === resume.id
                      ? 'bg-green-50 border-2 border-green-500'
                      : 'bg-gray-50 border-2 border-transparent hover:border-gray-300'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <FileText className="w-5 h-5 text-gray-400" />
                    <div>
                      <p className="font-medium text-gray-900">{resume.fileName}</p>
                      <p className="text-sm text-gray-500">
                        Uploaded {new Date(resume.uploadedAt).toLocaleDateString()}
                        <span className="ml-2 px-2 py-0.5 rounded-full text-xs font-medium bg-green-100 text-green-700">
                          {resume.status}
                        </span>
                      </p>
                    </div>
                  </div>
                  {selectedResumeId === resume.id && (
                    <CheckCircle className="w-5 h-5 text-green-600" />
                  )}
                </button>
              ))}
            </div>
          )}
        </div>

        {/* Interview Settings */}
        {resumes.length > 0 && (
          <>
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
              <h3 className="font-semibold text-gray-900 mb-4 flex items-center gap-2">
                <Settings className="w-5 h-5 text-gray-600" />
                Interview Settings
              </h3>

              {/* Interview Type */}
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Interview Type
                </label>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                  {interviewTypes.map((type) => (
                    <button
                      key={type.value}
                      onClick={() => setInterviewType(type.value as any)}
                      className={`p-4 rounded-xl text-left transition-all ${
                        interviewType === type.value
                          ? 'bg-green-50 border-2 border-green-500'
                          : 'bg-gray-50 border-2 border-transparent hover:border-gray-300'
                      }`}
                    >
                      <div className="text-2xl mb-1">{type.icon}</div>
                      <p className="font-medium text-gray-900">{type.label}</p>
                      <p className="text-sm text-gray-500">{type.description}</p>
                    </button>
                  ))}
                </div>
              </div>

              {/* Number of Questions */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Number of Questions: {numberOfQuestions}
                </label>
                <input
                  type="range"
                  min="5"
                  max="15"
                  value={numberOfQuestions}
                  onChange={(e) => setNumberOfQuestions(parseInt(e.target.value))}
                  className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-green-600"
                />
                <div className="flex justify-between text-xs text-gray-500 mt-1">
                  <span>5</span>
                  <span>10</span>
                  <span>15</span>
                </div>
              </div>
            </div>

            {/* Error Message */}
            {error && (
              <div className="bg-red-50 border border-red-200 rounded-xl p-4 flex items-center gap-3">
                <AlertCircle className="w-5 h-5 text-red-600" />
                <p className="text-sm text-red-700">{error}</p>
              </div>
            )}

            {/* Start Button */}
            <button
              onClick={handleStartInterview}
              disabled={isStarting}
              className="w-full py-4 bg-green-600 text-white rounded-xl font-medium hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {isStarting ? (
                <>
                  <Loader2 className="w-5 h-5 animate-spin" />
                  Starting Interview...
                </>
              ) : (
                <>
                  Start Interview
                  <ChevronRight className="w-5 h-5" />
                </>
              )}
            </button>

            {/* Info Note */}
            <p className="text-sm text-gray-500 text-center">
              Your interview will be personalized based on your selected resume and interview type.
            </p>
          </>
        )}
      </div>
    </div>
  );
};

export default NewInterview;