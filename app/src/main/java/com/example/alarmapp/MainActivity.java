package com.example.alarmapp;

import static com.example.alarmapp.ReaderContract.Entry.COLUMN_NAME_HOUR;
import static com.example.alarmapp.ReaderContract.Entry.COLUMN_NAME_MINUTE;
import static com.example.alarmapp.ReaderContract.Entry.ID;
import static com.example.alarmapp.ReaderContract.Entry.TABLE_NAME;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements TimePickerFragment.FragmentCallbacks {
    public static final String CHANNEL_ID = "ALARM_SERVICE_CHANNEL";
    TextView noAlarmText, nextAlarmLabel1, nextAlarmLabel2, alarmClockText;
    Button setTimeButton, removeAlarmButton;

    SQLiteDatabase sqlDB;
    MyDBHelper myHelper;

    private final String[] projection = {
            ID,
            COLUMN_NAME_HOUR,
            COLUMN_NAME_MINUTE
    };

    private final String selection = ID + " =?";
    private final String[] selectionArgs = {"1"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Next Alarm");

        noAlarmText = (TextView) findViewById(R.id.noAlarmText);
        nextAlarmLabel1 = (TextView) findViewById(R.id.nextAlarmLabel1);
        nextAlarmLabel2 = (TextView) findViewById(R.id.nextAlarmLabel2);
        alarmClockText = (TextView) findViewById(R.id.alarmClockText);
        setTimeButton = (Button) findViewById(R.id.setAlarmButton);
        removeAlarmButton = (Button) findViewById(R.id.removeAlarmButton);

        myHelper = new MyDBHelper(this);
        sqlDB = myHelper.getReadableDatabase();

        setUI();
        createNotificationChannel();

        setTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAlarmDialog();
            }
        });

        removeAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAlarm();
            }
        });
    }

    public void setUI() {
        if (haveAlarm()) {
            noAlarmText.setVisibility(View.GONE);
            setTimeButton.setVisibility(View.GONE);

            nextAlarmLabel1.setVisibility(View.VISIBLE);
            nextAlarmLabel2.setVisibility(View.VISIBLE);
            alarmClockText.setVisibility(View.VISIBLE);
            removeAlarmButton.setVisibility(View.VISIBLE);
            showAlarmList();
        } else {
            noAlarmText.setVisibility(View.VISIBLE);
            setTimeButton.setVisibility(View.VISIBLE);

            nextAlarmLabel1.setVisibility(View.GONE);
            nextAlarmLabel2.setVisibility(View.GONE);
            alarmClockText.setVisibility(View.GONE);
            removeAlarmButton.setVisibility(View.GONE);
        }
    }

    public boolean haveAlarm() {
        long count = DatabaseUtils.queryNumEntries(sqlDB, TABLE_NAME, selection, selectionArgs);

        return count > 0;
    }

    public void showAlarmList() {
        Cursor cursor = sqlDB.query(
                TABLE_NAME,
                projection, selection, selectionArgs, null, null, null
        );

        int hourOfDay = 0, minute = 0;
        while (cursor.moveToNext()) {
            hourOfDay = cursor.getInt(1);
            minute = cursor.getInt(2);
        }
        cursor.close();

        String ap;
        if (hourOfDay < 12) {
            ap = "AM";
        } else if (hourOfDay == 12) {
            ap = "PM";
        } else {
            hourOfDay -= 12;
            ap = "PM";
        }
        alarmClockText.setText(String.format("%02d:%02d %s", hourOfDay, minute, ap));
    }

    public void removeAlarm() {
        sqlDB.delete(TABLE_NAME, selection, selectionArgs);

        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.action);

        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        alarmManager.cancel(pendingIntent);
        Toast.makeText(getApplicationContext(), "알람이 취소되었습니다.", Toast.LENGTH_SHORT).show();
        pendingIntent.cancel();
        setUI();
    }

    public void showAlarmDialog() {
        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.show(getSupportFragmentManager(), "timePicker");
        setUI();
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onDestroy() {
        sqlDB.close();
        myHelper.close();
        super.onDestroy();
    }

    @Override
    public void TimeUpdated() {
        setUI();
    }
}