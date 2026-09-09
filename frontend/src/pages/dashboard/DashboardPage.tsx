import { useEffect } from 'react';
import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { Briefcase, LogOut, User, Mail, CheckCircle } from 'lucide-react';

const DashboardPage = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated, isLoading, logout } = useAuth();

  console.log('📊 Dashboard - User:', user);
  console.log('📊 Dashboard - isAuthenticated:', isAuthenticated);
  console.log('📊 Dashboard - isLoading:', isLoading);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      console.log('🚫 Not authenticated, redirecting to login');
      navigate('/login');
    }
  }, [isLoading, isAuthenticated, navigate]);

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-linear-to-br from-green-50 to-emerald-50 flex items-center justify-center">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-green-600 border-t-transparent rounded-full animate-spin mx-auto mb-4" />
          <p className="text-gray-600">Loading your dashboard...</p>
        </div>
      </div>
    );
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div className="min-h-screen bg-linear-to-br from-green-50 to-emerald-50">
      {/* Navbar */}
      <nav className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-2">
              <div className="bg-green-600 p-2 rounded-lg">
                <Briefcase className="w-6 h-6 text-white" />
              </div>
              <span className="text-xl font-bold text-gray-900">
                Mock<span className="text-green-600">Interview</span>
              </span>
            </div>

            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-600 hidden sm:block">
                Welcome, {user?.fullName || 'User'}
              </span>
              <button
                onClick={handleLogout}
                className="flex items-center gap-2 px-4 py-2 text-sm text-gray-600 hover:text-red-600 transition-colors rounded-lg hover:bg-red-50"
              >
                <LogOut className="w-4 h-4" />
                <span className="hidden sm:inline">Logout</span>
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="bg-white rounded-2xl shadow-xl p-8 md:p-12"
        >
          <div className="text-center">
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6"
            >
              <CheckCircle className="w-10 h-10 text-green-600" />
            </motion.div>
            
            <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
              Welcome, {user?.fullName || 'User'}! 🎉
            </h1>
            <p className="text-lg text-gray-600 mb-2">
              You have successfully logged in to MockInterview.
            </p>
            <p className="text-gray-500">
              Start your interview preparation journey today.
            </p>

            {/* User Info Card */}
            <div className="mt-8 max-w-md mx-auto bg-gray-50 rounded-xl p-6 border border-gray-200">
              <div className="flex items-center gap-3 mb-3">
                <div className="bg-green-100 p-2 rounded-lg">
                  <User className="w-5 h-5 text-green-600" />
                </div>
                <div className="text-left">
                  <p className="text-sm text-gray-500">Full Name</p>
                  <p className="font-medium text-gray-900">{user?.fullName || 'N/A'}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <div className="bg-green-100 p-2 rounded-lg">
                  <Mail className="w-5 h-5 text-green-600" />
                </div>
                <div className="text-left">
                  <p className="text-sm text-gray-500">Email</p>
                  <p className="font-medium text-gray-900">{user?.email || 'N/A'}</p>
                </div>
              </div>
              <div className="mt-3 pt-3 border-t border-gray-200">
                <p className="text-sm text-green-600 font-medium">
                  ✓ {user?.emailVerified ? 'Email Verified' : 'Email Not Verified'}
                </p>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="mt-8 grid grid-cols-1 sm:grid-cols-3 gap-4 max-w-2xl mx-auto">
              <div className="bg-indigo-50 p-4 rounded-xl border border-indigo-100 hover:shadow-md transition-shadow cursor-pointer">
                <h4 className="font-semibold text-gray-900">Upload Resume</h4>
                <p className="text-sm text-gray-500">Get your ATS score</p>
              </div>
              <div className="bg-purple-50 p-4 rounded-xl border border-purple-100 hover:shadow-md transition-shadow cursor-pointer">
                <h4 className="font-semibold text-gray-900">Mock Interview</h4>
                <p className="text-sm text-gray-500">Practice with AI</p>
              </div>
              <div className="bg-green-50 p-4 rounded-xl border border-green-100 hover:shadow-md transition-shadow cursor-pointer">
                <h4 className="font-semibold text-gray-900">View Results</h4>
                <p className="text-sm text-gray-500">Track your progress</p>
              </div>
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  );
};

export default DashboardPage;