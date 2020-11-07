package com.example.dustapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.AlarmManagerCompat;

import java.util.Calendar;

import static android.app.AlarmManager.RTC_WAKEUP;

public class Alarm extends BroadcastReceiver {
    final static int typeshuffle = 33619;

    public Alarm() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent i) {
        int hour = i.getIntExtra("hour", -1);
        int minute = i.getIntExtra("minute", -1);
        int type = i.getIntExtra("type", -1);

        if (type != -1) {

            DataManager dman = new DataManager(context, false);
            try {
                Object result = dman.execute().get();
            } catch (Exception e) {
            }

            DataPackage dpkg = dman.readData();
            NotificationController ncon = new NotificationController(context);

            if (dpkg.bigDust >= 81 || dpkg.smallDust >= 36) {
                ncon.showNotification(type);
            } else {
                ncon.showNotification(0);
            }
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent ni = new Intent(context, Alarm.class);
            ni.putExtra("hour", hour);
            ni.putExtra("minute", hour);
            ni.putExtra("type", type);
            PendingIntent pi = PendingIntent.getBroadcast(context, type + typeshuffle, i, 0);
            AlarmManagerCompat.setExactAndAllowWhileIdle(am, RTC_WAKEUP, calculateTime(hour, minute), pi);
        }
    }

    public boolean setAlarm(Context mcontext, int hour, int minute, int type) {
        if (hour != -1 && minute != -1 && type != -1) {
            AlarmManager am = (AlarmManager) mcontext.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(mcontext, Alarm.class);
            i.putExtra("hour", hour);
            i.putExtra("minute", hour);
            i.putExtra("type", type);
            PendingIntent pi = PendingIntent.getBroadcast(mcontext, type + typeshuffle, i, PendingIntent.FLAG_UPDATE_CURRENT);
            AlarmManagerCompat.setExactAndAllowWhileIdle(am, RTC_WAKEUP, calculateTime(hour, minute), pi);
            saveAlarm(hour, minute, type, mcontext);
            return true;
        }

        return false;
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender1 = PendingIntent.getBroadcast(context, 1 + typeshuffle, intent, 0);
        PendingIntent sender2 = PendingIntent.getBroadcast(context, 2 + typeshuffle, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender1);
        alarmManager.cancel(sender2);
    }

    public void saveAlarm(int hour, int minute, int type, Context mcontext) {
        String key = "NULL";
        if (type == 1) {
            key = "alarm1";
        } else if (type == 2) {
            key = "alarm2";
        }
        SharedPreferences sharedPref = mcontext.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("hour", hour);
        editor.putInt("minute", minute);
        editor.commit();
    }

    public void renewAlarm(Context mcontext) {
        SharedPreferences msharedPref = mcontext.getSharedPreferences("alarm1", Context.MODE_PRIVATE);
        SharedPreferences asharedPref = mcontext.getSharedPreferences("alarm2", Context.MODE_PRIVATE);
        int mhour = msharedPref.getInt("hour", -1);
        int mminute = msharedPref.getInt("minute", -1);
        int ahour = asharedPref.getInt("minute", -1);
        int aminute = asharedPref.getInt("minute", -1);

        if (mhour != -1 && mminute != -1) {
            setAlarm(mcontext, mhour, mminute, 1);
        }
        if (ahour != -1 && aminute != -1) {
            setAlarm(mcontext, ahour, aminute, 2);
        }
    }

    private long calculateTime(int hour, int minute) {
        Calendar currentTime = Calendar.getInstance();
        Calendar targetTime = Calendar.getInstance();
        targetTime.set(targetTime.get(Calendar.YEAR), targetTime.get(Calendar.MONTH), targetTime.get(Calendar.DATE), hour, minute, 0);

        if (targetTime.compareTo(currentTime) <= 0) {
            targetTime.set(targetTime.get(Calendar.YEAR), targetTime.get(Calendar.MONTH), targetTime.get(Calendar.DATE) + 1, hour, minute, 0);
        }

        return targetTime.getTimeInMillis();
    }
}