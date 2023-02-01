package com.xha.gulimall.auth.service;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
public interface OAuthService {
    String giteeOAuth(String code, HttpSession session);
}
