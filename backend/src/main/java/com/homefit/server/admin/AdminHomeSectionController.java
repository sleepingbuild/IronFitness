package com.homefit.server.admin;

import com.homefit.server.repository.HomeSectionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/home-sections")
public class AdminHomeSectionController {

    private final HomeSectionRepository homeSectionRepository;

    public AdminHomeSectionController(HomeSectionRepository homeSectionRepository) {
        this.homeSectionRepository = homeSectionRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeMenu", "home_sections");
        model.addAttribute("sections", homeSectionRepository.findAllByOrderBySortOrderAsc());
        return "admin/home_sections/list";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirect) {
        homeSectionRepository.findById(id).ifPresent(section -> {
            section.setEnabled(!section.isEnabled());
            homeSectionRepository.save(section);
        });
        redirect.addFlashAttribute("message", "Đã cập nhật trạng thái mục trang chủ.");
        return "redirect:/admin/home-sections";
    }
}
