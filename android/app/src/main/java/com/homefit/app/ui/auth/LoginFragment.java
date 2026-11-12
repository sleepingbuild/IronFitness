package com.homefit.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.homefit.app.R;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.remote.ApiClient;
import com.homefit.app.data.remote.ErrorCodes;
import com.homefit.app.databinding.FragmentLoginBinding;
import com.homefit.app.ui.common.AfterTextChanged;
import com.homefit.app.ui.common.SnackbarHelper;
import com.homefit.app.ui.common.UiUtils;
import com.homefit.app.util.ErrorMessages;
import com.homefit.app.util.Resource;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;
    private GoogleSignInClient mGoogleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    if (account != null && account.getIdToken() != null) {
                        handleGoogleLoginToBackend(account.getIdToken());
                    }
                } catch (ApiException e) {
                    Log.w("GoogleLogin", "Google sign in failed", e);
                    if (binding != null) {
                        SnackbarHelper.show(this, getString(R.string.login_google_error));
                    }
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);

        binding.etEmail.addTextChangedListener(new AfterTextChanged(() -> binding.tilEmail.setError(null)));
        binding.etPassword.addTextChangedListener(new AfterTextChanged(() -> binding.tilPassword.setError(null)));
        binding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit();
                return true;
            }
            return false;
        });

        binding.btnLogin.setOnClickListener(v -> submit());
        binding.btnGoRegister.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.action_login_to_register));

        binding.btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());

        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
    }

    private void submit() {
        String email = UiUtils.textOf(binding.etEmail).trim();
        String password = UiUtils.textOf(binding.etPassword);

        int emailError = AuthValidator.emailError(email);
        int passwordError = AuthValidator.loginPasswordError(password);
        binding.tilEmail.setError(emailError == 0 ? null : getString(emailError));
        binding.tilPassword.setError(passwordError == 0 ? null : getString(passwordError));

        String targetUrl = ApiClient.getBaseUrl(requireContext()) + "auth/login";

        if (emailError != 0 || passwordError != 0) {
            String msg = "Vui lòng nhập Email và Mật khẩu hợp lệ!\n\nURL dự kiến: " + targetUrl;
            showDebugDialog("Cảnh báo nhập liệu", msg);
            return;
        }

        UiUtils.hideKeyboard(this);
        Toast.makeText(requireContext(), "Đang gửi POST đến: " + targetUrl, Toast.LENGTH_LONG).show();
        viewModel.login(email, password);
    }

    private void render(@Nullable Resource<UserProfileEntity> resource) {
        if (resource == null) {
            setLoading(false);
            showFormError(null);
            return;
        }
        String targetUrl = ApiClient.getBaseUrl(requireContext()) + "auth/login";
        switch (resource.status) {
            case LOADING:
                setLoading(true);
                showFormError("Đang gửi POST " + targetUrl + "...");
                break;
            case SUCCESS:
                setLoading(false);
                showFormError(null);
                String successMsg = "Đăng nhập THÀNH CÔNG!\n\nTarget URL: " + targetUrl
                        + "\nEmail: " + (resource.data != null ? resource.data.email : "");
                Toast.makeText(requireContext(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                showDebugDialog("Kết quả Đăng nhập (200 OK)", successMsg, this::goToWorkout);
                break;
            case ERROR:
                setLoading(false);
                String errorMsg = ErrorCodes.UNAUTHORIZED.equals(resource.code)
                        ? getString(R.string.login_error_invalid)
                        : ErrorMessages.toText(requireContext(), resource);
                String fullError = "Đăng nhập THẤT BẠI!\n\nURL đang gọi: " + targetUrl
                        + "\nMã lỗi HTTP / Code: " + (resource.code != null ? resource.code : "ERR")
                        + "\nChi tiết lỗi: " + errorMsg;
                showFormError("Lỗi [" + resource.code + "]: " + errorMsg);
                showDebugDialog("Kết quả Gọi API Đăng nhập", fullError);
                Log.e(TAG, fullError);
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
                && navController.getCurrentDestination().getId() == R.id.loginFragment) {
            navController.navigate(R.id.action_login_to_workout);
        }
    }

    private void setLoading(boolean loading) {
        binding.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!loading);
        binding.btnLogin.setText(loading ? R.string.login_loading : R.string.login_button);
        binding.btnGoRegister.setEnabled(!loading);
        binding.btnGoogleLogin.setEnabled(!loading);
        binding.tilEmail.setEnabled(!loading);
        binding.tilPassword.setEnabled(!loading);
    }

    private void showFormError(@Nullable String message) {
        binding.tvFormError.setText(message);
        binding.tvFormError.setVisibility(message == null ? View.GONE : View.VISIBLE);
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleLoginToBackend(String idToken) {
        viewModel.loginWithGoogle(idToken).observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            String targetUrl = ApiClient.getBaseUrl(requireContext()) + "auth/google";
            switch (resource.status) {
                case LOADING:
                    setLoading(true);
                    showFormError("Đang đăng nhập qua Google...");
                    break;
                case SUCCESS:
                    setLoading(false);
                    showDebugDialog("Đăng nhập Google THÀNH CÔNG", "URL: " + targetUrl, this::goToWorkout);
                    break;
                case ERROR:
                    setLoading(false);
                    String msg = ErrorMessages.toText(requireContext(), resource);
                    showDebugDialog("Đăng nhập Google THẤT BẠI", "URL: " + targetUrl + "\nLỗi: " + msg);
                    break;
            }
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
