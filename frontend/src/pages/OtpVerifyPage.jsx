import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { Sparkles, MailOpen, AlertCircle, ArrowRight, RefreshCw } from 'lucide-react';

const OtpVerifyPage = () => {
  const [otp, setOtp] = useState('');
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [resending, setResending] = useState(false);
  
  const { login } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const emailParam = params.get('email');
    if (emailParam) {
      setEmail(emailParam);
    } else {
      setError('Missing email parameter. Please register or log in first.');
    }
  }, [location]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!email) return;
    setError('');
    setMessage('');
    setLoading(true);

    try {
      const response = await axios.post('http://localhost:8080/api/auth/verify-otp', { email, otp });
      const { token, email: verifiedEmail, name, userId } = response.data;
      login(token, verifiedEmail, name, userId);
      navigate('/upload-resume');
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.error || 'Verification failed. Please double-check your OTP code.');
    } finally {
      setLoading(false);
    }
  };

  const handleResend = async () => {
    if (!email) return;
    setError('');
    setMessage('');
    setResending(true);

    try {
      const response = await axios.post('http://localhost:8080/api/auth/resend-otp', { email });
      setMessage(response.data?.message || 'A new OTP has been sent. Please check your email (or server log console).');
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.error || 'Failed to resend OTP. Please try again later.');
    } finally {
      setResending(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#09090b] text-white flex items-center justify-center relative px-6 py-12">
      <div className="absolute top-[10%] left-[10%] w-[300px] h-[300px] rounded-full bg-indigo-500/10 blur-[80px] pointer-events-none" />
      <div className="absolute bottom-[10%] right-[10%] w-[350px] h-[350px] rounded-full bg-violet-600/10 blur-[90px] pointer-events-none" />

      <div className="w-full max-w-md bg-zinc-900/40 border border-zinc-800 p-8 rounded-3xl backdrop-blur-md shadow-2xl relative z-10">
        
        {/* Header */}
        <div className="flex flex-col items-center mb-8 text-center">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-tr from-indigo-500 to-violet-600 flex items-center justify-center shadow-lg shadow-indigo-500/20 mb-4">
            <MailOpen className="w-6 h-6 text-white" />
          </div>
          <h2 className="text-3xl font-bold tracking-tight bg-gradient-to-r from-white to-zinc-400 bg-clip-text text-transparent">
            Verify Your Email
          </h2>
          <p className="text-zinc-500 text-sm mt-2">
            We have sent a verification code to <br />
            <span className="text-zinc-300 font-semibold">{email || 'your email'}</span>
          </p>
        </div>

        {error && (
          <div className="mb-6 p-4 rounded-xl border border-red-500/30 bg-red-500/5 text-red-400 text-sm flex items-start gap-3">
            <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
            <span>{error}</span>
          </div>
        )}

        {message && (
          <div className="mb-6 p-4 rounded-xl border border-indigo-500/30 bg-indigo-500/5 text-indigo-400 text-sm flex items-start gap-3">
            <Sparkles className="w-5 h-5 shrink-0 mt-0.5" />
            <span>{message}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* OTP Code Field */}
          <div>
            <label className="block text-zinc-400 text-center text-sm font-semibold mb-3" htmlFor="otp">
              Enter 6-Digit Code
            </label>
            <input
              id="otp"
              name="otp"
              type="text"
              required
              maxLength={6}
              value={otp}
              onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
              placeholder="000000"
              className="w-full text-center tracking-[0.75em] text-2xl font-bold py-3.5 bg-zinc-950/80 border border-zinc-800 rounded-xl text-indigo-400 placeholder-zinc-800 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition duration-200"
            />
          </div>

          <button
            type="submit"
            disabled={loading || !email}
            className="w-full py-3.5 bg-gradient-to-r from-indigo-500 to-violet-600 hover:from-indigo-600 hover:to-violet-700 text-white rounded-xl font-semibold transition-all duration-300 shadow-lg shadow-indigo-500/25 flex items-center justify-center gap-2 group cursor-pointer"
          >
            {loading ? (
              <span className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
            ) : (
              <>
                Verify & Log In
                <ArrowRight className="w-4 h-4 transition group-hover:translate-x-0.5" />
              </>
            )}
          </button>
        </form>

        <div className="mt-8 flex flex-col items-center gap-2 text-sm">
          <span className="text-zinc-500">Didn't receive the email?</span>
          <button
            onClick={handleResend}
            disabled={resending || !email}
            className="flex items-center gap-1.5 text-indigo-400 hover:text-indigo-300 font-semibold transition disabled:text-zinc-600"
          >
            <RefreshCw className={`w-4 h-4 ${resending ? 'animate-spin' : ''}`} />
            {resending ? 'Sending...' : 'Resend OTP Code'}
          </button>
          <span className="text-xs text-zinc-600 text-center mt-2 italic">
            Check the backend server console log if Brevo key is not configured!
          </span>
        </div>
      </div>
    </div>
  );
};

export default OtpVerifyPage;
