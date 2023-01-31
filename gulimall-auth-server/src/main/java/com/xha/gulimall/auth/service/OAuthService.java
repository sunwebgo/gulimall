package com.xha.gulimall.auth.service;

import org.springframework.stereotype.Service;

@Service
public interface OAuthService {
    String giteeOAuth(String code);
}
