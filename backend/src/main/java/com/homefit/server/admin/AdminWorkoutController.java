package com.homefit.server.admin;

import com.homefit.server.domain.Workout;
import com.homefit.server.repository.ExerciseRepository;
import com.homefit.server.repository.WorkoutRepository;
import com.homefit.server.service.AuditLoggerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/workouts")
public class AdminWorkoutController {

    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final AuditLoggerService auditLoggerService;

    public AdminWorkoutController(WorkoutRepository workoutRepository,
                                  ExerciseRepository exerciseRepository,
                                  AuditLoggerService auditLoggerService) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.auditLoggerService = auditLoggerService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeMenu", "workouts");
        model.addAttribute("workouts", workoutRepository.findAllByOrderBySortOrderAsc());
        return "admin/workouts/list";
    }

    @GetMapping({"/new", "/create"})
    public String newForm(Model model) {
        model.addAttribute("activeMenu", "workouts");
        model.addAttribute("workout", new Workout());
        model.addAttribute("exercises", exerciseRepository.findAll());
        return "admin/workouts/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("activeMenu", "workouts");
        model.addAttribute("workout", workoutRepository.findById(id).orElseThrow());
        model.addAttribute("exercises", exerciseRepository.findAll());
        return "admin/workouts/form";
    }

    @PostMapping
    public String save(@ModelAttribute Workout workout, RedirectAttributes redirect) {
        boolean isNew = workout.getId() == null;
        if (!isNew) {
            workoutRepository.findById(workout.getId()).ifPresent(existing -> {
                existing.setTitleVi(workout.getTitleVi());
                existing.setType(workout.getType());
                existing.setPrimaryMuscle(workout.getPrimaryMuscle());
                existing.setLevel(workout.getLevel());
                existing.setGoal(workout.getGoal());
                existing.setWorkSec(workout.getWorkSec());
                existing.setRestSec(workout.getRestSec());
                existing.setStatus(workout.getStatus());
                existing.setCoverImageUrl(workout.getCoverImageUrl());
                workoutRepository.save(existing);
            });
            auditLoggerService.log("admin@homefit.com", "UPDATE", "Workout", String.valueOf(workout.getId()), "Cập nhật buổi tập " + workout.getTitleVi());
            redirect.addFlashAttribute("message", "Đã cập nhật buổi tập thành công.");
        } else {
            Workout saved = workoutRepository.save(workout);
            auditLoggerService.log("admin@homefit.com", "CREATE", "Workout", String.valueOf(saved.getId()), "Tạo mới buổi tập " + saved.getTitleVi());
            redirect.addFlashAttribute("message", "Đã thêm mới buổi tập thành công.");
        }
        return "redirect:/admin/workouts";
    }

    @PostMapping("/upload-cover")
    @ResponseBody
    public Map<String, String> uploadCover(@RequestParam("file") MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("status", "ERROR");
            result.put("message", "Tệp tin rỗng.");
            return result;
        }
        try {
            java.io.File uploadsDir = new java.io.File("uploads/workouts");
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".png";
            String filename = "wo_" + System.currentTimeMillis() + ext;
            java.io.File dest = new java.io.File(uploadsDir, filename);
            file.transferTo(dest);

            String fileUrl = "/uploads/workouts/" + filename;
            result.put("status", "OK");
            result.put("url", fileUrl);
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        workoutRepository.deleteById(id);
        auditLoggerService.log("admin@homefit.com", "DELETE", "Workout", String.valueOf(id), "Xoá buổi tập #" + id);
        redirect.addFlashAttribute("message", "Đã xoá buổi tập.");
        return "redirect:/admin/workouts";
    }

    @GetMapping(value = "/export", produces = "text/csv; charset=UTF-8")
    @ResponseBody
    public org.springframework.http.ResponseEntity<String> exportCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("id,type,titleVi,primaryMuscle,level,goal,workSec,restSec,rounds,equipment,coverImageUrl,sortOrder,status\n");
        for (Workout w : workoutRepository.findAllByOrderBySortOrderAsc()) {
            sb.append(w.getId()).append(",");
            sb.append(escapeCsv(w.getType())).append(",");
            sb.append(escapeCsv(w.getTitleVi())).append(",");
            sb.append(escapeCsv(w.getPrimaryMuscle())).append(",");
            sb.append(escapeCsv(w.getLevel())).append(",");
            sb.append(escapeCsv(w.getGoal())).append(",");
            sb.append(w.getWorkSec()).append(",");
            sb.append(w.getRestSec()).append(",");
            sb.append(w.getRounds()).append(",");
            sb.append(escapeCsv(w.getEquipment())).append(",");
            sb.append(escapeCsv(w.getCoverImageUrl())).append(",");
            sb.append(w.getSortOrder()).append(",");
            sb.append(escapeCsv(w.getStatus())).append("\n");
        }
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=workouts.csv")
                .body(sb.toString());
    }

    @PostMapping("/import")
    public String importCsv(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) {
        if (file.isEmpty()) {
            redirect.addFlashAttribute("errorMessage", "Vui lòng chọn file CSV.");
            return "redirect:/admin/workouts";
        }
        try {
            int imported = 0;
            try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                boolean firstLine = true;
                while ((line = reader.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        continue;
                    }
                    if (line.trim().isEmpty()) continue;
                    java.util.List<String> tokens = parseCsvLine(line);
                    if (tokens.size() < 3) continue;

                    Long id = parseLong(tokens.get(0));
                    String type = tokens.size() > 1 ? tokens.get(1).trim() : "body_part";
                    String titleVi = tokens.size() > 2 ? tokens.get(2).trim() : "";
                    String primaryMuscle = tokens.size() > 3 ? tokens.get(3).trim() : "Toàn thân";
                    String level = tokens.size() > 4 ? tokens.get(4).trim() : "easy";
                    String goal = tokens.size() > 5 ? tokens.get(5).trim() : "";
                    Integer workSec = tokens.size() > 6 ? parseInt(tokens.get(6)) : 30;
                    Integer restSec = tokens.size() > 7 ? parseInt(tokens.get(7)) : 15;
                    Integer rounds = tokens.size() > 8 ? parseInt(tokens.get(8)) : 1;
                    String equipment = tokens.size() > 9 ? tokens.get(9).trim() : "none";
                    String coverImageUrl = tokens.size() > 10 ? tokens.get(10).trim() : "";
                    Integer sortOrder = tokens.size() > 11 ? parseInt(tokens.get(11)) : 0;
                    String status = tokens.size() > 12 ? tokens.get(12).trim() : "PUBLISHED";

                    Workout w = null;
                    if (id != null) {
                        w = workoutRepository.findById(id).orElse(null);
                    }
                    if (w == null && !titleVi.isEmpty()) {
                        w = workoutRepository.findFirstByTitleVi(titleVi).orElseGet(Workout::new);
                    }
                    if (w == null) {
                        w = new Workout();
                    }

                    if (!type.isEmpty()) w.setType(type);
                    if (!titleVi.isEmpty()) w.setTitleVi(titleVi);
                    if (!primaryMuscle.isEmpty()) w.setPrimaryMuscle(primaryMuscle);
                    if (!level.isEmpty()) w.setLevel(level);
                    w.setGoal(goal);
                    if (workSec != null) w.setWorkSec(workSec);
                    if (restSec != null) w.setRestSec(restSec);
                    if (rounds != null) w.setRounds(rounds);
                    if (!equipment.isEmpty()) w.setEquipment(equipment);
                    if (!coverImageUrl.isEmpty()) w.setCoverImageUrl(coverImageUrl);
                    if (sortOrder != null) w.setSortOrder(sortOrder);
                    if (!status.isEmpty()) w.setStatus(status);

                    workoutRepository.save(w);
                    imported++;
                }
            }
            redirect.addFlashAttribute("message", "Nhập thành công " + imported + " buổi tập.");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi nhập CSV: " + e.getMessage());
        }
        return "redirect:/admin/workouts";
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private java.util.List<String> parseCsvLine(String line) {
        java.util.List<String> result = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result;
    }

    private Long parseLong(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try { return Long.parseLong(val.trim()); } catch (Exception e) { return null; }
    }

    private Integer parseInt(String val) {
        if (val == null || val.trim().isEmpty()) return null;
        try { return Integer.parseInt(val.trim()); } catch (Exception e) { return null; }
    }
}
