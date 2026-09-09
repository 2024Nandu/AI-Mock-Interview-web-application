import { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import { Link } from 'react-router-dom';
import { 
  Brain, 
  Play, 
  CheckCircle, 
  Clock, 
  BarChart3,
  Calendar,
  ArrowRight
} from 'lucide-react';
import { interviewService, type Interview } from '../../services/interviewService';

const InterviewList = () => {
  const [interviews, setInterviews] = useState<Interview[]>([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState<string>('all');

  useEffect(() => {
    fetchInterviews();
  }, []);

  const fetchInterviews = async () => {
    try {
      const data = await interviewService.getAllInterviews();
      setInterviews(data);
    } catch (error) {
      console.error('Error fetching interviews:', error);
    } finally {
      setLoading(false);
    }
  };

  const filteredInterviews = interviews.filter(interview => {
    return filterStatus === 'all' || interview.status === filterStatus;
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return 'bg-green-100 text-green-700';
      case 'IN_PROGRESS':
        return 'bg-yellow-100 text-yellow-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircle className="w-4 h-4" />;
      case 'IN_PROGRESS':
        return <Clock className="w-4 h-4" />;
      default:
        return <Clock className="w-4 h-4" />;
    }
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'TECHNICAL':
        return 'bg-blue-100 text-blue-700';
      case 'HR':
        return 'bg-purple-100 text-purple-700';
      case 'BEHAVIORAL':
        return 'bg-orange-100 text-orange-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  };

  return (
    <div className="max-w-6xl mx-auto">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">My Interviews</h1>
          <p className="text-gray-600 mt-2">
            Track all your mock interview sessions and performance.
          </p>
        </div>
        <Link
          to="/dashboard/interview/new"
          className="px-6 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-colors flex items-center gap-2"
        >
          <Brain className="w-5 h-5" />
          New Interview
        </Link>
      </div>

      {/* Filter */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-4 mb-6">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex items-center gap-2">
            <span className="text-sm text-gray-500">Filter:</span>
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="px-4 py-2 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent bg-white"
            >
              <option value="all">All</option>
              <option value="IN_PROGRESS">In Progress</option>
              <option value="COMPLETED">Completed</option>
              <option value="NOT_STARTED">Not Started</option>
            </select>
          </div>
        </div>
      </div>

      {/* Interview List */}
      {loading ? (
        <div className="text-center py-16">
          <div className="w-12 h-12 border-4 border-green-600 border-t-transparent rounded-full animate-spin mx-auto" />
          <p className="text-gray-500 mt-4">Loading your interviews...</p>
        </div>
      ) : filteredInterviews.length === 0 ? (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-12 text-center">
          <Brain className="w-16 h-16 text-gray-300 mx-auto mb-4" />
          <h3 className="text-xl font-semibold text-gray-900 mb-2">No interviews yet</h3>
          <p className="text-gray-500 mb-6">
            Start your first AI-powered mock interview today.
          </p>
          <Link
            to="/dashboard/interview/new"
            className="inline-flex items-center gap-2 px-6 py-2 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-colors"
          >
            <Brain className="w-5 h-5" />
            Start Interview
          </Link>
        </div>
      ) : (
        <div className="grid gap-4">
          {filteredInterviews.map((interview, index) => (
            <motion.div
              key={interview.id}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: index * 0.05 }}
              className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow"
            >
              <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 bg-green-50 rounded-xl flex items-center justify-center">
                    <Brain className="w-6 h-6 text-green-600" />
                  </div>
                  <div>
                    <div className="flex items-center gap-3">
                      <h3 className="font-semibold text-gray-900">
                        {interview.interviewType} Interview
                      </h3>
                      <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${getTypeColor(interview.interviewType)}`}>
                        {interview.interviewType}
                      </span>
                    </div>
                    <div className="flex items-center gap-3 mt-1">
                      <span className="text-sm text-gray-500 flex items-center gap-1">
                        <Calendar className="w-3 h-3" />
                        {new Date(interview.startedAt).toLocaleDateString()}
                      </span>
                      <span className="text-sm text-gray-500">
                        {interview.totalQuestions} questions
                      </span>
                      {interview.status === 'IN_PROGRESS' && (
                        <span className="text-sm text-gray-500">
                          Question {interview.currentQuestion}/{interview.totalQuestions}
                        </span>
                      )}
                      <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(interview.status)}`}>
                        {getStatusIcon(interview.status)}
                        {interview.status.replace('_', ' ')}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="flex items-center gap-2 w-full md:w-auto">
                  {interview.status === 'IN_PROGRESS' && (
                    <Link
                      to={`/dashboard/interview/${interview.id}`}
                      className="flex-1 md:flex-none px-4 py-2 text-sm bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center justify-center gap-2"
                    >
                      <Play className="w-4 h-4" />
                      Continue
                    </Link>
                  )}
                  {interview.status === 'COMPLETED' && (
                    <Link
                      to={`/dashboard/result/${interview.id}`}
                      className="flex-1 md:flex-none px-4 py-2 text-sm bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors flex items-center justify-center gap-2"
                    >
                      <BarChart3 className="w-4 h-4" />
                      View Results
                    </Link>
                  )}
                  {interview.status === 'NOT_STARTED' && (
                    <Link
                      to={`/dashboard/interview/${interview.id}`}
                      className="flex-1 md:flex-none px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
                    >
                      <Play className="w-4 h-4" />
                      Start
                    </Link>
                  )}
                  <Link
                    to={`/dashboard/interview/${interview.id}`}
                    className="px-3 py-2 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-100 rounded-lg transition-colors"
                  >
                    <ArrowRight className="w-4 h-4" />
                  </Link>
                </div>
              </div>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
};

export default InterviewList;