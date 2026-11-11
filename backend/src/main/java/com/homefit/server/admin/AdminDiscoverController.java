package com.homefit.server.admin;

import com.homefit.server.domain.DiscoverItem;
import com.homefit.server.repository.DiscoverItemRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/discover")
public class AdminDiscoverController {

    private final DiscoverItemRepository discoverItemRepository;

    public AdminDiscoverController(DiscoverItemRepository discoverItemRepository) {
        this.discoverItemRepository = discoverItemRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeMenu", "discover");
        model.addAttribute("items", discoverItemRepository.findAllByOrderBySortOrderAsc());
        return "admin/discover/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("activeMenu", "discover");
        model.addAttribute("item", new DiscoverItem());
        return "admin/discover/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "discover");
        model.addAttribute("item", discoverItemRepository.findById(id).orElseThrow());
        return "admin/discover/form";
    }

    @PostMapping
    public String save(@ModelAttribute DiscoverItem item, RedirectAttributes redirect) {
        if (item.getId() != null) {
            discoverItemRepository.findById(item.getId()).ifPresent(existing -> {
                existing.setTitleVi(item.getTitleVi());
                existing.setCategory(item.getCategory());
                existing.setSortOrder(item.getSortOrder());
                existing.setCoverUrl(item.getCoverUrl());
                existing.setDescription(item.getDescription());
                existing.setActive(item.isActive());
                discoverItemRepository.save(existing);
            });
            redirect.addFlashAttribute("message", "Đã cập nhật mục Khám phá thành công.");
        } else {
            discoverItemRepository.save(item);
            redirect.addFlashAttribute("message", "Đã thêm mục Khám phá mới.");
        }
        return "redirect:/admin/discover";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirect) {
        discoverItemRepository.findById(id).ifPresent(item -> {
            item.setActive(!item.isActive());
            discoverItemRepository.save(item);
        });
        redirect.addFlashAttribute("message", "Đã đổi trạng thái hiển thị mục Khám phá.");
        return "redirect:/admin/discover";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        discoverItemRepository.deleteById(id);
        redirect.addFlashAttribute("message", "Đã xoá mục Khám phá.");
        return "redirect:/admin/discover";
    }
}
