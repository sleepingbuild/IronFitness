package com.homefit.server.security;

import com.homefit.server.domain.Role;
import com.homefit.server.domain.User;
import com.homefit.server.repository.UserRepository;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Đăng nhập trang /admin: chỉ tài khoản có vai trò ADMIN và có mật khẩu mới đăng nhập được. */
@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public AdminUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String email = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        User user = users.findByEmail(email)
                .filter(u -> u.getRole() == Role.ADMIN && u.getPasswordHash() != null)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy quản trị viên"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles("ADMIN")
                .build();
    }
}
