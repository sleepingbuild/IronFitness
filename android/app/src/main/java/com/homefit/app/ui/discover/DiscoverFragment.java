package com.homefit.app.ui.discover;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.homefit.app.R;
import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.util.AppExecutors;
import com.homefit.app.util.BmiCalculator;

import java.util.Locale;
import android.util.Log;

public class DiscoverFragment extends Fragment {

    private static final String TAG = "DiscoverFragment";
    private TextView tvBmiScore;
    private TextView tvBmiCategoryBadge;
    private TextView tvUserStats;
    private TextView tvBmiRecommendation;
    private UserProfileEntity userProfile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discover, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvBmiScore = view.findViewById(R.id.tvBmiScore);
        tvBmiCategoryBadge = view.findViewById(R.id.tvBmiCategoryBadge);
        tvUserStats = view.findViewById(R.id.tvUserStats);
        tvBmiRecommendation = view.findViewById(R.id.tvBmiRecommendation);

        MaterialButton btnUpdate = view.findViewById(R.id.btnUpdateHeightWeight);
        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> showUpdateBmiDialog());
        }

        View cardLibrary = view.findViewById(R.id.cardExerciseLibraryTool);
        MaterialButton btnOpenLibrary = view.findViewById(R.id.btnOpenLibrary);

        View.OnClickListener openLibraryListener = v -> {
            try {
                com.homefit.app.ui.workout.AddExerciseBottomSheetDialogFragment bottomSheet =
                        new com.homefit.app.ui.workout.AddExerciseBottomSheetDialogFragment();
                bottomSheet.show(getChildFragmentManager(), "AddExerciseModal");
            } catch (Exception e) {
                Log.e(TAG, "Lỗi mở modal 95 bài tập: " + e.getMessage());
                try {
                    Bundle args = new Bundle();
                    args.putString("groupKey", "category_1");
                    args.putString("difficulty", "Easy");
                    args.putString("groupName", "Thư viện 95 bài tập");
                    NavHostFragment.findNavController(this).navigate(R.id.action_discover_to_exerciseList, args);
                } catch (Exception ex) {
                    Log.e(TAG, "Lỗi điều hướng NavController: " + ex.getMessage());
                }
            }
        };

        if (cardLibrary != null) cardLibrary.setOnClickListener(openLibraryListener);
        if (btnOpenLibrary != null) btnOpenLibrary.setOnClickListener(openLibraryListener);

        setupArticleCards(view);
        loadBmiData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBmiData();
    }

    private void loadBmiData() {
        AppExecutors.diskIO().execute(() -> {
            userProfile = AppDatabase.getInstance(requireContext()).userProfileDao().getNow();
            AppExecutors.mainThread().execute(this::updateBmiUi);
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
            badgeText = "GẦY (NHẸ CÂN)";
            recommendation = "Khuyên dùng: Các bài tập Xây dựng cơ bắp (Muscle) & Thân trên + Lõi để tăng trọng lượng nạc và sức mạnh.";
        } else if (bmi <= 24.9) {
            badgeText = "BÌNH THƯỜNG";
            recommendation = "Khuyên dùng: Duy trì chuỗi tập Toàn Thân (Keep Fit) & Cardio hàng ngày để giữ vóc dáng cân đối và dẻo dai.";
        } else if (bmi <= 29.9) {
            badgeText = "THỪA CÂN";
            recommendation = "Khuyên dùng: Tập trung các buổi tập Đốt cháy mỡ (Fat Loss) & HIIT toàn thân để thiêu đốt calo dư thừa nhanh chóng.";
        } else {
            badgeText = "BÉO PHÌ";
            recommendation = "Khuyên dùng: Bắt đầu với các bài tập Đốt mỡ nhẹ nhàng (Easy Fat Loss) & Kéo giãn để bảo vệ khớp gối.";
        }

        if (tvBmiCategoryBadge != null) tvBmiCategoryBadge.setText(badgeText);
        if (tvBmiRecommendation != null) tvBmiRecommendation.setText("Khuyên dùng: " + recommendation);
    }

    private void showUpdateBmiDialog() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_configure_exercise, null, false);
        // Quick dialog for height & weight
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

        android.widget.LinearLayout container = new android.widget.LinearLayout(requireContext());
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
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
                            AppExecutors.mainThread().execute(() -> {
                                Toast.makeText(requireContext(), "Đã cập nhật chỉ số BMI", Toast.LENGTH_SHORT).show();
                                loadBmiData();
                            });
                        });
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void setupArticleCards(View view) {
        View cardNutrition = view.findViewById(R.id.cardTipNutrition);
        if (cardNutrition != null) {
            cardNutrition.setOnClickListener(v -> showArticleDialog(
                    "🥗 Dinh Dưỡng HomeFit - Bí quyết tăng 200% hiệu quả tập",
                    "1. Nguyên tắc 40-30-30:\nKết hợp 40% Carb phức hợp (khoai lang, yến mạch), 30% Đạm nạc (ức gà, trứng, cá) và 30% Chất béo tốt (bơ, hạt hạnh nhân).\n\n" +
                    "2. Uống đủ nước:\nUống 500ml nước trước buổi tập 30 phút và duy trì 2.5 lít nước mỗi ngày giúp trao đổi chất tốt hơn 30%.\n\n" +
                    "3. Bữa ăn sau tập:\nNạp 20g đạm trong vòng 45 phút sau khi hoàn thành buổi tập để sửa chữa sợi cơ tổn thương."
            ));
        }

        View cardRecovery = view.findViewById(R.id.cardTipRecovery);
        if (cardRecovery != null) {
            cardRecovery.setOnClickListener(v -> showArticleDialog(
                    "💤 Mẹo Phục Hồi Cơ Bắp - Giấc ngủ & Kéo giãn",
                    "1. Giấc ngủ vàng 7-8 tiếng:\nCơ bắp phát triển trong khi ngủ nhờ hormone tăng trưởng HGH tiết ra nhiều nhất từ 23:00 đến 02:00 sáng.\n\n" +
                    "2. Kéo giãn cơ hạ nhiệt (Cool-down):\nDành 5 phút tập các bài giãn cơ tĩnh (Child's Pose, Cobra stretch) giúp thải bỏ Axit Lactic gây mỏi cơ.\n\n" +
                    "3. Tắm nước ấm thả lỏng:\nTắm nước ấm sau khi ngưng mồ hôi 15 phút giúp làm giãn mạch máu và giảm đau nhức cơ bắp."
            ));
        }

        View cardCommunity = view.findViewById(R.id.cardTipCommunity);
        if (cardCommunity != null) {
            cardCommunity.setOnClickListener(v -> showArticleDialog(
                    "👥 Cộng Đồng HomeFit Việt Nam",
                    "Hơn 100.000+ người tập tại nhà đang rèn luyện cùng HomeFit!\n\n" +
                    "• Đạt mốc 14 ngày chuỗi tập Streak để nhận huy hiệu Chiến Binh.\n" +
                    "• Thử thách bản thân với chuỗi Thử Thách 28 Ngày từ Easy đến Hard.\n" +
                    "• Chia sẻ kết quả BMI và hình ảnh thay đổi vóc dáng cùng bạn bè."
            ));
        }
    }

    private void showArticleDialog(String title, String content) {
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton("Đã hiểu", null)
                .show();
    }
}
