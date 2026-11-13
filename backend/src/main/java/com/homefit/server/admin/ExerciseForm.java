package com.homefit.server.admin;

import com.homefit.server.domain.Exercise;
import com.homefit.server.domain.MediaType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Biểu mẫu thêm/sửa bài tập. Quy tắc "đúng một trong thời gian hoặc số lần" được đảm bảo bằng cách
 * chọn một chế độ (mode) và nhập một giá trị (amount): máy chủ chỉ điền đúng một cột.
 */
public class ExerciseForm {

    /** Cách tính của bài: theo thời gian (giây) hoặc theo số lần. */
    public enum Mode { DURATION, REPS }

    @NotNull(message = "Vui lòng chọn nhóm cơ")
    private Long categoryId;

    @NotBlank(message = "Vui lòng nhập tên bài tập")
    @Size(max = 150, message = "Tên tối đa 150 ký tự")
    private String name = "";

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description = "";

    @NotBlank(message = "Vui lòng nhập địa chỉ ảnh hoặc video")
    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    @Pattern(regexp = "^$|^/\\S+|^https?://\\S+$", message = "Địa chỉ phải bắt đầu bằng /, http:// hoặc https://")
    private String mediaUrl = "";

    @NotNull(message = "Vui lòng chọn loại")
    private MediaType mediaType = MediaType.GIF;

    @NotNull(message = "Vui lòng chọn cách tính")
    private Mode mode = Mode.DURATION;

    @NotNull(message = "Vui lòng nhập giá trị")
    @Min(value = 1, message = "Giá trị phải từ 1 trở lên")
    @Max(value = 3600, message = "Giá trị tối đa 3600")
    private Integer amount = 30;

    @NotNull(message = "Vui lòng nhập thời gian nghỉ")
    @Min(value = 0, message = "Thời gian nghỉ không được âm")
    @Max(value = 600, message = "Thời gian nghỉ tối đa 600 giây")
    private Integer restSec = 15;

    @NotNull(message = "Vui lòng nhập thứ tự")
    @Min(value = 0, message = "Thứ tự không được âm")
    @Max(value = 9999, message = "Thứ tự tối đa 9999")
    private Integer sortOrder = 0;

    private boolean active = true;

    public static ExerciseForm from(Exercise e) {
        ExerciseForm f = new ExerciseForm();
        f.categoryId = e.getCategoryId();
        f.name = e.getName();
        f.description = e.getDescription() == null ? "" : e.getDescription();
        f.mediaUrl = e.getMediaUrl();
        f.mediaType = e.getMediaType();
        if (e.getDurationSec() != null) {
            f.mode = Mode.DURATION;
            f.amount = e.getDurationSec();
        } else {
            f.mode = Mode.REPS;
            f.amount = e.getReps();
        }
        f.restSec = e.getRestSec();
        f.sortOrder = e.getSortOrder();
        f.active = e.isActive();
        return f;
    }

    /** Giới hạn riêng theo chế độ (giây tối đa 3600, số lần tối đa 1000). Trả null nếu hợp lệ. */
    public String amountProblem() {
        if (amount == null || mode == null) {
            return null; // đã có lỗi @NotNull
        }
        if (mode == Mode.REPS && amount > 1000) {
            return "Số lần tối đa 1000";
        }
        return null;
    }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public MediaType getMediaType() { return mediaType; }
    public void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }

    public Mode getMode() { return mode; }
    public void setMode(Mode mode) { this.mode = mode; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public Integer getRestSec() { return restSec; }
    public void setRestSec(Integer restSec) { this.restSec = restSec; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
