package com.homefit.app.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.homefit.app.data.remote.ApiClient;
import com.homefit.app.data.remote.ApiService;
import com.homefit.app.data.remote.dto.DiscoverDto;
import com.homefit.app.util.AppExecutors;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class DiscoverRepository {

    private static final String TAG = "DiscoverRepository";
    private static final String PREF_NAME = "discover_cache_prefs";
    private static final String KEY_CACHE = "cached_discover_items_json";

    private final ApiService apiService;
    private final SharedPreferences prefs;
    private final Gson gson;

    public interface Callback<T> {
        void onResult(T data, boolean fromCache);
        void onError(Throwable throwable);
    }

    public DiscoverRepository(@NonNull Context context) {
        Context appCtx = context.getApplicationContext();
        this.apiService = ApiClient.get(appCtx);
        this.prefs = appCtx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public void fetchDiscoverItems(@NonNull Callback<List<DiscoverDto>> callback) {
        // 1. Load from cache first for instant offline display
        AppExecutors.diskIO().execute(() -> {
            List<DiscoverDto> cachedList = getCachedItems();
            if (cachedList != null && !cachedList.isEmpty()) {
                AppExecutors.mainThread().execute(() -> callback.onResult(cachedList, true));
            }

            // 2. Network fetch from server
            apiService.getDiscoverItems().enqueue(new retrofit2.Callback<List<DiscoverDto>>() {
                @Override
                public void onResponse(@NonNull Call<List<DiscoverDto>> call, @NonNull Response<List<DiscoverDto>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<DiscoverDto> serverList = new ArrayList<>(response.body());
                        // Sort by sortOrder ASC
                        Collections.sort(serverList, (a, b) -> Integer.compare(a.sortOrder, b.sortOrder));

                        // Save to cache
                        saveCache(serverList);

                        AppExecutors.mainThread().execute(() -> callback.onResult(serverList, false));
                    } else {
                        Log.w(TAG, "Lỗi HTTP khi lấy Discover items: " + response.code());
                        if (cachedList == null || cachedList.isEmpty()) {
                            AppExecutors.mainThread().execute(() -> callback.onResult(new ArrayList<>(), true));
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<DiscoverDto>> call, @NonNull Throwable t) {
                    Log.w(TAG, "Lỗi kết nối khi lấy Discover items: " + t.getMessage());
                    if (cachedList == null || cachedList.isEmpty()) {
                        AppExecutors.mainThread().execute(() -> callback.onError(t));
                    }
                }
            });
        });
    }

    private List<DiscoverDto> getCachedItems() {
        try {
            String json = prefs.getString(KEY_CACHE, null);
            if (json == null || json.trim().isEmpty()) return null;
            Type type = new TypeToken<List<DiscoverDto>>() {}.getType();
            return gson.fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi đọc cache Discover: " + e.getMessage());
            return null;
        }
    }

    private void saveCache(List<DiscoverDto> items) {
        try {
            String json = gson.toJson(items);
            prefs.edit().putString(KEY_CACHE, json).apply();
        } catch (Exception e) {
            Log.e(TAG, "Lỗi ghi cache Discover: " + e.getMessage());
        }
    }
}
