package com.homefit.app.ui.workout;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.homefit.app.R;
import com.homefit.app.data.local.model.CustomWorkoutItem;
import com.homefit.app.ui.common.MediaLoader;

import java.util.Locale;

public class ExerciseDetailBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnAddClickListener {
        void onAddClick();
    }

    private CustomWorkoutItem item;
    private OnAddClickListener onAddClickListener;

    public static ExerciseDetailBottomSheetDialogFragment newInstance(CustomWorkoutItem item) {
        ExerciseDetailBottomSheetDialogFragment fragment = new ExerciseDetailBottomSheetDialogFragment();
        fragment.item = item;
        return fragment;
    }

    public void setOnAddClickListener(OnAddClickListener listener) {
        this.onAddClickListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, com.google.android.material.R.style.Theme_Design_BottomSheetDialog);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setSkipCollapsed(true);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    bottomSheet.setLayoutParams(layoutParams);
                }
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_exercise_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (item == null) {
            dismiss();
            return;
        }

        TextView tvExerciseTitle = view.findViewById(R.id.tvExerciseTitle);
        ImageButton btnClose = view.findViewById(R.id.btnClose);
        ImageView ivMedia = view.findViewById(R.id.ivMedia);
        TextView tvParamLabel = view.findViewById(R.id.tvParamLabel);
        TextView tvParamValue = view.findViewById(R.id.tvParamValue);
        TextView tvInstructions = view.findViewById(R.id.tvInstructions);
        TextView chipMuscle1 = view.findViewById(R.id.chipMuscle1);
        TextView chipMuscle2 = view.findViewById(R.id.chipMuscle2);
        TextView chipMuscle3 = view.findViewById(R.id.chipMuscle3);
        ImageView ivMuscleDiagram = view.findViewById(R.id.ivMuscleDiagram);
        MaterialButton btnAdd = view.findViewById(R.id.btnAdd);

        // Header: Tên bài viết hoa
        String localizedName = item.exerciseName != null
                ? com.homefit.app.util.LocalizationHelper.getLocalizedExerciseName(requireContext(), item)
                : "BÀI TẬP";
        tvExerciseTitle.setText(localizedName.toUpperCase(Locale.getDefault()));

        btnClose.setOnClickListener(v -> dismiss());

        // Khung ảnh 2D minh họa
        if (item.mediaUrl != null && !item.mediaUrl.trim().isEmpty()) {
            MediaLoader.loadFull(ivMedia, item.mediaUrl, "IMAGE");
        } else {
            ivMedia.setImageResource(R.drawable.bg_media_placeholder);
        }

        // Dòng thông số gợi ý
        if (item.reps != null && item.reps > 0) {
            tvParamLabel.setText("LẦN LẶP LẠI");
            tvParamValue.setText("×" + item.reps);
        } else if (item.timeSec != null && item.timeSec > 0) {
            tvParamLabel.setText("THỜI GIAN");
            tvParamValue.setText(formatDuration(item.timeSec));
        } else {
            tvParamLabel.setText("LẦN LẶP LẠI");
            tvParamValue.setText("×10");
        }

        // Mục HƯỚNG DẪN
        String localizedDesc = com.homefit.app.util.LocalizationHelper.getLocalizedExerciseDescription(requireContext(), item);
        tvInstructions.setText(localizedDesc);

        // Mục VÙNG TẬP TRUNG (Chips & Sơ đồ cơ bắp)
        setupTargetMuscles(localizedName, item.target, chipMuscle1, chipMuscle2, chipMuscle3, ivMuscleDiagram);

        // Nút THÊM
        btnAdd.setOnClickListener(v -> {
            dismiss();
            if (onAddClickListener != null) {
                onAddClickListener.onAddClick();
            }
        });
    }

    private String formatDuration(int seconds) {
        int min = seconds / 60;
        int sec = seconds % 60;
        if (min > 0) {
            return String.format(Locale.US, "%02d:%02d", min, sec);
        }
        return seconds + "s";
    }

    private void setupTargetMuscles(String name, String target, TextView chip1, TextView chip2, TextView chip3, ImageView ivDiagram) {
        String combined = ((name != null ? name : "") + " " + (target != null ? target : "")).toLowerCase(Locale.US);
        String baseUrl = "https://ironfitness-et3g.onrender.com/images/muscles/";
        String coverUrl;

        if (combined.contains("bụng") || combined.contains("plank") || combined.contains("crunch") || combined.contains("abs")) {
            chip1.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cơ bụng"));
            chip2.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cơ xô"));
            chip3.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cơ lõi"));
            coverUrl = baseUrl + "abs.png";
        } else if (combined.contains("ngực") || combined.contains("push") || combined.contains("chống đẩy") || combined.contains("chest")) {
            chip1.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Ngực"));
            chip2.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cơ tay sau"));
            chip3.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Vai"));
            coverUrl = baseUrl + "chest.png";
        } else if (combined.contains("tay") || combined.contains("dip") || combined.contains("curl") || combined.contains("bicep") || combined.contains("tricep") || combined.contains("arms")) {
            chip1.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cơ tay trước"));
            chip2.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cơ tay sau"));
            chip3.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cẳng tay"));
            coverUrl = baseUrl + "arms.png";
        } else if (combined.contains("chân") || combined.contains("squat") || combined.contains("lunge") || combined.contains("calf") || combined.contains("bridge") || combined.contains("mông") || combined.contains("legs")) {
            chip1.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cơ đùi trước"));
            chip2.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cơ mông"));
            chip3.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Bắp chân"));
            coverUrl = baseUrl + "legs.png";
        } else if (combined.contains("lưng") || combined.contains("superman") || combined.contains("bird dog") || combined.contains("back")) {
            chip1.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cơ lưng dưới"));
            chip2.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cơ xô"));
            chip3.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cơ vai sau"));
            coverUrl = baseUrl + "back.png";
        } else {
            chip1.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Toàn thân"));
            chip2.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Cơ lõi"));
            chip3.setText("• " + com.homefit.app.util.LocalizationHelper.getLocalizedTarget(requireContext(), "Tim mạch"));
            coverUrl = baseUrl + "fullbody.png";
        }

        Glide.with(ivDiagram)
                .load(MediaLoader.resolveUrl(coverUrl))
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .fitCenter()
                .placeholder(R.drawable.bg_media_placeholder)
                .error(R.drawable.ic_workout_placeholder)
                .into(ivDiagram);
    }
}
