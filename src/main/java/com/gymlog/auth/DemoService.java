package com.gymlog.auth;

import com.gymlog.user.Role;
import com.gymlog.user.User;
import com.gymlog.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse createDemoUser() {
        String email;
        do {
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            email = "demo_" + uniqueId + "@gymlog.demo";
        } while (userRepository.existsByEmail(email));

        User user = User.builder()
                .name("Demo User")
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(Role.USER)
                .isVerified(true)
                .isDemo(true)
                .build();

        User saved = userRepository.save(user);

        log.info("Demo user created: {}", email);

        String token = jwtService.generateToken(saved.getEmail(), saved.getRole().name());
        return new AuthResponse(token, saved.getEmail(), saved.getRole().name(), saved.getId());
    }
}