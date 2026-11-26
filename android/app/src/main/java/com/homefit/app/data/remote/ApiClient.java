package com.homefit.app.data.remote;

import android.content.Context;
import android.util.Log;

import com.homefit.app.R;
import com.homefit.app.data.prefs.TokenStore;
import com.homefit.app.util.NetworkUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Tạo ApiService dùng chung. Base URL đọc từ string "api_base_url" (res/values/api_config.xml).
 * Timeout theo docs/API.md: connect 15s, read 25s.
 * Hỗ trợ OkHttp Cache 10MB và Offline Interceptor max-stale 7 ngày.
 * Bắt toàn bộ IOException / UnknownHostException / SocketTimeoutException an toàn.
 */
public final class ApiClient {

    private static final String TAG = "ApiClient";
    private static final long CACHE_SIZE = 10 * 1024 * 1024; // 10 MB
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

    public static String getBaseUrl(Context context) {
        return context.getApplicationContext().getString(R.string.api_base_url);
    }

    @SuppressWarnings("unused")
    public static synchronized void reset() {
        instance = null;
    }

    private static ApiService create(Context app) {
        String baseUrl = app.getString(R.string.api_base_url);
        Log.i(TAG, "Retrofit initializing with Base URL: " + baseUrl);
        TokenStore tokenStore = new TokenStore(app);

        File cacheDir = new File(app.getCacheDir(), "http_cache");
        Cache cache = new Cache(cacheDir, CACHE_SIZE);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        logging.redactHeader("Authorization");

        Interceptor offlineCacheInterceptor = chain -> {
            Request request = chain.request();
            if (!NetworkUtils.isNetworkAvailable(app)) {
                CacheControl cacheControl = new CacheControl.Builder()
                        .onlyIfCached()
                        .maxStale(7, TimeUnit.DAYS)
                        .build();
                request = request.newBuilder()
                        .cacheControl(cacheControl)
                        .build();
            }
            try {
                return chain.proceed(request);
            } catch (Exception e) {
                Log.w(TAG, "Ngoại lệ kết nối mạng trong offlineCacheInterceptor: " + e.getMessage());
                return new Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(504)
                        .message("Unsatisfiable Request (Offline Cache Fallback)")
                        .body(ResponseBody.create("{}", MediaType.parse("application/json")))
                        .build();
            }
        };

        Interceptor onlineCacheInterceptor = chain -> {
            try {
                Response response = chain.proceed(chain.request());
                if (NetworkUtils.isNetworkAvailable(app)) {
                    String cacheControl = response.header("Cache-Control");
                    if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache")
                            || cacheControl.contains("must-revalidate") || cacheControl.trim().isEmpty()) {
                        return response.newBuilder()
                                .removeHeader("Pragma")
                                .header("Cache-Control", "public, max-age=60")
                                .build();
                    }
                }
                return response;
            } catch (Exception e) {
                Log.w(TAG, "Ngoại lệ kết nối mạng trong onlineCacheInterceptor: " + e.getMessage());
                return new Response.Builder()
                        .request(chain.request())
                        .protocol(Protocol.HTTP_1_1)
                        .code(504)
                        .message("Unsatisfiable Request (Network Error)")
                        .body(ResponseBody.create("{}", MediaType.parse("application/json")))
                        .build();
            }
        };

        OkHttpClient plain = new OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(25, TimeUnit.SECONDS)
                .addInterceptor(offlineCacheInterceptor)
                .addNetworkInterceptor(onlineCacheInterceptor)
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
