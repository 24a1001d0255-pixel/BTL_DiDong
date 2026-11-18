package com.example.btl_didong;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    public static final String CHANNEL_ID = "food_picker_suggestions";
    public static final String CHANNEL_NAME = "Gợi ý món ăn hôm nay";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo gợi ý các quán ăn ngon gần bạn");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void sendSuggestionNotification(Context context, String placeName, String category, int placeId) {
        createNotificationChannel(context);

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("OPEN_PLACE_NAME", placeName);
        intent.putExtra("OPEN_PLACE_CATEGORY", category);
        intent.putExtra("OPEN_PLACE_ID", placeId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                101,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_home)
                .setContentTitle("🍜 Hôm nay ăn gì? Gợi ý quán ngon!")
                .setContentText("Gợi ý cho bạn: " + placeName + " (" + category + ") - Bấm để xem!")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("Gợi ý đặc biệt: " + placeName + " (" + category + "). Bấm vào đây để mở ứng dụng và xem chi tiết quán ăn!"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(1002, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
}
