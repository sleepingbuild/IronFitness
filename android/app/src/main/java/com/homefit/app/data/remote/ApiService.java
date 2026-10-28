package com.homefit.app.data.remote;

import com.homefit.app.data.remote.dto.AuthResponse;
import com.homefit.app.data.remote.dto.BatchSessionsRequest;
import com.homefit.app.data.remote.dto.BatchSessionsResponse;
import com.homefit.app.data.remote.dto.CategoryDto;
import com.homefit.app.data.remote.dto.ExerciseDto;
import com.homefit.app.data.remote.dto.GoogleLoginRequest;
import com.homefit.app.data.remote.dto.LoginRequest;
import com.homefit.app.data.remote.dto.RefreshRequest;
import com.homefit.app.data.remote.dto.RefreshResponse;
import com.homefit.app.data.remote.dto.RegisterRequest;
import com.homefit.app.data.remote.dto.UpdateProfileRequest;
import com.homefit.app.data.remote.dto.UserDto;
import com.homefit.app.data.remote.dto.WorkoutSessionDto;
import com.homefit.app.data.remote.dto.WorkoutSessionRequest;
import com.homefit.app.data.remote.dto.WorkoutSessionResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/** Khớp docs/API.md. Base URL kết thúc bằng "/api/v1/", đường dẫn bên dưới không có "/" đầu. */
public interface ApiService {

    // ---- Auth ----
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest body);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest body);

    @POST("auth/google")
    Call<AuthResponse> loginWithGoogle(@Body GoogleLoginRequest body);

    @POST("auth/refresh")
    Call<RefreshResponse> refresh(@Body RefreshRequest body);

    // ---- Hồ sơ ----
    @GET("me")
    Call<UserDto> getMe();

    @PUT("me/profile")
    Call<UserDto> updateProfile(@Body UpdateProfileRequest body);

    // ---- Thư viện bài tập (tham số null sẽ bị bỏ qua) ----
    @GET("categories")
    Call<List<CategoryDto>> getCategories(@Query("updatedAfter") String updatedAfter);

    @GET("exercises")
    Call<List<ExerciseDto>> getExercises(@Query("categoryId") Long categoryId,
                                         @Query("updatedAfter") String updatedAfter);

    // ---- Lịch sử tập ----
    @POST("workout-sessions")
    Call<WorkoutSessionResponse> createSession(@Body WorkoutSessionRequest body);

    @POST("workout-sessions/batch")
    Call<BatchSessionsResponse> syncBatch(@Body BatchSessionsRequest body);

    @GET("workout-sessions")
    Call<List<WorkoutSessionDto>> getSessions(@Query("from") String from, @Query("to") String to);
}
