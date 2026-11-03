package com.homefit.app.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Khôi phục lại lịch hẹn nhắc tập sau khi thiết bị khởi động lại (W2-TV2-05).
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("homefit_settings", Context.MODE_PRIVATE);
            boolean enabled = prefs.getBoolean("reminder_enabled", false);

            if (enabled) {
                int hour = prefs.getInt("reminder_hour", 18);
                int minute = prefs.getInt("reminder_minute", 0);
                ReminderScheduler.scheduleDaily(context, hour, minute);
            }
        }
    }
}