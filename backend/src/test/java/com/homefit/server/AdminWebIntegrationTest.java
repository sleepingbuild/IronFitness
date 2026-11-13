package com.homefit.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

/** Kiểm tra trang /admin ở mức công khai và bảo vệ. */
class AdminWebIntegrationTest extends ApiTestSupport {

    @Test
    void loginPageIsPublicAndRenders() throws Exception {
        HttpResponse<String> response = get("/admin/login", null);

        assertEquals(200, response.statusCode(), response.body());
        assertTrue(response.body().contains("HomeFit Admin"), response.body());
        assertTrue(response.body().contains("_csrf"), response.body());
    }

    @Test
    void postLoginDoesNotReturn403() throws Exception {
        HttpResponse<String> response = send("POST", "/admin/login", "username=admin@example.com&password=wrongpassword", "application/x-www-form-urlencoded");
        // Đăng nhập không bị lỗi 403 Forbidden (chuyển hướng sang /admin/login?error với 302)
        assertEquals(302, response.statusCode());
        String location = response.headers().firstValue("location").orElse("");
        assertTrue(location.contains("/admin/login?error"), location);
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
    void logoutHandledBySecurity() throws Exception {
        HttpResponse<String> response = send("POST", "/admin/logout", null, null);
        assertEquals(302, response.statusCode());
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
        assertTrue(status == 302 || status == 401 || status == 403, "status = " + status);
    }
}
