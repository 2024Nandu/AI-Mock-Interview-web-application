import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import OtpVerifyPage from './pages/OtpVerifyPage';
import ResumeUploadPage from './pages/ResumeUploadPage';
import RoleSelectPage from './pages/RoleSelectPage';
import InterviewPage from './pages/InterviewPage';
import ReportPage from './pages/ReportPage';

// A helper for Protected Routes
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) {
    return (
      <div className="min-h-screen bg-[#09090b] text-white flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-t-2 border-indigo-500" />
      </div>
    );
  }

  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

const App = () => {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/verify-otp" element={<OtpVerifyPage />} />
          
          <Route 
            path="/upload-resume" 
            element={
              <ProtectedRoute>
                <ResumeUploadPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/select-role" 
            element={
              <ProtectedRoute>
                <RoleSelectPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/interview/:sessionId" 
            element={
              <ProtectedRoute>
                <InterviewPage />
              </ProtectedRoute>
            } 
          />
          <Route 
            path="/report/:sessionId" 
            element={
              <ProtectedRoute>
                <ReportPage />
              </ProtectedRoute>
            } 
          />
          
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
};

export default App;