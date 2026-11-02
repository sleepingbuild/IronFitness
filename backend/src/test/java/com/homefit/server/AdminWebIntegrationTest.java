package com.homefit.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

/** Kiểm tra trang /admin ở mức công khai và bảo vệ. Việc đăng nhập admin thử bằng tay (cần mật khẩu admin). */
class AdminWebIntegrationTest extends ApiTestSupport {

    @Test
    void loginPageIsPublicAndRenders() throws Exception {
        HttpResponse<String> response = get("/admin/login", null);

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("HomeFit Admin"), response.body());
        // biểu mẫu đăng nhập có mã CSRF do Thymeleaf thêm vào
        assertTrue(response.body().contains("_csrf"), response.body());
    }

    @Test
    void adminPagesRedirectToLoginWhenAnonymous() throws Exception {
        for (String path : new String[] {"/admin", "/admin/categories"}) {
            HttpResponse<String> response = get(path, null);
            assertEquals(302, response.statusCode(), path);
            String location = response.headers().firstValue("location").orElse("");
            assertTrue(location.contains("/admin/login"), path + " -> " + location);
        }
    }

    @Test
    void logoutWithoutCsrfTokenIsRejected() throws Exception {
        assertEquals(403, send("POST", "/admin/logout", null, null).statusCode());
    }

    @Test
    void apiTokenDoesNotGiveAccessToAdmin() throws Exception {
        String token = registerAndGetToken();
        HttpResponse<String> response = get("/admin", token);
        // JWT chỉ dùng cho /api; /admin vẫn yêu cầu đăng nhập bằng form
        assertEquals(302, response.statusCode());
    }

    @Test
    void otherPathsAreDenied() throws Exception {
        int status = get("/khong-ton-tai", null).statusCode();
        assertTrue(status == 401 || status == 403, "status = " + status);
    }
}
