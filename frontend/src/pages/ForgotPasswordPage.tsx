import { useState } from 'react';
import { motion } from 'framer-motion';
import { Mail, ArrowRight, Briefcase, CheckCircle, XCircle } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';

const ForgotPasswordPage = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setSuccess(false);

    if (!email) {
      setError('Please enter your email address');
      return;
    }

    setIsLoading(true);

    // TODO: Connect to backend API
    // POST /api/v1/auth/forgot-password
    try {
      await new Promise(resolve => setTimeout(resolve, 1500));
      console.log('Forgot Password:', { email });
      setSuccess(true);
      
      // Redirect to OTP verification after 2 seconds
      setTimeout(() => {
        navigate('/verify-otp', { state: { email, isPasswordReset: true } });
      }, 2000);
    } catch (err) {
      setError('Failed to send reset link. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-linear-to-br from-green-50 to-emerald-50 flex items-center justify-center px-4 py-12">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="w-full max-w-md"
      >
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center gap-2 mb-4">
            <div className="bg-green-600 p-2 rounded-lg">
              <Briefcase className="w-8 h-8 text-white" />
            </div>
            <span className="text-2xl font-bold text-gray-900">
              Mock<span className="text-green-600">Interview</span>
            </span>
          </div>
          <h2 className="text-3xl font-bold text-gray-900">Forgot Password</h2>
          <p className="text-gray-600 mt-2">
            Enter your email to receive a password reset code
          </p>
        </div>

        {/* Forgot Password Form */}
        <div className="bg-white rounded-2xl shadow-xl p-8">
          {success ? (
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              className="text-center py-8"
            >
              <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <CheckCircle className="w-10 h-10 text-green-600" />
              </div>
              <h3 className="text-2xl font-bold text-gray-900 mb-2">Check Your Email!</h3>
              <p className="text-gray-600">
                We've sent a password reset code to{' '}
                <span className="font-medium text-gray-900">{email}</span>
              </p>
              <p className="text-sm text-gray-500 mt-4">Redirecting to verification...</p>
            </motion.div>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Email Field */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Email Address
                </label>
                <div className="relative">
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                  <input
                    type="email"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                    className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-transparent transition-all"
                    placeholder="you@example.com"
                  />
                </div>
              </div>

              {/* Error Message */}
              {error && (
                <motion.div
                  initial={{ opacity: 0, y: -10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="bg-red-50 text-red-600 px-4 py-3 rounded-xl text-sm flex items-center gap-2"
                >
                  <XCircle className="w-4 h-4" />
                  {error}
                </motion.div>
              )}

              {/* Submit Button */}
              <button
                type="submit"
                disabled={isLoading}
                className="w-full bg-green-600 text-white py-3 rounded-xl font-medium hover:bg-green-700 transition-all shadow-lg shadow-green-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                {isLoading ? (
                  <>
                    <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    Sending reset code...
                  </>
                ) : (
                  <>
                    Send Reset Code
                    <ArrowRight className="w-5 h-5" />
                  </>
                )}
              </button>

              {/* Back to Login */}
              <p className="text-center text-gray-600">
                Remember your password?{' '}
                <Link to="/login" className="text-green-600 font-medium hover:text-green-700">
                  Back to Login
                </Link>
              </p>
            </form>
          )}
        </div>

        {/* Help Text */}
        <p className="text-center text-sm text-gray-500 mt-6">
          We'll send a 6-digit code to your email for verification.
        </p>
      </motion.div>
    </div>
  );
};

export default ForgotPasswordPage;