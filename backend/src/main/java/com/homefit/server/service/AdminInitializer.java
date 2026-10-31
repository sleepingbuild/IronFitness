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
 * Tạo tài khoản admin đầu tiên từ cấu hình homefit.admin.* khi khởi động,
 * chỉ khi chưa có admin nào. Mật khẩu không nằm trong migration hay Git.
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
        if (users.existsByRole(Role.ADMIN)) {
            return;
        }
        String email = props.email();
        String password = props.password();
        if (email == null || email.isBlank() || password == null || password.length() < 8
                || password.startsWith("CHANGE_ME")) {
            log.warn("Chưa tạo tài khoản admin: đặt homefit.admin.email và homefit.admin.password "
                    + "(từ 8 ký tự, không dùng giá trị mẫu) trong application-local.yml.");
            return;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(normalized)) {
            log.warn("Email admin {} đã tồn tại với vai trò thường, bỏ qua việc tạo admin.", normalized);
            return;
        }
        User admin = new User();
        admin.setEmail(normalized);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setDisplayName("Quản trị viên");
        admin.setRole(Role.ADMIN);
        users.save(admin);
        log.info("Đã tạo tài khoản admin đầu tiên: {}", normalized);
    }
}
