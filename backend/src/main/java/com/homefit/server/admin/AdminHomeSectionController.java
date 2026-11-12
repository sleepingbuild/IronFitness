package com.homefit.server.admin;

import com.homefit.server.repository.HomeSectionRepository;
import com.homefit.server.service.AuditLoggerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/home-sections")
public class AdminHomeSectionController {

    private final HomeSectionRepository homeSectionRepository;
    private final AuditLoggerService auditLoggerService;

    public AdminHomeSectionController(HomeSectionRepository homeSectionRepository, AuditLoggerService auditLoggerService) {
        this.homeSectionRepository = homeSectionRepository;
        this.auditLoggerService = auditLoggerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeMenu", "home_sections");
        model.addAttribute("sections", homeSectionRepository.findAllByOrderBySortOrderAsc());
        return "admin/home_sections/list";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String titleVi,
                         @RequestParam int sortOrder,
                         RedirectAttributes redirect) {
        homeSectionRepository.findById(id).ifPresent(s -> {
            s.setTitleVi(titleVi.trim());
            s.setSortOrder(sortOrder);
            homeSectionRepository.save(s);
            auditLoggerService.log("admin@homefit.com", "UPDATE", "HomeSection", String.valueOf(id), "Cập nhật tên và thứ tự mục " + titleVi);
        });
        redirect.addFlashAttribute("message", "Đã lưu chỉnh sửa mục Trang chủ.");
        return "redirect:/admin/home-sections";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirect) {
        homeSectionRepository.findById(id).ifPresent(section -> {
            section.setEnabled(!section.isEnabled());
            homeSectionRepository.save(section);
            auditLoggerService.log("admin@homefit.com", "UPDATE", "HomeSection", String.valueOf(id), "Đổi trạng thái hiển thị mục " + section.getSectionKey());
        });
        redirect.addFlashAttribute("message", "Đã cập nhật trạng thái mục trang chủ.");
        return "redirect:/admin/home-sections";
    }
}
