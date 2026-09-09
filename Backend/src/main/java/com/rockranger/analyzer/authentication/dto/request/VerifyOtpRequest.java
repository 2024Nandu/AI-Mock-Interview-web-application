package com.rockranger.analyzer.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class VerifyOtpRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String otp;
    private String code;

    public VerifyOtpRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp != null ? otp : code;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getCode() {
        return code != null ? code : otp;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
