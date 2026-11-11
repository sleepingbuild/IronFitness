package com.homefit.app.ui.workout;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.homefit.app.data.local.AssetExerciseRepository;
import com.homefit.app.data.local.dto.LocalExerciseGroupDto;

import java.util.ArrayList;
import java.util.List;

public class HomeWorkoutViewModel extends AndroidViewModel {

    private final AssetExerciseRepository repository;
    private final MutableLiveData<List<LocalExerciseGroupDto>> allGroups = new MutableLiveData<>();
    private final MutableLiveData<List<LocalExerciseGroupDto>> popularGoalsData = new MutableLiveData<>();

    public HomeWorkoutViewModel(@NonNull Application application) {
        super(application);
        repository = new AssetExerciseRepository(application);
        loadData();
    }

    private void loadData() {
        allGroups.setValue(repository.getAllGroups());
    }

    public LiveData<List<LocalExerciseGroupDto>> getAllGroups() {
        return allGroups;
    }
    
    public LiveData<List<LocalExerciseGroupDto>> getPopularGoalsData() {
        return popularGoalsData;
    }

    public List<LocalExerciseGroupDto> getGroupsByTarget(String targetGroup) {
        List<LocalExerciseGroupDto> list = allGroups.getValue();
        if (list == null) return new ArrayList<>();
        List<LocalExerciseGroupDto> result = new ArrayList<>();
        for (LocalExerciseGroupDto dto : list) {
            if (targetGroup.equalsIgnoreCase(dto.group)) {
                result.add(dto);
            }
        }
        // Xếp theo độ khó: Easy -> Medium -> Hard
        result.sort((o1, o2) -> {
            int w1 = getDifficultyWeight(o1.difficulty);
            int w2 = getDifficultyWeight(o2.difficulty);
            return Integer.compare(w1, w2);
        });
        return result;
    }
    
    public void loadPopularGoals(String goalType) {
        List<LocalExerciseGroupDto> list = allGroups.getValue();
        if (list == null) {
            popularGoalsData.setValue(new ArrayList<>());
            return;
        }
        
        List<LocalExerciseGroupDto> result = new ArrayList<>();
        
        if ("muscle".equals(goalType)) {
            result.add(PopularWorkoutGenerator.createMuscleMedium1());
            result.add(PopularWorkoutGenerator.createMuscleMedium2());
            result.add(PopularWorkoutGenerator.createMuscleMedium3());
        } else if ("fatloss".equals(goalType)) {
            result.add(PopularWorkoutGenerator.createFatLossEasy());
            result.add(PopularWorkoutGenerator.createFatLossMedium());
            result.add(PopularWorkoutGenerator.createFatLossHard());
        } else if ("keepfit".equals(goalType)) {
            result.add(PopularWorkoutGenerator.createKeepFitEasy());
            result.add(PopularWorkoutGenerator.createKeepFitMedium());
            result.add(PopularWorkoutGenerator.createKeepFitHard());
        }
        
        popularGoalsData.setValue(result);
    }
    
    private int getDifficultyWeight(String diff) {
        if (diff == null) return 0;
        switch (diff.toLowerCase()) {
            case "easy": return 1;
            case "medium": return 2;
            case "hard": return 3;
            default: return 0;
        }
    }
}
