package com.homefit.server.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "content_releases")
public class ContentRelease {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "version", nullable = false, unique = true)
    private int version;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "manifest_json", columnDefinition = "LONGTEXT")
    private String manifestJson;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PUBLISHED";

    @Column(name = "created_by", nullable = false, length = 100)
    private String createdBy;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;

    @PrePersist
    void onCreate() {
        publishedAt = Instant.now();
    }

    public Long getId() { return id; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getManifestJson() { return manifestJson; }
    public void setManifestJson(String manifestJson) { this.manifestJson = manifestJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public Instant getPublishedAt() { return publishedAt; }
}
