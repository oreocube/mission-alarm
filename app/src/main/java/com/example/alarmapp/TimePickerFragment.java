package com.example.alarmapp;

import static com.example.alarmapp.ReaderContract.Entry.COLUMN_NAME_HOUR;
import static com.example.alarmapp.ReaderContract.Entry.COLUMN_NAME_MINUTE;
import static com.example.alarmapp.ReaderContract.Entry.TABLE_NAME;
import static com.example.alarmapp.ReaderContract.Entry.ID;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private AlarmManager mAlarmManager;
    private MyDBHelper myHelper;
    private FragmentCallbacks mCallbacks;

    public interface FragmentCallbacks {
        void TimeUpdated();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // 알람 매니저 인스턴스 얻기
        mAlarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        myHelper = new MyDBHelper(getContext());

        // 현재 시간으로 타임 피커를 설정
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // 타임 피커 다이얼로그를 현재 시간 설정으로 생성하고 반환
        return new TimePickerDialog(getContext(), this, hour, minute,
                DateFormat.is24HourFormat(getContext()));
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        scheduleAlarm(hourOfDay, minute);
        updateDB(hourOfDay, minute);
        mCallbacks.TimeUpdated();
    }

    private void scheduleAlarm(int hourOfDay, int minute) {
        // 설정된 시간
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        // 서비스로 펜딩 인텐트 생성
        Intent intent = new Intent(getContext(), AlarmService.class);
        PendingIntent operation;

        // 설정된 시간에 기기가 슬립상태에서도 알람이 동작되도록 설정
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            operation = PendingIntent.getForegroundService(
                    getContext(), 0, intent, 0);
        } else {
            operation = PendingIntent.getService(getContext(), 0, intent, 0);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(), operation);
        } else {
            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), operation);
        }

        // 토스트 메시지
        Toast.makeText(getContext(), "알람이 설정되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void updateDB(int hourOfDay, int minute) {
        // 알람이 있는지 확인하기
        SQLiteDatabase sqlDB = myHelper.getReadableDatabase();

        String selection = ID + " =?";
        String[] selectionArgs = {"1"};

        long count = DatabaseUtils.queryNumEntries(sqlDB, TABLE_NAME, selection, selectionArgs);
        if (count == 1) { // 알람이 있다면 지우기
            sqlDB.delete(TABLE_NAME, selection, selectionArgs);
        }

        // 새로운 알람 저장하기
        sqlDB = myHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ID, 1);
        values.put(COLUMN_NAME_HOUR, hourOfDay);
        values.put(COLUMN_NAME_MINUTE, minute);

        sqlDB.insert(TABLE_NAME, null, values);
        sqlDB.close();
        myHelper.close();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (FragmentCallbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }
}


