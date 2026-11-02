package com.homefit.server.admin;

import com.homefit.server.service.AdminUserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Danh sách người dùng và nút bật/tắt Premium. */
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("rows", service.listUsers());
        return "admin/users/list";
    }

    @PostMapping("/{id}/premium")
    public String togglePremium(@PathVariable Long id, RedirectAttributes redirect) {
        service.togglePremium(id);
        redirect.addFlashAttribute("message", "Đã đổi trạng thái Premium.");
        return "redirect:/admin/users";
    }
}
