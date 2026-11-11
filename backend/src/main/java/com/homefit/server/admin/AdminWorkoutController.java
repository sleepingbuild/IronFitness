package com.homefit.server.admin;

import com.homefit.server.domain.Workout;
import com.homefit.server.repository.ExerciseRepository;
import com.homefit.server.repository.WorkoutRepository;
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

    public AdminWorkoutController(WorkoutRepository workoutRepository, ExerciseRepository exerciseRepository) {
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeMenu", "workouts");
        model.addAttribute("workouts", workoutRepository.findAllByOrderBySortOrderAsc());
        return "admin/workouts/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("activeMenu", "workouts");
        model.addAttribute("workout", new Workout());
        model.addAttribute("exercises", exerciseRepository.findAll());
        return "admin/workouts/form";
    }

    @PostMapping
    public String save(@ModelAttribute Workout workout, RedirectAttributes redirect) {
        workoutRepository.save(workout);
        redirect.addFlashAttribute("message", "Đã lưu buổi tập thành công.");
        return "redirect:/admin/workouts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        workoutRepository.deleteById(id);
        redirect.addFlashAttribute("message", "Đã xoá buổi tập.");
        return "redirect:/admin/workouts";
    }
}
