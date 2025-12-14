package com.example.lostfind;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d("FCM", "메시지 수신됨: " + remoteMessage.getNotification());

        // ⭐︎⭐︎ 1) 알림 채널 생성 (Android 8 이상 필수) ⭐︎⭐︎
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "default",              // 채널 ID
                    "Default Channel",      // 채널 이름
                    NotificationManager.IMPORTANCE_HIGH  // 중요도
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // ⭐︎⭐︎ 2) 실제 알림 표시 ⭐︎⭐︎
        if (remoteMessage.getNotification() != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default")
//                    .setSmallIcon(R.mipmap.ic_launcher) // 아이콘 없으면 기본 런처 아이콘
                    .setSmallIcon(R.drawable.login_icon)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody())
                    .setAutoCancel(true);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.d("FCM", "새 FCM 토큰 생성됨: " + token);

    }
}
