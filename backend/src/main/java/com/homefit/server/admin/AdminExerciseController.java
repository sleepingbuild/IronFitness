package com.homefit.server.admin;

import com.homefit.server.service.AdminExerciseService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/** Quản lý bài tập: danh sách (lọc theo nhóm cơ, sắp xếp), thêm, sửa, ẩn/hiện. */
@Controller
@RequestMapping("/admin/exercises")
public class AdminExerciseController {

    private static final String FORM_VIEW = "admin/exercises/form";
    private static final String LIST_URL = "redirect:/admin/exercises";

    private final AdminExerciseService service;

    public AdminExerciseController(AdminExerciseService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false, defaultValue = "sortOrder") String sortBy,
                       @RequestParam(required = false, defaultValue = "asc") String sortDir,
                       Model model) {
        var list = service.listRows(categoryId, sortBy, sortDir);
        model.addAttribute("exercises", list);
        model.addAttribute("rows", list);
        model.addAttribute("categories", service.categoryOptions());
        model.addAttribute("categoryOptions", service.categoryOptions());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("totalCount", list.size());
        return "admin/exercises/list";
    }

    @GetMapping({"/new", "/create"})
    public String newForm(@RequestParam(required = false) Long categoryId, Model model) {
        ExerciseForm form = new ExerciseForm();
        form.setCategoryId(categoryId);
        prepare(model, form, "Thêm bài tập", "/admin/exercises");
        return FORM_VIEW;
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") ExerciseForm form, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        checkExtra(form, result);
        if (result.hasErrors()) {
            fillView(model, "Thêm bài tập", "/admin/exercises");
            return FORM_VIEW;
        }
        service.create(form);
        redirect.addFlashAttribute("message", "Đã thêm bài tập.");
        return LIST_URL;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        prepare(model, service.formFor(id), "Sửa bài tập", "/admin/exercises/" + id);
        return FORM_VIEW;
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("form") ExerciseForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        checkExtra(form, result);
        if (result.hasErrors()) {
            fillView(model, "Sửa bài tập", "/admin/exercises/" + id);
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

    @GetMapping(value = "/export", produces = "text/csv; charset=UTF-8")
    @ResponseBody
    public org.springframework.http.ResponseEntity<String> exportCsv() {
        String csv = service.exportCsv();
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=exercises.csv")
                .body(csv);
    }

    @PostMapping("/import")
    public String importCsv(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) {
        if (file.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Vui lòng chọn file CSV.");
            return LIST_URL;
        }
        try {
            int imported = service.importCsv(file);
            redirect.addFlashAttribute("message", "Nhập thành công " + imported + " bài tập.");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi nhập CSV: " + e.getMessage());
        }
        return LIST_URL;
    }
    
    @GetMapping("/check-link")
    @ResponseBody
    public Map<String, String> checkLink(@RequestParam String url) {
        return service.checkMediaLink(url);
    }

    @PostMapping("/upload-media")
    @ResponseBody
    public Map<String, String> uploadMedia(@RequestParam("file") MultipartFile file) {
        Map<String, String> result = new java.util.HashMap<>();
        if (file.isEmpty()) {
            result.put("status", "ERROR");
            result.put("message", "Tệp tin rỗng.");
            return result;
        }
        try {
            java.io.File uploadsDir = new java.io.File("uploads/exercises");
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".gif";
            String filename = "ex_" + System.currentTimeMillis() + ext;
            java.io.File dest = new java.io.File(uploadsDir, filename);
            file.transferTo(dest);

            String fileUrl = "/uploads/exercises/" + filename;
            result.put("status", "OK");
            result.put("url", fileUrl);
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
        }
        return result;
    }

    private void prepare(Model model, ExerciseForm form, String title, String action) {
        model.addAttribute("form", form);
        fillView(model, title, action);
    }

    private void fillView(Model model, String title, String action) {
        model.addAttribute("title", title);
        model.addAttribute("formAction", action);
        model.addAttribute("categoryOptions", service.categoryOptions());
    }

    private void checkExtra(ExerciseForm form, BindingResult result) {
        String problem = form.amountProblem();
        if (problem != null && !result.hasFieldErrors("amount")) {
            result.rejectValue("amount", "range", problem);
        }
        if (form.getCategoryId() != null && !result.hasFieldErrors("categoryId")
                && !service.categoryExists(form.getCategoryId())) {
            result.rejectValue("categoryId", "missing", "Nhóm cơ không tồn tại");
        }
    }
}
