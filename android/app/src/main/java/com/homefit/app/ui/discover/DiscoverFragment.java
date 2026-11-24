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

        setupArticleCards(view);
        observeBmiData();
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

    private void setupArticleCards(View view) {
        String lang = com.homefit.app.util.LocalizationHelper.getLanguage(requireContext());

        View cardNutrition = view.findViewById(R.id.cardTipNutrition);
        if (cardNutrition != null) {
            cardNutrition.setOnClickListener(v -> {
                String title;
                String body;
                switch (lang) {
                    case "en":
                        title = "IronFitness Nutrition - 200% Workout Performance Secrets";
                        body = "1. 40-30-30 Macro Rule:\nCombine 40% complex carbs (sweet potatoes, oats), 30% lean protein (chicken breast, eggs, fish) and 30% healthy fats (avocados, almonds).\n\n" +
                                "2. Stay Hydrated:\nDrink 500ml water 30 min before training and maintain 2.5L daily for 30% faster metabolism.\n\n" +
                                "3. Post-Workout Meal:\nConsume 20g protein within 45 min after workout to repair damaged muscle fibers.";
                        break;
                    case "ko":
                        title = "아이언피트니스 영양 가이드 - 운동 효과 200% 높이기";
                        body = "1. 40-30-30 영양 비율:\n복합 탄수화물 40%(고구마, 귀리), 단백질 30%(닭가슴살, 계란, 생선), 건강한 지방 30%(아보카도, 견과류)를 조합하세요.\n\n" +
                                "2. 충분한 수분 섭취:\n운동 30분 전 물 500ml를 마시고 하루 2.5L를 유지하면 신진대사가 30% 향상됩니다.\n\n" +
                                "3. 운동 후 식사:\n운동 종료 후 45분 이내에 단백질 20g을 섭취하여 근육 손상을 회복하세요.";
                        break;
                    case "ja":
                        title = "IronFitness 栄養ガイド - 運動効果200%アップの秘訣";
                        body = "1. 40-30-30 PFCバランス:\n複合炭水化物40%（サツマイモ、オートミール）、タンパク質30%（鶏胸肉、卵、魚）、良質な脂質30%（アボカド、ナッツ）を摂取しましょう。\n\n" +
                                "2. 水分補給:\n運動30分前に500mlの水を飲み、1日2.5Lを維持すると代謝が30%向上します。\n\n" +
                                "3. 運動後の栄養補給:\nトレーニング終了後45分以内にタンパク質20gを摂取し、筋肉の修復を促しましょう。";
                        break;
                    default:
                        title = "Dinh Dưỡng IronFitness - Bí quyết tăng 200% hiệu quả tập";
                        body = "1. Nguyên tắc 40-30-30:\nKết hợp 40% Carb phức hợp (khoai lang, yến mạch), 30% Đạm nạc (ức gà, trứng, cá) và 30% Chất béo tốt (bơ, hạt hạnh nhân).\n\n" +
                                "2. Uống đủ nước:\nUống 500ml nước trước buổi tập 30 phút và duy trì 2.5 lít nước mỗi ngày giúp trao đổi chất tốt hơn 30%.\n\n" +
                                "3. Bữa ăn sau tập:\nNạp 20g đạm trong vòng 45 phút sau khi hoàn thành buổi tập để sửa chữa sợi cơ tổn thương.";
                        break;
                }
                showArticleDialog(title, body);
            });
        }

        View cardRecovery = view.findViewById(R.id.cardTipRecovery);
        if (cardRecovery != null) {
            cardRecovery.setOnClickListener(v -> {
                String title;
                String body;
                switch (lang) {
                    case "en":
                        title = "Muscle Recovery Tips - Sleep & Stretching";
                        body = "1. 7-8 Hours Quality Sleep:\nMuscles grow during deep sleep thanks to peak HGH growth hormone secretion between 11 PM and 2 AM.\n\n" +
                                "2. Cool-down Stretching:\nSpend 5 min doing static stretches (Child's Pose, Cobra stretch) to clear muscle-fatiguing lactic acid.\n\n" +
                                "3. Warm Shower Relaxation:\nTake a warm shower 15 min after sweating stops to dilate blood vessels and ease muscle soreness.";
                        break;
                    case "ko":
                        title = "근육 회복 팁 - 수면 & 스트레칭";
                        body = "1. 7-8시간 황금 수면:\n성장 호르몬 HGH가 가장 많이 분비되는 밤 11시부터 새벽 2시 사이에 깊은 수면을 취하세요.\n\n" +
                                "2. 쿨다운 스트레칭:\n운동 후 5분간 정적 스트레칭(Child's Pose, Cobra stretch)을 하여 젖산을 배출하세요.\n\n" +
                                "3. 따뜻한 샤워:\n땀이 식은 후 15분 뒤 따뜻한 물로 샤워하면 혈관이 확장되고 근육통이 완화됩니다.";
                        break;
                    case "ja":
                        title = "リカバリーのコツ - 睡眠 & ストレッチ";
                        body = "1. 7〜8時間の質の良い睡眠:\n成長ホルモンHGHが最も分泌される午後11時から午前2時の間に深い睡眠をとりましょう。\n\n" +
                                "2. クールダウンストレッチ:\n運動後5分間の静的ストレッチ（Child's Pose、Cobra stretch）で乳酸を排出し疲労を軽減します。\n\n" +
                                "3. 温水シャワーでリラックス:\n汗が引いた15分後にぬるめのお湯を浴びると、血管が拡張し筋肉痛が和らぎます。";
                        break;
                    default:
                        title = "Mẹo Phục Hồi Cơ Bắp - Giấc ngủ & Kéo giãn";
                        body = "1. Giấc ngủ vàng 7-8 tiếng:\nCơ bắp phát triển trong khi ngủ nhờ hormone tăng trưởng HGH tiết ra nhiều nhất từ 23:00 đến 02:00 sáng.\n\n" +
                                "2. Kéo giãn cơ hạ nhiệt (Cool-down):\nDành 5 phút tập các bài giãn cơ tĩnh (Child's Pose, Cobra stretch) giúp thải bỏ Axit Lactic gây mỏi cơ.\n\n" +
                                "3. Tắm nước ấm thả lỏng:\nTắm nước ấm sau khi ngưng mồ hôi 15 phút giúp làm giãn mạch máu và giảm đau nhức cơ bắp.";
                        break;
                }
                showArticleDialog(title, body);
            });
        }

        View cardCommunity = view.findViewById(R.id.cardTipCommunity);
        if (cardCommunity != null) {
            cardCommunity.setOnClickListener(v -> {
                String title;
                String body;
                switch (lang) {
                    case "en":
                        title = "IronFitness Community";
                        body = "Over 100,000+ home athletes are training with IronFitness!\n\n" +
                                "• Reach a 14-day Streak milestone to unlock the Warrior Badge.\n" +
                                "• Challenge yourself with 28-Day Challenge programs from Easy to Hard.\n" +
                                "• Share your BMI progress and body transformation photos with friends.";
                        break;
                    case "ko":
                        title = "아이언피트니스 커뮤니티";
                        body = "10만 명 이상의 홈 트레이너가 아이언피트니스와 함께 운동하고 있습니다!\n\n" +
                                "• 14일 연속 운동 스트릭을 달성하여 워리어 배지를 획득하세요.\n" +
                                "• Easy부터 Hard까지 28일 도전 프로그램에 도전하세요.\n" +
                                "• BMI 변화와 변화된 체형 사진을 친구들과 공유하세요.";
                        break;
                    case "ja":
                        title = "IronFitness コミュニティ";
                        body = "10万人以上の宅トレユーザーがIronFitnessでトレーニング中！\n\n" +
                                "• 14日間の連続トレーニングを達成して「ウォリアーバッジ」を獲得しましょう。\n" +
                                "• 初級から上級までの28日間チャレンジプログラムに挑戦。\n" +
                                "• BMIの変化や体型のビフォーアフターを友達とシェアしましょう。";
                        break;
                    default:
                        title = "Cộng Đồng IronFitness Việt Nam";
                        body = "Hơn 100.000+ người tập tại nhà đang rèn luyện cùng IronFitness!\n\n" +
                                "• Đạt mốc 14 ngày chuỗi tập Streak để nhận huy hiệu Chiến Binh.\n" +
                                "• Thử thách bản thân với chuỗi Thử Thách 28 Ngày từ Easy đến Hard.\n" +
                                "• Chia sẻ kết quả BMI và hình ảnh thay đổi vóc dáng cùng bạn bè.";
                        break;
                }
                showArticleDialog(title, body);
            });
        }
    }

    private void showArticleDialog(String title, String content) {
        String lang = com.homefit.app.util.LocalizationHelper.getLanguage(requireContext());
        String buttonText;
        switch (lang) {
            case "en": buttonText = "Got it"; break;
            case "ko": buttonText = "확인"; break;
            case "ja": buttonText = "了解"; break;
            default: buttonText = "Đã hiểu"; break;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(buttonText, null)
                .show();
    }
}
