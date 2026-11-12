package com.homefit.server.admin;

import com.homefit.server.service.AdminUserService;
import com.homefit.server.service.AuditLoggerService;

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
    private final AuditLoggerService auditLoggerService;

    public AdminUserController(AdminUserService service, AuditLoggerService auditLoggerService) {
        this.service = service;
        this.auditLoggerService = auditLoggerService;
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
        auditLoggerService.log("admin@homefit.com", "GRANT_PRO", "User", String.valueOf(id), "Đã chuyển đổi trạng thái gói PRO cho người dùng #" + id);
        redirect.addFlashAttribute("message", "Đã đổi trạng thái Premium thành công.");
        return "redirect:/admin/users";
    }
}
