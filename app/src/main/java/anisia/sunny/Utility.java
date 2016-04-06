package anisia.sunny;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {
    public static final String DATE_FORMAT = "yyyyMMdd";

    public static String GetDayString(Context context, long dateMillis) {
        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        return getDayName(context, dateMillis);

    }

    public static String getDayName(Context context, long dateInMillis) {
        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else if (currentJulianDay + 1 < julianDay && julianDay < currentJulianDay + 7) {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        } else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd");
            return dateFormat.format(dateInMillis);
        }
    }

    public static String getFormattedMonthDay(Context context, long dateInMillis) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;

    }

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_default_location));
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_temperature_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(Context context, double temperature, boolean isMetric) {
        double temp;
        if (!isMetric) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        return context.getString(R.string.format_temperature, temp);
    }

    public static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    public static String formatWind(Context context, float windSpeed, float degree) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.wind_format_kmh;
        } else {
            windFormat = R.string.wind_format_mph;
            windSpeed = .621371192237334f * windSpeed;
        }

        String direction = "Unknown";
        if (degree >= 337.5 || degree < 22.5) {
            direction = "N";
        } else if (degree >= 22.5 && degree < 67.5) {
            direction = "NE";
        } else if (degree >= 67.5 && degree < 112.5) {
            direction = "E";
        } else if (degree >= 112.5 && degree < 157.5) {
            direction = "SE";
        } else if (degree >= 157.5 && degree < 202.5) {
            direction = "S";
        } else if (degree >= 202.5 && degree < 247.5) {
            direction = "SW";
        } else if (degree >= 247.4 && degree < 292.5) {
            direction = "W";
        } else if (degree >= 292.5 && degree < 22.5) {
            direction = "NW";
        }

        return String.format(context.getString(windFormat), windSpeed, direction);
    }

    public static int getResourceForWeatherCondition(int weatherId) {
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.rain;
        } else if (weatherId == 511) {
            return R.drawable.snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.storm;
        } else if (weatherId == 800) {
            return R.drawable.sun;
        } else if (weatherId == 801) {
            return R.drawable.light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.cloud;
        }
        return -1;
    }

    public static int getSmallResourceForWeatherCondition(int weatherId) {
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.small_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.small_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.small_rain;
        } else if (weatherId == 511) {
            return R.drawable.small_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.small_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.small_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.small_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.small_storm;
        } else if (weatherId == 800) {
            return R.drawable.small_sun;
        } else if (weatherId == 801) {
            return R.drawable.small_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.small_cloud;
        }
        return -1;
    }

    /**
     * Helper method to provide the art resource id according to the weather condition id returned
     * by the OpenWeatherMap call.
     * @param weatherId from OpenWeatherMap API response
     * @return resource id for the corresponding image. -1 if no relation is found.
     */
    /**public static int getArtResourceForWeatherCondition(int weatherId) {
     // Based on weather code data found at:
     // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
     if (weatherId >= 200 && weatherId <= 232) {
     return R.drawable.art_storm;
     } else if (weatherId >= 300 && weatherId <= 321) {
     return R.drawable.art_light_rain;
     } else if (weatherId >= 500 && weatherId <= 504) {
     return R.drawable.art_rain;
     } else if (weatherId == 511) {
     return R.drawable.art_snow;
     } else if (weatherId >= 520 && weatherId <= 531) {
     return R.drawable.art_rain;
     } else if (weatherId >= 600 && weatherId <= 622) {
     return R.drawable.art_rain;
     } else if (weatherId >= 701 && weatherId <= 761) {
     return R.drawable.art_fog;
     } else if (weatherId == 761 || weatherId == 781) {
     return R.drawable.art_storm;
     } else if (weatherId == 800) {
     return R.drawable.art_clear;
     } else if (weatherId == 801) {
     return R.drawable.art_light_clouds;
     } else if (weatherId >= 802 && weatherId <= 804) {
     return R.drawable.art_clouds;
     }
     return -1;
     }*/
}