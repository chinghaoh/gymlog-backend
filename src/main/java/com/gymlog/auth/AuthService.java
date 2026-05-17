package com.gymlog.auth;

import com.gymlog.common.AppException;
import com.gymlog.user.Role;
import com.gymlog.user.User;
import com.gymlog.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import com.gymlog.email.EmailService;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;


    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(
                    HttpStatus.CONFLICT,
                    "Email already exists: " + request.getEmail());
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .isVerified(false)
                .verificationToken(verificationToken)
                .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
                .build();

        User saved = userRepository.save(user);

        try {
            log.info("Sending verification email with token: {}", verificationToken);
            log.info("Saved user verification token: {}", saved.getVerificationToken());
            emailService.sendVerificationEmail(saved.getEmail(), verificationToken);
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send verification email. Please try again.");
        }

        log.info("New user registered: {}", saved.getEmail());

        String token = jwtService.generateToken(
                saved.getEmail(),
                saved.getRole().name());

        return new AuthResponse(
                token,
                saved.getEmail(),
                saved.getRole().name(),
                saved.getId());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        if (!user.getIsVerified()) {
            throw new AppException(HttpStatus.FORBIDDEN, "Please verify your email before logging in.");
        }

        log.info("User logged in: {}", user.getEmail());

        String token = jwtService.generateToken(
                user.getEmail(),
                user.getRole().name());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole().name(),
                user.getId());
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new AppException(
                        HttpStatus.BAD_REQUEST, "Invalid verification token"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new AppException(HttpStatus.BAD_REQUEST, "Verification token has expired");
        }

        user.setIsVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }
}