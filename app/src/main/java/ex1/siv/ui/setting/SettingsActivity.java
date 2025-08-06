package ex1.siv.ui.setting;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;

import ex1.siv.R;
import ex1.siv.util.ShowUtil;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            EditTextPreference timerPref = findPreference(CommonSetting.FIELD_TIMER_TIME);
            assert timerPref != null;
            timerPref.setOnPreferenceChangeListener((preference, newValue) -> {
                long time = Long.parseLong(newValue.toString());
                if (!CommonSetting.isValidTimerTime(time)) {
                    ShowUtil.showUserError(getContext(), R.string.pref_err_timer);
                    return false;
                }
                return true;
            });

            EditTextPreference naviPercentPref = findPreference(CommonSetting.FIELD_NAVI_PERCENT);
            assert naviPercentPref != null;
            naviPercentPref.setOnPreferenceChangeListener((preference, newValue) -> {
                int percent = Integer.parseInt(newValue.toString());
                if (!CommonSetting.isValidNaviPercent(percent)) {
                    ShowUtil.showUserError(getContext(), R.string.pref_err_navi_percent);
                    return false;
                }
                return true;
            });
        }
    }
}