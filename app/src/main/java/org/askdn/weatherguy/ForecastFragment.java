package org.askdn.weatherguy;

import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    View rootView;
    public final String CLASS_ID = "ForecastFragment";
    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_forecast, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.forecast_refresh) {
            showSnackBar();
            executeWeatherUpdate();
            return true;
        }
        if (id == R.id.forecast_action_settings) {
            Intent settings = new Intent(getActivity(), SettingsActivity.class);
            startActivity(settings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textviewd,
                new ArrayList<String>());

        ListView displayList = (ListView) rootView.findViewById(R.id.listview_forecast);
        displayList.setAdapter(mForecastAdapter);

        displayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent launchDetail = new Intent(getActivity(), DetailActivity.class);
                launchDetail.putExtra(CLASS_ID, mForecastAdapter.getItem(position));
                startActivity(launchDetail);
            }
        });
        return rootView;
    }

    public void showSnackBar() {
        Snackbar.make(rootView, getString(R.string.title_updateweather), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void executeWeatherUpdate() {
        String params[] = getDesiredLocation();
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(), mForecastAdapter);
        weatherTask.execute(params[0]);
    }

    public String[] getDesiredLocation() {

        String[] br = new String[5];
        SharedPreferences userpref = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        br[0] = userpref.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_locationdefault));
        br[1] = userpref.getString(getString(R.string.pref_units_key),
                getString(R.string.pref_unitdefault));
        br[2] = userpref.getString(getString(R.string.pref_numberdays_key),
                getString(R.string.pref_numberdaysdefault));
        return br;
    }

    @Override
    public void onStart() {
        super.onStart();
        executeWeatherUpdate();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        showSnackBar();


    }



}

