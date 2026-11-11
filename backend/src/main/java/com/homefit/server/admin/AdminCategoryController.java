package com.homefit.server.admin;

import com.homefit.server.service.AdminCategoryService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Quản lý nhóm cơ: danh sách, thêm, sửa, ẩn/hiện. */
@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private static final String FORM_VIEW = "admin/categories/form";
    private static final String LIST_URL = "redirect:/admin/categories";

    private final AdminCategoryService service;

    public AdminCategoryController(AdminCategoryService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model) {
        var list = service.listAll();
        model.addAttribute("categories", list);
        model.addAttribute("rows", list);
        return "admin/categories/list";
    }

    @GetMapping({"/new", "/create"})
    public String newForm(Model model) {
        prepareForm(model, new CategoryForm(), "Thêm nhóm cơ", "/admin/categories");
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") CategoryForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        rejectDuplicateSlug(form, null, result);
        if (result.hasErrors()) {
            model.addAttribute("title", "Thêm nhóm cơ");
            model.addAttribute("formAction", "/admin/categories");
            return FORM_VIEW;
        }
        service.create(form);
        redirect.addFlashAttribute("message", "Đã thêm nhóm cơ.");
        return LIST_URL;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        prepareForm(model, service.formFor(id), "Sửa nhóm cơ", "/admin/categories/" + id);
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") CategoryForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        rejectDuplicateSlug(form, id, result);
        if (result.hasErrors()) {
            model.addAttribute("title", "Sửa nhóm cơ");
            model.addAttribute("formAction", "/admin/categories/" + id);
            return FORM_VIEW;
        }
        service.update(id, form);
        redirect.addFlashAttribute("message", "Đã lưu thay đổi.");
        return LIST_URL;
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes redirect) {
        service.toggle(id);
        redirect.addFlashAttribute("message", "Đã đổi trạng thái hiển thị.");
        return LIST_URL;
    }

    private void prepareForm(Model model, CategoryForm form, String title, String action) {
        model.addAttribute("form", form);
        model.addAttribute("title", title);
        model.addAttribute("formAction", action);
    }

    private void rejectDuplicateSlug(CategoryForm form, Long excludeId, BindingResult result) {
        if (!result.hasFieldErrors("slug") && service.slugTaken(form.getSlug().trim(), excludeId)) {
            result.rejectValue("slug", "duplicate", "Slug này đã được dùng");
        }
    }
}
