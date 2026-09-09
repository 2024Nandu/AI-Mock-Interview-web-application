import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import VerifyOTPPage from './pages/VerifyOTPPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';

function App() {
  return (
    <Router>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/verify-otp" element={<VerifyOTPPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
        
        {/* Protected Routes - Will be added later */}
        {/* <Route path="/dashboard" element={<DashboardLayout />}> */}
        {/*   <Route index element={<DashboardHome />} /> */}
        {/*   <Route path="resume" element={<ResumeUpload />} /> */}
        {/*   <Route path="ats-score" element={<ATSScore />} /> */}
        {/*   <Route path="interview" element={<InterviewSession />} /> */}
        {/*   <Route path="profile" element={<Profile />} /> */}
        {/* </Route> */}
      </Routes>
    </Router>
  );
}

export default App;