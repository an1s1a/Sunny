package anisia.sunny;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import anisia.sunny.data.WeatherContract;
import anisia.sunny.sync.SunnySyncAdapter;

/**
 * A {@link PreferenceActivity} that presents a set of application settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */

//TODO non usare PreferenceActivity ma PreferenceFragment perchè dalle api 3.0 in poi è preferibile
public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);

        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_location_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_temperature_units_key)));
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_icon_pack_key)));
    }

    @Override
    protected void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    // Unregisters a shared preference change listener
    @Override
    protected void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        setPreferenceSummary(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        setPreferenceSummary(preference, value);
        return true;
    }

    private void setPreferenceSummary(Preference preference, Object value){
        String stringValue = value.toString();
        String key = preference.getKey();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (key.equals(getString(R.string.pref_location_key))){
            @SunnySyncAdapter.LocationStatus int locationStatus = Utility.getLocationStatus(this);
            switch (locationStatus) {
                case SunnySyncAdapter.LOCATION_STATUS_OK:
                    preference.setSummary(stringValue);
                    break;
                case SunnySyncAdapter.LOCATION_STATUS_UNKNOWN:
                    String unknown = getString(R.string.pref_location_unknown_description, value.toString());
                    preference.setSummary(unknown);
                    break;
                case SunnySyncAdapter.LOCATION_STATUS_INVALID:
                    String invalid = getString(R.string.pref_location_error_description,value.toString());
                    preference.setSummary(invalid);
                    break;
                default:
                    preference.setSummary(stringValue);
            }
        }  else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_location_key))){
            Utility.resetLocationStatus(this);
            SunnySyncAdapter.syncImmediately(this);
        } else if(key.equals(getString(R.string.pref_temperature_units_key))){
            getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
        } else if(key.equals(getString(R.string.pref_location_status_key))){
            Preference preference = findPreference(getString(R.string.pref_location_key));
            bindPreferenceSummaryToValue(preference);
        } else if (key.equals(getString(R.string.pref_icon_pack_key))) {
            String pref_icon = Utility.getPreferredIconPack(getApplicationContext());
            //if((pref_icon.equalsIgnoreCase(getString(R.string.app_name)))){
                getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI, null);
            //}

        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}