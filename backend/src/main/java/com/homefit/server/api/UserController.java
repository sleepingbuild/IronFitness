package com.homefit.server.api;

import com.homefit.server.api.dto.UserResponse;
import com.homefit.server.service.UserService;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/{id}/upgrade-pro")
    public UserResponse upgradeProPath(@PathVariable Long id) {
        return userService.upgradePro(id);
    }
}
