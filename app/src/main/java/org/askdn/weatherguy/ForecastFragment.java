package org.askdn.weatherguy;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {


    private ArrayAdapter<String> mForecastAdapter;
    public ForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
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

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("api.openweathermap.org")
                .appendPath("data")
                .appendPath("2.5")
                .appendPath("forecast")
                .appendPath("daily")
                .appendQueryParameter("q","London")
                .appendQueryParameter("mode","json")
                .appendQueryParameter("units","metric")
                .appendQueryParameter("cnt","7");

        return rootView;
    }

    public static class FetchDataFromNetwork extends AsyncTask<String, Void, Void> {

        private final static String API ="4ca365c61eb4ee39b15e931654452e5b";
        private final String LOG_TAG = FetchDataFromNetwork.class.getSimpleName();
        protected Void doInBackground(String... params) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.openweathermap.org")
                    .appendPath("data")
                    .appendPath("2.5")
                    .appendPath("forecast")
                    .appendPath("daily")
                    .appendQueryParameter("q","London")
                    .appendQueryParameter("mode","json")
                    .appendQueryParameter("units","metric")
                    .appendQueryParameter("cnt","7")
                    .appendQueryParameter("appid",API);

            String api_call_string = builder.build().toString();

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
                if(is==null) return null;

                reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while((line=reader.readLine())!=null) {
                    buffer.append(line+"\n");
                }
                if(buffer.length()==0) return null; // stream was empty
                inputStringJson = buffer.toString();

                Log.i("EK",inputStringJson);

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

            return null;
        }
    }

}

