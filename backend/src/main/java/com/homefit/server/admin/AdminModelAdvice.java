package com.homefit.server.admin;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Đưa email quản trị viên đang đăng nhập vào mọi trang /admin (thanh điều hướng). */
@ControllerAdvice(basePackages = "com.homefit.server.admin")
public class AdminModelAdvice {

    @ModelAttribute("adminEmail")
    public String adminEmail(Principal principal) {
        return principal == null ? null : principal.getName();
    }
}
