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

    /** Hồ sơ trong Room (nguồn dữ liệu của màn hình). */
    public LiveData<UserProfileEntity> getProfile() {
        return profile;
    }

    public LiveData<Boolean> getSaving() {
        return saving;
    }

    /** Kết quả lần lưu gần nhất, chỉ xử lý một lần (Snackbar, điều hướng). */
    public LiveData<Event<Resource<UserProfileEntity>>> getSaveResult() {
        return saveResult;
    }

    /** Đã điền sẵn các ô nhập từ hồ sơ hay chưa (để xoay màn hình không ghi đè chữ đang gõ). */
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
