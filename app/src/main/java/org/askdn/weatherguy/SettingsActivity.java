package org.askdn.weatherguy;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by ashish on 18/12/15.
 */
public class SettingsActivity extends Activity {

    public static final String pref_location ="location";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content,new SettingsFragment())
                .commit();

    }
}
