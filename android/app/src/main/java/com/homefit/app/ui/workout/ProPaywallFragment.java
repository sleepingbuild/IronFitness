package com.homefit.app.ui.workout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.homefit.app.R;
import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.util.AppExecutors;

public class ProPaywallFragment extends Fragment {

    private RadioButton rbMonthly;
    private RadioButton rbYearly;
    private MaterialButton btnStartPro;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pro_paywall, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnBack = view.findViewById(R.id.btnBack);
        MaterialButton btnRestore = view.findViewById(R.id.btnRestore);
        MaterialCardView cardMonthly = view.findViewById(R.id.cardMonthly);
        MaterialCardView cardYearly = view.findViewById(R.id.cardYearly);
        rbMonthly = view.findViewById(R.id.rbMonthly);
        rbYearly = view.findViewById(R.id.rbYearly);
        btnStartPro = view.findViewById(R.id.btnStartPro);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        cardMonthly.setOnClickListener(v -> {
            rbMonthly.setChecked(true);
            rbYearly.setChecked(false);
        });

        cardYearly.setOnClickListener(v -> {
            rbMonthly.setChecked(false);
            rbYearly.setChecked(true);
        });

        btnRestore.setOnClickListener(v ->
            Toast.makeText(requireContext(), getString(R.string.pro_restored_toast), Toast.LENGTH_SHORT).show()
        );

        btnStartPro.setOnClickListener(v -> openCheckoutScreen());

        checkProState();
    }

    private void checkProState() {
        AppDatabase db = AppDatabase.getInstance(requireContext());
        AppExecutors.diskIO().execute(() -> {
            UserProfileEntity profile = db.userProfileDao().getNow();
            boolean isPro = profile != null && profile.isPremium;
            AppExecutors.mainThread().execute(() -> {
                if (btnStartPro != null) {
                    btnStartPro.setText(isPro ? R.string.renew_workout_upper : R.string.start_workout_upper);
                }
            });
        });
    }

    private void openCheckoutScreen() {
        boolean isYearly = rbYearly != null && rbYearly.isChecked();
        CheckoutBottomSheetFragment checkoutSheet = CheckoutBottomSheetFragment.newInstance(isYearly);
        checkoutSheet.setOnPaymentSuccessListener(() -> {
            if (getContext() != null && isAdded()) {
                NavHostFragment.findNavController(ProPaywallFragment.this).navigateUp();
            }
        });
        checkoutSheet.show(getChildFragmentManager(), "CheckoutBottomSheet");
    }
}
