package com.homefit.server.service;

import com.homefit.server.config.AdminProperties;
import com.homefit.server.domain.Role;
import com.homefit.server.domain.User;
import com.homefit.server.repository.UserRepository;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Khởi tạo hoặc cập nhật tài khoản admin từ cấu hình homefit.admin.* khi ứng dụng khởi động.
 */
@Component
public class AdminInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AdminProperties props;

    public AdminInitializer(UserRepository users, PasswordEncoder passwordEncoder, AdminProperties props) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = props.email();
        String password = props.password();
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.warn("Chưa cấu hình tài khoản admin (homefit.admin.email / password).");
            return;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        User admin = users.findByEmail(normalized).orElseGet(() -> {
            User u = new User();
            u.setEmail(normalized);
            u.setDisplayName("Quản trị viên");
            return u;
        });
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        users.save(admin);
        log.info("Đã đồng bộ tài khoản admin thành công: {}", normalized);
    }
}
