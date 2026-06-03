package com.gdg.backend.service;

import com.gdg.backend.dto.request.UserUpdateRequest;
import com.gdg.backend.dto.response.UserResponse;
import com.gdg.backend.entity.User;
import com.gdg.backend.repository.EntryRepository;
import com.gdg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final EntryRepository entryRepository;
    private final MlClientService mlClientService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateMyInfo(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            user.setUsername(request.getUsername());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);
        return UserResponse.from(user);
    }

    // 10.1 회원 탈퇴 — ML 데이터 삭제 후 BE DB CASCADE 삭제
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
        String email = user.getEmail();
        mlClientService.deleteUserData(String.valueOf(userId));
        userRepository.deleteById(userId);
        // 10.3 탈퇴 완료 후 30일 보관 안내 메일 — 트랜잭션 커밋 후 발송
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                emailService.sendAccountDeletionNotice(email);
            }
        });
    }

    // 10.4 90일 미사용 사용자 자동 삭제 — 매일 03:00 KST
    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteInactiveUsers() {
        LocalDateTime cutoff = LocalDateTime.now(ZoneId.of("Asia/Seoul")).minusDays(90);
        Set<Long> activeIds = Set.copyOf(entryRepository.findActiveUserIdsSince(cutoff));

        List<User> allUsers = userRepository.findAll();
        allUsers.stream()
                .filter(u -> !activeIds.contains(u.getUserId()))
                .filter(u -> u.getCreatedAt() != null
                        && u.getCreatedAt().isBefore(cutoff))
                .forEach(u -> {
                    try {
                        mlClientService.deleteUserData(String.valueOf(u.getUserId()));
                        userRepository.delete(u);
                    } catch (Exception e) {
                        // 한 명 실패해도 계속 진행
                    }
                });
    }
}
