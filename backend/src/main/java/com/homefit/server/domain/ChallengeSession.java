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
@Table(name = "challenge_sessions")
public class ChallengeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private ChallengeProgram program;

    @Column(name = "week_num", nullable = false)
    private int weekNum;

    @Column(name = "day_index", nullable = false)
    private int dayIndex;

    @Column(name = "is_rest_day", nullable = false)
    private boolean restDay;

    @Column(name = "workout_id")
    private Long workoutId;

    public Long getId() { return id; }
    public ChallengeProgram getProgram() { return program; }
    public void setProgram(ChallengeProgram program) { this.program = program; }
    public int getWeekNum() { return weekNum; }
    public void setWeekNum(int weekNum) { this.weekNum = weekNum; }
    public int getDayIndex() { return dayIndex; }
    public void setDayIndex(int dayIndex) { this.dayIndex = dayIndex; }
    public boolean isRestDay() { return restDay; }
    public void setRestDay(boolean restDay) { this.restDay = restDay; }
    public Long getWorkoutId() { return workoutId; }
    public void setWorkoutId(Long workoutId) { this.workoutId = workoutId; }
}
