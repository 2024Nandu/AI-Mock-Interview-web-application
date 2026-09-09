import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Link, useNavigate } from 'react-router-dom';
import { 
  FileText, 
  Trash2, 
  Eye, 
  BarChart3, 
  Clock,
  Download,
  Search,
  Filter,
  Loader2,
  AlertCircle
} from 'lucide-react';
import { resumeService, type Resume } from '../../services/resumeService';

const ResumeList = () => {
  const navigate = useNavigate();
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState<string>('all');
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [analyzingId, setAnalyzingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchResumes();
  }, []);

  const fetchResumes = async () => {
    try {
      const data = await resumeService.getAllResumes();
      setResumes(data);
    } catch (error) {
      console.error('Error fetching resumes:', error);
      setError('Failed to load resumes');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this resume?')) return;
    
    setDeletingId(id);
    try {
      await resumeService.deleteResume(id);
      setResumes(resumes.filter(r => r.id !== id));
    } catch (error) {
      console.error('Error deleting resume:', error);
      setError('Failed to delete resume');
    } finally {
      setDeletingId(null);
    }
  };

  const handleAnalyze = async (id: number) => {
    setAnalyzingId(id);
    setError(null);
    try {
      // First, trigger the analysis
      await resumeService.analyzeResume(id);
      
      // Then, wait a moment and fetch the analysis
      // Poll for analysis status (max 10 attempts)
      let attempts = 0;
      const maxAttempts = 10;
      
      while (attempts < maxAttempts) {
        await new Promise(resolve => setTimeout(resolve, 1000)); // Wait 1 second
        
        try {
          // Check if analysis is ready
          const analysis = await resumeService.getResumeAnalysis(id);
          if (analysis) {
            // Analysis is ready, navigate to analysis page
            navigate(`/dashboard/resume/${id}/analysis`);
            return;
          }
        } catch (err: any) {
          // If 404, analysis is not ready yet, continue polling
          if (err.response?.status !== 404) {
            throw err;
          }
        }
        attempts++;
      }
      
      // If we get here, analysis is taking too long
      setError('Analysis is taking longer than expected. Please try again later.');
      
    } catch (err: any) {
      console.error('Error analyzing resume:', err);
      setError(err.response?.data?.message || 'Failed to analyze resume. Please try again.');
    } finally {
      setAnalyzingId(null);
    }
  };

  const handleViewAnalysis = (id: number) => {
    navigate(`/dashboard/resume/${id}/analysis`);
  };

  const filteredResumes = resumes.filter(resume => {
    const matchesSearch = resume.fileName.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = filterStatus === 'all' || resume.status === filterStatus;
    return matchesSearch && matchesStatus;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ANALYZED':
        return 'bg-green-100 text-green-700';
      case 'PROCESSING':
        return 'bg-yellow-100 text-yellow-700';
      case 'ERROR':
        return 'bg-red-100 text-red-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'ANALYZED':
        return <BarChart3 className="w-4 h-4" />;
      case 'PROCESSING':
        return <Clock className="w-4 h-4" />;
      case 'ERROR':
        return <AlertCircle className="w-4 h-4" />;
      default:
        return <FileText className="w-4 h-4" />;
    }
  };

  return (
    <div className="max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">My Resumes</h1>
          <p className="text-gray-600 mt-2">
            Manage your uploaded resumes and track their analysis status.
          </p>
        </div>
        <Link
          to="/dashboard/resume/upload"
          className="px-6 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-colors flex items-center gap-2"
        >
          <FileText className="w-5 h-5" />
          Upload New
        </Link>
      </div>

      {/* Error Message */}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-xl p-4 flex items-center gap-3 mb-6">
          <AlertCircle className="w-5 h-5 text-red-600" />
          <p className="text-sm text-red-700">{error}</p>
          <button
            onClick={() => setError(null)}
            className="ml-auto text-red-600 hover:text-red-800"
          >
            ×
          </button>
        </div>
      )}

      {/* Search and Filter */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-4 mb-6">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search resumes..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent"
            />
          </div>
          <div className="flex items-center gap-2">
            <Filter className="w-5 h-5 text-gray-400" />
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent bg-white"
            >
              <option value="all">All Status</option>
              <option value="ANALYZED">Analyzed</option>
              <option value="PROCESSING">Processing</option>
              <option value="PENDING">Pending</option>
              <option value="ERROR">Error</option>
            </select>
          </div>
        </div>
      </div>

      {/* Resume List */}
      {loading ? (
        <div className="text-center py-16">
          <div className="w-12 h-12 border-4 border-green-600 border-t-transparent rounded-full animate-spin mx-auto" />
          <p className="text-gray-500 mt-4">Loading your resumes...</p>
        </div>
      ) : filteredResumes.length === 0 ? (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-12 text-center">
          <FileText className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h3 className="text-xl font-semibold text-gray-900 mb-2">No resumes found</h3>
          <p className="text-gray-500 mb-6">
            {searchTerm || filterStatus !== 'all' 
              ? 'Try adjusting your search or filter' 
              : 'Upload your first resume to get started'}
          </p>
          <Link
            to="/dashboard/resume/upload"
            className="inline-flex items-center gap-2 px-6 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-colors"
          >
            <FileText className="w-5 h-5" />
            Upload Resume
          </Link>
        </div>
      ) : (
        <div className="grid gap-4">
          {filteredResumes.map((resume, index) => (
            <motion.div
              key={resume.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: index * 0.05 }}
              className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 bg-green-50 rounded-xl flex items-center justify-center">
                    <FileText className="w-6 h-6 text-green-600" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900">{resume.fileName}</h3>
                    <div className="flex items-center gap-3 mt-1">
                      <span className="text-sm text-gray-500">
                        {new Date(resume.uploadedAt).toLocaleDateString()}
                      </span>
                      <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(resume.status)}`}>
                        {getStatusIcon(resume.status)}
                        {resume.status}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2 w-full sm:w-auto">
                  {resume.status === 'ANALYZED' && (
                    <button
                      onClick={() => handleViewAnalysis(resume.id)}
                      className="flex-1 sm:flex-none px-4 py-2 text-sm bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center justify-center gap-2"
                    >
                      <Eye className="w-4 h-4" />
                      View Analysis
                    </button>
                  )}
                  {resume.status === 'PROCESSING' && (
                    <button
                      disabled
                      className="flex-1 sm:flex-none px-4 py-2 text-sm bg-gray-200 text-gray-500 rounded-lg cursor-not-allowed flex items-center justify-center gap-2"
                    >
                      <Loader2 className="w-4 h-4 animate-spin" />
                      Processing
                    </button>
                  )}
                  {(resume.status === 'PENDING' || resume.status === 'ERROR') && (
                    <button
                      onClick={() => handleAnalyze(resume.id)}
                      disabled={analyzingId === resume.id}
                      className="flex-1 sm:flex-none px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
                    >
                      {analyzingId === resume.id ? (
                        <>
                          <Loader2 className="w-4 h-4 animate-spin" />
                          Analyzing...
                        </>
                      ) : (
                        <>
                          <BarChart3 className="w-4 h-4" />
                          Analyze
                        </>
                      )}
                    </button>
                  )}
                  {resume.fileUrl && (
                    <a
                      href={resume.fileUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="px-3 py-2 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors flex items-center gap-2"
                    >
                      <Download className="w-4 h-4" />
                    </a>
                  )}
                  <button
                    onClick={() => handleDelete(resume.id)}
                    disabled={deletingId === resume.id}
                    className="px-3 py-2 text-sm text-red-600 hover:text-red-700 hover:bg-red-50 rounded-lg transition-colors disabled:opacity-50 flex items-center gap-2"
                  >
                    {deletingId === resume.id ? (
                      <Loader2 className="w-4 h-4 animate-spin" />
                    ) : (
                      <Trash2 className="w-4 h-4" />
                    )}
                  </button>
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
};

export default ResumeList;