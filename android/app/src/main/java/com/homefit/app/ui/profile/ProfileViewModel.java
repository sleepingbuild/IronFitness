package com.homefit.app.ui.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.repository.AuthRepository;
import com.homefit.app.ui.common.Event;
import com.homefit.app.util.Resource;

public class ProfileViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final LiveData<UserProfileEntity> profile;
    private final MutableLiveData<Boolean> saving = new MutableLiveData<>(false);
    private final MediatorLiveData<Event<Resource<UserProfileEntity>>> saveResult = new MediatorLiveData<>();
    private boolean formPrefilled;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
        profile = authRepository.getProfile();
    }

    /** Há»“ sÆ¡ trong Room (nguá»“n dá»¯ liá»‡u cá»§a mÃ n hÃ¬nh). */
    public LiveData<UserProfileEntity> getProfile() {
        return profile;
    }

    public LiveData<Boolean> getSaving() {
        return saving;
    }

    /** Káº¿t quáº£ láº§n lÆ°u gáº§n nháº¥t, chá»‰ xá»­ lÃ½ má»™t láº§n (Snackbar, Ä‘iá»u hÆ°á»›ng). */
    public LiveData<Event<Resource<UserProfileEntity>>> getSaveResult() {
        return saveResult;
    }

    /** ÄÃ£ Ä‘iá»n sáºµn cÃ¡c Ã´ nháº­p tá»« há»“ sÆ¡ hay chÆ°a (Ä‘á»ƒ xoay mÃ n hÃ¬nh khÃ´ng ghi Ä‘Ã¨ chá»¯ Ä‘ang gÃµ). */
    public boolean isFormPrefilled() {
        return formPrefilled;
    }

    public void setFormPrefilled(boolean formPrefilled) {
        this.formPrefilled = formPrefilled;
    }

    public void save(@NonNull String displayName, @Nullable Double heightCm, @Nullable Double weightKg) {
        if (Boolean.TRUE.equals(saving.getValue())) {
            return;
        }
        saving.setValue(true);
        LiveData<Resource<UserProfileEntity>> source = authRepository.updateProfile(displayName, heightCm, weightKg);
        saveResult.addSource(source, resource -> {
            if (resource == null || resource.isLoading()) {
                return;
            }
            saveResult.removeSource(source);
            saving.setValue(false);
            saveResult.setValue(new Event<>(resource));
        });
    }

    public void logout() {
        authRepository.logout();
    }
}

