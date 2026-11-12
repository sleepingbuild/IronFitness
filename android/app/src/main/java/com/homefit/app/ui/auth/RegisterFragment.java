package com.homefit.app.ui.auth;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.homefit.app.R;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.remote.ApiClient;
import com.homefit.app.databinding.FragmentRegisterBinding;
import com.homefit.app.ui.common.AfterTextChanged;
import com.homefit.app.ui.common.UiUtils;
import com.homefit.app.util.ErrorMessages;
import com.homefit.app.util.Resource;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";
    private FragmentRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        binding.etName.addTextChangedListener(new AfterTextChanged(() -> binding.tilName.setError(null)));
        binding.etEmail.addTextChangedListener(new AfterTextChanged(() -> binding.tilEmail.setError(null)));
        binding.etPassword.addTextChangedListener(new AfterTextChanged(() -> binding.tilPassword.setError(null)));
        binding.etConfirm.addTextChangedListener(new AfterTextChanged(() -> binding.tilConfirm.setError(null)));
        binding.etConfirm.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });
        binding.btnRegister.setOnClickListener(v -> submit());
        binding.btnGoLogin.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
    }

    private void submit() {
        String name = UiUtils.textOf(binding.etName).trim();
        String email = UiUtils.textOf(binding.etEmail).trim();
        String password = UiUtils.textOf(binding.etPassword);
        String confirm = UiUtils.textOf(binding.etConfirm);

        int nameError = AuthValidator.nameError(name);
        int emailError = AuthValidator.emailError(email);
        int passwordError = AuthValidator.registerPasswordError(password);
        int confirmError = passwordError == 0 ? AuthValidator.confirmError(password, confirm) : 0;

        binding.tilName.setError(nameError == 0 ? null : getString(nameError));
        binding.tilEmail.setError(emailError == 0 ? null : getString(emailError));
        binding.tilPassword.setError(passwordError == 0 ? null : getString(passwordError));
        binding.tilConfirm.setError(confirmError == 0 ? null : getString(confirmError));

        String targetUrl = ApiClient.getBaseUrl(requireContext()) + "auth/register";

        if (nameError != 0 || emailError != 0 || passwordError != 0 || confirmError != 0) {
            StringBuilder sb = new StringBuilder("Thông tin nhập chưa đúng:\n");
            if (nameError != 0) sb.append("- Tên không được rỗng.\n");
            if (emailError != 0) sb.append("- Email chưa đúng định dạng.\n");
            if (passwordError != 0) sb.append("- Mật khẩu phải tối thiểu 8 ký tự.\n");
            if (confirmError != 0) sb.append("- Mật khẩu xác nhận không khớp.\n");
            sb.append("\nURL dự kiến: ").append(targetUrl);

            showDebugDialog("Cảnh báo nhập liệu", sb.toString());
            return;
        }

        UiUtils.hideKeyboard(this);
        Toast.makeText(requireContext(), "Đang gửi POST đến: " + targetUrl, Toast.LENGTH_LONG).show();
        viewModel.register(email, password, name);
    }

    private void render(@Nullable Resource<UserProfileEntity> resource) {
        if (resource == null) {
            setLoading(false);
            showFormError(null);
            return;
        }
        String targetUrl = ApiClient.getBaseUrl(requireContext()) + "auth/register";
        switch (resource.status) {
            case LOADING:
                setLoading(true);
                showFormError("Đang gửi POST " + targetUrl + "...");
                break;
            case SUCCESS:
                setLoading(false);
                showFormError(null);
                String successMsg = "Đăng ký THÀNH CÔNG!\n\nTarget URL: " + targetUrl
                        + "\nEmail: " + (resource.data != null ? resource.data.email : "");
                Toast.makeText(requireContext(), "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                showDebugDialog("Kết quả Đăng ký (200 OK)", successMsg, this::goToWorkout);
                break;
            case ERROR:
                setLoading(false);
                String errorText = ErrorMessages.toText(requireContext(), resource);
                String fullMsg = "Đăng ký THẤT BẠI!\n\nURL đang gọi: " + targetUrl
                        + "\nMã lỗi HTTP / Code: " + (resource.code != null ? resource.code : "HTTP_ERR")
                        + "\nChi tiết lỗi: " + errorText;
                showFormError("Lỗi [" + resource.code + "]: " + errorText);
                showDebugDialog("Kết quả Gọi API Đăng ký", fullMsg);
                Log.e(TAG, fullMsg);
                break;
            default:
                break;
        }
    }

    private void showDebugDialog(String title, String message) {
        showDebugDialog(title, message, null);
    }

    private void showDebugDialog(String title, String message, @Nullable Runnable onDismiss) {
        if (!isAdded() || getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Đóng", (dialog, which) -> {
                    if (onDismiss != null) onDismiss.run();
                })
                .setCancelable(false)
                .show();
    }

    private void goToWorkout() {
        NavController navController = NavHostFragment.findNavController(this);
        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId() == R.id.registerFragment) {
            navController.navigate(R.id.action_register_to_workout);
        }
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!loading);
        binding.btnRegister.setText(loading ? R.string.register_loading : R.string.register_button);
        binding.btnGoLogin.setEnabled(!loading);
        binding.tilName.setEnabled(!loading);
        binding.tilEmail.setEnabled(!loading);
        binding.tilPassword.setEnabled(!loading);
        binding.tilConfirm.setEnabled(!loading);
    }

    private void showFormError(@Nullable String message) {
        binding.tvFormError.setText(message);
        binding.tvFormError.setVisibility(message == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
