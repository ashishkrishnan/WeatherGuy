package org.askdn.weatherguy;

/**
 * Created by ashish on 21/2/16.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.askdn.weatherguy.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

       public final int VIEW_TYPE_TODAY = 0;
       public final int VIEW_TYPE_FUTURE_DAY=1;
       public final int VIEW_TYPE_COUNT =2;

       public static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };

    @Override
    public int getCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
        Remember that these views are reused as needed.
     */

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch(viewType) {
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_forecast;
                break;
            }
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        InitViewHolder viewHolder = new InitViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        InitViewHolder viewHolder = (InitViewHolder) view.getTag();

        boolean isMetric = Utility.isMetric(mContext);
        int weatherID = cursor.getInt(COL_WEATHER_ID);
        double high = cursor.getDouble(COL_WEATHER_MAX_TEMP);
        double low  =cursor.getDouble(COL_WEATHER_MIN_TEMP);
        long currentDateTime = cursor.getLong(COL_WEATHER_DATE);
        String weather_desc = cursor.getString(COL_WEATHER_DESC);

        viewHolder.dateView.setText(Utility.getFriendlyDayString(mContext,currentDateTime));
        viewHolder.descriptionView.setText(weather_desc);
        viewHolder.highTempView.setText(Utility.formatTemperature(high,isMetric));
        viewHolder.lowTempView.setText(Utility.formatTemperature(low,isMetric));



        /*ImageView iconView = (ImageView) view.findViewById(R.id.list_item_icon);
        int weatherID = cursor.getInt(COL_WEATHER_ID);

        boolean isMetric = Utility.isMetric(mContext);

        double high = cursor.getDouble(COL_WEATHER_MAX_TEMP);
        double low  =cursor.getDouble(COL_WEATHER_MIN_TEMP);

        TextView set_low = (TextView) view.findViewById(R.id.list_item_low_textview);
        set_low.setText(Utility.formatTemperature(low,isMetric));

        TextView set_high = (TextView) view.findViewById(R.id.list_item_high_textview);
        set_high.setText(Utility.formatTemperature(high,isMetric));

        long currentDateTime = cursor.getLong(COL_WEATHER_DATE);
        TextView set_date = (TextView) view.findViewById(R.id.list_item_date_textview);
        set_date.setText(Utility.formatDate(currentDateTime));

        String weather_desc = cursor.getString(COL_WEATHER_DESC);
        TextView set_desc = (TextView) view.findViewById(R.id.list_item_forecast_textview);
        set_desc.setText(weather_desc);*/

    }

    public static class InitViewHolder {

        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView highTempView;
        public final TextView lowTempView;

        InitViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}