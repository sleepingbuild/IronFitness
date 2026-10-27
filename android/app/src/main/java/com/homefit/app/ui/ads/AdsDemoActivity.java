package com.homefit.app.ui.ads;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.ads.AdView;
import com.homefit.app.R;
import com.homefit.app.ads.AdsManager;
import com.homefit.app.databinding.ActivityAdsDemoBinding;

/**
 * Màn hình TẠM để minh chứng tích hợp AdMob (W1-TV1-08).
 * Sẽ được thay bằng Splash/màn chính khi Agent 2 và Agent 3 giao màn hình thật.
 */
public class AdsDemoActivity extends AppCompatActivity {

    private ActivityAdsDemoBinding binding;
    @Nullable
    private AdView bannerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdsDemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Chừa chỗ cho thanh trạng thái và thanh điều hướng (màn hình tràn viền).
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        binding.btnShowInterstitial.setOnClickListener(v -> {
            boolean shown = AdsManager.showInterstitial(this, () -> {
                Toast.makeText(this, R.string.ads_demo_toast_closed, Toast.LENGTH_SHORT).show();
                updateStatus();
            }, true);
            if (!shown) {
                Toast.makeText(this, R.string.ads_demo_toast_not_ready, Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnTogglePremium.setOnClickListener(v -> {
            AdsManager.setAdsEnabled(!AdsManager.isAdsEnabled());
            refreshBanner();
            updateStatus();
        });

        binding.btnRefreshStatus.setOnClickListener(v -> updateStatus());

        refreshBanner();
        updateStatus();
    }

    private void refreshBanner() {
        if (bannerView != null) {
            bannerView.destroy();
            bannerView = null;
        }
        bannerView = AdsManager.attachBanner(binding.adContainer);
    }

    private void updateStatus() {
        boolean enabled = AdsManager.isAdsEnabled();
        binding.tvAdsStatus.setText(enabled
                ? R.string.ads_demo_status_on : R.string.ads_demo_status_off);
        binding.tvInterstitialStatus.setText(AdsManager.isInterstitialReady()
                ? R.string.ads_demo_interstitial_ready : R.string.ads_demo_interstitial_not_ready);
        binding.btnTogglePremium.setText(enabled
                ? R.string.ads_demo_btn_premium_on : R.string.ads_demo_btn_premium_off);
        binding.btnShowInterstitial.setEnabled(enabled);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bannerView != null) {
            bannerView.resume();
        }
        updateStatus();
    }

    @Override
    protected void onPause() {
        if (bannerView != null) {
            bannerView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (bannerView != null) {
            bannerView.destroy();
            bannerView = null;
        }
        binding = null;
        super.onDestroy();
    }
}
