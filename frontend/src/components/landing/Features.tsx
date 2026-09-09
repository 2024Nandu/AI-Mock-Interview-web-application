import { motion } from 'framer-motion';
import { 
  Upload, 
  FileSearch, 
  Mic, 
  BarChart3, 
  Clock, 
  Award
} from 'lucide-react';

const Features = () => {
  const features = [
    {
      icon: Upload,
      title: 'Resume Upload',
      description: 'Upload your resume in PDF, DOC, or DOCX format. Our AI parser extracts all relevant information instantly.',
      color: 'bg-green-50',
      iconColor: 'text-green-600'
    },
    {
      icon: FileSearch,
      title: 'ATS Score Analysis',
      description: 'Get detailed ATS compatibility score with suggestions to improve your resume for better visibility.',
      color: 'bg-green-50',
      iconColor: 'text-green-600'
    },
    {
      icon: Mic,
      title: 'AI Mock Interview',
      description: 'Practice with our AI interviewer that asks relevant questions based on your resume and job role.',
      color: 'bg-green-50',
      iconColor: 'text-green-600'
    },
    {
      icon: BarChart3,
      title: 'Performance Analytics',
      description: 'Track your progress with detailed analytics on your interview performance and areas of improvement.',
      color: 'bg-green-50',
      iconColor: 'text-green-600'
    },
    {
      icon: Clock,
      title: 'Real-time Feedback',
      description: 'Get instant feedback on your answers with suggestions for improvement and better responses.',
      color: 'bg-green-50',
      iconColor: 'text-green-600'
    },
    {
      icon: Award,
      title: 'Skill Assessment',
      description: 'Comprehensive skill evaluation with personalized recommendations for career development.',
      color: 'bg-green-50',
      iconColor: 'text-green-600'
    },
  ];

  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.1
      }
    }
  };

  const itemVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: {
      opacity: 1,
      y: 0,
      transition: {
        duration: 0.5
      }
    }
  };

  return (
    <section id="features" className="section-padding bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5 }}
          className="text-center max-w-3xl mx-auto mb-16"
        >
          <div className="inline-flex items-center gap-2 bg-green-50 text-green-600 px-4 py-2 rounded-full mb-4">
            <span className="text-sm font-medium">Features</span>
          </div>
          <h2 className="heading-lg text-gray-900 mb-4">
            Everything You Need to <span className="text-green-600">Succeed</span>
          </h2>
          <p className="text-body">
            Our comprehensive platform provides all the tools you need to prepare for 
            your next job interview and land your dream role.
          </p>
        </motion.div>

        {/* Features Grid */}
        <motion.div
          variants={containerVariants}
          initial="hidden"
          whileInView="visible"
          viewport={{ once: true }}
          className="grid md:grid-cols-2 lg:grid-cols-3 gap-8"
        >
          {features.map((feature, index) => (
            <motion.div
              key={index}
              variants={itemVariants}
              whileHover={{ 
                y: -8,
                transition: { duration: 0.2 }
              }}
              className="group relative bg-white rounded-2xl p-8 shadow-sm hover:shadow-xl transition-shadow duration-300 border border-gray-100 hover:border-green-200"
            >
              {/* Icon */}
              <div className={`${feature.color} w-14 h-14 rounded-xl flex items-center justify-center mb-5 group-hover:scale-110 transition-transform duration-300`}>
                <feature.icon className={`w-7 h-7 ${feature.iconColor}`} />
              </div>

              {/* Content */}
              <h3 className="text-xl font-semibold text-gray-900 mb-3">
                {feature.title}
              </h3>
              <p className="text-gray-600 leading-relaxed">
                {feature.description}
              </p>

              {/* Hover Gradient Border */}
              <div className="absolute inset-0 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-300 pointer-events-none">
                <div className="absolute inset-0 rounded-2xl bg-linear-to-r from-green-500/10 to-emerald-500/10" />
              </div>
            </motion.div>
          ))}
        </motion.div>

        {/* Bottom Stats */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5, delay: 0.2 }}
          className="mt-20 grid grid-cols-2 lg:grid-cols-4 gap-8 pt-12 border-t border-gray-200"
        >
          <div className="text-center">
            <div className="text-3xl font-bold text-green-600">10K+</div>
            <p className="text-sm text-gray-500 mt-1">Resumes Analyzed</p>
          </div>
          <div className="text-center">
            <div className="text-3xl font-bold text-green-600">5K+</div>
            <p className="text-sm text-gray-500 mt-1">Mock Interviews</p>
          </div>
          <div className="text-center">
            <div className="text-3xl font-bold text-green-600">95%</div>
            <p className="text-sm text-gray-500 mt-1">Success Rate</p>
          </div>
          <div className="text-center">
            <div className="text-3xl font-bold text-green-600">4.9/5</div>
            <p className="text-sm text-gray-500 mt-1">User Rating</p>
          </div>
        </motion.div>
      </div>
    </section>
  );
};

export default Features;