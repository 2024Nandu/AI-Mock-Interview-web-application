import { motion } from 'framer-motion';
import { Star, Quote, Briefcase } from 'lucide-react';

const Testimonials = () => {
  const testimonials = [
    {
      name: 'Sarah Johnson',
      role: 'Software Engineer at Google',
      image: 'https://ui-avatars.com/api/?name=Sarah+Johnson&background=22C55E&color=fff&size=60',
      content: 'This platform completely transformed my interview preparation. The AI interviewer asked relevant questions and provided valuable feedback that helped me land my dream job at Google.',
      rating: 5
    },
    {
      name: 'Michael Chen',
      role: 'Product Manager at Microsoft',
      image: 'https://ui-avatars.com/api/?name=Michael+Chen&background=16A34A&color=fff&size=60',
      content: 'The ATS score feature was a game-changer. I improved my resume based on the suggestions and got 3x more interview calls. The mock interviews were incredibly realistic.',
      rating: 5
    },
    {
      name: 'Emily Rodriguez',
      role: 'Data Scientist at Amazon',
      image: 'https://ui-avatars.com/api/?name=Emily+Rodriguez&background=15803D&color=fff&size=60',
      content: 'I was nervous about technical interviews, but practicing with this AI interviewer gave me the confidence I needed. The real-time feedback helped me improve my communication skills significantly.',
      rating: 5
    },
  ];

  return (
    <section id="testimonials" className="section-padding bg-white pt-20">
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
            <span className="text-sm font-medium">Testimonials</span>
          </div>
          <h2 className="heading-lg text-gray-900 mb-4">
            What Our <span className="text-green-600">Users Say</span>
          </h2>
          <p className="text-body">
            Join thousands of satisfied users who have successfully landed their dream jobs
            using our platform.
          </p>
        </motion.div>

        {/* Testimonials Grid */}
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-8">
          {testimonials.map((testimonial, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              whileHover={{ y: -8 }}
              className="bg-gray-50 rounded-2xl p-8 border border-gray-100 hover:border-green-200 hover:shadow-xl transition-all duration-300"
            >
              {/* Quote Icon */}
              <Quote className="w-8 h-8 text-green-400 mb-4" />

              {/* Content */}
              <p className="text-gray-700 leading-relaxed mb-6">
                "{testimonial.content}"
              </p>

              {/* Rating Stars */}
              <div className="flex items-center gap-1 mb-4">
                {[...Array(5)].map((_, i) => (
                  <Star
                    key={i}
                    className={`w-4 h-4 ${
                      i < testimonial.rating
                        ? 'fill-yellow-400 text-yellow-400'
                        : 'text-gray-300'
                    }`}
                  />
                ))}
              </div>

              {/* User Info */}
              <div className="flex items-center gap-3 pt-4 border-t border-gray-200">
                <img
                  src={testimonial.image}
                  alt={testimonial.name}
                  className="w-12 h-12 rounded-full"
                />
                <div>
                  <h4 className="font-semibold text-gray-900">
                    {testimonial.name}
                  </h4>
                  <p className="text-sm text-gray-500 flex items-center gap-1">
                    <Briefcase className="w-3 h-3" />
                    {testimonial.role}
                  </p>
                </div>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Trust Indicators */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5, delay: 0.3 }}
          className="mt-16 pt-12 border-t border-gray-200"
        >
          <div className="grid grid-cols-2 md:grid-cols-4 gap-8">
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">10,000+</div>
              <p className="text-sm text-gray-500">Active Users</p>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">500+</div>
              <p className="text-sm text-gray-500">Companies</p>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">4.9/5</div>
              <p className="text-sm text-gray-500">Average Rating</p>
            </div>
            <div className="text-center">
              <div className="text-2xl font-bold text-green-600">95%</div>
              <p className="text-sm text-gray-500">Satisfaction Rate</p>
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  );
};

export default Testimonials;