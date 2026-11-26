package com.homefit.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class DiscoverDto {
    public Long id;
    public String titleVi;
    public String titleEn;
    public String titleKo;
    public String titleJa;
    
    public String description;
    @SerializedName(value = "summary", alternate = {"content"})
    public String summary;

    public String category;
    
    public String coverUrl;
    @SerializedName(value = "coverImageUrl", alternate = {"mediaUrl"})
    public String coverImageUrl;

    public String actionUrl;
    @SerializedName(value = "deepLink")
    public String deepLink;

    public int sortOrder;
    public boolean active = true;

    public String getTitle(String lang) {
        if ("en".equalsIgnoreCase(lang) && titleEn != null && !titleEn.trim().isEmpty()) {
            return titleEn;
        }
        if ("ko".equalsIgnoreCase(lang) && titleKo != null && !titleKo.trim().isEmpty()) {
            return titleKo;
        }
        if ("ja".equalsIgnoreCase(lang) && titleJa != null && !titleJa.trim().isEmpty()) {
            return titleJa;
        }
        if (titleVi != null && !titleVi.trim().isEmpty()) {
            return titleVi;
        }
        if (titleEn != null && !titleEn.trim().isEmpty()) {
            return titleEn;
        }
        return "";
    }

    public String getDescriptionText() {
        if (description != null && !description.trim().isEmpty()) return description;
        if (summary != null && !summary.trim().isEmpty()) return summary;
        return "";
    }

    public String getCover() {
        if (coverUrl != null && !coverUrl.trim().isEmpty()) return coverUrl;
        if (coverImageUrl != null && !coverImageUrl.trim().isEmpty()) return coverImageUrl;
        return "";
    }

    public String getAction() {
        if (actionUrl != null && !actionUrl.trim().isEmpty()) return actionUrl;
        if (deepLink != null && !deepLink.trim().isEmpty()) return deepLink;
        return "";
    }
}
