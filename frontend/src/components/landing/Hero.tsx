import { motion } from 'framer-motion';
import { ArrowRight, Play, Sparkles, FileText, Brain, BarChart3, Target } from 'lucide-react';

const Hero = () => {
  return (
    <section className="relative min-h-screen flex items-center pt-20 overflow-hidden">
      {/* Background Elements */}
      <div className="absolute inset-0 -z-10">
        <div className="absolute top-20 left-10 w-72 h-72 bg-green-100 rounded-full blur-3xl opacity-30" />
        <div className="absolute bottom-20 right-10 w-96 h-96 bg-green-100 rounded-full blur-3xl opacity-30" />
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-150 h-150 bg-green-50 rounded-full blur-3xl opacity-20" />
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 w-full">
        {/* Main Content - Centered */}
        <div className="flex flex-col items-center justify-center text-center max-w-4xl mx-auto">
          {/* Badge */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="inline-flex items-center gap-2 bg-green-50 text-green-600 px-4 py-2 rounded-full mb-6"
          >
            <Sparkles className="w-4 h-4" />
            <span className="text-sm font-medium">AI-Powered Interview Platform</span>
          </motion.div>

          {/* Main Heading */}
          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            className="text-4xl sm:text-5xl lg:text-6xl font-bold text-gray-900 leading-tight mb-6"
          >
            Ace Your Next{' '}
            <span className="text-green-600 relative">
              Interview
              <motion.span
                className="absolute -bottom-2 left-0 w-full h-1 bg-green-200"
                initial={{ scaleX: 0 }}
                animate={{ scaleX: 1 }}
                transition={{ duration: 0.8, delay: 0.5 }}
              />
            </span>
            <br />
            with AI Mock Practice
          </motion.h1>

          {/* Description */}
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            className="text-lg text-gray-600 mb-8 max-w-2xl"
          >
            Upload your resume, get real-time ATS feedback, and practice with 
            our AI interviewer to land your dream job with confidence.
          </motion.p>

          {/* CTA Buttons */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            className="flex flex-wrap gap-4 justify-center"
          >
            <a
              href="/register"
              className="group inline-flex items-center gap-2 px-8 py-4 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-all shadow-lg shadow-green-200 font-medium"
            >
              Get Started Free
              <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
            </a>
            <a
              href="#how-it-works"
              className="inline-flex items-center gap-2 px-8 py-4 bg-white text-gray-700 rounded-xl hover:bg-gray-50 transition-all shadow-md font-medium border border-gray-200"
            >
              <Play className="w-5 h-5" />
              Watch Demo
            </a>
          </motion.div>

          {/* Stats */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.4 }}
            className="flex gap-8 mt-8 pt-8 border-t border-gray-200"
          >
            <div>
              <div className="flex items-center gap-1 text-2xl font-bold text-gray-900">
                10K+
              </div>
              <p className="text-sm text-gray-500">Interviews Conducted</p>
            </div>
            <div>
              <div className="flex items-center gap-1 text-2xl font-bold text-gray-900">
                95%
              </div>
              <p className="text-sm text-gray-500">Success Rate</p>
            </div>
            <div>
              <div className="flex items-center gap-1 text-2xl font-bold text-gray-900">
                4.9
              </div>
              <p className="text-sm text-gray-500">User Rating</p>
            </div>
          </motion.div>
        </div>

        {/* Feature Cards Section */}
        <motion.div
          initial={{ opacity: 0, y: 30 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.5 }}
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mt-16 max-w-5xl mx-auto"
        >
          {/* Card 1 */}
          <div className="bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-shadow duration-300 border border-gray-100 text-center group hover:border-green-300">
            <div className="w-12 h-12 bg-green-50 rounded-lg flex items-center justify-center mx-auto mb-3 group-hover:bg-green-100 transition-colors">
              <FileText className="w-6 h-6 text-green-600" />
            </div>
            <h4 className="font-semibold text-gray-900 mb-1">Resume Upload</h4>
            <p className="text-sm text-gray-500">Upload your resume for instant analysis</p>
          </div>

          {/* Card 2 */}
          <div className="bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-shadow duration-300 border border-gray-100 text-center group hover:border-green-300">
            <div className="w-12 h-12 bg-green-50 rounded-lg flex items-center justify-center mx-auto mb-3 group-hover:bg-green-100 transition-colors">
              <Brain className="w-6 h-6 text-green-600" />
            </div>
            <h4 className="font-semibold text-gray-900 mb-1">AI Mock Interview</h4>
            <p className="text-sm text-gray-500">Practice with intelligent AI interviewer</p>
          </div>

          {/* Card 3 */}
          <div className="bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-shadow duration-300 border border-gray-100 text-center group hover:border-green-300">
            <div className="w-12 h-12 bg-green-50 rounded-lg flex items-center justify-center mx-auto mb-3 group-hover:bg-green-100 transition-colors">
              <BarChart3 className="w-6 h-6 text-green-600" />
            </div>
            <h4 className="font-semibold text-gray-900 mb-1">ATS Score</h4>
            <p className="text-sm text-gray-500">Get detailed resume compatibility analysis</p>
          </div>

          {/* Card 4 */}
          <div className="bg-white rounded-xl p-6 shadow-sm hover:shadow-lg transition-shadow duration-300 border border-gray-100 text-center group hover:border-green-300">
            <div className="w-12 h-12 bg-green-50 rounded-lg flex items-center justify-center mx-auto mb-3 group-hover:bg-green-100 transition-colors">
              <Target className="w-6 h-6 text-green-600" />
            </div>
            <h4 className="font-semibold text-gray-900 mb-1">Real-time Feedback</h4>
            <p className="text-sm text-gray-500">Instant improvement suggestions</p>
          </div>
        </motion.div>
      </div>
    </section>
  );
};

export default Hero;