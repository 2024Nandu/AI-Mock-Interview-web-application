package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.OtpVerification;
import com.example.demo.entity.User;
import com.example.demo.repository.OtpVerificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       OtpVerificationRepository otpVerificationRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.otpVerificationRepository = otpVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setVerified(true);
        userRepository.save(user);

        logger.info("Registered and verified user: {}", user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getName());
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getId());
    }

    @Transactional
    public AuthResponse verifyOtp(String email, String otpCode) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setVerified(true);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getName());
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getId());
    }

    @Transactional
    public String resendOtp(String email) {
        return "OTP verification is currently disabled.";
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!user.isVerified()) {
            user.setVerified(true);
            userRepository.save(user);
        }

        logger.info("User logged in successfully: {}", user.getEmail());
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getName());
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getId());
    }

    private void sendNewOtp(String email) {
        // Generate a 6-digit OTP code
        int num = random.nextInt(900000) + 100000;
        String otpCode = String.valueOf(num);

        OtpVerification verification = new OtpVerification();
        verification.setEmail(email);
        verification.setOtpCode(otpCode);
        verification.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verification.setUsed(false);
        verification.setPurpose(OtpVerification.OtpPurpose.REGISTRATION);
        otpVerificationRepository.save(verification);

        // Async or synchronous email send
        emailService.sendOtpEmail(email, otpCode);
    }
}
