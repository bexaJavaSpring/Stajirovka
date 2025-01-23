package com.example.stajirovka.service;

import com.example.stajirovka.util.JwtUtil;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class JwtTokenService {
    private final JwtUtil jwtUtil;
    private final String tokenSecret;

    public JwtTokenService(JwtUtil jwtUtil, @Value("${jwt.token.secret}") String tokenSecret) {
        this.jwtUtil = jwtUtil;
        this.tokenSecret = tokenSecret;
    }

    public Boolean isValid(String token) {
        return jwtUtil.isTokenValid(token, getTokenSecret());
    }

    public String generateToken(@NonNull String subject) {
        return jwtUtil.jwt(new HashMap<>(), subject, getTokenSecret());
    }

    public String subject(String token) {
        return jwtUtil.getSubject(token, getTokenSecret());
    }

    private String getTokenSecret() {
        return tokenSecret;
    }
}
