import { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Plus, Minus } from 'lucide-react';

const FAQ = () => {
  const [openIndex, setOpenIndex] = useState<number | null>(null);

  const faqs = [
    {
      question: 'How does the AI mock interview work?',
      answer: 'Our AI interviewer analyzes your resume and generates relevant questions based on your experience, skills, and target role. You can practice in a realistic interview environment and receive instant feedback on your responses, communication style, and areas for improvement.'
    },
    {
      question: 'What file formats are supported for resume upload?',
      answer: 'We support PDF, DOC, and DOCX file formats. Our AI parser extracts information from your resume including work experience, education, skills, and certifications to provide personalized interview preparation.'
    },
    {
      question: 'How accurate is the ATS score analysis?',
      answer: 'Our ATS score analysis uses advanced algorithms that simulate how Applicant Tracking Systems evaluate resumes. It checks for keywords, formatting, relevant experience, and industry-specific requirements to provide an accurate compatibility score with actionable recommendations.'
    },
    {
      question: 'Can I practice for specific job roles?',
      answer: 'Yes! You can select your target job role and industry. Our AI tailors questions based on your specific domain, whether it\'s software engineering, product management, data science, marketing, or any other field.'
    },
    {
      question: 'Is my data secure and private?',
      answer: 'Absolutely. We take data security seriously. Your resume and personal information are encrypted and stored securely. We never share your data with third parties. You can delete your account and data at any time.'
    },
    {
      question: 'Do I need to pay to use the platform?',
      answer: 'We offer a free tier with basic features including resume upload, ATS score, and one mock interview session. Premium plans are available for unlimited interviews, detailed analytics, and advanced features to accelerate your job search.'
    },
  ];

  const toggleFAQ = (index: number) => {
    setOpenIndex(openIndex === index ? null : index);
  };

  return (
    <section id="faq" className="section-padding bg-gray-50 pt-20">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5 }}
          className="text-center max-w-3xl mx-auto mb-16"
        >
          <div className="inline-flex items-center gap-2 bg-green-50 text-green-600 px-4 py-2 rounded-full mb-4">
            <span className="text-sm font-medium">FAQ</span>
          </div>
          <h2 className="heading-lg text-gray-900 mb-4">
            Frequently Asked <span className="text-green-600">Questions</span>
          </h2>
          <p className="text-body">
            Find answers to common questions about our platform and how it can help you
            prepare for your next interview.
          </p>
        </motion.div>

        {/* FAQ Accordion */}
        <div className="space-y-4">
          {faqs.map((faq, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 10 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.3, delay: index * 0.05 }}
              className="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow duration-300 border border-gray-100 hover:border-green-200 overflow-hidden"
            >
              <button
                onClick={() => toggleFAQ(index)}
                className="w-full flex items-center justify-between p-6 text-left hover:bg-green-50 transition-colors duration-200"
              >
                <span className="text-lg font-semibold text-gray-900 pr-8">
                  {faq.question}
                </span>
                <span className="shrink-0 ml-4">
                  {openIndex === index ? (
                    <Minus className="w-5 h-5 text-green-600" />
                  ) : (
                    <Plus className="w-5 h-5 text-gray-400 hover:text-green-600 transition-colors" />
                  )}
                </span>
              </button>

              <AnimatePresence>
                {openIndex === index && (
                  <motion.div
                    initial={{ height: 0, opacity: 0 }}
                    animate={{ height: 'auto', opacity: 1 }}
                    exit={{ height: 0, opacity: 0 }}
                    transition={{ duration: 0.3 }}
                    className="overflow-hidden"
                  >
                    <div className="px-6 pb-6 text-gray-600 leading-relaxed">
                      {faq.answer}
                    </div>
                  </motion.div>
                )}
              </AnimatePresence>
            </motion.div>
          ))}
        </div>

        {/* Bottom CTA */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className="text-center mt-12"
        >
          <p className="text-gray-600 mb-4">
            Still have questions? We're here to help.
          </p>
          <a
            href="/contact"
            className="inline-flex items-center gap-2 text-green-600 font-medium hover:text-green-700 transition-colors"
          >
            Contact Support
            <Plus className="w-4 h-4" />
          </a>
        </motion.div>
      </div>
    </section>
  );
};

export default FAQ;