package com.vsblog.admin;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username:}")
    private String username;

    @Value("${app.admin.password:}")
    private String password;

    public AdminBootstrap(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initializeAdmin() {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return;
        }

        if (adminUserRepository.findByUsername(username).isEmpty()) {
            adminUserRepository.save(new AdminUser(username, passwordEncoder.encode(password)));
        }
    }
}
