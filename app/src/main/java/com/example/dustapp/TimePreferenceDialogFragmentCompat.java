package com.example.dustapp;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;

public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat implements DialogPreference.TargetFragment {
    TimePicker timePicker = null;

    @Override
    protected View onCreateDialogView(Context context) {
        timePicker = new TimePicker(context);
        return (timePicker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        timePicker.setIs24HourView(false);
        TimePreference pref = (TimePreference) getPreference();
        timePicker.setCurrentHour(pref.hour);
        timePicker.setCurrentMinute(pref.minute);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            TimePreference pref = (TimePreference) getPreference();
            pref.hour = timePicker.getCurrentHour();
            pref.minute = timePicker.getCurrentMinute();

            String key = "null";
            Bundle bundle = this.getArguments();
            if (bundle != null) {
                key = bundle.getString("key", "nokey");
            }

            if (key != "null") {
                int type = -1;
                if (key.equals("alarm1")) {
                    type = 1;
                }
                if (key.equals("alarm2")) {
                    type = 2;
                }
                Alarm alarm = new Alarm();
                if (bundle.getBoolean("switch")) {
                    alarm.setAlarm(getActivity(), pref.hour, pref.minute, type);
                } else {
                    alarm.saveAlarm(pref.hour, pref.minute, type, getActivity());
                }
            }

            String value = TimePreference.timeToString(pref.hour, pref.minute);
            if (pref.callChangeListener(value)) pref.persistStringValue(value);
        }
    }

    @Override
    public Preference findPreference(CharSequence charSequence) {
        return getPreference();
    }
}
