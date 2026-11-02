package com.homefit.app.ui.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
// FIXME: Thay báº±ng NotificationManager Ä‘Ãºng chuáº©n
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Reminder", "Nháº¯c táº­p: ÄÃ£ Ä‘áº¿n giá» táº­p luyá»‡n!");
        // (Tuáº§n sau Agent 6 lÃ m Notification)
    }
}
