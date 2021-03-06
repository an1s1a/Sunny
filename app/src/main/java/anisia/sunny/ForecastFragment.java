package anisia.sunny;

/**
 * Created by Utente on 13/12/2015.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import anisia.sunny.data.WeatherContract;
import anisia.sunny.sync.SunnySyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String EXTRA_MESSAGE = "anisia.sunny.MESSAGE";
    public static final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private final static int FORECAST_LOADER = 0;
    private static final String[] FORECAST_COLUMNS = {
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

    //These indices are linked to FORECAST_COLUMNS if FORECAST_COLUMNS change you have to modify also
    //these indices, because they represent the number of the column
    public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_LOCATION_SETTING = 5;
    public static final int COL_WEATHER_CONDITION_ID = 6;
    public static final int COL_LOCATION_LAT = 7;
    public static final int COL_LOCATION_LONG = 8;

    private ForecastAdapter forecastAdapter;
    private ListView mListView;
    private int mPosition = ListView.INVALID_POSITION;
    private static final String SELECTED_KEY = "selected_position";
    private boolean useTodayLayout;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_location_status_key)));
        updateEmptyView();
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_map){
            openPreferredLocationInMap();
            return true;
        }
        /*
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        forecastAdapter = new ForecastAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        View emptyListView = rootView.findViewById(R.id.listview_forecast_empty);
        mListView.setEmptyView(emptyListView);
        mListView.setAdapter(forecastAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE)
                    ));
                }
                mPosition = position;
            }
        });
        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        forecastAdapter.setUseTodayLayout(useTodayLayout);

        return rootView;
    }

    //la PRIMA VARIABILE è il tipo di valore da passare come parametro al nuovo task
    //in questo caso è la stringa che i ndica il nome della città
    //la SECONDA VARIABILE dovrebbe servire per indicare la percentuale di completamento della progress bar
    //la TERZA VARIABILE indica il tipo di valore che il task in background deve
    // ritornare al task principale in questo caso è la stringa con le previsioni meteo


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.builWeatherLocationWithStartDate(locationSetting,
                System.currentTimeMillis());
        //we pass the FORECAST_COLUMNS as projection in the constructor
        return new CursorLoader(getActivity(), weatherForLocationUri, FORECAST_COLUMNS, null, null, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        forecastAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
        updateEmptyView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }

    public void updateWeather() {
        //String location = Utility.getPreferredLocation(getActivity());
        //new FetchWeatherTask(getActivity()).execute(location);
        SunnySyncAdapter.syncImmediately(getActivity());
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
        if (forecastAdapter != null) {
            forecastAdapter.setUseTodayLayout(this.useTodayLayout);
        }
    }

    public void onLocationChanged() {
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    private void openPreferredLocationInMap(){
        if ( null != forecastAdapter ) {
            Cursor c = forecastAdapter.getCursor();
            if ( null != c ) {
                c.moveToFirst();
                Log.i(LOG_TAG, "Cursor " + c.toString());
                String posLat = c.getString(COL_LOCATION_LAT);
                Log.i(LOG_TAG, "Cursor lat" + posLat);
                String posLong = c.getString(COL_LOCATION_LONG);
                Log.i(LOG_TAG, c.getString(COL_LOCATION_SETTING));
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    public void updateEmptyView(){
        if(forecastAdapter.getCount() == 0){
            TextView textView = (TextView) getView().findViewById(R.id.listview_forecast_empty);
            if(textView != null){
                boolean isConnected = Utility.isConnected(getActivity());
                int message = R.string.empty_string;
                @SunnySyncAdapter.LocationStatus int location = Utility.getLocationStatus(getActivity());
                switch (location){
                    case SunnySyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.empty_forecast_list_server_down;
                        break;
                    case SunnySyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.empty_forecast_list_server_error;
                        break;
                    case SunnySyncAdapter.LOCATION_STATUS_INVALID:
                        message = R.string.empty_forecast_list_invalid_location;
                        Log.i(LOG_TAG, "Location invalid");
                        break;
                    default:
                        if(!isConnected){
                            message = R.string.empty_string_no_connection;
                        }
                }
                textView.setText(message);
            }
        }
    }
}
