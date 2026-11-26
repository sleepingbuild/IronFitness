package com.homefit.app.ui.discover;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.homefit.app.R;
import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.remote.dto.DiscoverDto;
import com.homefit.app.data.repository.DiscoverRepository;
import com.homefit.app.ui.common.MediaLoader;
import com.homefit.app.util.AppExecutors;
import com.homefit.app.util.BmiCalculator;
import com.homefit.app.util.LocalizationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DiscoverFragment extends Fragment {

    private static final String TAG = "DiscoverFragment";

    private TextView tvBmiScore;
    private TextView tvBmiCategoryBadge;
    private TextView tvUserStats;
    private TextView tvBmiRecommendation;
    private UserProfileEntity userProfile;

    // Dynamic views for Server Discover items
    private MaterialCardView cardDiscoverBanner;
    private ImageView ivBannerCover;
    private TextView tvBannerCategory;
    private TextView tvBannerTitle;
    private TextView tvBannerDescription;
    private MaterialButton btnBannerAction;

    private View layoutTipsSection;
    private RecyclerView rvDiscoverTips;
    private DiscoverTipAdapter tipAdapter;

    private View layoutEcosystemSection;
    private RecyclerView rvDiscoverEcosystem;
    private DiscoverEcosystemAdapter ecosystemAdapter;

    private DiscoverRepository discoverRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        discoverRepository = new DiscoverRepository(requireContext());

        // BMI Views
        tvBmiScore = view.findViewById(R.id.tvBmiScore);
        tvBmiCategoryBadge = view.findViewById(R.id.tvBmiCategoryBadge);
        tvUserStats = view.findViewById(R.id.tvUserStats);
        tvBmiRecommendation = view.findViewById(R.id.tvBmiRecommendation);

        MaterialButton btnUpdate = view.findViewById(R.id.btnUpdateHeightWeight);
        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> showUpdateBmiDialog());
        }

        // 94 Exercises Library view
        View cardLibrary = view.findViewById(R.id.cardExerciseLibraryTool);
        MaterialButton btnOpenLibrary = view.findViewById(R.id.btnOpenLibrary);

        View.OnClickListener openLibraryListener = v -> {
            try {
                com.homefit.app.ui.workout.AddExerciseBottomSheetDialogFragment bottomSheet =
                        new com.homefit.app.ui.workout.AddExerciseBottomSheetDialogFragment();
                bottomSheet.show(getChildFragmentManager(), "AddExerciseModal");
            } catch (Exception e) {
                Log.e(TAG, "Lỗi mở modal 94 bài tập: " + e.getMessage());
                try {
                    Bundle args = new Bundle();
                    args.putString("groupKey", "category_1");
                    args.putString("difficulty", "Easy");
                    args.putString("groupName", "Thư viện 94 bài tập");
                    NavHostFragment.findNavController(this).navigate(R.id.action_discover_to_exerciseList, args);
                } catch (Exception ex) {
                    Log.e(TAG, "Lỗi điều hướng NavController: " + ex.getMessage());
                }
            }
        };

        if (cardLibrary != null) cardLibrary.setOnClickListener(openLibraryListener);
        if (btnOpenLibrary != null) btnOpenLibrary.setOnClickListener(openLibraryListener);

        // Dynamic Banner Views
        cardDiscoverBanner = view.findViewById(R.id.cardDiscoverBanner);
        ivBannerCover = view.findViewById(R.id.ivBannerCover);
        tvBannerCategory = view.findViewById(R.id.tvBannerCategory);
        tvBannerTitle = view.findViewById(R.id.tvBannerTitle);
        tvBannerDescription = view.findViewById(R.id.tvBannerDescription);
        btnBannerAction = view.findViewById(R.id.btnBannerAction);

        // Tips Section Views
        layoutTipsSection = view.findViewById(R.id.layoutTipsSection);
        rvDiscoverTips = view.findViewById(R.id.rvDiscoverTips);
        if (rvDiscoverTips != null) {
            rvDiscoverTips.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            tipAdapter = new DiscoverTipAdapter(requireContext(), this::showArticleDetailDialog);
            rvDiscoverTips.setAdapter(tipAdapter);
        }

        // Ecosystem Section Views
        layoutEcosystemSection = view.findViewById(R.id.layoutEcosystemSection);
        rvDiscoverEcosystem = view.findViewById(R.id.rvDiscoverEcosystem);
        if (rvDiscoverEcosystem != null) {
            rvDiscoverEcosystem.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
            ecosystemAdapter = new DiscoverEcosystemAdapter(requireContext(), this::handleDiscoverItemClick);
            rvDiscoverEcosystem.setAdapter(ecosystemAdapter);
        }

        observeBmiData();
        loadDiscoverData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDiscoverData();
    }

    private void loadDiscoverData() {
        if (!isAdded()) return;
        discoverRepository.fetchDiscoverItems(new DiscoverRepository.Callback<List<DiscoverDto>>() {
            @Override
            public void onResult(List<DiscoverDto> data, boolean fromCache) {
                if (!isAdded() || data == null) return;
                renderDiscoverSections(data);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.w(TAG, "Không thể nạp dữ liệu Khám phá từ server: " + throwable.getMessage());
            }
        });
    }

    private void renderDiscoverSections(@NonNull List<DiscoverDto> allItems) {
        String lang = LocalizationHelper.getLanguage(requireContext());

        DiscoverDto bannerItem = null;
        List<DiscoverDto> tipsList = new ArrayList<>();
        List<DiscoverDto> ecosystemList = new ArrayList<>();

        for (DiscoverDto item : allItems) {
            if (!item.active) continue;

            String cat = item.category != null ? item.category.trim() : "";
            if (cat.equalsIgnoreCase("Giới thiệu App") || cat.equalsIgnoreCase("App Intro") || cat.equalsIgnoreCase("Banner")) {
                if (bannerItem == null) {
                    bannerItem = item;
                }
            } else if (cat.equalsIgnoreCase("Hệ Sinh Thái Dự Án") || cat.equalsIgnoreCase("Hệ sinh thái") || cat.equalsIgnoreCase("Ecosystem")) {
                ecosystemList.add(item);
            } else {
                tipsList.add(item);
            }
        }

        // 1. Render Banner Hero
        if (bannerItem != null && cardDiscoverBanner != null) {
            final DiscoverDto finalBanner = bannerItem;
            cardDiscoverBanner.setVisibility(View.VISIBLE);

            if (tvBannerCategory != null) {
                tvBannerCategory.setText(finalBanner.category != null ? finalBanner.category : getString(R.string.discover_app_intro_section));
            }
            if (tvBannerTitle != null) {
                tvBannerTitle.setText(finalBanner.getTitle(lang));
            }
            if (tvBannerDescription != null) {
                tvBannerDescription.setText(finalBanner.getDescriptionText());
            }

            String coverUrl = MediaLoader.resolveUrl(finalBanner.getCover());
            if (ivBannerCover != null) {
                if (!coverUrl.isEmpty()) {
                    Glide.with(this)
                            .load(coverUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.bg_media_placeholder)
                            .error(R.drawable.ic_workout_placeholder)
                            .centerCrop()
                            .into(ivBannerCover);
                } else {
                    ivBannerCover.setImageResource(R.drawable.bg_media_placeholder);
                }
            }

            View.OnClickListener bannerClickListener = v -> handleDiscoverItemClick(finalBanner);
            cardDiscoverBanner.setOnClickListener(bannerClickListener);
            if (btnBannerAction != null) {
                btnBannerAction.setOnClickListener(bannerClickListener);
            }
        } else if (cardDiscoverBanner != null) {
            cardDiscoverBanner.setVisibility(View.GONE);
        }

        // 2. Render Góc Kiến Thức & Mẹo
        if (layoutTipsSection != null && tipAdapter != null) {
            if (!tipsList.isEmpty()) {
                layoutTipsSection.setVisibility(View.VISIBLE);
                tipAdapter.setItems(tipsList);
            } else {
                layoutTipsSection.setVisibility(View.GONE);
            }
        }

        // 3. Render Hệ Sinh Thái Dự Án
        if (layoutEcosystemSection != null && ecosystemAdapter != null) {
            if (!ecosystemList.isEmpty()) {
                layoutEcosystemSection.setVisibility(View.VISIBLE);
                ecosystemAdapter.setItems(ecosystemList);
            } else {
                layoutEcosystemSection.setVisibility(View.GONE);
            }
        }
    }

    private void handleDiscoverItemClick(DiscoverDto item) {
        if (item == null) return;
        String action = item.getAction();

        if (action != null && (action.equalsIgnoreCase("/pro") || action.contains("upgrade-pro"))) {
            try {
                NavHostFragment.findNavController(this).navigate(R.id.action_discover_to_proPaywall);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi mở màn hình PRO: " + e.getMessage());
                Toast.makeText(requireContext(), "HomeFit PRO", Toast.LENGTH_SHORT).show();
            }
        } else if (action != null && (action.startsWith("http://") || action.startsWith("https://"))) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(action));
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Lỗi mở URL: " + e.getMessage());
                showArticleDetailDialog(item);
            }
        } else {
            showArticleDetailDialog(item);
        }
    }

    private void showArticleDetailDialog(DiscoverDto item) {
        if (item == null || !isAdded()) return;

        try {
            BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
            View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_discover_detail, null, false);

            ImageView ivCover = view.findViewById(R.id.ivDetailCover);
            TextView tvCategory = view.findViewById(R.id.tvDetailCategory);
            TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
            TextView tvContent = view.findViewById(R.id.tvDetailContent);
            MaterialButton btnClose = view.findViewById(R.id.btnDetailClose);

            String lang = LocalizationHelper.getLanguage(requireContext());

            if (tvCategory != null) {
                tvCategory.setText(item.category != null ? item.category : getString(R.string.discover_tips_section));
            }
            if (tvTitle != null) {
                tvTitle.setText(item.getTitle(lang));
            }
            if (tvContent != null) {
                tvContent.setText(item.getDescriptionText());
            }

            String coverUrl = MediaLoader.resolveUrl(item.getCover());
            if (ivCover != null) {
                if (!coverUrl.isEmpty()) {
                    Glide.with(requireContext())
                            .load(coverUrl)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .placeholder(R.drawable.bg_media_placeholder)
                            .error(R.drawable.ic_workout_placeholder)
                            .centerCrop()
                            .into(ivCover);
                } else {
                    ivCover.setImageResource(R.drawable.bg_media_placeholder);
                }
            }

            if (btnClose != null) {
                btnClose.setOnClickListener(v -> dialog.dismiss());
            }

            dialog.setContentView(view);
            dialog.show();

        } catch (Exception e) {
            Log.e(TAG, "Lỗi hiển thị dialog chi tiết bài viết: " + e.getMessage());
            new AlertDialog.Builder(requireContext())
                    .setTitle(item.getTitle(LocalizationHelper.getLanguage(requireContext())))
                    .setMessage(item.getDescriptionText())
                    .setPositiveButton(R.string.discover_close, null)
                    .show();
        }
    }

    private void observeBmiData() {
        AppDatabase.getInstance(requireContext()).userProfileDao().get().observe(getViewLifecycleOwner(), profile -> {
            this.userProfile = profile;
            updateBmiUi();
        });
    }

    private void updateBmiUi() {
        if (!isAdded()) return;

        double height = (userProfile != null && userProfile.heightCm != null && userProfile.heightCm > 0) ? userProfile.heightCm : 170.0;
        double weight = (userProfile != null && userProfile.weightKg != null && userProfile.weightKg > 0) ? userProfile.weightKg : 65.0;

        double bmi = BmiCalculator.calculate(height, weight);
        if (tvBmiScore != null) {
            tvBmiScore.setText(String.format(Locale.US, "%.1f", bmi));
        }

        if (tvUserStats != null) {
            tvUserStats.setText(String.format(Locale.US, "%.0f cm • %.1f kg", height, weight));
        }

        String badgeText;
        String recommendation;

        if (bmi < 18.5) {
            badgeText = getString(R.string.bmi_badge_underweight);
            recommendation = getString(R.string.bmi_rec_underweight);
        } else if (bmi <= 24.9) {
            badgeText = getString(R.string.bmi_badge_normal);
            recommendation = getString(R.string.bmi_rec_normal);
        } else if (bmi <= 29.9) {
            badgeText = getString(R.string.bmi_badge_overweight);
            recommendation = getString(R.string.bmi_rec_overweight);
        } else {
            badgeText = getString(R.string.bmi_badge_obese);
            recommendation = getString(R.string.bmi_rec_obese);
        }

        if (tvBmiCategoryBadge != null) tvBmiCategoryBadge.setText(badgeText);
        if (tvBmiRecommendation != null) tvBmiRecommendation.setText(recommendation);
    }

    private void showUpdateBmiDialog() {
        final EditText etHeight = new EditText(requireContext());
        etHeight.setHint("Chiều cao (cm)");
        etHeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        final EditText etWeight = new EditText(requireContext());
        etWeight.setHint("Cân nặng (kg)");
        etWeight.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        if (userProfile != null) {
            if (userProfile.heightCm != null) etHeight.setText(String.valueOf(userProfile.heightCm));
            if (userProfile.weightKg != null) etWeight.setText(String.valueOf(userProfile.weightKg));
        }

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(48, 24, 48, 24);
        container.addView(etHeight);
        container.addView(etWeight);

        new AlertDialog.Builder(requireContext())
                .setTitle("Cập nhật Chiều cao & Cân nặng")
                .setView(container)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    try {
                        String hStr = etHeight.getText().toString().trim();
                        String wStr = etWeight.getText().toString().trim();
                        if (!hStr.isEmpty()) {
                            double h = Double.parseDouble(hStr);
                            if (h > 50 && h < 250) {
                                if (userProfile == null) userProfile = new UserProfileEntity();
                                userProfile.heightCm = h;
                            }
                        }
                        if (!wStr.isEmpty()) {
                            double w = Double.parseDouble(wStr);
                            if (w > 20 && w < 300) {
                                if (userProfile == null) userProfile = new UserProfileEntity();
                                userProfile.weightKg = w;
                            }
                        }
                        AppExecutors.diskIO().execute(() -> {
                            AppDatabase.getInstance(requireContext()).userProfileDao().upsert(userProfile);
                            new com.homefit.app.data.repository.AuthRepository(requireContext()).updateProfile(
                                    userProfile.displayName != null ? userProfile.displayName : "Hội viên",
                                    userProfile.heightCm,
                                    userProfile.weightKg
                            );
                            AppExecutors.mainThread().execute(() -> {
                                Toast.makeText(requireContext(), "Đã cập nhật chỉ số BMI", Toast.LENGTH_SHORT).show();
                            });
                        });
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
