package com.example.dustapp;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("설정");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            PreferenceManager preferenceManager = getPreferenceManager();
            SwitchPreference enable = (SwitchPreference) findPreference("cancel1");
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            DialogFragment dialogFragment = null;
            if (preference instanceof TimePreference) {
                dialogFragment = new TimePreferenceDialogFragmentCompat();
                Bundle bundle = new Bundle(2);
                bundle.putString("key", preference.getKey());
                PreferenceManager preferenceManager = getPreferenceManager();
                bundle.putBoolean("switch", preferenceManager.getSharedPreferences().getBoolean("cancel1", true));
                dialogFragment.setArguments(bundle);
            }

            if (dialogFragment != null) {
                dialogFragment.setTargetFragment(this, 0);
                dialogFragment.show(this.getFragmentManager(), "androidx.preference.PreferenceFragment.DIALOG");
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            if (preference.getKey().equals("cancel1")) {
                PreferenceManager preferenceManager = getPreferenceManager();
                Alarm alarm = new Alarm();
                if (preferenceManager.getSharedPreferences().getBoolean("cancel1", true)) {
                    alarm.renewAlarm(getActivity());
                } else {
                    alarm.cancelAlarm(getActivity());
                }
                return true;
            }
            return false;
        }
    }
}