package com.homefit.app.ui.workout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.homefit.app.R;
import com.homefit.app.ads.AdsManager;
import com.homefit.app.data.local.AppDatabase;
import com.homefit.app.data.local.entity.UserProfileEntity;
import com.homefit.app.data.remote.ApiClient;
import com.homefit.app.data.remote.PaymentConfig;
import com.homefit.app.data.remote.dto.CreateOrderRequestDto;
import com.homefit.app.data.remote.dto.CreateOrderResponseDto;
import com.homefit.app.data.remote.dto.GooglePlayVerifyRequestDto;
import com.homefit.app.data.remote.dto.PaymentStatusResponseDto;
import com.homefit.app.data.remote.dto.UserDto;
import com.homefit.app.data.repository.AuthRepository;
import com.homefit.app.util.AppExecutors;
import com.homefit.app.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutBottomSheetFragment extends BottomSheetDialogFragment {

    private static final String TAG = "CheckoutBottomSheet";
    private static final String ARG_IS_YEARLY = "arg_is_yearly";
    private static final String PRODUCT_ID_MONTHLY = "ironfitness_pro_monthly";
    private static final String PRODUCT_ID_YEARLY = "ironfitness_pro_yearly";

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

    private ImageView ivVietQrCode;
    private TextView tvCountdownTimer;
    private TextView tvBankInfo;
    private TextView tvOrderCode;
    private TextView tvPaymentStatusInfo;
    private MaterialButton btnCopyStk;
    private MaterialButton btnCopyContent;
    private MaterialButton btnSaveQr;
    private MaterialButton btnCheckStatus;

    private String currentOrderCode = "";
    private long currentAmount = 928000;
    private CountDownTimer countDownTimer;
    private final Handler pollingHandler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;
    private boolean isPaid = false;

    // Google Play Billing Client
    private BillingClient billingClient;
    private ProductDetails monthlyProductDetails;
    private ProductDetails yearlyProductDetails;

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
        currentAmount = isYearly ? 928000 : 232000;
        currentOrderCode = "IFPRO" + (100000 + new Random().nextInt(899999));

        ImageButton btnClose = view.findViewById(R.id.btnCloseCheckout);
        TextView tvOrderPlanTitle = view.findViewById(R.id.tvOrderPlanTitle);
        TextView tvOrderPlanSubtitle = view.findViewById(R.id.tvOrderPlanSubtitle);
        TextView tvOrderDiscountBadge = view.findViewById(R.id.tvOrderDiscountBadge);
        TextView tvOrderTotalPrice = view.findViewById(R.id.tvOrderTotalPrice);

        rbPayGooglePlay = view.findViewById(R.id.rbPayGooglePlay);
        rbPayBankQr = view.findViewById(R.id.rbPayBankQr);
        cardPayGooglePlay = view.findViewById(R.id.cardPayGooglePlay);
        cardPayBankQr = view.findViewById(R.id.cardPayBankQr);
        llBankQrDetails = view.findViewById(R.id.llBankQrDetails);
        progressCheckout = view.findViewById(R.id.progressCheckout);
        btnConfirmPayment = view.findViewById(R.id.btnConfirmPayment);

        ivVietQrCode = view.findViewById(R.id.ivVietQrCode);
        tvCountdownTimer = view.findViewById(R.id.tvCountdownTimer);
        tvBankInfo = view.findViewById(R.id.tvBankInfo);
        tvOrderCode = view.findViewById(R.id.tvOrderCode);
        tvPaymentStatusInfo = view.findViewById(R.id.tvPaymentStatusInfo);
        btnCopyStk = view.findViewById(R.id.btnCopyStk);
        btnCopyContent = view.findViewById(R.id.btnCopyContent);
        btnSaveQr = view.findViewById(R.id.btnSaveQr);
        btnCheckStatus = view.findViewById(R.id.btnCheckStatus);

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

        if (btnClose != null) btnClose.setOnClickListener(v -> dismiss());

        cardPayGooglePlay.setOnClickListener(v -> selectPaymentMethod(true));
        cardPayBankQr.setOnClickListener(v -> selectPaymentMethod(false));
        btnConfirmPayment.setOnClickListener(v -> processPayment());

        setupBankQrActions();
        createOrderOnServer();
        setupGooglePlayBilling();
    }

    private void setupGooglePlayBilling() {
        if (getContext() == null) return;
        PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
                for (Purchase purchase : purchases) {
                    handleGooglePlayPurchase(purchase);
                }
            } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(requireContext(), "Đã hủy thanh toán Google Play", Toast.LENGTH_SHORT).show();
                }
            } else {
                showGooglePlayUnavailableAlert();
            }
        };

        billingClient = BillingClient.newBuilder(requireContext())
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().enablePrepaidPlans().build())
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Connection disconnected, will retry when launching flow if needed
            }
        });
    }

    private void queryProductDetails() {
        if (billingClient == null || !billingClient.isReady()) return;
        List<QueryProductDetailsParams.Product> productList = new ArrayList<>();
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_MONTHLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build());
        productList.add(QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_YEARLY)
                .setProductType(BillingClient.ProductType.SUBS)
                .build());

        QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(params, (billingResult, productDetailsList) -> {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && productDetailsList != null) {
                for (ProductDetails pd : productDetailsList) {
                    if (PRODUCT_ID_MONTHLY.equals(pd.getProductId())) {
                        monthlyProductDetails = pd;
                    } else if (PRODUCT_ID_YEARLY.equals(pd.getProductId())) {
                        yearlyProductDetails = pd;
                    }
                }
            }
        });
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
            stopPolling();
        } else {
            cardPayBankQr.setStrokeColor(0xFF0066FF);
            cardPayBankQr.setStrokeWidth((int) (2 * getResources().getDisplayMetrics().density));
            cardPayGooglePlay.setStrokeColor(0xFFE2E8F0);
            cardPayGooglePlay.setStrokeWidth((int) (1 * getResources().getDisplayMetrics().density));
            llBankQrDetails.setVisibility(View.VISIBLE);
            startCountdownAndPolling();
        }
    }

    private void createOrderOnServer() {
        if (!isAdded()) return;
        CreateOrderRequestDto request = new CreateOrderRequestDto(isYearly ? "YEARLY" : "MONTHLY", currentAmount);
        ApiClient.get(requireContext()).createOrder(request).enqueue(new Callback<CreateOrderResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<CreateOrderResponseDto> call, @NonNull Response<CreateOrderResponseDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CreateOrderResponseDto body = response.body();
                    if (body.orderCode != null) currentOrderCode = body.orderCode;
                }
                updateQrView();
            }

            @Override
            public void onFailure(@NonNull Call<CreateOrderResponseDto> call, @NonNull Throwable t) {
                Log.e(TAG, "Lỗi tạo đơn hàng server: " + t.getMessage());
                updateQrView();
            }
        });
    }

    private void updateQrView() {
        if (!isAdded()) return;
        if (tvOrderCode != null) {
            tvOrderCode.setText("• Nội dung CK: " + currentOrderCode);
        }
        if (tvBankInfo != null) {
            tvBankInfo.setText("• MB Bank: " + PaymentConfig.ACCOUNT_NO + " (" + PaymentConfig.ACCOUNT_NAME + ")");
        }

        String vietQrUrl = PaymentConfig.getVietQrUrl(currentAmount, currentOrderCode);
        if (ivVietQrCode != null) {
            Glide.with(this)
                    .load(vietQrUrl)
                    .placeholder(R.drawable.bg_media_placeholder)
                    .into(ivVietQrCode);
        }
    }

    private void setupBankQrActions() {
        if (btnCopyStk != null) {
            btnCopyStk.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("STK", PaymentConfig.ACCOUNT_NO);
                if (clipboard != null) clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), R.string.payment_stk_copied, Toast.LENGTH_SHORT).show();
            });
        }

        if (btnCopyContent != null) {
            btnCopyContent.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Content", currentOrderCode);
                if (clipboard != null) clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), R.string.payment_content_copied, Toast.LENGTH_SHORT).show();
            });
        }

        if (btnSaveQr != null) {
            btnSaveQr.setOnClickListener(v -> saveQrImageToGallery());
        }

        if (btnCheckStatus != null) {
            btnCheckStatus.setOnClickListener(v -> checkPaymentStatusOnServer(true));
        }
    }

    private void saveQrImageToGallery() {
        if (!isAdded() || ivVietQrCode == null) return;
        try {
            android.graphics.drawable.BitmapDrawable drawable = (android.graphics.drawable.BitmapDrawable) ivVietQrCode.getDrawable();
            if (drawable == null || drawable.getBitmap() == null) {
                Toast.makeText(requireContext(), "Đang tải mã QR, vui lòng thử lại sau giây lát!", Toast.LENGTH_SHORT).show();
                return;
            }
            android.graphics.Bitmap bitmap = drawable.getBitmap();

            android.content.ContentValues values = new android.content.ContentValues();
            values.put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "VietQR_" + currentOrderCode + ".png");
            values.put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                values.put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/IronFitness");
            }

            android.net.Uri uri = requireContext().getContentResolver().insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                java.io.OutputStream out = requireContext().getContentResolver().openOutputStream(uri);
                if (out != null) {
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                    Toast.makeText(requireContext(), "Đã lưu ảnh mã QR vào Thư viện ảnh!", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(requireContext(), "Không thể tạo file lưu ảnh QR.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi lưu ảnh QR: " + e.getMessage());
            Toast.makeText(requireContext(), "Lỗi khi lưu ảnh QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startCountdownAndPolling() {
        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(15 * 60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (tvCountdownTimer != null) {
                    long minutes = millisUntilFinished / 1000 / 60;
                    long seconds = (millisUntilFinished / 1000) % 60;
                    tvCountdownTimer.setText(String.format(Locale.US, "Mã QR hết hạn sau: %02d:%02d", minutes, seconds));
                }
            }

            @Override
            public void onFinish() {
                if (tvCountdownTimer != null) {
                    tvCountdownTimer.setText("Mã QR đã hết hạn. Vui lòng tạo đơn mới!");
                }
                stopPolling();
            }
        }.start();

        startPolling();
    }

    private void startPolling() {
        stopPolling();
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPaid || !isAdded()) return;
                checkPaymentStatusOnServer(false);
                pollingHandler.postDelayed(this, 4000);
            }
        };
        pollingHandler.postDelayed(pollingRunnable, 4000);
    }

    private void stopPolling() {
        if (pollingRunnable != null) {
            pollingHandler.removeCallbacks(pollingRunnable);
        }
    }

    private void checkPaymentStatusOnServer(boolean isManualCheck) {
        if (!isAdded() || isPaid) return;
        if (isManualCheck && progressCheckout != null) {
            progressCheckout.setVisibility(View.VISIBLE);
        }

        ApiClient.get(requireContext()).getPaymentStatus(currentOrderCode).enqueue(new Callback<PaymentStatusResponseDto>() {
            @Override
            public void onResponse(@NonNull Call<PaymentStatusResponseDto> call, @NonNull Response<PaymentStatusResponseDto> response) {
                if (progressCheckout != null) progressCheckout.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    PaymentStatusResponseDto res = response.body();
                    if ("PAID".equalsIgnoreCase(res.status) || res.isPro) {
                        onPaymentSuccessConfirmed(res.proExpiryDate);
                    } else if (isManualCheck) {
                        Toast.makeText(requireContext(), "Chưa nhận được chuyển khoản. Vui lòng thử lại sau khi chuyển!", Toast.LENGTH_SHORT).show();
                    }
                } else if (isManualCheck) {
                    Toast.makeText(requireContext(), "Đang chờ biến động số dư từ ngân hàng...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentStatusResponseDto> call, @NonNull Throwable t) {
                if (progressCheckout != null) progressCheckout.setVisibility(View.GONE);
                if (isManualCheck) {
                    Toast.makeText(requireContext(), "Chưa ghi nhận thanh toán trên server.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void processPayment() {
        if (rbPayBankQr.isChecked()) {
            checkPaymentStatusOnServer(true);
        } else {
            launchGooglePlayBilling();
        }
    }

    private void launchGooglePlayBilling() {
        if (billingClient == null || !billingClient.isReady()) {
            showGooglePlayUnavailableAlert();
            return;
        }

        ProductDetails targetDetails = isYearly ? yearlyProductDetails : monthlyProductDetails;
        if (targetDetails == null) {
            showGooglePlayUnavailableAlert();
            return;
        }

        List<ProductDetails.SubscriptionOfferDetails> offers = targetDetails.getSubscriptionOfferDetails();
        String offerToken = (offers != null && !offers.isEmpty()) ? offers.get(0).getOfferToken() : "";

        List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList = new ArrayList<>();
        productDetailsParamsList.add(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(targetDetails)
                        .setOfferToken(offerToken)
                        .build()
        );

        BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build();

        BillingResult result = billingClient.launchBillingFlow(requireActivity(), flowParams);
        if (result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
            showGooglePlayUnavailableAlert();
        }
    }

    private void showGooglePlayUnavailableAlert() {
        if (!isAdded() || getContext() == null) return;
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cổng thanh toán Google Play")
                .setMessage("Cổng thanh toán Google Play chỉ khả dụng trên bản cài đặt từ Google Play Store. Vui lòng sử dụng phương thức Chuyển khoản VietQR bên dưới.")
                .setPositiveButton("Đã hiểu", null)
                .show();
    }

    private void handleGooglePlayPurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                    verifyGooglePlayPurchaseOnServer(purchase);
                });
            } else {
                verifyGooglePlayPurchaseOnServer(purchase);
            }
        }
    }

    private void verifyGooglePlayPurchaseOnServer(Purchase purchase) {
        if (!isAdded()) return;
        if (progressCheckout != null) progressCheckout.setVisibility(View.VISIBLE);

        String productId = (purchase.getProducts() != null && !purchase.getProducts().isEmpty())
                ? purchase.getProducts().get(0)
                : (isYearly ? PRODUCT_ID_YEARLY : PRODUCT_ID_MONTHLY);

        GooglePlayVerifyRequestDto request = new GooglePlayVerifyRequestDto(purchase.getPurchaseToken(), productId);
        ApiClient.get(requireContext()).verifyGooglePlayPayment(request).enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(@NonNull Call<UserDto> call, @NonNull Response<UserDto> response) {
                if (progressCheckout != null) progressCheckout.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    UserDto userDto = response.body();
                    onPaymentSuccessConfirmed(userDto.proExpiryDate);
                } else {
                    long fallbackExpiry = System.currentTimeMillis() + (isYearly ? 365L : 30L) * 86400000L;
                    onPaymentSuccessConfirmed(DateUtils.toIso(fallbackExpiry));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UserDto> call, @NonNull Throwable t) {
                if (progressCheckout != null) progressCheckout.setVisibility(View.GONE);
                long fallbackExpiry = System.currentTimeMillis() + (isYearly ? 365L : 30L) * 86400000L;
                onPaymentSuccessConfirmed(DateUtils.toIso(fallbackExpiry));
            }
        });
    }

    private void playSuccessFeedback() {
        try {
            Context context = getContext();
            if (context != null) {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(300);
                    }
                }
                ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 250);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi phát âm thanh/rung: " + e.getMessage());
        }
    }

    private void onPaymentSuccessConfirmed(@Nullable String newProExpiryDateIso) {
        if (isPaid) return;
        isPaid = true;
        stopPolling();
        if (countDownTimer != null) countDownTimer.cancel();

        playSuccessFeedback();

        long expiryMillis = DateUtils.parseIso(newProExpiryDateIso);
        if (expiryMillis <= System.currentTimeMillis()) {
            expiryMillis = System.currentTimeMillis() + (isYearly ? 365L : 30L) * 86400000L;
        }
        final long finalExpiryMillis = expiryMillis;

        AppDatabase db = AppDatabase.getInstance(requireContext());
        AppExecutors.diskIO().execute(() -> {
            UserProfileEntity profile = db.userProfileDao().getNow();
            if (profile == null) {
                profile = new UserProfileEntity();
                profile.displayName = "Hội viên PRO";
            }
            profile.isPremium = true;
            profile.proExpiryDate = finalExpiryMillis;
            db.userProfileDao().upsert(profile);

            AuthRepository authRepo = new AuthRepository(requireContext());
            authRepo.upgradePro();

            AppExecutors.mainThread().execute(() -> {
                AdsManager.setAdsEnabled(false);
                showSuccessDialog(finalExpiryMillis);
            });
        });
    }

    private void showSuccessDialog(long proExpiryDateMs) {
        if (!isAdded() || getContext() == null) return;
        String expiryStr = DateUtils.formatDateDdMmYyyy(proExpiryDateMs);
        String message = expiryStr.isEmpty()
                ? "Tài khoản của bạn đã được nâng cấp lên gói IronFitness PRO thành công!"
                : "Tài khoản của bạn đã được nâng cấp lên gói IronFitness PRO thành công!\n• Ngày hết hạn: " + expiryStr;

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("🎉 Nâng cấp IronFitness PRO thành công!")
                .setMessage(message)
                .setPositiveButton("Bắt đầu trải nghiệm", (dialog, which) -> {
                    dismiss();
                    if (listener != null) {
                        listener.onSuccess();
                    }
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public void onDestroyView() {
        stopPolling();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (billingClient != null) {
            try {
                billingClient.endConnection();
            } catch (Exception e) {
                Log.e(TAG, "Lỗi đóng kết nối billingClient: " + e.getMessage());
            }
        }
        super.onDestroyView();
    }
}
