import { motion } from 'framer-motion';
import { 
  Upload, 
  FileSearch, 
  Mic, 
  Award,
  ArrowRight
} from 'lucide-react';

const HowItWorks = () => {
  const steps = [
    {
      icon: Upload,
      title: 'Upload Your Resume',
      description: 'Upload your resume in PDF, DOC, or DOCX format. Our AI parser extracts your skills, experience, and education.',
      color: 'bg-green-50',
      iconColor: 'text-green-600',
      number: '01'
    },
    {
      icon: FileSearch,
      title: 'Get ATS Score',
      description: 'Receive a detailed ATS compatibility score with actionable insights to improve your resume.',
      color: 'bg-green-50',
      iconColor: 'text-green-600',
      number: '02'
    },
    {
      icon: Mic,
      title: 'Practice Mock Interview',
      description: 'Practice with our AI interviewer that asks personalized questions based on your resume and target role.',
      color: 'bg-green-50',
      iconColor: 'text-green-600',
      number: '03'
    },
    {
      icon: Award,
      title: 'Get Feedback & Improve',
      description: 'Receive detailed feedback on your answers, communication skills, and areas for improvement.',
      color: 'bg-green-50',
      iconColor: 'text-green-600',
      number: '04'
    },
  ];

  return (
    <section id="how-it-works" className="section-padding bg-gray-50 pt-20">
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
            <span className="text-sm font-medium">How It Works</span>
          </div>
          <h2 className="heading-lg text-gray-900 mb-4">
            Simple Steps to <span className="text-green-600">Success</span>
          </h2>
          <p className="text-body">
            Get started in minutes with our easy-to-follow process. 
            No technical skills required.
          </p>
        </motion.div>

        {/* Steps */}
        <div className="grid md:grid-cols-2 lg:grid-cols-4 gap-8 relative">
          {/* Connecting Line */}
          <div className="hidden lg:block absolute top-1/3 left-0 w-full h-0.5 bg-green-200 -translate-y-1/2">
            <div className="w-full h-full bg-linear-to-r from-green-400 to-green-600" />
          </div>

          {steps.map((step, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              className="relative"
            >
              <div className="bg-white rounded-2xl p-8 shadow-sm hover:shadow-xl transition-shadow duration-300 border border-gray-100 hover:border-green-200 h-full relative z-10">
                {/* Step Number */}
                <div className="text-5xl font-bold text-green-100 absolute top-4 right-4">
                  {step.number}
                </div>

                {/* Icon */}
                <div className={`${step.color} w-16 h-16 rounded-xl flex items-center justify-center mb-6`}>
                  <step.icon className={`w-8 h-8 ${step.iconColor}`} />
                </div>

                {/* Content */}
                <h3 className="text-xl font-semibold text-gray-900 mb-3">
                  {step.title}
                </h3>
                <p className="text-gray-600 leading-relaxed">
                  {step.description}
                </p>

                {/* Step Indicator */}
                {index < steps.length - 1 && (
                  <div className="hidden lg:block absolute -right-4 top-1/2 -translate-y-1/2 z-20">
                    <ArrowRight className="w-6 h-6 text-green-400" />
                  </div>
                )}
              </div>
            </motion.div>
          ))}
        </div>

        {/* Bottom CTA */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className="text-center mt-16"
        >
          <a
            href="/register"
            className="inline-flex items-center gap-2 px-8 py-4 bg-green-600 text-white rounded-xl hover:bg-green-700 transition-all shadow-lg shadow-green-200 font-medium"
          >
            Start Your Journey Now
            <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
          </a>
          <p className="text-sm text-gray-500 mt-4">
            No credit card required • Free to get started
          </p>
        </motion.div>
      </div>
    </section>
  );
};

export default HowItWorks;