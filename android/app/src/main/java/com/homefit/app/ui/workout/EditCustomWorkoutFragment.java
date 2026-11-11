package com.homefit.app.ui.workout;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import com.homefit.app.R;
import com.homefit.app.data.local.model.CustomWorkout;
import com.homefit.app.data.local.model.CustomWorkoutItem;
import com.homefit.app.data.repository.CustomWorkoutRepository;

import java.util.ArrayList;
import java.util.List;

public class EditCustomWorkoutFragment extends Fragment {

    private CustomWorkoutRepository repository;
    private CustomWorkout currentWorkout;

    private TextView tvWorkoutSummary;
    private LinearLayout llWarningBanner;
    private TextView tvWarningText;
    private Button btnWarningAction;
    private MaterialButton btnSave;
    private MaterialButton btnStart;

    private EditCustomWorkoutItemAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_custom_workout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repository = new CustomWorkoutRepository(requireContext());

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        EditText etWorkoutTitle = view.findViewById(R.id.etWorkoutTitle);
        ImageButton btnSettings = view.findViewById(R.id.btnSettings);
        tvWorkoutSummary = view.findViewById(R.id.tvWorkoutSummary);
        MaterialButton btnAddExercise = view.findViewById(R.id.btnAddExercise);
        llWarningBanner = view.findViewById(R.id.llWarningBanner);
        tvWarningText = view.findViewById(R.id.tvWarningText);
        btnWarningAction = view.findViewById(R.id.btnWarningAction);
        RecyclerView rvExerciseItems = view.findViewById(R.id.rvExerciseItems);
        btnSave = view.findViewById(R.id.btnSave);
        btnStart = view.findViewById(R.id.btnStart);

        String workoutId = getArguments() != null ? getArguments().getString("workoutId") : null;

        if (workoutId != null) {
            CustomWorkout loaded = repository.getWorkoutById(workoutId);
            if (loaded != null) {
                currentWorkout = loaded;
            }
        }

        if (currentWorkout == null) {
            currentWorkout = new CustomWorkout();
            List<CustomWorkout> all = repository.getAllWorkouts();
            currentWorkout.title = "Buổi tập của tôi " + (all.size() + 1);
        }

        etWorkoutTitle.setText(currentWorkout.title);

        adapter = new EditCustomWorkoutItemAdapter(new EditCustomWorkoutItemAdapter.OnItemActionListener() {
            @Override
            public void onItemDelete(int position, CustomWorkoutItem item) {
                currentWorkout.items.remove(position);
                adapter.setItems(currentWorkout.items);
                updateSummaryAndWarnings();
            }

            @Override
            public void onItemClickValue(int position, CustomWorkoutItem item) {
                showValueEditDialog(position, item);
            }
        });

        rvExerciseItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExerciseItems.setAdapter(adapter);
        adapter.setItems(currentWorkout.items);

        btnBack.setOnClickListener(v -> handleBackPress());

        btnSettings.setOnClickListener(v -> showSettingsDialog());

        btnAddExercise.setOnClickListener(v -> showAddExerciseBottomSheet());

        etWorkoutTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentWorkout.title = s.toString().trim();
                updateSaveButtonState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSave.setOnClickListener(v -> {
            saveWorkout();
            Toast.makeText(requireContext(), "Đã lưu buổi tập", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).navigateUp();
        });

        btnStart.setOnClickListener(v -> {
            saveWorkout();
            startWorkoutPlayer();
        });

