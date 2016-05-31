package anisia.sunny;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import anisia.sunny.data.WeatherContract;

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private final static String LOG_TAG = DetailActivity.class.getSimpleName();
    static final String DETAIL_URI = "URI";
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private static final int DETAIL_LOADER = 0;
    private static final String[] FORECAST_COLUMN = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.WeatherEntry.COLUMN_LOC_KEY
    };

    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_TEMP_MAX = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_HUMIDITY = 5;
    private static final int COL_WEATHER_PRESSURE = 6;
    private static final int COL_WEATHER_DEGREES = 7;
    private static final int COL_WEATHER_WIND_SPEED = 8;
    private static final int COL_WEATTHER_CONDITION_ID = 9;

    private ShareActionProvider shareActionProvider;
    private String forecastDetail;
    private Uri mUri;

    private ImageView iconView;
    private TextView weekDayView;
    private TextView dateView;
    private TextView maxTempView;
    private TextView minTempView;
    private TextView descriptionView;
    private TextView humidityView;
    private TextView windView;
    private TextView pressureView;
    private CompassView compassView;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        MenuItem menuItem = menu.findItem(R.id.action_share);

        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(getSendIntent());
            Log.i(LOG_TAG, "ShareActionProviedr is not null");
        } else {
            Log.i(LOG_TAG, "ShareActionProviedr is null");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        iconView = (ImageView) rootView.findViewById(R.id.detail_image_icon);
        weekDayView = (TextView) rootView.findViewById(R.id.detail_text_day);
        dateView = (TextView) rootView.findViewById(R.id.detail_text_date);
        maxTempView = (TextView) rootView.findViewById(R.id.detail_text_max_temp);
        minTempView = (TextView) rootView.findViewById(R.id.detail_text_min_temp);
        descriptionView = (TextView) rootView.findViewById(R.id.detail_text_description);
        humidityView = (TextView) rootView.findViewById(R.id.detail_text_humidity);
        windView = (TextView) rootView.findViewById(R.id.detail_text_wind);
        pressureView = (TextView) rootView.findViewById(R.id.detail_text_pressure);
        compassView = (CompassView)rootView.findViewById(R.id.my_compass);

        return rootView;
    }

    public Intent getSendIntent() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        sendIntent.putExtra(Intent.EXTRA_TEXT, forecastDetail + FORECAST_SHARE_HASHTAG);
        sendIntent.setType("text/plain");

        return sendIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUri != null) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(getActivity(), mUri, FORECAST_COLUMN, null, null, null);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In on loaedFinished()");
        String pref_icon = Utility.getPreferredIconPack(getActivity());
        if (data != null && data.moveToFirst()) {
            //Read weather_ID from cursor data
            int weatherId = data.getInt(COL_WEATTHER_CONDITION_ID);
            //Check icon pack preference
            if(pref_icon.equalsIgnoreCase(getContext().getString(R.string.app_name))){
                iconView.setImageResource(Utility.getResourceForWeatherCondition(weatherId));
            } else {
                Glide.with(this)
                        .load(Utility.getUrlForWeatherCondition(getActivity(), weatherId))
                        .error(Utility.getResourceForWeatherCondition(weatherId))
                        .into(iconView);
            }

            //Read description and update view for weather description
            String description = Utility.getStringForWeatherCondition(getActivity(), weatherId);
            descriptionView.setText(description);
            descriptionView.setContentDescription(getString(R.string.a11y_forecast, description));

            //Read max temperature and update its view
            boolean isMetric = Utility.isMetric(getActivity());

            double maxTemp = data.getDouble(COL_WEATHER_TEMP_MAX);
            String maxTempString = Utility.formatTemperature(getActivity(), maxTemp);
            maxTempView.setText(maxTempString);
            maxTempView.setContentDescription(getString(R.string.a11y_max_temp, maxTempString));

            //Read min temperature from cursor data and update related view
            double minTemp = data.getDouble(COL_WEATHER_MIN_TEMP);
            String minTempString = Utility.formatTemperature(getActivity(),minTemp);
            minTempView.setText(minTempString);
            minTempView.setContentDescription(getString(R.string.a11y_min_temp, minTempString));

            //Read date from cursor data and update related view
            long date = data.getLong(COL_WEATHER_DATE);
            String dayText = Utility.getDayName(getActivity(), date);
            String dateText = Utility.getFormattedMonthDay(getActivity(), date);
            weekDayView.setText(dayText);
            dateView.setText(dateText);

            //Read humidity from cursor data and update related view
            float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
            humidityView.setText(getActivity().getString(R.string.format_humidity, humidity));
            humidityView.setContentDescription(humidityView.getText());

            //Read pressure from cursor data and update related view
            float pressure = data.getFloat(COL_WEATHER_PRESSURE);
            pressureView.setText(getActivity().getString(R.string.format_pressure, pressure));
            pressureView.setContentDescription(pressureView.getText());

            //Read wind from cursor data and update view
            float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
            float windDirection = data.getFloat(COL_WEATHER_DEGREES);
            windView.setText(Utility.formatWind(getActivity(), windSpeed, windDirection));
            compassView.update(windDirection);
            compassView.setContentDescription(windView.getText());

            forecastDetail = String.format("%s - %s - %s/%s", dateText, description, maxTemp, minTemp);

        }

        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(getSendIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onLocationChanged(String newLocation) {
        // replace the uri, since the location has changed
        Uri uri = mUri;
        if (uri != null) {
            long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
            Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
            mUri = updatedUri;
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }
}