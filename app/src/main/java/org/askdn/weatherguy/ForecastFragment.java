package org.askdn.weatherguy;

import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
            Snackbar.make(rootView, "Updating the weather data", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            FetchDataFromNetwork weatherTask = new FetchDataFromNetwork();
            weatherTask.execute();
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
        String forecast_Array[] = {
                "Today - Chennai - 78/86",
                "Tomorrow - Chennai - 68/78",
                "Saturday - Chennai - 78/66",
                "Sunday - Chennai - 96/45",
                "Monday - Chennai - 69/56"
        };

        List<String> forecastList = new ArrayList<>(
                Arrays.asList(forecast_Array));

        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),R.layout.list_item_forecast,
                R.id.list_item_forecast_textviewd,
                forecastList);

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



    public class FetchDataFromNetwork extends AsyncTask<String, Void, String[]> {

        private final String API = "4ca365c61eb4ee39b15e931654452e5b";
        public final String LOG_TAG = FetchDataFromNetwork.class.getSimpleName();

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
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject weatherAccess = new JSONObject(forecastJsonStr);
            JSONArray weatherList = weatherAccess.getJSONArray(OWM_LIST);

            String resultStrs[] = new String[numDays];
/*
            Time time = new Time();
            time.setToNow();*/


            long time = System.currentTimeMillis();
            String datetime = getReadableDateString(time);
            for(int i=0;i<weatherList.length();i++) {

                JSONObject forecastDays = weatherList.getJSONObject(i);
                JSONObject currentDayTemp = forecastDays.getJSONObject(OWM_TEMPERATURE);

                double max = currentDayTemp.getDouble(OWM_MAX);
                double min = currentDayTemp.getDouble(OWM_MIN);
                String maxMinTemp = formatHighLows(max,min);

                JSONObject currWeatherConditionsjson = forecastDays.getJSONArray(OWM_WEATHER).getJSONObject(0);
                String weatherConditions = currWeatherConditionsjson.getString(OWM_DESCRIPTION);

                String weatherForecast = maxMinTemp +" - " + weatherConditions;
                resultStrs[i]=weatherForecast;
            }

            return resultStrs;

        }

        protected String[] doInBackground(String... params) {

            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data")
                    .appendPath("2.5")
                    .appendPath("forecast")
                    .appendPath("daily")
                    .appendQueryParameter("q","Sahibganj")
                    .appendQueryParameter("mode","json")
                    .appendQueryParameter("units","metric")
                    .appendQueryParameter("cnt","7")
                    .appendQueryParameter("appid",API);

            String api_call_string = builder.build().toString();

            Log.i("appid",api_call_string);
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String inputStringJson=null;

            try {
                // Create a new URL object
                URL url = new URL(api_call_string);
                // Open a URL connection
                urlConnection = (HttpURLConnection) url.openConnection();
                // Set the Connection Request Method
                urlConnection.setRequestMethod("GET");
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
                return getWeatherDataFromJson(inputStringJson, 7);
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

