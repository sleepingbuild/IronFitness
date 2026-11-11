package com.homefit.app.ui.common;

import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdView;
import com.homefit.app.ads.AdsManager;

public class BannerAdHelper {

    @Nullable
    private AdView adView;
    @Nullable
    private FrameLayout container;

    public void attach(@NonNull FrameLayout container) {
        if (this.container == container && adView != null) {
            return;
        }
        this.container = container;
        adView = AdsManager.attachBanner(container);
    }

    public void onResume() {
        if (container == null) {
            return;
        }
        if (!AdsManager.isAdsEnabled()) {
            removeBanner();
        } else if (adView == null) {
            adView = AdsManager.attachBanner(container);
        } else {
            adView.resume();
        }
    }

    public void onPause() {
        if (adView != null) {
            adView.pause();
        }
    }

    public void destroy() {
        removeBanner();
        container = null;
    }

    private void removeBanner() {
        if (adView != null) {
            adView.destroy();
            if (container != null) {
                container.removeView(adView);
            }
            adView = null;
        }
    }
}
