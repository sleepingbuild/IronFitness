package com.homefit.server.repository;

import com.homefit.server.domain.Role;
import com.homefit.server.domain.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /** Email luôn được chuẩn hóa chữ thường trước khi lưu và tìm. */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRole(Role role);

    Optional<User> findByGoogleId(String googleId);

    long countByRole(Role role);

    /** Trang quản trị: 200 người dùng thường mới nhất. */
    List<User> findTop200ByRoleOrderByIdDesc(Role role);
}
