package com.homefit.app.data.remote;

import android.content.Context;
import android.util.Log;

import com.homefit.app.R;
import com.homefit.app.data.prefs.TokenStore;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Tạo ApiService dùng chung. Base URL đọc từ string "api_base_url" (res/values/api_config.xml).
 * Timeout theo docs/API.md: connect 10s, read 20s.
 */
public final class ApiClient {

    private static final String TAG = "ApiClient";
    private static volatile ApiService instance;

    private ApiClient() {
    }

    public static ApiService get(Context context) {
        if (instance == null) {
            synchronized (ApiClient.class) {
                if (instance == null) {
                    instance = create(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = null;
    }

    private static ApiService create(Context app) {
        String baseUrl = app.getString(R.string.api_base_url);
        Log.i(TAG, "Retrofit initializing with Base URL: " + baseUrl);
        TokenStore tokenStore = new TokenStore(app);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        logging.redactHeader("Authorization");

        OkHttpClient plain = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        ApiService refreshApi = retrofit(baseUrl, plain).create(ApiService.class);

        OkHttpClient authed = plain.newBuilder()
                .addInterceptor(new AuthInterceptor(tokenStore))
                .authenticator(new TokenAuthenticator(tokenStore, refreshApi))
                .build();

        return retrofit(baseUrl, authed).create(ApiService.class);
    }

    private static Retrofit retrofit(String baseUrl, OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