        updateSummaryAndWarnings();
    }

    private void handleBackPress() {
        NavHostFragment.findNavController(this).navigateUp();
    }

    private void showSettingsDialog() {
        final EditText etRest = new EditText(requireContext());
        etRest.setInputType(InputType.TYPE_CLASS_NUMBER);
        etRest.setText(String.valueOf(currentWorkout.settings.restSec));

        new AlertDialog.Builder(requireContext())
                .setTitle("Cài đặt buổi tập")
                .setMessage("Thời gian nghỉ giữa các bài (giây):")
                .setView(etRest)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    try {
                        int rest = Integer.parseInt(etRest.getText().toString().trim());
                        if (rest >= 0 && rest <= 120) {
                            currentWorkout.settings.restSec = rest;
                            for (CustomWorkoutItem item : currentWorkout.items) {
                                item.restAfterSec = rest;
                            }
                            updateSummaryAndWarnings();
                        }
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showAddExerciseBottomSheet() {
        AddExerciseBottomSheetDialogFragment bottomSheet = new AddExerciseBottomSheetDialogFragment();
        bottomSheet.setOnExercisesSelectedListener(selectedItems -> {
            if (currentWorkout.items == null) {
                currentWorkout.items = new ArrayList<>();
            }
            for (CustomWorkoutItem selected : selectedItems) {
                CustomWorkoutItem newItem = new CustomWorkoutItem();
                newItem.exerciseId = selected.exerciseId;
                newItem.exerciseName = selected.exerciseName;
                newItem.target = selected.target;
                newItem.reps = selected.reps;
                newItem.sets = selected.sets;
                newItem.timeSec = selected.timeSec;
                newItem.measure = selected.measure;
                newItem.value = selected.value;
                newItem.restAfterSec = currentWorkout.settings.restSec;
                newItem.instructions = selected.instructions;
                newItem.tips = selected.tips;
                currentWorkout.items.add(newItem);
            }
            adapter.setItems(currentWorkout.items);
            updateSummaryAndWarnings();
        });
        bottomSheet.show(getChildFragmentManager(), "AddExerciseBottomSheet");
    }

    private void showValueEditDialog(int position, CustomWorkoutItem item) {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_configure_exercise, null, false);
        TextView tvExerciseTitle = view.findViewById(R.id.tvExerciseTitle);
        TextInputEditText etReps = view.findViewById(R.id.etReps);
        TextInputEditText etSets = view.findViewById(R.id.etSets);
        TextInputEditText etTimeSec = view.findViewById(R.id.etTimeSec);

        tvExerciseTitle.setText(item.exerciseName != null ? item.exerciseName : "Bài tập");

        if (item.reps != null && item.reps > 0) {
            etReps.setText(String.valueOf(item.reps));
        }
        if (item.sets != null && item.sets > 0) {
            etSets.setText(String.valueOf(item.sets));
        }
        if (item.timeSec != null && item.timeSec > 0) {
            etTimeSec.setText(String.valueOf(item.timeSec));
        }

        new AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String repsStr = etReps.getText() != null ? etReps.getText().toString().trim() : "";
                    String setsStr = etSets.getText() != null ? etSets.getText().toString().trim() : "";
                    String timeStr = etTimeSec.getText() != null ? etTimeSec.getText().toString().trim() : "";

                    item.reps = parseOptionalInt(repsStr);
                    item.sets = parseOptionalInt(setsStr);
                    item.timeSec = parseOptionalInt(timeStr);

                    adapter.notifyItemChanged(position);
                    updateSummaryAndWarnings();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private Integer parseOptionalInt(String text) {
        if (text == null || text.isEmpty()) return null;
        try {
            int val = Integer.parseInt(text);
            return val > 0 ? val : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateSummaryAndWarnings() {
        int duration = currentWorkout.calculateDurationMin();
        int count = currentWorkout.items != null ? currentWorkout.items.size() : 0;
        tvWorkoutSummary.setText(duration + " phút • " + count + " bài");

        boolean hasWarmup = false;
        boolean hasCooldown = false;

        if (currentWorkout.items != null && !currentWorkout.items.isEmpty()) {
            CustomWorkoutItem first = currentWorkout.items.get(0);
            CustomWorkoutItem last = currentWorkout.items.get(currentWorkout.items.size() - 1);
            if (first.target != null && (first.target.toLowerCase().contains("khởi động") || "warmup".equalsIgnoreCase(first.role))) {
                hasWarmup = true;
            }
            if (last.target != null && (last.target.toLowerCase().contains("giãn cơ") || "cooldown".equalsIgnoreCase(last.role))) {
                hasCooldown = true;
            }
        }

        if (count < 3) {
            llWarningBanner.setVisibility(View.VISIBLE);
            tvWarningText.setText("Cần ít nhất 3 bài tập để lưu buổi tập.");
            btnWarningAction.setVisibility(View.GONE);
        } else if (!hasWarmup) {
            llWarningBanner.setVisibility(View.VISIBLE);
            tvWarningText.setText("Nên thêm bài khởi động ở đầu buổi.");
            btnWarningAction.setVisibility(View.GONE);
        } else if (!hasCooldown) {
            llWarningBanner.setVisibility(View.VISIBLE);
            tvWarningText.setText("Nên thêm bài giãn cơ ở cuối buổi.");
            btnWarningAction.setVisibility(View.GONE);
        } else {
            llWarningBanner.setVisibility(View.GONE);
        }

        updateSaveButtonState();
    }

    private void updateSaveButtonState() {
        int count = currentWorkout.items != null ? currentWorkout.items.size() : 0;
        boolean titleValid = currentWorkout.title != null && !currentWorkout.title.trim().isEmpty();
        boolean canSave = count >= 3 && titleValid;

        btnSave.setEnabled(canSave);
        btnStart.setEnabled(count > 0);
    }

    private void saveWorkout() {
        if (currentWorkout.title == null || currentWorkout.title.trim().isEmpty()) {
            currentWorkout.title = "Buổi tập của tôi";
        }
        repository.saveWorkout(currentWorkout);
    }

    private void startWorkoutPlayer() {
        if (currentWorkout == null || currentWorkout.items.isEmpty()) return;
        Bundle args = new Bundle();
        args.putString("customWorkoutId", currentWorkout.id);
        args.putString("groupKey", "custom");
        args.putString("difficulty", "Custom");
        NavHostFragment.findNavController(this).navigate(R.id.action_editCustom_to_player, args);
    }
}
