package com.homefit.app.ui.player;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.media.ToneGenerator;
import android.media.AudioManager;
import java.util.Locale;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.homefit.app.R;
import com.homefit.app.ads.AdsManager;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto.LocalWorkoutDto;
import com.homefit.app.data.prefs.WorkoutProgressStore;
import com.homefit.app.data.repository.ChallengeRepository;
import com.homefit.app.databinding.FragmentPlayerBinding;
import com.homefit.app.ui.common.BannerAdHelper;
import com.homefit.app.ui.common.MediaLoader;

public class PlayerFragment extends Fragment {

    private FragmentPlayerBinding binding;
    private PlayerViewModel viewModel;
    private ExoPlayer exoPlayer;
    private final BannerAdHelper restBannerAdHelper = new BannerAdHelper();

    private String groupKey;
    private String difficulty;
    private String groupName;
    private String customWorkoutId;
    private String workoutKey;
    private String workoutTitle;
    private int resumeIndex = 0;

    private int lastExerciseIndex = -1;
    private PlayerState lastState = null;
    private String lastLoadedExerciseName = null;

    private TextToSpeech textToSpeech;
    private ToneGenerator toneGenerator;
    private boolean isSoundMuted = false;
    private int lastSpokenIndex = -1;
    private PlayerState lastSpokenState = null;
    private int lastBeepSecond = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPlayerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(PlayerViewModel.class);

        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        binding.videoPlayerView.setPlayer(exoPlayer);
        exoPlayer.setRepeatMode(ExoPlayer.REPEAT_MODE_ALL);

