package com.homefit.app.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.homefit.app.MainActivity;
import com.homefit.app.R;

/**
 * Nhận Broadcast từ AlarmManager và hiển thị Local Notification (W2-TV2-05).
 */
public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "workout_reminder_channel";
    private static final int NOTIFICATION_ID = 2001;

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) return;

        // minSdk 26 nên không cần kiểm tra SDK_INT >= O
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Nhắc nhở tập luyện",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription("Thông báo nhắc nhở bạn luyện tập hằng ngày");
        notificationManager.createNotificationChannel(channel);

        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, mainIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("Đến giờ tập luyện rồi!")
                .setContentText("Dành ra chút thời gian mỗi ngày để nâng cao sức khỏe cùng HomeFit nhé.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}