package com.homefit.app.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.homefit.app.R;
import com.homefit.app.data.repository.ChallengeRepository;

public class ChallengeDifficultyBottomSheetFragment extends BottomSheetDialogFragment {

    public interface OnDifficultyChangedListener {
        void onDifficultyChanged(String newLevel);
    }

    private OnDifficultyChangedListener listener;
    private ChallengeRepository repository;

    private MaterialCardView cardEasy, cardMedium, cardHard;
    private RadioButton rbEasy, rbMedium, rbHard;
    private TextView tvEasyProgress, tvMediumProgress, tvHardProgress;
    private Button btnResetCurrentLevel;
    private MaterialButton btnSelect;

    private String currentSelectedLevel = "easy";
    private String activeLevel = "easy";

    public void setOnDifficultyChangedListener(OnDifficultyChangedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_challenge_difficulty, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new ChallengeRepository(requireContext());
        activeLevel = repository.getSelectedLevel();
        currentSelectedLevel = activeLevel;

        cardEasy = view.findViewById(R.id.cardEasy);
        cardMedium = view.findViewById(R.id.cardMedium);
        cardHard = view.findViewById(R.id.cardHard);
        rbEasy = view.findViewById(R.id.rbEasy);
        rbMedium = view.findViewById(R.id.rbMedium);
        rbHard = view.findViewById(R.id.rbHard);
        tvEasyProgress = view.findViewById(R.id.tvEasyProgress);
        tvMediumProgress = view.findViewById(R.id.tvMediumProgress);
        tvHardProgress = view.findViewById(R.id.tvHardProgress);
        btnResetCurrentLevel = view.findViewById(R.id.btnResetCurrentLevel);
        btnSelect = view.findViewById(R.id.btnSelect);

        updateSelectionUI(currentSelectedLevel);
        updateProgressUI();

        cardEasy.setOnClickListener(v -> updateSelectionUI("easy"));
        cardMedium.setOnClickListener(v -> updateSelectionUI("medium"));
        cardHard.setOnClickListener(v -> updateSelectionUI("hard"));

        btnResetCurrentLevel.setOnClickListener(v -> confirmResetLevel());

        btnSelect.setOnClickListener(v -> {
            if (!currentSelectedLevel.equalsIgnoreCase(activeLevel)) {
                confirmSwitchLevel(currentSelectedLevel);
            } else {
                dismiss();
            }
        });
    }

    private void updateSelectionUI(String level) {
        currentSelectedLevel = level;
        rbEasy.setChecked("easy".equalsIgnoreCase(level));
        rbMedium.setChecked("medium".equalsIgnoreCase(level));
        rbHard.setChecked("hard".equalsIgnoreCase(level));

        int activeColor = requireContext().getColor(R.color.hf_primary);
        int inactiveColor = requireContext().getColor(R.color.hf_outline);

        cardEasy.setStrokeColor("easy".equalsIgnoreCase(level) ? activeColor : inactiveColor);
        cardMedium.setStrokeColor("medium".equalsIgnoreCase(level) ? activeColor : inactiveColor);
        cardHard.setStrokeColor("hard".equalsIgnoreCase(level) ? activeColor : inactiveColor);

        btnResetCurrentLevel.setText("Làm lại độ khó " + repository.getLevelTitleVi(currentSelectedLevel));
    }

    private void updateProgressUI() {
        int easyCount = repository.getCompletedCount("easy");
        int mediumCount = repository.getCompletedCount("medium");
        int hardCount = repository.getCompletedCount("hard");

        tvEasyProgress.setText(easyCount > 0 ? (easyCount == 24 ? "Đã xong" : easyCount + "/24") : "Chưa tập");
        tvMediumProgress.setText(mediumCount > 0 ? (mediumCount == 24 ? "Đã xong" : mediumCount + "/24") : "Chưa tập");
        tvHardProgress.setText(hardCount > 0 ? (hardCount == 24 ? "Đã xong" : hardCount + "/24") : "Chưa tập");
    }

    private void confirmSwitchLevel(String newLevel) {
        String levelName = repository.getLevelTitleVi(newLevel);
        int completed = repository.getCompletedCount(newLevel);
        String msg = "Tiến độ của mỗi độ khó được tính riêng.\nĐộ khó " + levelName + " " +
                (completed > 0 ? "sẽ tiếp tục (" + completed + "/24 buổi)." : "sẽ bắt đầu từ Ngày 1.");

        new AlertDialog.Builder(requireContext())
                .setTitle("Chuyển độ khó")
                .setMessage(msg)
                .setPositiveButton("Chuyển", (dialog, which) -> {
                    repository.setSelectedLevel(newLevel);
                    if (listener != null) {
                        listener.onDifficultyChanged(newLevel);
                    }
                    dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void confirmResetLevel() {
        String levelName = repository.getLevelTitleVi(currentSelectedLevel);
        new AlertDialog.Builder(requireContext())
                .setTitle("Làm lại độ khó " + levelName)
                .setMessage("Bạn có chắc chắn muốn xóa tiến độ của độ khó " + levelName + " và bắt đầu lại từ Ngày 1?")
                .setPositiveButton("Làm lại", (dialog, which) -> {
                    repository.resetLevelProgress(currentSelectedLevel);
                    updateProgressUI();
                    if (currentSelectedLevel.equalsIgnoreCase(activeLevel) && listener != null) {
                        listener.onDifficultyChanged(currentSelectedLevel);
                    }
                    Toast.makeText(requireContext(), "Đã làm lại độ khó " + levelName, Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
