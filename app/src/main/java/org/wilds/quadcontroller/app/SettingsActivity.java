package org.wilds.quadcontroller.app;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.widget.TextView;


public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
    }


    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, final Preference preference) {
        if (preference.getKey().equals("about")) {
            // Inflate the about message contents
            View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

            // When linking text, force to always use default color. This works
            // around a pressed color state bug.
            TextView textView = (TextView) messageView.findViewById(R.id.about_credits);
            int defaultColor = textView.getTextColors().getDefaultColor();
            textView.setTextColor(defaultColor);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.drawable.ic_launcher);
            builder.setTitle(R.string.app_name);
            builder.setView(messageView);
            builder.create();
            builder.show();
            return true;
        }
        return false;
    }
}
