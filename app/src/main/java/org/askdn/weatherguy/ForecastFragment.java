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
    public ArrayAdapter<String> mForecastAdapter;
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
            Intent settings = new Intent(getActivity(),SettingsActivity.class);
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
                getActivity(),R.layout.list_item_forecast,
                R.id.list_item_forecast_textviewd,
                new ArrayList<String>());

        ListView displayList = (ListView) rootView.findViewById(R.id.listview_forecast);
        displayList.setAdapter(mForecastAdapter);

        displayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent launchDetail = new Intent(getActivity(),DetailActivity.class);
                launchDetail.putExtra(CLASS_ID,mForecastAdapter.getItem(position));
                startActivity(launchDetail);
            }
        });
        return rootView;
    }

    public void showSnackBar()
    {
        Snackbar.make(rootView, getString(R.string.title_updateweather), Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void executeWeatherUpdate(){
        String params[] = getDesiredLocation();
        FetchDataFromNetwork weatherTask = new FetchDataFromNetwork();
        weatherTask.execute(params[0],params[1],params[2]);
    }

    public String[] getDesiredLocation() {

        String[] br = new String[5];
        SharedPreferences userpref = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        br[0]=userpref.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_locationdefault));
        br[1]=userpref.getString(getString(R.string.pref_units_key),
                getString(R.string.pref_unitdefault));
        br[2]=userpref.getString(getString(R.string.pref_numberdays_key),
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

    public class FetchDataFromNetwork extends AsyncTask<String, Void, String[]> {

        public final String URI_SCHEME = "http";
        public final String URI_DOMAIN = "api.openweathermap.org";
        public final String URI_DATA = "data";
        public final String URI_VERSION = "2.5";
        public final String URI_FORECAST = "forecast";
        public final String URI_FORECAST_TYPE = "daily";
        public final String URI_OUTPUT_TYPE = "json";
        public final String URL_RESPONSE_TYPE = "GET";
        public final String LOG_TAG = FetchDataFromNetwork.class.getSimpleName();
        /**
         * Prepare the date & time readable String
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEEE dd MMM yyyy ");
            return shortenedDateFormat.format(time);
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }

        public void getJulianTime() {


        }

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            String day;

            JSONObject weatherAccess = new JSONObject(forecastJsonStr);
            JSONArray weatherList = weatherAccess.getJSONArray(OWM_LIST);

            Time dayTime = new Time();
            dayTime.setToNow();
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(),
                    dayTime.gmtoff);
            dayTime = new Time();

            String resultStrs[] = new String[numDays];
            for(int i=0;i<weatherList.length();i++) {

                JSONObject forecastDays = weatherList.getJSONObject(i);
                JSONObject currentDayTemp = forecastDays.getJSONObject(OWM_TEMPERATURE);

                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);


                double max = currentDayTemp.getDouble(OWM_MAX);
                double min = currentDayTemp.getDouble(OWM_MIN);
                String maxMinTemp = formatHighLows(max,min);

                JSONObject currWeatherConditionsjson = forecastDays.getJSONArray(OWM_WEATHER).getJSONObject(0);
                String weatherConditions = currWeatherConditionsjson.getString(OWM_DESCRIPTION);

                String weatherForecast = day + " - "+ maxMinTemp +" - " + weatherConditions;
                resultStrs[i]=weatherForecast;
            }

            return resultStrs;

        }

        protected String[] doInBackground(String... params) {

            Uri.Builder builder = new Uri.Builder();
            builder.scheme(URI_SCHEME)
                    .authority(URI_DOMAIN)
                    .appendPath(URI_DATA)
                    .appendPath(URI_VERSION)
                    .appendPath(URI_FORECAST)
                    .appendPath(URI_FORECAST_TYPE)
                    .appendQueryParameter("q",params[0])
                    .appendQueryParameter("mode",URI_OUTPUT_TYPE)
                    .appendQueryParameter("units",params[1])
                    .appendQueryParameter("cnt",params[2])
                    .appendQueryParameter("appid",getString(R.string.appid));

            String api_call_string = builder.build().toString();
            String inputStringJson=null;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                // Create a new URL object
                URL url = new URL(api_call_string);
                // Open a URL connection
                urlConnection = (HttpURLConnection) url.openConnection();
                // Set the Connection Request Method
                urlConnection.setRequestMethod(URL_RESPONSE_TYPE);
                // Establish a connection
                urlConnection.connect();

                // Connect to a stream
                InputStream is = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(is==null)
                    return null;

                reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while((line=reader.readLine())!=null) {
                    buffer.append(line+"\n");
                }
                if(buffer.length()==0) return null; // stream was empty
                inputStringJson = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG,"Error", e);

            }

            finally {
                if(urlConnection!=null) {
                    urlConnection.disconnect();
                }
                if(reader!=null) {
                    try {
                        reader.close();
                    }
                    catch(IOException e) {
                        Log.e(LOG_TAG,"Error closing data stream", e);
                    }
                }
            }

            try {
                return getWeatherDataFromJson(inputStringJson, Integer.parseInt(params[2]));
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            if (strings != null) {
                mForecastAdapter.clear();
                for(String dayForecastStr : strings) {
                    mForecastAdapter.add(dayForecastStr);
                }
                // New data is back from the server.  Hooray!
            }





        }
    }

}

