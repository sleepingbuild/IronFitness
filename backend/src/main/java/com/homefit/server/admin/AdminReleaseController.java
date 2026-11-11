package com.homefit.server.admin;

import com.homefit.server.domain.ContentRelease;
import com.homefit.server.repository.ContentReleaseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/releases")
public class AdminReleaseController {

    private final ContentReleaseRepository releaseRepository;

    public AdminReleaseController(ContentReleaseRepository releaseRepository) {
        this.releaseRepository = releaseRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("activeMenu", "releases");
        model.addAttribute("releases", releaseRepository.findAll());
        model.addAttribute("latestRelease", releaseRepository.findTopByOrderByVersionDesc().orElse(null));
        return "admin/releases/list";
    }

    @PostMapping("/create")
    public String createRelease(@RequestParam("notes") String notes, RedirectAttributes redirect) {
        int nextVersion = releaseRepository.findTopByOrderByVersionDesc()
                .map(r -> r.getVersion() + 1)
                .orElse(1);

        ContentRelease release = new ContentRelease();
        release.setVersion(nextVersion);
        release.setNotes(notes);
        release.setCreatedBy("Admin");
        release.setStatus("PUBLISHED");
        release.setManifestJson("{\"version\": " + nextVersion + ", \"syncedAt\": \"" + java.time.Instant.now() + "\"}");

        releaseRepository.save(release);
        redirect.addFlashAttribute("message", "Đã tạo và xuất bản Content Release v" + nextVersion);
        return "redirect:/admin/releases";
    }
}
