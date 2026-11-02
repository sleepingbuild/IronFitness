package com.homefit.server;

import com.homefit.server.domain.User;
import com.homefit.server.repository.UserRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

/**
 * Nền cho kiểm thử tích hợp: khởi động máy chủ thật (cổng ngẫu nhiên) kết nối MySQL local, gọi API bằng HTTP thật.
 * Cần MySQL đang chạy và application-local.yml đã cấu hình.
 * Dữ liệu thử dùng email bắt đầu bằng "it-" và bị xóa sau mỗi test (lịch sử và token xóa theo nhờ ON DELETE CASCADE).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
abstract class ApiTestSupport {

    static final String TEST_EMAIL_PREFIX = "it-";
    static final String PASSWORD = "Test12345";
    static final String START = "2026-10-31T08:00:00Z";
    static final String END = "2026-10-31T08:20:00Z";

    @Autowired
    private Environment environment;

    @Autowired
    private UserRepository users;

    final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @AfterEach
    void deleteTestUsers() {
        List<User> toDelete = users.findAll().stream()
                .filter(u -> u.getEmail().startsWith(TEST_EMAIL_PREFIX))
                .toList();
        users.deleteAll(toDelete);
    }

    String baseUrl() {
        return "http://localhost:" + environment.getProperty("local.server.port");
    }

    HttpResponse<String> send(String method, String path, String json, String token) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl() + path))
                .timeout(Duration.ofSeconds(20));
        if (json != null) {
            builder.header("Content-Type", "application/json; charset=utf-8");
        }
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        HttpRequest.BodyPublisher body = json == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8);
        builder.method(method, body);
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    HttpResponse<String> get(String path, String token) throws Exception {
        return send("GET", path, null, token);
    }

    HttpResponse<String> post(String path, String json, String token) throws Exception {
        return send("POST", path, json, token);
    }

    HttpResponse<String> put(String path, String json, String token) throws Exception {
        return send("PUT", path, json, token);
    }

    static String newEmail() {
        return TEST_EMAIL_PREFIX + UUID.randomUUID().toString().substring(0, 12) + "@example.test";
    }

    HttpResponse<String> register(String email, String password) throws Exception {
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password
                + "\",\"displayName\":\"Người Dùng Thử\"}";
        return post("/api/v1/auth/register", json, null);
    }

    HttpResponse<String> login(String email, String password) throws Exception {
        String json = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        return post("/api/v1/auth/login", json, null);
    }

    /** Đăng ký một người dùng mới và trả về access token. */
    String registerAndGetToken() throws Exception {
        HttpResponse<String> response = register(newEmail(), PASSWORD);
        if (response.statusCode() != 201) {
            throw new IllegalStateException("Đăng ký thất bại: " + response.statusCode() + " " + response.body());
        }
        return stringField(response.body(), "accessToken");
    }

    static String sessionJson(String uuid, Long categoryId, String started, String completed,
                              int seconds, int done) {
        return "{\"clientUuid\":\"" + uuid + "\",\"categoryId\":" + categoryId
                + ",\"startedAt\":\"" + started + "\",\"completedAt\":\"" + completed
                + "\",\"totalSeconds\":" + seconds + ",\"exercisesDone\":" + done + "}";
    }

    static String stringField(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    static String numberField(String json, String name) {
        Matcher m = Pattern.compile("\"" + name + "\"\\s*:\\s*(\\d+)").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    static int count(String text, String part) {
        return text.split(Pattern.quote(part), -1).length - 1;
    }
}
