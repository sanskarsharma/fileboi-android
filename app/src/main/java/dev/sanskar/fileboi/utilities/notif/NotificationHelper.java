package dev.sanskar.fileboi.utilities.notif;

import android.content.ContextWrapper;
import android.graphics.Color;
import android.content.Context;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import androidx.core.app.NotificationCompat;

import dev.sanskar.fileboi.R;

public class NotificationHelper extends ContextWrapper {

    private NotificationManager notifManager;
    public static final String CHANNEL_ONE_ID = "FILE_UPLOAD_NOTIF_CHANNEL";
    public static final String CHANNEL_ONE_NAME = "File uploads";

    public NotificationHelper(Context context) {
        super(context);
        createChannels();
    }

    public void createChannels() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationChannel.setDescription("Default notification channel for file upload notifications.");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getManager().createNotificationChannel(notificationChannel);

        }

    }

    //Create the notification that’ll be posted to Channel One//
    public NotificationCompat.Builder getFileUploadNotification() {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ONE_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setAutoCancel(true);
    }

    public void notify(int id, NotificationCompat.Builder notification) {
        getManager().notify(id, notification.build());
    }

    //Send your notifications to the NotificationManager system service//
    private NotificationManager getManager() {
        if (notifManager == null) {
            notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notifManager;
    }
}
