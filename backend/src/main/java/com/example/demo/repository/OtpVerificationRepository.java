package com.example.demo.repository;

import com.example.demo.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findFirstByEmailAndOtpCodeAndUsedFalseOrderByCreatedAtDesc(String email, String otpCode);
    Optional<OtpVerification> findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(String email);
}
