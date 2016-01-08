package anisia.sunny;

/**
 * Created by Utente on 13/12/2015.
 */

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.action_refresh){
            FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
            fetchWeatherTask.execute("Rotterdam");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        List<String> listItem = new ArrayList<>();
        listItem.add("Lun, 7 Dic, Sunny");
        listItem.add("Mar, 8 Dic, Cloudy");
        listItem.add("Lun, 9 Dic, Sunny");
        listItem.add("Mar, 10 Dic, Sunny");
        listItem.add("Lun, 11 Dic, Rainy");
        listItem.add("Mar, 12 Dic, Windy");
        listItem.add("Lun, 13 Dic, Sunny");
        listItem.add("Mar, 14 Dic, Snowy");
        listItem.add("Lun, 15 Dic, Sunny");
        listItem.add("Mar, 16 Dic, Snowy");
        listItem.add("Lun, 17 Dic, Snowy");
        listItem.add("Mar, 18 Dic, Sunny");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, listItem);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(arrayAdapter);

        return rootView;
    }

    //la PRIMA VARIABILE è il tipo di valore da passare come parametro al nuovo task
    //in questo caso è la stringa che i ndica il nome della città
    //la SECONDA VARIABILE dovrebbe servire per indicare la percentuale di completamento della progress bar
    //la TERZA VARIABILE indica il tipo di valore che il task in background deve
    // ritornare al task principale in questo caso è la stringa con le previsioni meteo
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        private String getReadableDateString(long time){
            //SimpleDatefOrmat serve per formattare la data nel formato scelto, in questo caso
            //è EE MM dd
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE MM dd");
            return simpleDateFormat.format(time);
        }

        //metodo per convertire le temperature min e max in Double in stringhe
        public String formatHighLow(double high, double low){
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            String stringTemp = roundedLow + "/" + roundedHigh;

            return stringTemp;
        }

        //Ritorna la stringa da visualizzare nella lista
        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)throws JSONException{
            // These are the names of the JSON objects that need to be extracted
            final String OWM_LIST = "list";
            final String OWM_TEMP = "temp";
            final String OWM_MIN = "min";
            final String OWM_MAX = "max";
            final String OWM_WEATHER = "weather";
            final String OWM_MAIN = "main";

            JSONObject forecastJsonObject = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJsonObject.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.
            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.
            Date dayTime = new Date();
            //con il codice seguente creiamo un oggetto GC a partire da dayTime per poter manipolare le date
            GregorianCalendar calendar = (GregorianCalendar)Calendar.getInstance();
            calendar.setTime(dayTime);

            // we start at the day returned by local time. Otherwise this is a mess.
            int startDay = calendar.get(Calendar.DAY_OF_MONTH);
            String[] results = new String[numDays];
            for (int i = 0; i<weatherArray.length(); i++){
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highLow;

                // Get the JSON object representing the day in any iteration
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday"
                calendar.add(Calendar.DAY_OF_MONTH,1);
                long dateTime = calendar.getTimeInMillis();
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_MAIN);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMP);
                double highTemperature = temperatureObject.getDouble(OWM_MAX);
                double lowTemperature = temperatureObject.getDouble(OWM_MIN);
                highLow = formatHighLow(highTemperature, lowTemperature);

                results[i] = day + "-" + description + "-" + highLow;
            }

            for (String s: results){
                Log.v(LOG_TAG, "Previsione: " + s);
            }

            return results;
        }


        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
// so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

// Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            //Dichiarazioni per creare l'URL
            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                //usa URIBUILDER per costruire l'url da richiedere
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM ="q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM ="cnt";
                final String APPID_PARAM = "APPID";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI "+ builtUri.toString());
                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.v(LOG_TAG,"Forecast JSON String: " + forecastJsonStr);

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            } catch(JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
            }
            return null;
        }
    }

}