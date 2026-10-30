package com.homefit.app.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.homefit.app.R;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.remote.ErrorCodes;
import com.homefit.app.databinding.FragmentLoginBinding;
import com.homefit.app.ui.common.AfterTextChanged;
import com.homefit.app.ui.common.UiUtils;
import com.homefit.app.util.ErrorMessages;
import com.homefit.app.util.Resource;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

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

        viewModel.getState().observe(getViewLifecycleOwner(), this::render);
    }

    private void submit() {
        String email = UiUtils.textOf(binding.etEmail).trim();
        String password = UiUtils.textOf(binding.etPassword);

        int emailError = AuthValidator.emailError(email);
        int passwordError = AuthValidator.loginPasswordError(password);
        binding.tilEmail.setError(emailError == 0 ? null : getString(emailError));
        binding.tilPassword.setError(passwordError == 0 ? null : getString(passwordError));
        if (emailError != 0 || passwordError != 0) {
            return;
        }
        UiUtils.hideKeyboard(this);
        viewModel.login(email, password);
    }

    private void render(@Nullable Resource<UserProfileEntity> resource) {
        if (resource == null) {
            setLoading(false);
            showFormError(null);
            return;
        }
        switch (resource.status) {
            case LOADING:
                setLoading(true);
                showFormError(null);
                break;
            case SUCCESS:
                setLoading(false);
                showFormError(null);
                goToWorkout();
                break;
            case ERROR:
                setLoading(false);
                showFormError(ErrorCodes.UNAUTHORIZED.equals(resource.code)
                        ? getString(R.string.login_error_invalid)
                        : ErrorMessages.toText(requireContext(), resource));
                break;
            default:
                break;
        }
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
        binding.tilEmail.setEnabled(!loading);
        binding.tilPassword.setEnabled(!loading);
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
