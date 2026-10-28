package com.homefit.app.data.remote.dto;

public class CategoryDto {
    public long id;
    public String name;
    public String slug;
    public String iconUrl;
    public int sortOrder;
    /** false nghĩa là danh mục đã bị tắt: app phải xóa khỏi Room. */
    public Boolean isActive;
    /** ISO-8601 UTC. */
    public String updatedAt;
}
