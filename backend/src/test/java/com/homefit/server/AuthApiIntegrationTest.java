package com.homefit.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;

class AuthApiIntegrationTest extends ApiTestSupport {

    @Test
    void registerCreatesUserAndReturnsTokens() throws Exception {
        String email = newEmail();
        HttpResponse<String> response = register(email.toUpperCase(), PASSWORD);

        assertEquals(201, response.statusCode());
        String body = response.body();
        assertTrue(body.contains("\"accessToken\""));
        assertTrue(body.contains("\"refreshToken\""));
        assertTrue(body.contains("\"expiresIn\""));
        assertTrue(body.contains("\"isPremium\":false"), body);
        // email được chuẩn hóa thành chữ thường
        assertEquals(email, stringField(body, "email"));
    }

    @Test
    void registerTwiceReturnsConflict() throws Exception {
        String email = newEmail();
        assertEquals(201, register(email, PASSWORD).statusCode());

        HttpResponse<String> second = register(email, PASSWORD);
        assertEquals(409, second.statusCode());
        assertTrue(second.body().contains("\"code\":\"CONFLICT\""), second.body());
    }

    @Test
    void registerRejectsInvalidInput() throws Exception {
        HttpResponse<String> badEmail = register("khong-phai-email", PASSWORD);
        assertEquals(400, badEmail.statusCode());
        assertTrue(badEmail.body().contains("\"code\":\"VALIDATION_ERROR\""), badEmail.body());

        HttpResponse<String> shortPassword = register(newEmail(), "ngan");
        assertEquals(400, shortPassword.statusCode());
        assertTrue(shortPassword.body().contains("VALIDATION_ERROR"));
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        String email = newEmail();
        register(email, PASSWORD);

        HttpResponse<String> response = login(email, "SaiMatKhau99");
        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("\"code\":\"UNAUTHORIZED\""), response.body());
    }

    @Test
    void loginWithRightPasswordReturnsTokens() throws Exception {
        String email = newEmail();
        register(email, PASSWORD);

        HttpResponse<String> response = login(email, PASSWORD);
        assertEquals(200, response.statusCode());
        assertNotNull(stringField(response.body(), "accessToken"));
    }

    @Test
    void protectedEndpointNeedsValidToken() throws Exception {
        HttpResponse<String> none = get("/api/v1/me", null);
        assertEquals(401, none.statusCode());
        assertTrue(none.body().contains("\"code\":\"UNAUTHORIZED\""), none.body());

        HttpResponse<String> garbage = get("/api/v1/me", "khong.phai.jwt");
        assertEquals(401, garbage.statusCode());
    }

    @Test
    void meReturnsOwnProfileAndProfileCanBeUpdated() throws Exception {
        String email = newEmail();
        String token = stringField(register(email, PASSWORD).body(), "accessToken");

        HttpResponse<String> me = get("/api/v1/me", token);
        assertEquals(200, me.statusCode());
        assertEquals(email, stringField(me.body(), "email"));

        HttpResponse<String> updated = put("/api/v1/me/profile",
                "{\"displayName\":\"Tên Mới\",\"heightCm\":170.5,\"weightKg\":65.0}", token);
        assertEquals(200, updated.statusCode(), updated.body());

        String again = get("/api/v1/me", token).body();
        assertTrue(again.contains("170.5"), again);
        assertTrue(again.contains("Tên Mới"), again);
    }

    @Test
    void profileValidationRejectsOutOfRangeValues() throws Exception {
        String token = registerAndGetToken();

        HttpResponse<String> tooTall = put("/api/v1/me/profile",
                "{\"displayName\":\"A\",\"heightCm\":300,\"weightKg\":65}", token);
        assertEquals(400, tooTall.statusCode());
        assertTrue(tooTall.body().contains("VALIDATION_ERROR"), tooTall.body());

        HttpResponse<String> tooLight = put("/api/v1/me/profile",
                "{\"displayName\":\"A\",\"heightCm\":170,\"weightKg\":10}", token);
        assertEquals(400, tooLight.statusCode());

        HttpResponse<String> blankName = put("/api/v1/me/profile",
                "{\"displayName\":\"  \",\"heightCm\":170,\"weightKg\":65}", token);
        assertEquals(400, blankName.statusCode());
    }

    @Test
    void measurementsCanBeCleared() throws Exception {
        String token = registerAndGetToken();
        put("/api/v1/me/profile", "{\"displayName\":\"A\",\"heightCm\":170,\"weightKg\":65}", token);

        HttpResponse<String> cleared = put("/api/v1/me/profile",
                "{\"displayName\":\"A\",\"heightCm\":null,\"weightKg\":null}", token);
        assertEquals(200, cleared.statusCode(), cleared.body());
        assertTrue(cleared.body().contains("\"heightCm\":null"), cleared.body());
    }

    @Test
    void refreshRotatesTokensAndOldOneStopsWorking() throws Exception {
        HttpResponse<String> registered = register(newEmail(), PASSWORD);
        String oldRefresh = stringField(registered.body(), "refreshToken");

        HttpResponse<String> first = post("/api/v1/auth/refresh", "{\"refreshToken\":\"" + oldRefresh + "\"}", null);
        assertEquals(200, first.statusCode(), first.body());
        String newRefresh = stringField(first.body(), "refreshToken");
        assertNotNull(newRefresh);
        assertNotEquals(oldRefresh, newRefresh);

        HttpResponse<String> reuse = post("/api/v1/auth/refresh", "{\"refreshToken\":\"" + oldRefresh + "\"}", null);
        assertEquals(401, reuse.statusCode());

        HttpResponse<String> second = post("/api/v1/auth/refresh", "{\"refreshToken\":\"" + newRefresh + "\"}", null);
        assertEquals(200, second.statusCode());
    }

    @Test
    void refreshWithUnknownTokenIsUnauthorized() throws Exception {
        HttpResponse<String> response = post("/api/v1/auth/refresh", "{\"refreshToken\":\"khong-ton-tai\"}", null);
        assertEquals(401, response.statusCode());
        assertTrue(response.body().contains("UNAUTHORIZED"));
    }
}
