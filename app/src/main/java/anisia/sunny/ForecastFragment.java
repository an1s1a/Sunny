package anisia.sunny;

/**
 * Created by Utente on 13/12/2015.
 */

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import anisia.sunny.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public final static String EXTRA_MESSAGE = "anisia.sunny.MESSAGE";

    private ForecastAdapter forecastAdapter;

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
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String locationSetting = Utility.getPreferredLocation(getActivity());

        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting,
                System.currentTimeMillis());

        Cursor cursor = getActivity().getContentResolver().query(weatherForLocationUri, null, null, null, sortOrder);
        forecastAdapter = new ForecastAdapter(getActivity(), cursor, 0);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(forecastAdapter);


        return rootView;
    }

    //la PRIMA VARIABILE è il tipo di valore da passare come parametro al nuovo task
    //in questo caso è la stringa che i ndica il nome della città
    //la SECONDA VARIABILE dovrebbe servire per indicare la percentuale di completamento della progress bar
    //la TERZA VARIABILE indica il tipo di valore che il task in background deve
    // ritornare al task principale in questo caso è la stringa con le previsioni meteo

    public void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }



    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }

}
