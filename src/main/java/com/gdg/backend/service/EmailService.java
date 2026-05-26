package com.gdg.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromAddress;

    // 7.4 주간 퀴즈 준비 알림
    public void sendWeeklyQuizReady(String toEmail, String weekId) {
        sendMail(toEmail,
                "[Step Back] 이번 주 자가진단 퀴즈가 준비됐어요",
                "안녕하세요!\n\n이번 주(" + weekId + ") 자가진단 퀴즈가 준비되었어요.\n\n앱에서 확인해보세요.");
    }

    // 10.3 회원 탈퇴 후 30일 데이터 보관 안내
    public void sendAccountDeletionNotice(String toEmail) {
        sendMail(toEmail,
                "[Step Back] 회원 탈퇴 완료 안내",
                "안녕하세요.\n\n회원 탈퇴가 완료되었습니다.\n\n개인정보는 관련 법령에 따라 30일간 보관 후 완전히 삭제됩니다.\n\n감사합니다.");
    }

    private void sendMail(String to, String subject, String text) {
        if (to == null || to.isBlank()) {
            log.warn("메일 발송 스킵 — 수신 주소 없음");
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            log.info("메일 발송 완료: to={}", to);
        } catch (MailException e) {
            log.error("메일 발송 실패: to={}", to, e);
        }
    }
}
