package com.gdg.backend.service;

import com.gdg.backend.entity.User;
import com.gdg.backend.repository.UserRepository;
import com.gdg.backend.util.JwtUtil;
import com.gdg.backend.dto.response.AuthResponse;
import com.gdg.backend.dto.request.LoginRequest;
import com.gdg.backend.dto.request.SignupRequest;
import com.gdg.backend.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new AuthException("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다.");
        }

        User user = User.of(request.getEmail(), request.getUsername(), passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("USER_NOT_FOUND", "존재하지 않는 이메일입니다."));

        if (user.getPasswordHash() == null) {
            throw new AuthException("OAUTH_USER", "소셜 로그인 계정입니다. 비밀번호 로그인을 사용할 수 없습니다.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("INVALID_PASSWORD", "비밀번호가 틀렸습니다.");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token);
    }
}
