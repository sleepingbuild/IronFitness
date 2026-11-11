package com.homefit.server.admin;

import com.homefit.server.repository.ChallengeProgramRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/challenges")
public class AdminChallengeController {

    private final ChallengeProgramRepository challengeProgramRepository;

    public AdminChallengeController(ChallengeProgramRepository challengeProgramRepository) {
        this.challengeProgramRepository = challengeProgramRepository;
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
}
