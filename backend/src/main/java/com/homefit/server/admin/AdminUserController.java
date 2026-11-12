package com.homefit.server.admin;

import com.homefit.server.service.AdminUserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Danh sách người dùng, xem chi tiết và nút bật/tắt Premium. */
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        var list = service.listUsers();
        model.addAttribute("users", list);
        model.addAttribute("rows", list);
        return "admin/users/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("detail", service.getUserDetail(id));
        return "admin/users/detail";
    }

    @PostMapping({"/{id}/premium", "/{id}/toggle-premium", "/{id}/toggle"})
    public String togglePremium(@PathVariable Long id, RedirectAttributes redirect) {
        service.togglePremium(id);
        redirect.addFlashAttribute("message", "Đã đổi trạng thái Premium.");
        return "redirect:/admin/users";
    }
}
