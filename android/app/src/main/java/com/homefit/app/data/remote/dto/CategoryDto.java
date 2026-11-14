package com.homefit.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CategoryDto {
    @SerializedName("id")
    public long id;

    @SerializedName("slug")
    public String slug;

    @SerializedName("name")
    public String name;

    @SerializedName(value = "iconUrl", alternate = {"icon_url"})
    public String iconUrl;

    @SerializedName(value = "sortOrder", alternate = {"sort_order"})
    public int sortOrder;

    @SerializedName(value = "isActive", alternate = {"is_active", "active"})
    public Boolean isActive = true;

    @SerializedName(value = "updatedAt", alternate = {"updated_at"})
    public String updatedAt;
}
