package com.homefit.server.admin;

import com.homefit.server.domain.AppConfig;
import com.homefit.server.repository.AppConfigRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/settings")
public class AdminAppConfigController {

    private final AppConfigRepository appConfigRepository;

    public AdminAppConfigController(AppConfigRepository appConfigRepository) {
        this.appConfigRepository = appConfigRepository;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("activeMenu", "settings");
        model.addAttribute("proTitle", getConfigValue("pro_title", "Gói Nâng Cấp HomeFit PRO ⚡"));
        model.addAttribute("proBenefits", getConfigValue("pro_benefits", "Mở khóa tất cả bài tập nâng cao;Không có quảng cáo làm phiền;Tùy chỉnh giáo trình tập luyện cá nhân;Đồng bộ tiến độ đa thiết bị cloud"));
        model.addAttribute("proPrice", getConfigValue("pro_price", "59.000 VNĐ / tháng"));
        model.addAttribute("proEnabled", getConfigValue("pro_enabled", "true"));
        model.addAttribute("reminderMsg", getConfigValue("reminder_message_default", "Đã đến giờ rèn luyện sức khỏe cùng HomeFit! Hãy dành 15 phút hôm nay nhé 💪"));
        return "admin/settings/form";
    }

    @PostMapping
    public String save(@RequestParam Map<String, String> params, RedirectAttributes redirect) {
        saveConfig("pro_title", params.getOrDefault("proTitle", "Gói Nâng Cấp HomeFit PRO ⚡"), "Tiêu đề gói PRO");
        saveConfig("pro_benefits", params.getOrDefault("proBenefits", ""), "Danh sách quyền lợi gói PRO (phân cách bằng dấu chấm phẩy)");
        saveConfig("pro_price", params.getOrDefault("proPrice", "59.000 VNĐ / tháng"), "Giá hiển thị gói PRO");
        saveConfig("pro_enabled", params.containsKey("proEnabled") ? "true" : "false", "Trạng thái bật/tắt gói PRO");
        saveConfig("reminder_message_default", params.getOrDefault("reminderMsg", ""), "Nội dung nhắc nhở tập luyện mặc định");

        redirect.addFlashAttribute("message", "Đã lưu thành công cấu hình hệ thống & gói PRO.");
        return "redirect:/admin/settings";
    }

    private String getConfigValue(String key, String defaultValue) {
        return appConfigRepository.findByConfigKey(key)
                .map(AppConfig::getConfigValue)
                .orElse(defaultValue);
    }

    private void saveConfig(String key, String value, String description) {
        AppConfig config = appConfigRepository.findByConfigKey(key).orElseGet(() -> {
            AppConfig c = new AppConfig();
            c.setConfigKey(key);
            return c;
        });
        config.setConfigValue(value != null ? value.trim() : "");
        config.setDescription(description);
        config.setUpdatedBy("admin");
        appConfigRepository.save(config);
    }
}
