import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import PublicRoute from './components/PublicRoute';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import VerifyOTPPage from './pages/VerifyOTPPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import DashboardLayout from './pages/dashboard/DashboardLayout';
import DashboardHome from './pages/dashboard/DashboardHome';
import ResumeUpload from './pages/dashboard/ResumeUpload';
import ResumeList from './pages/dashboard/ResumeList';
import ResumeAnalysisPage from './pages/dashboard/ResumeAnalysis';
import InterviewList from './pages/dashboard/InterviewList';
import NewInterview from './pages/dashboard/NewInterview';
import InterviewSession from './pages/dashboard/InterviewSession';
import ResultsList from './pages/dashboard/ResultsList';
import ResultDetail from './pages/dashboard/ResultDetail';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<LandingPage />} />
          <Route 
            path="/login" 
            element={
              <PublicRoute>
                <LoginPage />
              </PublicRoute>
            } 
          />
          <Route 
            path="/register" 
            element={
              <PublicRoute>
                <RegisterPage />
              </PublicRoute>
            } 
          />
          <Route 
            path="/verify-otp" 
            element={
              <PublicRoute>
                <VerifyOTPPage />
              </PublicRoute>
            } 
          />
          <Route 
            path="/forgot-password" 
            element={
              <PublicRoute>
                <ForgotPasswordPage />
              </PublicRoute>
            } 
          />
          <Route 
            path="/reset-password" 
            element={
              <PublicRoute>
                <ResetPasswordPage />
              </PublicRoute>
            } 
          />
          
          {/* Protected Dashboard Routes */}
          <Route 
            path="/dashboard" 
            element={
              <ProtectedRoute>
                <DashboardLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<DashboardHome />} />
            <Route path="resume/upload" element={<ResumeUpload />} />
            <Route path="resumes" element={<ResumeList />} />
            <Route path="resume/:id/analysis" element={<ResumeAnalysisPage />} />
            <Route path="interviews" element={<InterviewList />} />
            <Route path="interview/new" element={<NewInterview />} />
            <Route path="interview/:id" element={<InterviewSession />} />
            <Route path="results" element={<ResultsList />} />
            <Route path="result/:id" element={<ResultDetail />} />
          </Route>
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;