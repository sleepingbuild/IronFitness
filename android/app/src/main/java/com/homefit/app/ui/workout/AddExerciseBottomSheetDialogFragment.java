package com.homefit.app.ui.workout;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.homefit.app.R;
import com.homefit.app.data.local.AssetExerciseRepository;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto;
import com.homefit.app.data.local.model.CustomWorkoutItem;

import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class AddExerciseBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public interface OnExercisesSelectedListener {
        void onExercisesSelected(List<CustomWorkoutItem> selectedItems);
    }

    private OnExercisesSelectedListener listener;
    private AssetExerciseRepository repository;

    private TextView chipFilterArea;
    private EditText etSearch;
    private ImageButton btnClearSearch;
    private RecyclerView rvExercises;
    private MaterialButton btnBottomAction;

    private AddExerciseAdapter adapter;
    private final List<CustomWorkoutItem> allExerciseItems = new ArrayList<>();
    private final List<String> selectedAreas = new ArrayList<>();

    public void setOnExercisesSelectedListener(OnExercisesSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_exercise, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnClose = view.findViewById(R.id.btnClose);
        chipFilterArea = view.findViewById(R.id.chipFilterArea);
        etSearch = view.findViewById(R.id.etSearch);
        btnClearSearch = view.findViewById(R.id.btnClearSearch);
        rvExercises = view.findViewById(R.id.rvExercises);
        btnBottomAction = view.findViewById(R.id.btnBottomAction);

        repository = new AssetExerciseRepository(requireContext());

        adapter = new AddExerciseAdapter(count -> {
            if (count > 0) {
                btnBottomAction.setText("Thêm (" + count + ")");
            } else {
                btnBottomAction.setText("Đóng");
            }
        });

        adapter.setOnItemClickListener((item, isSelected, callback) ->
                showConfigureDialog(item, isSelected, callback));

        rvExercises.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvExercises.setAdapter(adapter);

        btnClose.setOnClickListener(v -> dismiss());

        chipFilterArea.setOnClickListener(v -> showAreaFilterDialog());

        btnClearSearch.setOnClickListener(v -> {
            etSearch.setText("");
            btnClearSearch.setVisibility(View.GONE);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                filterExercises(query);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnBottomAction.setOnClickListener(v -> {
            List<CustomWorkoutItem> selected = adapter.getSelectedItems();
            if (!selected.isEmpty() && listener != null) {
                listener.onExercisesSelected(selected);
            }
            dismiss();
        });

        loadExerciseLibrary();
    }

    private void showConfigureDialog(CustomWorkoutItem item, boolean isSelected, AddExerciseAdapter.OnSelectionToggleCallback callback) {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    String repsStr = etReps.getText() != null ? etReps.getText().toString().trim() : "";
                    String setsStr = etSets.getText() != null ? etSets.getText().toString().trim() : "";
                    String timeStr = etTimeSec.getText() != null ? etTimeSec.getText().toString().trim() : "";

                    item.reps = parseOptionalInt(repsStr);
                    item.sets = parseOptionalInt(setsStr);
                    item.timeSec = parseOptionalInt(timeStr);

                    callback.onToggle(true);
                })
                .setNegativeButton("Hủy", null);

        if (isSelected) {
            builder.setNeutralButton("Bỏ chọn", (dialog, which) -> callback.onToggle(false));
        }

        builder.show();
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

    private void loadExerciseLibrary() {
        allExerciseItems.clear();
        com.homefit.app.util.AppExecutors.diskIO().execute(() -> {
            List<com.homefit.app.data.local.entity.ExerciseEntity> entities = com.homefit.app.data.local.AppDatabase.getInstance(requireContext()).exerciseDao().getAllNow();
            List<CustomWorkoutItem> items = new ArrayList<>();
            if (entities != null && !entities.isEmpty()) {
                for (com.homefit.app.data.local.entity.ExerciseEntity e : entities) {
                    CustomWorkoutItem item = new CustomWorkoutItem();
                    item.exerciseId = String.valueOf(e.id);
                    item.exerciseName = e.name;
                    item.target = e.description != null ? e.description : "";
                    item.instructions = new ArrayList<>();
                    if (e.description != null) item.instructions.add(e.description);
                    item.tips = "Giữ nhịp thở đều.";

                    item.reps = e.reps;
                    item.timeSec = e.durationSec;
                    item.sets = null;

                    items.add(item);
                }
            } else {
                List<LocalWorkoutDto> dtos = repository.getAllUniqueWorkouts();
                for (LocalWorkoutDto dto : dtos) {
                    CustomWorkoutItem item = new CustomWorkoutItem();
                    item.exerciseId = dto.id;
                    item.exerciseName = dto.name;
                    item.target = dto.target;
                    item.instructions = dto.instructions;
                    item.tips = dto.tips;

                    item.reps = null;
                    item.sets = null;
                    item.timeSec = null;

                    items.add(item);
                }
            }

            Collator collator = Collator.getInstance(new Locale("vi", "VN"));
            Collections.sort(items, (item1, item2) ->
                    collator.compare(item1.exerciseName != null ? item1.exerciseName : "",
                                     item2.exerciseName != null ? item2.exerciseName : ""));

            com.homefit.app.util.AppExecutors.mainThread().execute(() -> {
                allExerciseItems.clear();
                allExerciseItems.addAll(items);
                updateFilterChipText(allExerciseItems.size());
                adapter.setItems(allExerciseItems);
            });
        });
    }

    private void showAreaFilterDialog() {
        String[] areas = new String[]{"Tất cả", "Bụng", "Ngực", "Tay", "Vai", "Lưng", "Chân", "Mông", "Toàn thân", "Khởi động", "Giãn cơ"};
        boolean[] checked = new boolean[areas.length];

        if (selectedAreas.isEmpty()) {
            checked[0] = true;
        } else {
            for (int i = 0; i < areas.length; i++) {
                checked[i] = selectedAreas.contains(areas[i]);
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Chọn khu vực")
                .setMultiChoiceItems(areas, checked, (dialog, which, isChecked) -> {
                    if (which == 0 && isChecked) {
                        for (int i = 1; i < checked.length; i++) {
                            ((AlertDialog) dialog).getListView().setItemChecked(i, false);
                            checked[i] = false;
                        }
                    } else if (which > 0 && isChecked) {
                        ((AlertDialog) dialog).getListView().setItemChecked(0, false);
                        checked[0] = false;
                    }
                })
                .setPositiveButton("Áp dụng", (dialog, which) -> {
                    selectedAreas.clear();
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    for (int i = 0; i < areas.length; i++) {
                        if (alertDialog.getListView().isItemChecked(i)) {
                            if (i == 0) {
                                selectedAreas.clear();
                                break;
                            }
                            selectedAreas.add(areas[i]);
                        }
                    }
                    filterExercises(etSearch.getText().toString().trim());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void filterExercises(String query) {
        List<CustomWorkoutItem> filtered = new ArrayList<>();
        String normQuery = stripAccents(query).toLowerCase(Locale.US);

        for (CustomWorkoutItem item : allExerciseItems) {
            boolean matchesArea = true;
            if (!selectedAreas.isEmpty()) {
                matchesArea = false;
                String target = item.target != null ? item.target.toLowerCase(Locale.US) : "";
                String name = item.exerciseName != null ? item.exerciseName.toLowerCase(Locale.US) : "";
                for (String area : selectedAreas) {
                    String normArea = area.toLowerCase(Locale.US);
                    if (target.contains(normArea) || name.contains(normArea)) {
                        matchesArea = true;
                        break;
                    }
                }
            }

            if (!matchesArea) continue;

            if (normQuery.isEmpty()) {
                filtered.add(item);
            } else {
                String normName = stripAccents(item.exerciseName != null ? item.exerciseName : "").toLowerCase(Locale.US);
                String normTarget = stripAccents(item.target != null ? item.target : "").toLowerCase(Locale.US);
                if (normName.contains(normQuery) || normTarget.contains(normQuery)) {
                    filtered.add(item);
                }
            }
        }

        updateFilterChipText(filtered.size());
        adapter.setItems(filtered);
    }

    private void updateFilterChipText(int count) {
        if (selectedAreas.isEmpty()) {
            chipFilterArea.setText("Tất cả các khu vực (" + count + ")");
        } else if (selectedAreas.size() == 1) {
            chipFilterArea.setText(selectedAreas.get(0) + " (" + count + ")");
        } else if (selectedAreas.size() == 2) {
            chipFilterArea.setText(selectedAreas.get(0) + ", " + selectedAreas.get(1) + " (" + count + ")");
        } else {
            chipFilterArea.setText(selectedAreas.size() + " khu vực (" + count + ")");
        }
    }

    private String stripAccents(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }
}
