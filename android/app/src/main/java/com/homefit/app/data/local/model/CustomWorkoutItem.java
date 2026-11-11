package com.homefit.app.data.local.model;

import java.util.List;

public class CustomWorkoutItem {
    public String exerciseId;
    public String exerciseName;
    public String target;
    public String muscleArea; // bung, nguc, tay, vai, back, leg, toan_than, khoi_dong, gian_co
    public String role;       // warmup, main, cooldown
    public int order;
    
    public Integer reps = null;    // Số rep (e.g. 12)
    public Integer sets = null;    // Số set/hiệp (e.g. 2)
    public Integer timeSec = null; // Thời gian tính theo giây (e.g. 30)

    public String measure = "reps"; // legacy support
    public int value = 10;          // legacy support
    public int restAfterSec = 20;
    public List<String> instructions;
    public String tips;

    public String getFormattedSubtitle() {
        StringBuilder sb = new StringBuilder();

        if (reps != null && reps > 0) {
            sb.append(reps).append(" reps");
        }
        if (timeSec != null && timeSec > 0) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(timeSec).append("s");
        }
        if (sets != null && sets > 1) {
            if (sb.length() > 0) sb.append(" • ");
            sb.append(sets).append(" sets");
        }

        if (sb.length() == 0) {
            if (reps == null && timeSec == null && sets == null) {
                return "Tự tập";
            }
            if ("time".equalsIgnoreCase(measure) && value > 0) {
                sb.append(value).append("s");
            } else if (value > 0) {
                sb.append("x").append(value);
            } else {
                return "Tự tập";
            }
        }

        return sb.toString();
    }
}
