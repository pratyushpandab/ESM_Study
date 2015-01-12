package com.aware.plugin.esmstudy;

import com.aware.Aware;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    public static final String STATUS_PLUGIN_ESM = "status_plugin_esm";
    //public static final String QUESTIONNAIRES_PLUGIN_ESM_QUESTIONNAIRE = "questionnaires_plugin_esm_questionnaire";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("asd", "Settings onCreate");
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        syncSettings();
    }

    private void syncSettings() {
        CheckBoxPreference active = (CheckBoxPreference) findPreference(STATUS_PLUGIN_ESM);
        Log.d("asd", "SyncSettings");
        //Log.d("asd", Aware.getSetting(getApplicationContext(), QUESTIONNAIRES_PLUGIN_ESM_QUESTIONNAIRE));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d("asd", "onSharedPreferenceChanged");
        Preference preference = (Preference) findPreference(key);

        if( preference.getKey().equals(STATUS_PLUGIN_ESM)) {
            boolean is_active = sharedPreferences.getBoolean(key, false);
            Aware.setSetting(getApplicationContext(), key, is_active);
            if( is_active ) {
                Aware.startPlugin(getApplicationContext(), getPackageName());
            } else {
                Aware.stopPlugin(getApplicationContext(), getPackageName());
            }
        }

        Intent apply = new Intent(Aware.ACTION_AWARE_REFRESH);
        sendBroadcast(apply);
    }
}

/*

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class Settings extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.esm_settings);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
*/
