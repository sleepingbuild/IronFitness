package com.homefit.server.admin;

import com.homefit.server.domain.ChallengeProgram;
import com.homefit.server.domain.ChallengeSession;
import com.homefit.server.repository.ChallengeProgramRepository;
import com.homefit.server.repository.WorkoutRepository;
import com.homefit.server.service.AuditLoggerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/challenges")
public class AdminChallengeController {

    private final ChallengeProgramRepository challengeProgramRepository;
    private final WorkoutRepository workoutRepository;
    private final AuditLoggerService auditLoggerService;

    public AdminChallengeController(ChallengeProgramRepository challengeProgramRepository,
                                    WorkoutRepository workoutRepository,
                                    AuditLoggerService auditLoggerService) {
        this.challengeProgramRepository = challengeProgramRepository;
        this.workoutRepository = workoutRepository;
        this.auditLoggerService = auditLoggerService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String list(Model model) {
        model.addAttribute("activeMenu", "challenges");
        model.addAttribute("easyProgram", challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc("easy", "PUBLISHED").orElse(null));
        model.addAttribute("mediumProgram", challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc("medium", "PUBLISHED").orElse(null));
        model.addAttribute("hardProgram", challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc("hard", "PUBLISHED").orElse(null));
        return "admin/challenges/list";
    }

    @GetMapping("/{level}")
    @Transactional(readOnly = true)
    public String viewLevel(@PathVariable String level, Model model) {
        String lvl = normalizeLevel(level);
        model.addAttribute("activeMenu", "challenges");
        model.addAttribute("level", lvl);
        model.addAttribute("program", challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc(lvl, "PUBLISHED").orElse(null));
        return "admin/challenges/detail";
    }

    @GetMapping("/{level}/day/{dayIndex}")
    @Transactional(readOnly = true)
    public String editDay(@PathVariable String level, @PathVariable int dayIndex, Model model) {
        String lvl = normalizeLevel(level);
        model.addAttribute("activeMenu", "challenges");
        model.addAttribute("level", lvl);
        model.addAttribute("dayIndex", dayIndex);

        var workouts = workoutRepository.findAllByOrderBySortOrderAsc();
        model.addAttribute("workouts", workouts != null ? workouts : List.of());

        ChallengeProgram program = challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc(lvl, "PUBLISHED").orElse(null);
        ChallengeSession session = null;
        if (program != null && program.getSessions() != null) {
            for (ChallengeSession s : program.getSessions()) {
                if (s.getDayIndex() == dayIndex) {
                    session = s;
                    break;
                }
            }
        }
        model.addAttribute("challengeSession", session);
        return "admin/challenges/day_edit";
    }

    @PostMapping("/{level}/day/{dayIndex}")
    @Transactional
    public String saveDay(@PathVariable String level,
                          @PathVariable int dayIndex,
                          @RequestParam(required = false, defaultValue = "false") boolean isRestDay,
                          @RequestParam(required = false) Long workoutId,
                          RedirectAttributes redirect) {
        String lvl = normalizeLevel(level);
        ChallengeProgram program = challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc(lvl, "PUBLISHED").orElseGet(() -> {
            ChallengeProgram p = new ChallengeProgram();
            p.setLevel(lvl);
            p.setStatus("PUBLISHED");
            p.setVersion(1);
            return challengeProgramRepository.save(p);
        });

        if (program.getSessions() == null) {
            program.setSessions(new ArrayList<>());
        }

        ChallengeSession session = null;
        for (ChallengeSession s : program.getSessions()) {
            if (s.getDayIndex() == dayIndex) {
                session = s;
                break;
            }
        }
        if (session == null) {
            session = new ChallengeSession();
            session.setProgram(program);
            session.setDayIndex(dayIndex);
            session.setWeekNum(((dayIndex - 1) / 7) + 1);
            program.getSessions().add(session);
        }

        session.setRestDay(isRestDay);
        session.setWorkoutId(workoutId);
        challengeProgramRepository.save(program);

        auditLoggerService.log("admin@homefit.com", "UPDATE", "ChallengeDay", lvl + "-day" + dayIndex, "Cập nhật Thử thách " + lvl + " Ngày " + dayIndex);
        redirect.addFlashAttribute("message", "Đã lưu thành công Ngày " + dayIndex + " cho Thử thách " + lvl + "!");

        return "redirect:/admin/challenges/" + lvl;
    }

    private static String normalizeLevel(String level) {
        return level != null ? level.trim().toLowerCase(Locale.ROOT) : "easy";
    }
}
