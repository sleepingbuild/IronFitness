package com.homefit.app.ui.workout;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.homefit.app.R;
import com.homefit.app.ads.AdsManager;
import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.util.AppExecutors;

import java.util.Random;

public class CheckoutBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String ARG_IS_YEARLY = "arg_is_yearly";

    public interface OnPaymentSuccessListener {
        void onSuccess();
    }

    private boolean isYearly = true;
    private OnPaymentSuccessListener listener;

    private RadioButton rbPayGooglePlay;
    private RadioButton rbPayBankQr;
    private MaterialCardView cardPayGooglePlay;
    private MaterialCardView cardPayBankQr;
    private LinearLayout llBankQrDetails;
    private LinearProgressIndicator progressCheckout;
    private MaterialButton btnConfirmPayment;

    public static CheckoutBottomSheetFragment newInstance(boolean isYearly) {
        CheckoutBottomSheetFragment fragment = new CheckoutBottomSheetFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_YEARLY, isYearly);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnPaymentSuccessListener(OnPaymentSuccessListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            isYearly = getArguments().getBoolean(ARG_IS_YEARLY, true);
        }

        ImageButton btnClose = view.findViewById(R.id.btnCloseCheckout);
        TextView tvOrderPlanTitle = view.findViewById(R.id.tvOrderPlanTitle);
        TextView tvOrderPlanSubtitle = view.findViewById(R.id.tvOrderPlanSubtitle);
        TextView tvOrderDiscountBadge = view.findViewById(R.id.tvOrderDiscountBadge);
        TextView tvOrderTotalPrice = view.findViewById(R.id.tvOrderTotalPrice);
        TextView tvOrderCode = view.findViewById(R.id.tvOrderCode);

        rbPayGooglePlay = view.findViewById(R.id.rbPayGooglePlay);
        rbPayBankQr = view.findViewById(R.id.rbPayBankQr);
        cardPayGooglePlay = view.findViewById(R.id.cardPayGooglePlay);
        cardPayBankQr = view.findViewById(R.id.cardPayBankQr);
        llBankQrDetails = view.findViewById(R.id.llBankQrDetails);
        progressCheckout = view.findViewById(R.id.progressCheckout);
        btnConfirmPayment = view.findViewById(R.id.btnConfirmPayment);

        if (isYearly) {
            tvOrderPlanTitle.setText("IronFitness PRO (Gói 1 Năm)");
            tvOrderPlanSubtitle.setText("Tiết kiệm 67% • Dùng thử 7 ngày miễn phí");
            tvOrderDiscountBadge.setVisibility(View.VISIBLE);
            tvOrderTotalPrice.setText("928.000 đ");
        } else {
            tvOrderPlanTitle.setText("IronFitness PRO (Gói 1 Tháng)");
            tvOrderPlanSubtitle.setText("Gia hạn tự động hàng tháng • Hủy bất kỳ lúc nào");
            tvOrderDiscountBadge.setVisibility(View.GONE);
            tvOrderTotalPrice.setText("232.000 đ");
        }

        int orderNum = 1000 + new Random().nextInt(8999);
        tvOrderCode.setText("• Nội dung: PRO-" + orderNum);

        btnClose.setOnClickListener(v -> dismiss());

        cardPayGooglePlay.setOnClickListener(v -> selectPaymentMethod(true));
        cardPayBankQr.setOnClickListener(v -> selectPaymentMethod(false));

        btnConfirmPayment.setOnClickListener(v -> processPayment());
    }

    private void selectPaymentMethod(boolean isGooglePlay) {
        rbPayGooglePlay.setChecked(isGooglePlay);
        rbPayBankQr.setChecked(!isGooglePlay);

        if (isGooglePlay) {
            cardPayGooglePlay.setStrokeColor(0xFF0066FF);
            cardPayGooglePlay.setStrokeWidth((int) (2 * getResources().getDisplayMetrics().density));
            cardPayBankQr.setStrokeColor(0xFFE2E8F0);
            cardPayBankQr.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
            llBankQrDetails.setVisibility(View.GONE);
        } else {
            cardPayBankQr.setStrokeColor(0xFF0066FF);
            cardPayBankQr.setStrokeWidth((int) (2 * getResources().getDisplayMetrics().density));
            cardPayGooglePlay.setStrokeColor(0xFFE2E8F0);
            cardPayGooglePlay.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
            llBankQrDetails.setVisibility(View.VISIBLE);
        }
    }

    private void processPayment() {
        if (progressCheckout != null) progressCheckout.setVisibility(View.VISIBLE);
        if (btnConfirmPayment != null) {
            btnConfirmPayment.setEnabled(false);
            btnConfirmPayment.setText("Đang xử lý giao dịch...");
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            AppExecutors.diskIO().execute(() -> {
                UserProfileEntity profile = db.userProfileDao().getNow();
                if (profile == null) {
                    profile = new UserProfileEntity();
                    profile.email = "user@example.com";
                    profile.displayName = "Hội viên PRO";
                }
                profile.isPremium = true;
                db.userProfileDao().upsert(profile);

                AppExecutors.mainThread().execute(() -> {
                    AdsManager.setAdsEnabled(false);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Thanh toán thành công! Chúc mừng bạn đã nâng cấp IronFitness PRO!", Toast.LENGTH_LONG).show();
                    }
                    dismiss();
                    if (listener != null) {
                        listener.onSuccess();
                    }
                });
            });
        }, 1200);
    }
}
