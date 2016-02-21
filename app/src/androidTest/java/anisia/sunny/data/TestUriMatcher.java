package anisia.sunny.data;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by Utente on 19/02/2016.
 */
public class TestUriMatcher extends AndroidTestCase{

    private static final String LOCATION_QUERY = "London, UK";
    private static final long TEST_DATE = 1419033600L;
    private static final long TEST_LOCATION_ID = 10L;

    private static final Uri TEST_WEATHER_DIR = WeatherContract.WeatherEntry.CONTENT_URI;
    private static final Uri TEST_WEATHER_WITH_LOCATION_DIR = WeatherContract.WeatherEntry.buildWeatherLocation(LOCATION_QUERY);
    private static final Uri TEST_WEATHER_WITH_LOCATION_AND_DATE_DIR = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(LOCATION_QUERY, TEST_DATE);

    private static final Uri TEST_LOCATION_DIR = WeatherContract.LocationEntry.CONTENT_URI;
}
