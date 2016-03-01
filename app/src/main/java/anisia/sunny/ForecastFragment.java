package anisia.sunny;

/**
 * Created by Utente on 13/12/2015.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public final static String EXTRA_MESSAGE = "anisia.sunny.MESSAGE";

    private ArrayAdapter<String> arrayAdapter;

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


        arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, new ArrayList<String>());
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String day = arrayAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, day);
                startActivity(intent);
            }
        });

        return rootView;
    }

    //la PRIMA VARIABILE è il tipo di valore da passare come parametro al nuovo task
    //in questo caso è la stringa che i ndica il nome della città
    //la SECONDA VARIABILE dovrebbe servire per indicare la percentuale di completamento della progress bar
    //la TERZA VARIABILE indica il tipo di valore che il task in background deve
    // ritornare al task principale in questo caso è la stringa con le previsioni meteo

    public void updateWeather() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String city = sharedPref.getString(getString(R.string.pref_location_key), "Rotterdam");
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask(getActivity(), arrayAdapter);
        fetchWeatherTask.execute(city);
    }



    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }

}
