package com.foodapp.service;

import com.foodapp.dto.AuthRequest;
import com.foodapp.dto.AuthResponse;
import com.foodapp.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
}
