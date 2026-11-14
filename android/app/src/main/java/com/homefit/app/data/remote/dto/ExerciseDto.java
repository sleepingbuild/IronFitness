package com.homefit.app.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ExerciseDto {
    @SerializedName("id")
    public long id;

    @SerializedName(value = "categoryId", alternate = {"category_id"})
    public long categoryId;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName(value = "mediaUrl", alternate = {"media_url", "mediaURL"})
    public String mediaUrl;

    @SerializedName(value = "mediaType", alternate = {"media_type"})
    public String mediaType;

    @SerializedName(value = "durationSec", alternate = {"duration_sec"})
    public Integer durationSec;

    @SerializedName("reps")
    public Integer reps;

    @SerializedName(value = "restSec", alternate = {"rest_sec"})
    public int restSec;

    @SerializedName(value = "sortOrder", alternate = {"sort_order"})
    public int sortOrder;

    @SerializedName(value = "isActive", alternate = {"is_active", "active"})
    public Boolean isActive;

    @SerializedName(value = "updatedAt", alternate = {"updated_at"})
    public String updatedAt;
}
