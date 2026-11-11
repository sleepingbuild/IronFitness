package com.homefit.server.admin;

import com.homefit.server.repository.AuditLogRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/audit-logs")
public class AdminAuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AdminAuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeMenu", "audit_logs");
        model.addAttribute("logs", auditLogRepository.findTop100ByOrderByCreatedAtDesc());
        return "admin/audit_logs/list";
    }
}