        Bundle args = getArguments();
        if (args != null) {
            customWorkoutId = args.getString("customWorkoutId", null);
            groupKey = args.getString("groupKey", "");
            difficulty = args.getString("difficulty", "");
            groupName = args.getString("groupName", "");
            resumeIndex = args.getInt("resumeIndex", 0);

            if (customWorkoutId != null && !customWorkoutId.isEmpty()) {
                workoutKey = customWorkoutId;
                workoutTitle = "Tùy chỉnh";
                viewModel.loadCustomWorkout(customWorkoutId, resumeIndex);
            } else {
                workoutKey = groupKey + "_" + difficulty;
                workoutTitle = groupName + " " + difficulty;
                if (!groupKey.isEmpty() && !difficulty.isEmpty()) {
                    viewModel.loadExercises(groupKey, difficulty, groupName, resumeIndex);
                }
            }
        }

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
        } catch (Exception ignored) {}

        textToSpeech = new TextToSpeech(requireContext().getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS && textToSpeech != null) {
                com.homefit.app.ui.settings.SettingsStore store = new com.homefit.app.ui.settings.SettingsStore(requireContext());
                String code = store.getLanguageCode();
                Locale locale = new Locale("vi", "VN");
                if ("en".equalsIgnoreCase(code)) locale = Locale.US;
                else if ("ko".equalsIgnoreCase(code)) locale = Locale.KOREA;
                else if ("ja".equalsIgnoreCase(code)) locale = Locale.JAPAN;
                textToSpeech.setLanguage(locale);
            }
        });

        setupButtons();
        setupBackPressHandler();
        observeViewModel();
    }

    private void setupButtons() {
        if (binding == null) return;
        com.homefit.app.ui.settings.SettingsStore settingsStore = new com.homefit.app.ui.settings.SettingsStore(requireContext());
        isSoundMuted = !settingsStore.isSoundEffectsEnabled();

        binding.btnBackExit.setOnClickListener(v -> handleBackExit());
        binding.btnPlayPause.setOnClickListener(v -> viewModel.onPlayPauseClicked());
        binding.btnNext.setOnClickListener(v -> viewModel.onNextClicked());
        binding.btnPrevious.setOnClickListener(v -> viewModel.onPreviousClicked());
        binding.btnDoneReps.setOnClickListener(v -> viewModel.onRepsDoneClicked());

        if (binding.btnSoundToggle != null) {
            binding.btnSoundToggle.setImageResource(isSoundMuted ? R.drawable.ic_volume_off : R.drawable.ic_volume_up);
            binding.btnSoundToggle.setOnClickListener(v -> {
                isSoundMuted = !isSoundMuted;
                settingsStore.setSoundEffectsEnabled(!isSoundMuted);
                settingsStore.setVoiceCoachEnabled(!isSoundMuted);
                binding.btnSoundToggle.setImageResource(isSoundMuted ? R.drawable.ic_volume_off : R.drawable.ic_volume_up);
                Toast.makeText(requireContext(), isSoundMuted ? "Tắt âm thanh & Huấn luyện viên giọng nói" : "Bật âm thanh & Huấn luyện viên giọng nói", Toast.LENGTH_SHORT).show();
            });
        }

        binding.btnAddRest20s.setOnClickListener(v -> viewModel.addRestSeconds(20));
        binding.btnSkipRest.setOnClickListener(v -> viewModel.skipRest());
        binding.btnEditRestTime.setOnClickListener(v -> showEditRestTimeDialog());
    }

    private void setupBackPressHandler() {
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackExit();
            }
        });
    }

    private void handleBackExit() {
        if (!isAdded()) return;
        viewModel.pauseWorkout();

        new AlertDialog.Builder(requireContext())
                .setTitle("Thoát buổi tập?")
                .setMessage("Bạn có chắc chắn muốn thoát buổi tập này không?")
                .setPositiveButton("Có", (dialog, which) -> showSaveProgressOptionsDialog())
                .setNegativeButton("Không", (dialog, which) -> viewModel.onPlayPauseClicked())
                .show();
    }

    private void showSaveProgressOptionsDialog() {
        if (!isAdded()) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Thoát buổi tập")
                .setMessage("Bạn có muốn lưu tiến độ để lần sau tập tiếp không?")
                .setPositiveButton("Lưu tiến độ", (dialog, which) -> {
                    saveWorkoutProgress();
                    NavHostFragment.findNavController(this).navigateUp();
                })
                .setNegativeButton("Khởi động lại", (dialog, which) -> {
                    clearWorkoutProgress();
                    NavHostFragment.findNavController(this).navigateUp();
                })
                .setNeutralButton("Hủy", null)
                .show();
    }

    private void saveWorkoutProgress() {
        if (!isAdded()) return;
        PlayerUiState state = viewModel.getUiState().getValue();
        if (state != null && state.totalExercises > 0 && workoutKey != null) {
            WorkoutProgressStore store = new WorkoutProgressStore(requireContext());
            store.saveProgress(workoutKey, workoutTitle, state.currentExerciseIndex, state.totalExercises);
        }
    }

    private void clearWorkoutProgress() {
        if (!isAdded()) return;
        if (workoutKey != null) {
            WorkoutProgressStore store = new WorkoutProgressStore(requireContext());
            store.clearProgress(workoutKey);
        }
    }

    private void showEditRestTimeDialog() {
        if (!isAdded()) return;
        final android.widget.EditText etRest = new android.widget.EditText(requireContext());
        etRest.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etRest.setText("20");

        new AlertDialog.Builder(requireContext())
                .setTitle("Chỉnh sửa thời gian nghỉ")
                .setMessage("Nhập số giây muốn cộng thêm:")
                .setView(etRest)
                .setPositiveButton("Cộng thêm", (dialog, which) -> {
                    try {
                        int seconds = Integer.parseInt(etRest.getText().toString().trim());
                        if (seconds > 0) {
                            viewModel.addRestSeconds(seconds);
                        }
                    } catch (Exception ignored) {}
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (binding == null || !isAdded()) return;

            if (state.state == PlayerState.FINISHED) {
                clearWorkoutProgress();
                markChallengeCompletedIfAny();
                AdsManager.showInterstitial(requireActivity(), () -> navigateToSummary(state), true);
                return;
            }

            updateUi(state);
        });
    }

    private void markChallengeCompletedIfAny() {
        if (!isAdded()) return;
        if (groupKey != null && groupKey.startsWith("challenge_")) {
            try {
                String level = groupKey.replace("challenge_", "");
                int dayIndex = Integer.parseInt(difficulty);
                ChallengeRepository challengeRepo = new ChallengeRepository(requireContext());
                challengeRepo.markDayCompleted(level, dayIndex);
            } catch (Exception ignored) {}
        }
    }

    private void navigateToSummary(PlayerUiState state) {
        if (!isAdded() || getView() == null) return;
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putInt("totalExercises", state.totalExercises);
        args.putInt("totalSeconds", state.totalWorkoutSeconds);
        args.putInt("activeDurationSec", state.activeDurationSec);
        args.putDouble("caloriesBurned", state.caloriesBurned);
        args.putString("groupKey", groupKey);
        args.putString("difficulty", difficulty);
        args.putString("groupName", groupName);
        if (customWorkoutId != null) {
            args.putString("customWorkoutId", customWorkoutId);
        }
        navController.navigate(R.id.workoutSummaryFragment, args);
    }

    private void updateUi(PlayerUiState state) {
        if (binding == null || !isAdded()) return;

        LocalWorkoutDto current = state.currentExercise;

        // Top Workout Progress Bar
        if (binding.pbWorkoutProgress != null && state.totalExercises > 0) {
            int progressPercent = (int) Math.round(((double) (state.currentExerciseIndex + 1) / state.totalExercises) * 100);
            binding.pbWorkoutProgress.setProgress(progressPercent, true);
        }

        if (state.state == PlayerState.RESTING) {
            binding.clExercisingLayout.setVisibility(View.GONE);
            binding.llRestingLayout.setVisibility(View.VISIBLE);

            binding.tvNextProgress.setText(com.homefit.app.util.LocalizationHelper.getLocalizedNextHeader(requireContext(), state.currentExerciseIndex + 1, state.totalExercises));
            binding.tvNextExerciseName.setText(com.homefit.app.util.LocalizationHelper.getLocalizedExerciseName(requireContext(), current));
            binding.tvNextRepsSubtitle.setText(com.homefit.app.util.LocalizationHelper.getLocalizedRepsSets(requireContext(), current != null ? current.reps_sets : ""));
            binding.tvRestCounter.setText(formatTime(state.remainingSeconds));

            if (state.currentExerciseIndex != lastExerciseIndex || state.state != lastState) {
                loadRestingMedia(current);
                restBannerAdHelper.attach(binding.adContainerResting);
                restBannerAdHelper.onResume();
            }

        } else {
            binding.clExercisingLayout.setVisibility(View.VISIBLE);
            binding.llRestingLayout.setVisibility(View.GONE);

            binding.tvProgress.setText(com.homefit.app.util.LocalizationHelper.getLocalizedProgressHeader(requireContext(), state.currentExerciseIndex + 1, state.totalExercises));

            switch (state.state) {
                case PREPARING:
                    binding.tvStateHeader.setText(com.homefit.app.util.LocalizationHelper.getLocalizedStateHeader(requireContext(), state.state));
                    binding.tvExerciseName.setText(com.homefit.app.util.LocalizationHelper.getLocalizedExerciseName(requireContext(), current));
                    binding.tvCounter.setText(formatTime(state.remainingSeconds));
                    showControls(false);
                    break;
                case EXERCISING:
                    binding.tvStateHeader.setText(com.homefit.app.util.LocalizationHelper.getLocalizedStateHeader(requireContext(), state.state));
                    binding.tvExerciseName.setText(com.homefit.app.util.LocalizationHelper.getLocalizedExerciseName(requireContext(), current));
                    if (state.isRepsBased) {
                        binding.tvCounter.setText(com.homefit.app.util.LocalizationHelper.getLocalizedRepsSets(requireContext(), current != null ? current.reps_sets : ""));
                    } else {
                        binding.tvCounter.setText(formatTime(state.remainingSeconds));
                    }
                    showControls(state.isRepsBased);
                    break;
                case PAUSED:
                    binding.tvStateHeader.setText(com.homefit.app.util.LocalizationHelper.getLocalizedStateHeader(requireContext(), state.state));
                    binding.btnPlayPause.setImageResource(R.drawable.ic_play_arrow_24);
                    break;
                default:
                    break;
            }

            if (state.state != PlayerState.PAUSED) {
                binding.btnPlayPause.setImageResource(R.drawable.ic_pause_24);
            }

            binding.videoPlayerView.setVisibility(View.GONE);
            binding.ivMedia.setVisibility(View.VISIBLE);

            boolean exerciseChanged = (state.currentExerciseIndex != lastExerciseIndex) ||
                    (current != null && !current.name.equals(lastLoadedExerciseName)) ||
                    (lastState == PlayerState.RESTING);

            if (exerciseChanged) {
                loadExerciseDetailsAndMedia(current);
                if (current != null) {
                    lastLoadedExerciseName = current.name;
                }
            }
        }

        lastExerciseIndex = state.currentExerciseIndex;
        lastState = state.state;

        // Sound effects & Voice Coach TTS speaking
        if (!isSoundMuted) {
            if (state.remainingSeconds > 0 && state.remainingSeconds <= 3 && state.remainingSeconds != lastBeepSecond) {
                lastBeepSecond = state.remainingSeconds;
                if (toneGenerator != null) {
                    try {
                        toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 150);
                    } catch (Exception ignored) {}
                }
                if (textToSpeech != null && (state.state == PlayerState.PREPARING || state.state == PlayerState.RESTING || state.state == PlayerState.EXERCISING)) {
                    String countdownWord = String.valueOf(state.remainingSeconds);
                    if (state.remainingSeconds == 3) countdownWord = "Ba";
                    else if (state.remainingSeconds == 2) countdownWord = "Hai";
                    else if (state.remainingSeconds == 1) countdownWord = "Một";
                    try {
                        textToSpeech.speak(countdownWord, TextToSpeech.QUEUE_FLUSH, null, "countdown_tts");
                    } catch (Exception ignored) {}
                }
            }

            if (state.currentExerciseIndex != lastSpokenIndex || state.state != lastSpokenState) {
                lastSpokenIndex = state.currentExerciseIndex;
                lastSpokenState = state.state;
                if (textToSpeech != null) {
                    String localizedName = com.homefit.app.util.LocalizationHelper.getLocalizedExerciseName(requireContext(), current);
                    String lang = com.homefit.app.util.LocalizationHelper.getLanguage(requireContext());
                    String speechText = null;

                    if (state.state == PlayerState.PREPARING) {
                        switch (lang) {
                            case "en": speechText = "Get ready for: " + localizedName; break;
                            case "ko": speechText = "준비: " + localizedName; break;
                            case "ja": speechText = "準備: " + localizedName; break;
                            default: speechText = "Chuẩn bị: " + localizedName; break;
                        }
                    } else if (state.state == PlayerState.RESTING) {
                        switch (lang) {
                            case "en": speechText = "Rest time. Next up: " + localizedName; break;
                            case "ko": speechText = "휴식 시간. 다음 운동: " + localizedName; break;
                            case "ja": speechText = "休憩時間。次の運動: " + localizedName; break;
                            default: speechText = "Hoàn thành! Nghỉ ngơi. Bài tiếp theo: " + localizedName; break;
                        }
                    } else if (state.state == PlayerState.EXERCISING) {
                        switch (lang) {
                            case "en": speechText = "Start: " + localizedName; break;
                            case "ko": speechText = "운동 시작: " + localizedName; break;
                            case "ja": speechText = "スタート: " + localizedName; break;
                            default: speechText = "Bắt đầu bài tập: " + localizedName; break;
                        }
                    }
                    if (speechText != null && !speechText.isBlank()) {
                        try {
                            textToSpeech.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "exercise_tts");
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    private void loadRestingMedia(LocalWorkoutDto current) {
        if (!isAdded() || binding == null) return;
        if (current != null && current.mediaUrl != null && !current.mediaUrl.isEmpty()) {
            MediaLoader.loadFullWithName(binding.ivNextThumb, current.name, current.mediaUrl, "GIF");
        } else if (current != null && current.name != null) {
            String name = current.name;
            com.homefit.app.util.AppExecutors.diskIO().execute(() -> {
                if (!isAdded()) return;
                var list = com.homefit.app.data.local.AppDatabase.getInstance(requireContext().getApplicationContext()).exerciseDao().getAllNow();
                String foundUrl = "";
                String normCurrent = name.toLowerCase(Locale.US).replaceAll("[^a-zA-Z0-9]", "");
                for (var e : list) {
                    if (e.name == null) continue;
                    String normE = e.name.toLowerCase(Locale.US).replaceAll("[^a-zA-Z0-9]", "");
                    if (e.name.equalsIgnoreCase(name) || (!normE.isEmpty() && normCurrent.contains(normE))) {
                        foundUrl = e.mediaUrl;
                        break;
                    }
                }
                final String url = foundUrl;
                com.homefit.app.util.AppExecutors.mainThread().execute(() -> {
                    if (binding != null && isAdded()) {
                        MediaLoader.loadFullWithName(binding.ivNextThumb, name, url, "GIF");
                    }
                });
            });
        } else {
            binding.ivNextThumb.setImageResource(R.drawable.bg_media_placeholder);
        }
    }

    private void loadExerciseDetailsAndMedia(LocalWorkoutDto current) {
        if (binding == null || !isAdded()) return;
        if (current == null) {
            binding.tvExerciseDescription.setText(com.homefit.app.util.LocalizationHelper.getLocalizedExerciseDescription(requireContext(), null));
            binding.tvTargetMuscleCaption.setText(com.homefit.app.util.LocalizationHelper.getLocalizedTargetCaption(requireContext(), "Toàn thân"));
            binding.ivMedia.setImageResource(R.drawable.bg_media_placeholder);
            return;
        }

        String targetMuscle = "Toàn thân";
        if (current.target != null && !current.target.isBlank()) {
            targetMuscle = current.target;
        } else if (groupKey != null && !groupKey.isBlank()) {
            String groupLoc = com.homefit.app.util.LocalizationHelper.getLocalizedGroup(requireContext(), groupKey);
            if (groupLoc != null && !groupLoc.isBlank()) {
                targetMuscle = groupLoc;
            }
        }
        if ("Toàn thân".equals(targetMuscle) && current.name != null) {
            String nameLower = current.name.toLowerCase(Locale.US);
            if (nameLower.contains("bụng") || nameLower.contains("plank") || nameLower.contains("crunch") || nameLower.contains("abs")) targetMuscle = "Cơ Bụng";
            else if (nameLower.contains("ngực") || nameLower.contains("push") || nameLower.contains("chest")) targetMuscle = "Cơ Ngực";
            else if (nameLower.contains("tay") || nameLower.contains("dip") || nameLower.contains("curl") || nameLower.contains("arm")) targetMuscle = "Cơ Tay";
            else if (nameLower.contains("chân") || nameLower.contains("squat") || nameLower.contains("lunge") || nameLower.contains("leg")) targetMuscle = "Cơ Chân & Mông";
            else if (nameLower.contains("lưng") || nameLower.contains("back")) targetMuscle = "Cơ Lưng";
        }
        binding.tvTargetMuscleCaption.setText(com.homefit.app.util.LocalizationHelper.getLocalizedTargetCaption(requireContext(), targetMuscle));

        String locDesc = com.homefit.app.util.LocalizationHelper.getLocalizedExerciseDescription(requireContext(), current);
        binding.tvExerciseDescription.setText(locDesc);

        MediaLoader.loadFullWithName(binding.ivMedia, current.name, current.mediaUrl, "GIF");

        if (current.name != null) {
            String name = current.name;
            com.homefit.app.util.AppExecutors.diskIO().execute(() -> {
                if (!isAdded()) return;
                var list = com.homefit.app.data.local.AppDatabase.getInstance(requireContext().getApplicationContext()).exerciseDao().getAllNow();
                com.homefit.app.data.local.entity.ExerciseEntity match = null;
                String normCurrent = name.toLowerCase(Locale.US).replaceAll("[^a-zA-Z0-9]", "");
                for (var e : list) {
                    if (e.name == null) continue;
                    String normE = e.name.toLowerCase(Locale.US).replaceAll("[^a-zA-Z0-9]", "");
                    if (e.name.equalsIgnoreCase(name)
                            || (!normE.isEmpty() && normCurrent.contains(normE))
                            || (!normCurrent.isEmpty() && normE.contains(normCurrent))) {
                        match = e;
                        break;
                    }
                }
                final var finalMatch = match;
                com.homefit.app.util.AppExecutors.mainThread().execute(() -> {
                    if (binding == null || !isAdded()) return;
                    if (finalMatch != null) {
                        String matchDesc = com.homefit.app.util.LocalizationHelper.getLocalizedExerciseDescription(requireContext(), finalMatch);
                        if (matchDesc != null && !matchDesc.isBlank()) {
                            binding.tvExerciseDescription.setText(matchDesc);
                        }
                        if (finalMatch.mediaUrl != null && !finalMatch.mediaUrl.isBlank()) {
                            MediaLoader.loadFullWithName(binding.ivMedia, name, finalMatch.mediaUrl, finalMatch.mediaType != null ? finalMatch.mediaType : "GIF");
                        }
                    }
                });
            });
        }
    }

    private void showControls(boolean isRepsBased) {
        if (binding == null) return;
        if (isRepsBased) {
            binding.llControls.setVisibility(View.GONE);
            binding.btnDoneReps.setVisibility(View.VISIBLE);
        } else {
            binding.llControls.setVisibility(View.VISIBLE);
            binding.btnDoneReps.setVisibility(View.GONE);
        }
    }

    private String formatTime(int seconds) {
        int min = Math.max(0, seconds) / 60;
        int sec = Math.max(0, seconds) % 60;
        return String.format(Locale.US, "%02d:%02d", min, sec);
    }

    @Override
    public void onResume() {
        super.onResume();
        restBannerAdHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        restBannerAdHelper.onPause();
    }

    @Override
    public void onDestroyView() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        restBannerAdHelper.destroy();
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
        binding = null;
        super.onDestroyView();
    }
}
