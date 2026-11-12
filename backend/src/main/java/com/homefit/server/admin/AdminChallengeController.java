package com.homefit.server.admin;

import com.homefit.server.domain.ChallengeProgram;
import com.homefit.server.domain.ChallengeSession;
import com.homefit.server.repository.ChallengeProgramRepository;
import com.homefit.server.repository.WorkoutRepository;
import com.homefit.server.service.AuditLoggerService;
import org.springframework.stereotype.Controller;
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
    public String list(Model model) {
        model.addAttribute("activeMenu", "challenges");
        model.addAttribute("easyProgram", challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc("easy", "PUBLISHED").orElse(null));
        model.addAttribute("mediumProgram", challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc("medium", "PUBLISHED").orElse(null));
        model.addAttribute("hardProgram", challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc("hard", "PUBLISHED").orElse(null));
        return "admin/challenges/list";
    }

    @GetMapping("/{level}")
    public String viewLevel(@PathVariable String level, Model model) {
        model.addAttribute("activeMenu", "challenges");
        model.addAttribute("level", level);
        model.addAttribute("program", challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc(level, "PUBLISHED").orElse(null));
        return "admin/challenges/detail";
    }

    @GetMapping("/{level}/day/{dayIndex}")
    public String editDay(@PathVariable String level, @PathVariable int dayIndex, Model model) {
        model.addAttribute("activeMenu", "challenges");
        model.addAttribute("level", level);
        model.addAttribute("dayIndex", dayIndex);
        model.addAttribute("workouts", workoutRepository.findAll());

        ChallengeProgram program = challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc(level, "PUBLISHED").orElse(null);
        ChallengeSession session = null;
        if (program != null && program.getSessions() != null) {
            for (ChallengeSession s : program.getSessions()) {
                if (s.getDayIndex() == dayIndex) {
                    session = s;
                    break;
                }
            }
        }
        model.addAttribute("session", session);
        return "admin/challenges/day_edit";
    }

    @PostMapping("/{level}/day/{dayIndex}")
    public String saveDay(@PathVariable String level,
                          @PathVariable int dayIndex,
                          @RequestParam(required = false, defaultValue = "false") boolean isRestDay,
                          @RequestParam(required = false) Long workoutId,
                          RedirectAttributes redirect) {
        ChallengeProgram program = challengeProgramRepository.findTopByLevelAndStatusOrderByVersionDesc(level, "PUBLISHED").orElseGet(() -> {
            ChallengeProgram p = new ChallengeProgram();
            p.setLevel(level);
            p.setStatus("PUBLISHED");
            p.setVersion(1);
            return challengeProgramRepository.save(p);
        });

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

        auditLoggerService.log("admin@homefit.com", "UPDATE", "ChallengeDay", level + "-day" + dayIndex, "Cập nhật Thử thách " + level + " Ngày " + dayIndex);
        redirect.addFlashAttribute("message", "Đã lưu thành công Ngày " + dayIndex + " cho Thử thách " + level + "!");

        return "redirect:/admin/challenges/" + level;
    }
}
