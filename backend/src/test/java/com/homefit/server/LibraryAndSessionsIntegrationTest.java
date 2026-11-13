package com.homefit.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class LibraryAndSessionsIntegrationTest extends ApiTestSupport {

    private static final String ALL = "updatedAfter=2000-01-01T00:00:00Z";

    // ---------- thư viện ----------

    @Test
    void categoriesIncludeSeedData() throws Exception {
        String token = registerAndGetToken();
        HttpResponse<String> response = get("/api/v1/categories?" + ALL, token);

        assertEquals(200, response.statusCode());
        String body = response.body();
        assertTrue(body.contains("\"slug\":\"abs\""), body);
        assertTrue(body.contains("\"slug\":\"full_body\""), body);
        assertTrue(body.contains("\"isActive\""), body);
        assertTrue(body.contains("\"updatedAt\""), body);
    }

    @Test
    void exercisesAreReturnedAsArray() throws Exception {
        String token = registerAndGetToken();
        HttpResponse<String> response = get("/api/v1/exercises?" + ALL, token);

        assertEquals(200, response.statusCode());
        assertTrue(response.body().trim().startsWith("["), response.body());
    }

    @Test
    void exercisesCanBeFilteredByCategory() throws Exception {
        String token = registerAndGetToken();
        String categories = get("/api/v1/categories?" + ALL, token).body();
        String categoryId = numberField(categories, "id");
        assertNotNull(categoryId);

        HttpResponse<String> response = get("/api/v1/exercises?categoryId=" + categoryId + "&" + ALL, token);
        assertEquals(200, response.statusCode());
        String body = response.body();
        // mọi bài trả về đều thuộc nhóm đã lọc
        assertEquals(count(body, "\"id\""), count(body, "\"categoryId\":" + categoryId), body);
    }

    @Test
    void updatedAfterInTheFutureReturnsNothing() throws Exception {
        String token = registerAndGetToken();
        assertEquals("[]", get("/api/v1/categories?updatedAfter=2999-01-01T00:00:00Z", token).body().trim());
        assertEquals("[]", get("/api/v1/exercises?updatedAfter=2999-01-01T00:00:00Z", token).body().trim());
    }

    @Test
    void invalidQueryParametersAreRejected() throws Exception {
        String token = registerAndGetToken();

        HttpResponse<String> badDate = get("/api/v1/categories?updatedAfter=hom-qua", token);
        assertEquals(400, badDate.statusCode());
        assertTrue(badDate.body().contains("VALIDATION_ERROR"), badDate.body());

        HttpResponse<String> badId = get("/api/v1/exercises?categoryId=abc", token);
        assertEquals(400, badId.statusCode());
        assertTrue(badId.body().contains("VALIDATION_ERROR"), badId.body());
    }

    @Test
    void libraryRequiresLogin() throws Exception {
        assertEquals(401, get("/api/v1/categories", null).statusCode());
        assertEquals(401, get("/api/v1/exercises", null).statusCode());
    }

    @Test
    void unknownApiPathReturnsJsonNotFound() throws Exception {
        String token = registerAndGetToken();
        HttpResponse<String> response = get("/api/v1/khong-ton-tai", token);

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("NOT_FOUND") || response.body().contains("Not Found") || response.body().contains("404"), response.body());
    }

    // ---------- lịch sử buổi tập ----------

    @Test
    void sessionIsIdempotentByClientUuid() throws Exception {
        String token = registerAndGetToken();
        String uuid = UUID.randomUUID().toString();
        String json = sessionJson(uuid, null, START, END, 1200, 8);

        HttpResponse<String> first = post("/api/v1/workout-sessions", json, token);
        assertEquals(201, first.statusCode(), first.body());
        String id = numberField(first.body(), "id");
        assertNotNull(id);

        HttpResponse<String> second = post("/api/v1/workout-sessions", json, token);
        assertEquals(200, second.statusCode(), second.body());
        assertEquals(id, numberField(second.body(), "id"));

        String list = get("/api/v1/workout-sessions", token).body();
        assertEquals(1, count(list, uuid), list);
    }

    @Test
    void uppercaseUuidIsTreatedAsTheSameSession() throws Exception {
        String token = registerAndGetToken();
        String uuid = UUID.randomUUID().toString();

        assertEquals(201, post("/api/v1/workout-sessions",
                sessionJson(uuid, null, START, END, 600, 4), token).statusCode());
        assertEquals(200, post("/api/v1/workout-sessions",
                sessionJson(uuid.toUpperCase(), null, START, END, 600, 4), token).statusCode());
    }

    @Test
    void sessionValidationRejectsBadData() throws Exception {
        String token = registerAndGetToken();
        String uuid = UUID.randomUUID().toString();

        assertEquals(400, post("/api/v1/workout-sessions",
                sessionJson("not-a-uuid", null, START, END, 600, 4), token).statusCode());
        assertEquals(400, post("/api/v1/workout-sessions",
                sessionJson(uuid, null, END, START, 600, 4), token).statusCode());
        assertEquals(400, post("/api/v1/workout-sessions",
                sessionJson(uuid, null, START, END, -5, 4), token).statusCode());
        assertEquals(400, post("/api/v1/workout-sessions",
                sessionJson(uuid, null, START, END, 600, 5000), token).statusCode());
        assertEquals(400, post("/api/v1/workout-sessions",
                sessionJson(uuid, null, START, "2999-01-01T00:00:00Z", 600, 4), token).statusCode());
    }

    @Test
    void sessionWithUnknownCategoryIsRejected() throws Exception {
        String token = registerAndGetToken();
        HttpResponse<String> response = post("/api/v1/workout-sessions",
                sessionJson(UUID.randomUUID().toString(), 999999L, START, END, 600, 4), token);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("VALIDATION_ERROR"), response.body());
    }

    @Test
    void sessionCanReferenceAnExistingCategory() throws Exception {
        String token = registerAndGetToken();
        String categoryId = numberField(get("/api/v1/categories?" + ALL, token).body(), "id");
        String uuid = UUID.randomUUID().toString();

        HttpResponse<String> response = post("/api/v1/workout-sessions",
                sessionJson(uuid, Long.valueOf(categoryId), START, END, 900, 6), token);
        assertEquals(201, response.statusCode(), response.body());
        assertTrue(get("/api/v1/workout-sessions", token).body().contains("\"categoryId\":" + categoryId));
    }

    @Test
    void batchAcceptsGoodSessionsAndReportsBadOnes() throws Exception {
        String token = registerAndGetToken();
        String good = UUID.randomUUID().toString();
        String batch = "{\"sessions\":[" + sessionJson(good, null, START, END, 600, 5) + ","
                + sessionJson("not-a-uuid", null, START, END, 600, 5) + "]}";

        HttpResponse<String> response = post("/api/v1/workout-sessions/batch", batch, token);
        assertEquals(200, response.statusCode(), response.body());
        String body = response.body();
        assertTrue(body.contains("\"syncedUuids\":[\"" + good + "\"]"), body);
        assertTrue(body.contains("\"clientUuid\":\"not-a-uuid\""), body);

        // gửi lại cùng lô: buổi hợp lệ vẫn được xác nhận, không tạo bản trùng
        post("/api/v1/workout-sessions/batch", batch, token);
        assertEquals(1, count(get("/api/v1/workout-sessions", token).body(), good));
    }

    @Test
    void emptyBatchIsRejected() throws Exception {
        String token = registerAndGetToken();
        assertEquals(400, post("/api/v1/workout-sessions/batch", "{\"sessions\":[]}", token).statusCode());
    }

    @Test
    void sessionsAreIsolatedPerUser() throws Exception {
        String tokenA = registerAndGetToken();
        String tokenB = registerAndGetToken();
        String uuid = UUID.randomUUID().toString();
        String json = sessionJson(uuid, null, START, END, 600, 4);

        assertEquals(201, post("/api/v1/workout-sessions", json, tokenA).statusCode());

        // người dùng khác không thấy lịch sử của A
        assertEquals("[]", get("/api/v1/workout-sessions", tokenB).body().trim());
        // và có thể dùng cùng clientUuid cho lịch sử của riêng mình
        assertEquals(201, post("/api/v1/workout-sessions", json, tokenB).statusCode());
    }

    @Test
    void historyCanBeFilteredByTime() throws Exception {
        String token = registerAndGetToken();
        String uuid = UUID.randomUUID().toString();
        post("/api/v1/workout-sessions", sessionJson(uuid, null, START, END, 600, 4), token);

        String inRange = get("/api/v1/workout-sessions?from=2026-10-01T00:00:00Z&to=2026-12-01T00:00:00Z", token).body();
        assertEquals(1, count(inRange, uuid), inRange);

        String outOfRange = get("/api/v1/workout-sessions?from=2020-01-01T00:00:00Z&to=2020-12-01T00:00:00Z", token).body();
        assertEquals("[]", outOfRange.trim());

        assertEquals(400, get("/api/v1/workout-sessions?from=hom-qua", token).statusCode());
    }
}
