package anisia.sunny;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import anisia.sunny.data.WeatherContract;

public class DetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().add(R.id.container,
                    new DetailFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings){
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private ShareActionProvider shareActionProvider;
        private final static String LOG_TAG = DetailActivity.class.getSimpleName();
        private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
        private static final int DETAIL_LOADER = 0;
        private static final String[] FORECAST_COLUMN = {
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
        };

        private static final int COL_WEATHER_ID = 0;
        private static final int COL_WEATHER_DATE = 1;
        private static final int COL_WEATHER_DESC = 2;
        private static final int COL_WEATHER_TEMP_MAX = 3;
        private static final int COL_WEATHER_MIN_TEMP = 4;

        private String forecastDetail;


        public DetailFragment(){
            setHasOptionsMenu(true);
        }


        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

            MenuItem menuItem = menu.findItem(R.id.action_share);

            shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

           if(shareActionProvider != null){
                shareActionProvider.setShareIntent(getSendIntent());
                Log.i(LOG_TAG, "ShareActionProviedr is not null");
            } else {
                Log.i(LOG_TAG, "ShareActionProviedr is null");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
            Intent intent = getActivity().getIntent();
            if(intent != null){
                forecastDetail = intent.getDataString();
            }
            if(null != forecastDetail){
                ((TextView) rootView.findViewById(R.id.forecast_detail)).setText(forecastDetail);
            }

            return rootView;
        }

        public Intent getSendIntent(){
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
            Log.v(LOG_TAG, "In onCreateLoader()");
            Intent intent = getActivity().getIntent();
            if(intent == null){
                return null;
            }

            return new CursorLoader(getActivity(),intent.getData(), FORECAST_COLUMN, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v(LOG_TAG, "In on loaedFinished()");
            if(!data.moveToFirst()){
                return;
            }
            String dateString = Utility.formatDate(data.getLong(COL_WEATHER_DATE));
            String weatherDescription = data.getString(COL_WEATHER_DESC);
            boolean isMetric = Utility.isMetric(getActivity());
            String maxTemp = Utility.formatTemperature(getActivity(),data.getDouble(COL_WEATHER_TEMP_MAX), isMetric);
            String minTemp = Utility.formatTemperature(getActivity(),data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);

            forecastDetail = String.format("%s - %s - %s/%s", dateString, weatherDescription, maxTemp, minTemp);

            TextView detailTextView = (TextView)getView().findViewById(R.id.forecast_detail);
            detailTextView.setText(forecastDetail);

            if(shareActionProvider != null){
                shareActionProvider.setShareIntent(getSendIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }


}
