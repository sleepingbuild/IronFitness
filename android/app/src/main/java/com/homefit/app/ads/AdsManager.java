package com.homefit.app.ads;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

/**
 * Điểm duy nhất của app dùng AdMob (xem docs/ADS.md).
 * Tất cả hàm phải gọi trên main thread.
 *
 * Các ID bên dưới là ID THỬ NGHIỆM của Google. Nếu dùng ID thật, xem docs/ADS.md mục 9.
 */
public final class AdsManager {

    private static final String TAG = "AdsManager";

    // ID thử nghiệm chính thức của Google.
    private static final String BANNER_UNIT_ID = "ca-app-pub-3940256099942544/9214589741";
    private static final String INTERSTITIAL_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";

    /** Khoảng cách tối thiểu giữa hai lần hiện interstitial. */
    private static final long MIN_INTERSTITIAL_INTERVAL_MS = 3 * 60 * 1000L;

    private static boolean initialized = false;
    private static boolean adsEnabled = true;
    private static boolean interstitialLoading = false;
    private static long lastInterstitialShownAt = -MIN_INTERSTITIAL_INTERVAL_MS;
    @Nullable
    private static InterstitialAd interstitial;
    @Nullable
    private static Context appContext;

    private AdsManager() {
    }

    /** Gọi một lần khi app khởi động. */
    public static void init(@NonNull Application app) {
        if (initialized) {
            return;
        }
        initialized = true;
        appContext = app.getApplicationContext();
        MobileAds.initialize(appContext, initializationStatus -> preloadInterstitial());
    }

    /** Gọi với {@code !isPremium}. Tắt quảng cáo sẽ bỏ interstitial đã tải. */
    public static void setAdsEnabled(boolean enabled) {
        adsEnabled = enabled;
        if (!enabled) {
            interstitial = null;
        } else if (initialized) {
            preloadInterstitial();
        }
    }

    public static boolean isAdsEnabled() {
        return adsEnabled;
    }

    /** True nếu một interstitial đã tải xong và có thể hiện. */
    public static boolean isInterstitialReady() {
        return adsEnabled && interstitial != null;
    }

    /**
     * Gắn banner vào container. Trả về AdView để nơi gọi gọi {@code destroy()} khi hủy view.
     * Trả về null nếu quảng cáo đang tắt hoặc chưa init.
     */
    @Nullable
    public static AdView attachBanner(@NonNull FrameLayout container) {
        container.removeAllViews();
        if (!adsEnabled || !initialized) {
            container.setVisibility(View.GONE);
            return null;
        }
        container.setVisibility(View.VISIBLE);
        AdView adView = new AdView(container.getContext());
        adView.setAdUnitId(BANNER_UNIT_ID);
        adView.setAdSize(AdSize.BANNER);
        container.addView(adView);
        adView.loadAd(new AdRequest.Builder().build());
        return adView;
    }

    /**
     * Dùng trong luồng thật: hiện interstitial nếu đã sẵn sàng và đủ khoảng cách thời gian.
     * {@code onDone} LUÔN được gọi đúng một lần. Trả về true nếu quảng cáo được hiện.
     */
    public static boolean showInterstitialIfReady(@NonNull Activity activity, @NonNull Runnable onDone) {
        return showInterstitial(activity, onDone, false);
    }

    /**
     * Như trên, nhưng {@code ignoreInterval = true} bỏ qua giới hạn 3 phút (chỉ dùng cho màn demo).
     */
    public static boolean showInterstitial(@NonNull Activity activity, @NonNull Runnable onDone,
                                           boolean ignoreInterval) {
        long now = SystemClock.elapsedRealtime();
        boolean intervalOk = ignoreInterval
                || now - lastInterstitialShownAt >= MIN_INTERSTITIAL_INTERVAL_MS;
        InterstitialAd ad = interstitial;

        if (!adsEnabled || !initialized || ad == null || !intervalOk) {
            if (adsEnabled && initialized && ad == null) {
                preloadInterstitial();
            }
            onDone.run();
            return false;
        }

        interstitial = null;
        final boolean[] finished = {false};
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                lastInterstitialShownAt = SystemClock.elapsedRealtime();
                preloadInterstitial();
                finishOnce();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                Log.w(TAG, "Interstitial không hiện được: " + adError.getMessage());
                preloadInterstitial();
                finishOnce();
            }

            private void finishOnce() {
                if (!finished[0]) {
                    finished[0] = true;
                    onDone.run();
                }
            }
        });
        ad.show(activity);
        return true;
    }

    private static void preloadInterstitial() {
        if (!adsEnabled || interstitial != null || interstitialLoading || appContext == null) {
            return;
        }
        interstitialLoading = true;
        InterstitialAd.load(appContext, INTERSTITIAL_UNIT_ID, new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd ad) {
                        interstitialLoading = false;
                        // Nếu trong lúc tải người dùng đã chuyển sang Premium thì bỏ.
                        interstitial = adsEnabled ? ad : null;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError error) {
                        interstitialLoading = false;
                        interstitial = null;
                        Log.w(TAG, "Không tải được interstitial: " + error.getMessage());
                    }
                });
    }
}
