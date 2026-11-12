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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        workoutRepository.deleteById(id);
        auditLoggerService.log("admin@homefit.com", "DELETE", "Workout", String.valueOf(id), "Xoá buổi tập #" + id);
        redirect.addFlashAttribute("message", "Đã xoá buổi tập.");
        return "redirect:/admin/workouts";
    }
}
