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
        AdminStatsService.Stats stats = statsService.stats();
        model.addAttribute("stats", stats);
        model.addAttribute("totalUsers", stats.users());
        model.addAttribute("totalPremiumUsers", statsService.countPremiumUsers());
        model.addAttribute("totalExercises", stats.exercises());
        model.addAttribute("totalSessions", stats.sessions());
        return "admin/dashboard";
    }
}
