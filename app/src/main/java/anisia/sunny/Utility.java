package anisia.sunny;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.Time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import anisia.sunny.sync.SunnySyncAdapter;

public class Utility {
    public static final String DATE_FORMAT = "yyyyMMdd";

    public static boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected;
        if(activeNetwork != null && activeNetwork.isConnectedOrConnecting()){
            isConnected = true;
        } else {
            isConnected = false;
        }
        return isConnected;
    }

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

    public static String getPreferredIconPack(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getString(context.getString(R.string.pref_icon_pack_key), context.getString(R.string.pref_default_icon_pack));
    }


    @SuppressWarnings("ResourceType")
    public static @SunnySyncAdapter.LocationStatus int getLocationStatus(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int locationStatus = sp.getInt(context.getString(R.string.pref_location_status_key), SunnySyncAdapter.LOCATION_STATUS_UNKNOWN);
        return locationStatus;
    }

    public static void resetLocationStatus(Context context){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(context.getString(R.string.pref_location_status_key), SunnySyncAdapter.LOCATION_STATUS_UNKNOWN);
        spe.apply();
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_temperature_units_key),
                context.getString(R.string.pref_units_metric))
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(Context context, double temperature) {
        double temp;
        if (!isMetric(context)) {
            temp = 9 * temperature / 5 + 32;
        } else {
            temp = temperature;
        }
        return String.format(context.getString(R.string.format_temperature), temp);
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

    public static String getUrlForWeatherCondition(Context context, int weatherId) {
        if (weatherId >= 200 && weatherId <= 232) {
            return context.getString(R.string.format_icon_url, "storm");
        } else if (weatherId >= 300 && weatherId <= 321) {
            return context.getString(R.string.format_icon_url, "light_rain");
        } else if (weatherId >= 500 && weatherId <= 504) {
            return context.getString(R.string.format_icon_url, "rain");
        } else if (weatherId == 511) {
            return context.getString(R.string.format_icon_url, "snow");
        } else if (weatherId >= 520 && weatherId <= 531) {
            return context.getString(R.string.format_icon_url, "rain");
        } else if (weatherId >= 600 && weatherId <= 622) {
            return context.getString(R.string.format_icon_url, "snow");
        } else if (weatherId >= 701 && weatherId <= 761) {
            return context.getString(R.string.format_icon_url, "fog");
        } else if (weatherId == 761 || weatherId == 781) {
            return context.getString(R.string.format_icon_url, "storm");
        } else if (weatherId == 800) {
            return context.getString(R.string.format_icon_url, "clear");
        } else if (weatherId == 801) {
            return context.getString(R.string.format_icon_url, "light_clouds");
        } else if (weatherId >= 802 && weatherId <= 804) {
            return context.getString(R.string.format_icon_url, "clouds");
        }
        return null;
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

    public static String getStringForWeatherCondition(Context context, int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        int stringId;
        if (weatherId >= 200 && weatherId <= 232) {
            stringId = R.string.condition_2xx;
        } else if (weatherId >= 300 && weatherId <= 321) {
            stringId = R.string.condition_3xx;
        } else switch(weatherId) {
            case 500:
                stringId = R.string.condition_500;
                break;
            case 501:
                stringId = R.string.condition_501;
                break;
            case 502:
                stringId = R.string.condition_502;
                break;
            case 503:
                stringId = R.string.condition_503;
                break;
            case 504:
                stringId = R.string.condition_504;
                break;
            case 511:
                stringId = R.string.condition_511;
                break;
            case 520:
                stringId = R.string.condition_520;
                break;
            case 531:
                stringId = R.string.condition_531;
                break;
            case 600:
                stringId = R.string.condition_600;
                break;
            case 601:
                stringId = R.string.condition_601;
                break;
            case 602:
                stringId = R.string.condition_602;
                break;
            case 611:
                stringId = R.string.condition_611;
                break;
            case 612:
                stringId = R.string.condition_612;
                break;
            case 615:
                stringId = R.string.condition_615;
                break;
            case 616:
                stringId = R.string.condition_616;
                break;
            case 620:
                stringId = R.string.condition_620;
                break;
            case 621:
                stringId = R.string.condition_621;
                break;
            case 622:
                stringId = R.string.condition_622;
                break;
            case 701:
                stringId = R.string.condition_701;
                break;
            case 711:
                stringId = R.string.condition_711;
                break;
            case 721:
                stringId = R.string.condition_721;
                break;
            case 731:
                stringId = R.string.condition_731;
                break;
            case 741:
                stringId = R.string.condition_741;
                break;
            case 751:
                stringId = R.string.condition_751;
                break;
            case 761:
                stringId = R.string.condition_761;
                break;
            case 762:
                stringId = R.string.condition_762;
                break;
            case 771:
                stringId = R.string.condition_771;
                break;
            case 781:
                stringId = R.string.condition_781;
                break;
            case 800:
                stringId = R.string.condition_800;
                break;
            case 801:
                stringId = R.string.condition_801;
                break;
            case 802:
                stringId = R.string.condition_802;
                break;
            case 803:
                stringId = R.string.condition_803;
                break;
            case 804:
                stringId = R.string.condition_804;
                break;
            case 900:
                stringId = R.string.condition_900;
                break;
            case 901:
                stringId = R.string.condition_901;
                break;
            case 902:
                stringId = R.string.condition_902;
                break;
            case 903:
                stringId = R.string.condition_903;
                break;
            case 904:
                stringId = R.string.condition_904;
                break;
            case 905:
                stringId = R.string.condition_905;
                break;
            case 906:
                stringId = R.string.condition_906;
                break;
            case 951:
                stringId = R.string.condition_951;
                break;
            case 952:
                stringId = R.string.condition_952;
                break;
            case 953:
                stringId = R.string.condition_953;
                break;
            case 954:
                stringId = R.string.condition_954;
                break;
            case 955:
                stringId = R.string.condition_955;
                break;
            case 956:
                stringId = R.string.condition_956;
                break;
            case 957:
                stringId = R.string.condition_957;
                break;
            case 958:
                stringId = R.string.condition_958;
                break;
            case 959:
                stringId = R.string.condition_959;
                break;
            case 960:
                stringId = R.string.condition_960;
                break;
            case 961:
                stringId = R.string.condition_961;
                break;
            case 962:
                stringId = R.string.condition_962;
                break;
            default:
                return context.getString(R.string.condition_unknown, weatherId);
        }
        return context.getString(stringId);
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