package com.gdg.backend.controller;

import com.gdg.backend.dto.request.UserUpdateRequest;
import com.gdg.backend.dto.response.UserResponse;
import com.gdg.backend.entity.User;
import com.gdg.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "유저 프로필 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "로그인한 유저의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getMyInfo(user.getUserId()));
    }

    @Operation(summary = "내 정보 수정", description = "이름 또는 비밀번호를 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateMyInfo(
            @AuthenticationPrincipal User user,
            @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateMyInfo(user.getUserId(), request));
    }
}
