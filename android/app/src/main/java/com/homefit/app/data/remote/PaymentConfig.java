package com.homefit.app.data.remote;

import android.net.Uri;
import java.util.Locale;

public class PaymentConfig {
    public static final String BANK_ID = "MB"; // MB Bank (Ngan hang TMCP Quan doi)
    public static final String ACCOUNT_NO = "0386841085";
    public static final String ACCOUNT_NAME = "PHAM HOANG PHI";

    public static String getVietQrUrl(long amount, String orderCode) {
        String nameEncoded = Uri.encode(ACCOUNT_NAME);
        return String.format(Locale.US,
                "https://img.vietqr.io/image/%s-%s-compact2.png?amount=%d&addInfo=%s&accountName=%s",
                BANK_ID, ACCOUNT_NO, amount, orderCode, nameEncoded);
    }
}
