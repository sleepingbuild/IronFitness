package com.homefit.server.admin;

import com.homefit.server.service.AdminStatsService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

    private final AdminStatsService statsService;

    public AdminDashboardController(AdminStatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping({"/admin", "/admin/", "/admin/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("stats", statsService.stats());
        return "admin/dashboard";
    }
}
