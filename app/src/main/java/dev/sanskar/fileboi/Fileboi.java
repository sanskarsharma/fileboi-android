package dev.sanskar.fileboi;

import android.app.Application;
import android.content.Context;

import dev.sanskar.fileboi.utilities.notif.NotificationHelper;

public class Fileboi extends Application {
    public static Fileboi instance;

    public Fileboi() {
        instance = this;
    }

    public static Fileboi getInstance() {
        return instance;
    }

    public static Context getContext(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        NotificationHelper notificationHelper = new NotificationHelper(getContext());
        notificationHelper.createChannels();
    }

}
