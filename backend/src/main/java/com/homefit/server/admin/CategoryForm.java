package com.homefit.server.admin;

import com.homefit.server.domain.Category;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dữ liệu biểu mẫu thêm/sửa nhóm cơ (lớp thường có getter/setter để Thymeleaf liên kết). */
public class CategoryForm {

    @NotBlank(message = "Vui lòng nhập tên nhóm")
    @Size(max = 100, message = "Tên tối đa 100 ký tự")
    private String name = "";

    @NotBlank(message = "Vui lòng nhập slug")
    @Size(max = 100, message = "Slug tối đa 100 ký tự")
    @Pattern(regexp = "^[a-z0-9_]*$", message = "Slug chỉ gồm chữ thường không dấu, số và dấu gạch dưới")
    private String slug = "";

    @Size(max = 500, message = "Địa chỉ ảnh tối đa 500 ký tự")
    @Pattern(regexp = "^$|^https?://\\S+$", message = "Địa chỉ ảnh phải bắt đầu bằng http:// hoặc https://")
    private String iconUrl = "";

    @NotNull(message = "Vui lòng nhập thứ tự")
    @Min(value = 0, message = "Thứ tự không được âm")
    @Max(value = 9999, message = "Thứ tự tối đa 9999")
    private Integer sortOrder = 0;

    private boolean active = true;

    public static CategoryForm from(Category c) {
        CategoryForm f = new CategoryForm();
        f.name = c.getName();
        f.slug = c.getSlug();
        f.iconUrl = c.getIconUrl() == null ? "" : c.getIconUrl();
        f.sortOrder = c.getSortOrder();
        f.active = c.isActive();
        return f;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getIconUrl() { return iconUrl; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
