package com.homefit.server.service;

import com.homefit.server.domain.Role;
import com.homefit.server.domain.User;
import com.homefit.server.repository.UserRepository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Danh sách người dùng và bật/tắt Premium (Premium không thấy quảng cáo). */
@Service
public class AdminUserService {

    public record Row(Long id, String email, String displayName, boolean premium, String createdAtText) {
    }

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final UserRepository users;

    public AdminUserService(UserRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Row> listUsers() {
        return users.findTop200ByOrderByIdDesc().stream()
                .map(u -> new Row(u.getId(), u.getEmail(), u.getDisplayName(), u.isPremium(),
                        u.getCreatedAt() == null ? "" : DISPLAY.format(u.getCreatedAt())))
                .toList();
    }

    @Transactional
    public void togglePremium(Long id) {
        User user = users.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        user.setPremium(!user.isPremium());
        users.save(user);
    }
}
