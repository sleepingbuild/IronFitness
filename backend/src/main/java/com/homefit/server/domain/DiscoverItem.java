package com.homefit.server.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "discover_items")
public class DiscoverItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title_vi", nullable = false, length = 200)
    private String titleVi;

    @Column(name = "title_en", length = 200)
    private String titleEn;

    @Column(name = "title_ko", length = 200)
    private String titleKo;

    @Column(name = "title_ja", length = 200)
    private String titleJa;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "category", nullable = false, length = 50)
    private String category = "Mẹo tập luyện";

    @Column(name = "cover_url", nullable = false, length = 500)
    private String coverUrl;

    @Column(name = "action_url", length = 500)
    private String actionUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void onTouch() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitleVi() { return titleVi; }
    public void setTitleVi(String titleVi) { this.titleVi = titleVi; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }

    public String getTitleKo() { return titleKo; }
    public void setTitleKo(String titleKo) { this.titleKo = titleKo; }

    public String getTitleJa() { return titleJa; }
    public void setTitleJa(String titleJa) { this.titleJa = titleJa; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @JsonProperty("summary")
    public String getSummary() { return description; }

    @JsonProperty("content")
    public String getContent() { return description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    @JsonProperty("coverImageUrl")
    public String getCoverImageUrl() { return coverUrl; }

    @JsonProperty("mediaUrl")
    public String getMediaUrl() { return coverUrl; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    @JsonProperty("deepLink")
    public String getDeepLink() { return actionUrl; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getUpdatedAt() { return updatedAt; }
}
