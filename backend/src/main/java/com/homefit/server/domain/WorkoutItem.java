package com.homefit.server.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "workout_items")
public class WorkoutItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_id", nullable = false)
    private Workout workout;

    @Column(name = "block", nullable = false, length = 10)
    private String block; // A (warmup), B (main), C (cooldown)

    @Column(name = "position", nullable = false)
    private int position = 1;

    @Column(name = "exercise_id", nullable = false)
    private Long exerciseId;

    @Column(name = "round_num", nullable = false)
    private int roundNum = 1;

    @Column(name = "measure", nullable = false, length = 20)
    private String measure; // time, reps

    @Column(name = "value", nullable = false)
    private int value;

    @Column(name = "rest_after_sec", nullable = false)
    private int restAfterSec = 15;

    public Long getId() { return id; }
    public Workout getWorkout() { return workout; }
    public void setWorkout(Workout workout) { this.workout = workout; }
    public String getBlock() { return block; }
    public void setBlock(String block) { this.block = block; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public Long getExerciseId() { return exerciseId; }
    public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
    public int getRoundNum() { return roundNum; }
    public void setRoundNum(int roundNum) { this.roundNum = roundNum; }
    public String getMeasure() { return measure; }
    public void setMeasure(String measure) { this.measure = measure; }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public int getRestAfterSec() { return restAfterSec; }
    public void setRestAfterSec(int restAfterSec) { this.restAfterSec = restAfterSec; }
}
