import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { useAuth } from '../../contexts/AuthContext';
import { 
  FileText, 
  Brain, 
  BarChart3, 
  CheckCircle,
  Upload,
  Play,
  TrendingUp,
  FileCheck
} from 'lucide-react';
import { resumeService, type Resume } from '../../services/resumeService';
import { interviewService, type Interview } from '../../services/interviewService';

const DashboardHome = () => {
  const { user } = useAuth();
  const [resumes, setResumes] = useState<Resume[]>([]);
  const [interviews, setInterviews] = useState<Interview[]>([]);
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({
    totalResumes: 0,
    totalInterviews: 0,
    completedInterviews: 0,
    averageScore: 0,
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [resumesData, interviewsData] = await Promise.all([
          resumeService.getAllResumes(),
          interviewService.getAllInterviews(),
        ]);
        
        setResumes(resumesData);
        setInterviews(interviewsData);
        
        const completed = interviewsData.filter(i => i.status === 'COMPLETED');
        setStats({
          totalResumes: resumesData.length,
          totalInterviews: interviewsData.length,
          completedInterviews: completed.length,
          averageScore: 0, // Will be calculated from completed interviews
        });
      } catch (error) {
        console.error('Error fetching dashboard data:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const statCards = [
    {
      title: 'Resumes Uploaded',
      value: stats.totalResumes,
      icon: FileText,
      color: 'bg-blue-50',
      iconColor: 'text-blue-600',
    },
    {
      title: 'Total Interviews',
      value: stats.totalInterviews,
      icon: Brain,
      color: 'bg-purple-50',
      iconColor: 'text-purple-600',
    },
    {
      title: 'Completed',
      value: stats.completedInterviews,
      icon: CheckCircle,
      color: 'bg-green-50',
      iconColor: 'text-green-600',
    },
    {
      title: 'Avg Score',
      value: stats.averageScore || '--',
      icon: TrendingUp,
      color: 'bg-orange-50',
      iconColor: 'text-orange-600',
    },
  ];

  const quickActions = [
    {
      title: 'Upload Resume',
      description: 'Upload your resume for ATS analysis',
      icon: Upload,
      color: 'bg-indigo-50',
      iconColor: 'text-indigo-600',
      path: '/dashboard/resume/upload',
    },
    {
      title: 'Start Interview',
      description: 'Begin a new AI mock interview',
      icon: Play,
      color: 'bg-green-50',
      iconColor: 'text-green-600',
      path: '/dashboard/interview/new',
    },
    {
      title: 'View Results',
      description: 'Check your interview performance',
      icon: BarChart3,
      color: 'bg-purple-50',
      iconColor: 'text-purple-600',
      path: '/dashboard/results',
    },
    {
      title: 'Recent Resumes',
      description: 'View your uploaded resumes',
      icon: FileCheck,
      color: 'bg-orange-50',
      iconColor: 'text-orange-600',
      path: '/dashboard/resumes',
    },
  ];

  return (
    <div className="space-y-8">
      {/* Welcome Section */}
      <div className="bg-white rounded-2xl shadow-sm p-8 border border-gray-100">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">
              Welcome back, {user?.fullName || 'User'}! 👋
            </h1>
            <p className="text-gray-600 mt-2">
              Ready to ace your next interview? Start by uploading your resume or practicing with our AI interviewer.
            </p>
          </div>
          <div className="hidden sm:flex items-center gap-2 bg-green-50 px-4 py-2 rounded-full">
            <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse" />
            <span className="text-sm text-green-700 font-medium">All Systems Ready</span>
          </div>
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        {statCards.map((stat, index) => (
          <motion.div
            key={index}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3, delay: index * 0.1 }}
            className="bg-white rounded-2xl shadow-sm p-6 border border-gray-100 hover:shadow-md transition-shadow"
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-500">{stat.title}</p>
                <p className="text-3xl font-bold text-gray-900 mt-1">{stat.value}</p>
              </div>
              <div className={`${stat.color} p-3 rounded-xl`}>
                <stat.icon className={`w-6 h-6 ${stat.iconColor}`} />
              </div>
            </div>
          </motion.div>
        ))}
      </div>

      {/* Quick Actions */}
      <div>
        <h2 className="text-xl font-semibold text-gray-900 mb-4">Quick Actions</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
          {quickActions.map((action, index) => (
            <motion.a
              key={index}
              href={action.path}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: 0.4 + index * 0.1 }}
              className="bg-white rounded-2xl p-6 border border-gray-100 hover:shadow-lg transition-all cursor-pointer group"
            >
              <div className={`${action.color} w-12 h-12 rounded-xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform`}>
                <action.icon className={`w-6 h-6 ${action.iconColor}`} />
              </div>
              <h3 className="font-semibold text-gray-900">{action.title}</h3>
              <p className="text-sm text-gray-500 mt-1">{action.description}</p>
            </motion.a>
          ))}
        </div>
      </div>

      {/* Recent Activity */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Resumes */}
        <div className="bg-white rounded-2xl shadow-sm p-6 border border-gray-100">
          <h3 className="font-semibold text-gray-900 mb-4">Recent Resumes</h3>
          {loading ? (
            <div className="text-center py-8">
              <div className="w-8 h-8 border-2 border-green-600 border-t-transparent rounded-full animate-spin mx-auto" />
            </div>
          ) : resumes.length === 0 ? (
            <div className="text-center py-8">
              <FileText className="w-12 h-12 text-gray-300 mx-auto mb-3" />
              <p className="text-gray-500">No resumes uploaded yet</p>
              <a href="/dashboard/resume/upload" className="text-green-600 text-sm font-medium hover:underline">
                Upload your first resume
              </a>
            </div>
          ) : (
            <div className="space-y-3">
              {resumes.slice(0, 5).map((resume) => (
                <div key={resume.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-xl">
                  <div className="flex items-center gap-3">
                    <FileText className="w-5 h-5 text-gray-400" />
                    <div>
                      <p className="font-medium text-gray-900">{resume.fileName}</p>
                      <p className="text-sm text-gray-500">
                        {new Date(resume.uploadedAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                    resume.status === 'ANALYZED' 
                      ? 'bg-green-100 text-green-700'
                      : resume.status === 'PROCESSING'
                      ? 'bg-yellow-100 text-yellow-700'
                      : 'bg-gray-100 text-gray-700'
                  }`}>
                    {resume.status}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Recent Interviews */}
        <div className="bg-white rounded-2xl shadow-sm p-6 border border-gray-100">
          <h3 className="font-semibold text-gray-900 mb-4">Recent Interviews</h3>
          {loading ? (
            <div className="text-center py-8">
              <div className="w-8 h-8 border-2 border-green-600 border-t-transparent rounded-full animate-spin mx-auto" />
            </div>
          ) : interviews.length === 0 ? (
            <div className="text-center py-8">
              <Brain className="w-12 h-12 text-gray-300 mx-auto mb-3" />
              <p className="text-gray-500">No interviews yet</p>
              <a href="/dashboard/interview/new" className="text-green-600 text-sm font-medium hover:underline">
                Start your first interview
              </a>
            </div>
          ) : (
            <div className="space-y-3">
              {interviews.slice(0, 5).map((interview) => (
                <div key={interview.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-xl">
                  <div className="flex items-center gap-3">
                    <Brain className="w-5 h-5 text-gray-400" />
                    <div>
                      <p className="font-medium text-gray-900">
                        {interview.interviewType} Interview
                      </p>
                      <p className="text-sm text-gray-500">
                        {new Date(interview.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                    interview.status === 'COMPLETED'
                      ? 'bg-green-100 text-green-700'
                      : interview.status === 'IN_PROGRESS'
                      ? 'bg-yellow-100 text-yellow-700'
                      : 'bg-gray-100 text-gray-700'
                  }`}>
                    {interview.status.replace('_', ' ')}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DashboardHome;