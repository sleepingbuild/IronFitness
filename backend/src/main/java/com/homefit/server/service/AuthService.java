package com.homefit.server.service;

import com.homefit.server.api.ApiException;
import com.homefit.server.api.dto.AuthResponse;
import com.homefit.server.api.dto.LoginRequest;
import com.homefit.server.api.dto.RefreshRequest;
import com.homefit.server.api.dto.RefreshResponse;
import com.homefit.server.api.dto.RegisterRequest;
import com.homefit.server.api.dto.UserResponse;
import com.homefit.server.domain.RefreshToken;
import com.homefit.server.domain.User;
import com.homefit.server.repository.RefreshTokenRepository;
import com.homefit.server.repository.UserRepository;
import com.homefit.server.security.TokenService;

import java.time.Instant;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Đăng ký, đăng nhập, làm mới token. Controller chỉ gọi lớp này. */
@Service
public class AuthService {

    private static final String BAD_CREDENTIALS = "Email hoặc mật khẩu không đúng.";
    private static final String BAD_REFRESH = "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.";

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens,
                       PasswordEncoder passwordEncoder, TokenService tokens) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.tokens = tokens;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (users.existsByEmail(email)) {
            throw ApiException.conflict("Email này đã được đăng ký.");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim());
        user = users.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmail(normalize(request.email())).orElse(null);
        if (user == null || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized(BAD_CREDENTIALS);
        }
        return buildAuthResponse(user);
    }

    /** Xoay vòng: refresh token cũ bị thu hồi, trả cặp token mới. */
    @Transactional
    public RefreshResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokens.findByTokenHash(TokenService.hash(request.refreshToken()))
                .orElseThrow(() -> ApiException.unauthorized(BAD_REFRESH));
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw ApiException.unauthorized(BAD_REFRESH);
        }
        User user = users.findById(stored.getUserId())
                .orElseThrow(() -> ApiException.unauthorized(BAD_REFRESH));
        stored.setRevoked(true);
        refreshTokens.save(stored);
        String newRefresh = issueRefreshToken(user.getId());
        return new RefreshResponse(tokens.createAccessToken(user), newRefresh, tokens.accessTokenSeconds());
    }

    private AuthResponse buildAuthResponse(User user) {
        String refresh = issueRefreshToken(user.getId());
        return new AuthResponse(tokens.createAccessToken(user), refresh,
                tokens.accessTokenSeconds(), UserResponse.from(user));
    }

    private String issueRefreshToken(Long userId) {
        String value = tokens.newRefreshTokenValue();
        RefreshToken entity = new RefreshToken();
        entity.setUserId(userId);
        entity.setTokenHash(TokenService.hash(value));
        entity.setExpiresAt(tokens.refreshTokenExpiry());
        refreshTokens.save(entity);
        return value;
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
