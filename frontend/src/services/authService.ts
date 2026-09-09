import api from './api';

export interface RegisterData {
  fullName: string;
  email: string;
  password: string;
}

export interface LoginData {
  email: string;
  password: string;
}

export interface OTPVerifyData {
  email: string;
  code: string;
}

export interface ForgotPasswordData {
  email: string;
}

export interface ResetPasswordData {
  email: string;
  code: string;
  newPassword: string;
}

export interface User {
  id: string;
  fullName: string;
  email: string;
  emailVerified: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface VerifyOtpResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export const authService = {
  // Register user
  register: async (data: RegisterData): Promise<{ message: string }> => {
    const response = await api.post('/auth/register', data);
    return response.data;
  },

  // Login user
  login: async (data: LoginData): Promise<LoginResponse> => {
    const response = await api.post('/auth/login', data);
    const { accessToken, refreshToken } = response.data;
    
    console.log('🔐 Login response:', response.data);
    
    // Store tokens in localStorage
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    
    return response.data;
  },

  // Verify OTP
  verifyOTP: async (data: OTPVerifyData): Promise<VerifyOtpResponse> => {
    const response = await api.post('/auth/otp/verify', data);
    const { accessToken, refreshToken } = response.data;
    
    console.log('✅ OTP verify response:', response.data);
    
    // Store tokens in localStorage
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    
    return response.data;
  },

  // Request/Resend OTP
  requestOTP: async (email: string): Promise<{ message: string }> => {
    const response = await api.post('/auth/otp/request', { email });
    return response.data;
  },

  // Forgot Password
  forgotPassword: async (data: ForgotPasswordData): Promise<{ message: string }> => {
    const response = await api.post('/auth/forgot-password', data);
    return response.data;
  },

  // Reset Password
  resetPassword: async (data: ResetPasswordData): Promise<{ message: string }> => {
    const response = await api.post('/auth/reset-password', data);
    return response.data;
  },

  // Refresh Token
  refreshToken: async (): Promise<{ accessToken: string; refreshToken: string }> => {
    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      throw new Error('No refresh token available');
    }
    const response = await api.post('/auth/refresh', { refreshToken });
    const { accessToken, refreshToken: newRefreshToken } = response.data;
    
    console.log('🔄 Refresh token response:', response.data);
    
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', newRefreshToken);
    
    return response.data;
  },

  // Logout
  logout: async (): Promise<void> => {
    try {
      await api.post('/auth/logout');
    } finally {
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
    }
  },

  // Get current user - Backend returns UserResponse directly
  getCurrentUser: async (): Promise<User> => {
    const response = await api.get('/users/me');
    console.log('👤 Get current user response:', response.data);
    
    // Backend returns UserResponse directly (id, fullName, email, emailVerified)
    // No wrapping in a "user" property
    return response.data;
  },
};

export default authService;