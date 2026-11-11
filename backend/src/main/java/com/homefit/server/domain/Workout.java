package com.homefit.server.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "workouts")
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 50)
    private String type; // body_part, timed, goal, tabata, stretch, signature

    @Column(name = "title_vi", nullable = false, length = 200)
    private String titleVi;

    @Column(name = "title_en", length = 200)
    private String titleEn;

    @Column(name = "primary_muscle", nullable = false, length = 50)
    private String primaryMuscle;

    @Column(name = "level", nullable = false, length = 20)
    private String level; // easy, medium, hard

    @Column(name = "goal", length = 50)
    private String goal;

    @Column(name = "work_sec", nullable = false)
    private int workSec = 30;

    @Column(name = "rest_sec", nullable = false)
    private int restSec = 15;

    @Column(name = "round_rest_sec", nullable = false)
    private int roundRestSec = 30;

    @Column(name = "rounds", nullable = false)
    private int rounds = 1;

    @Column(name = "equipment", nullable = false, length = 50)
    private String equipment = "none";

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT"; // DRAFT, PENDING, PUBLISHED, ARCHIVED

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "workout", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<WorkoutItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitleVi() { return titleVi; }
    public void setTitleVi(String titleVi) { this.titleVi = titleVi; }
    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }
    public String getPrimaryMuscle() { return primaryMuscle; }
    public void setPrimaryMuscle(String primaryMuscle) { this.primaryMuscle = primaryMuscle; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }
    public int getWorkSec() { return workSec; }
    public void setWorkSec(int workSec) { this.workSec = workSec; }
    public int getRestSec() { return restSec; }
    public void setRestSec(int restSec) { this.restSec = restSec; }
    public int getRoundRestSec() { return roundRestSec; }
    public void setRoundRestSec(int roundRestSec) { this.roundRestSec = roundRestSec; }
    public int getRounds() { return rounds; }
    public void setRounds(int rounds) { this.rounds = rounds; }
    public String getEquipment() { return equipment; }
    public void setEquipment(String equipment) { this.equipment = equipment; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<WorkoutItem> getItems() { return items; }
    public void setItems(List<WorkoutItem> items) { this.items = items; }
}
