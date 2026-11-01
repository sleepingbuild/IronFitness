package com.homefit.server.service;

import com.homefit.server.admin.CategoryForm;
import com.homefit.server.domain.Category;
import com.homefit.server.repository.CategoryRepository;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Quản lý nhóm cơ trên trang quản trị. Xóa là xóa mềm (ẩn) để không làm hỏng lịch sử. */
@Service
public class AdminCategoryService {

    /** Dòng hiển thị trong bảng, thời gian đã đổi sang giờ Việt Nam. */
    public record Row(Long id, String name, String slug, int sortOrder, boolean active, String updatedAtText) {
    }

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final CategoryRepository categories;

    public AdminCategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<Row> listAll() {
        return categories.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(c -> new Row(c.getId(), c.getName(), c.getSlug(), c.getSortOrder(), c.isActive(),
                        c.getUpdatedAt() == null ? "" : DISPLAY.format(c.getUpdatedAt())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryForm formFor(Long id) {
        return CategoryForm.from(find(id));
    }

    @Transactional(readOnly = true)
    public boolean slugTaken(String slug, Long excludeId) {
        if (slug == null) {
            return false;
        }
        return excludeId == null
                ? categories.existsBySlug(slug)
                : categories.existsBySlugAndIdNot(slug, excludeId);
    }

    @Transactional
    public void create(CategoryForm form) {
        Category category = new Category();
        apply(category, form);
        categories.save(category);
    }

    @Transactional
    public void update(Long id, CategoryForm form) {
        Category category = find(id);
        apply(category, form);
        categories.save(category);
    }

    /** Ẩn hoặc hiện lại. updatedAt đổi nên app sẽ nhận thay đổi ở lần đồng bộ sau. */
    @Transactional
    public void toggle(Long id) {
        Category category = find(id);
        category.setActive(!category.isActive());
        categories.save(category);
    }

    private Category find(Long id) {
        return categories.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy nhóm cơ"));
    }

    private static void apply(Category category, CategoryForm form) {
        category.setName(form.getName().trim());
        category.setSlug(form.getSlug().trim());
        String icon = form.getIconUrl();
        category.setIconUrl(icon == null || icon.isBlank() ? null : icon.trim());
        category.setSortOrder(form.getSortOrder());
        category.setActive(form.isActive());
    }
}
