package com.example.alarmapp;

import static android.content.Context.POWER_SERVICE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String action = "com.example.alarmapp.ALARM_START";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action)) {
            Log.d("로그", "onReceive: ");

            PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "com.example.alarmapp:tag");
            wakeLock.acquire(60000);

            Intent intentService = new Intent(context, AlarmService.class);
            ContextCompat.startForegroundService(context, intentService);
        }
    }
}
