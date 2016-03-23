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
            direction ="NE";
        } else if (degree >= 67.5 && degree < 112.5) {
            direction ="E";
        } else if (degree >= 112.5 && degree < 157.5) {
            direction ="SE";
        } else if (degree >= 157.5 && degree < 202.5) {
            direction ="S";
        } else if (degree >= 202.5 && degree < 247.5) {
            direction ="SW";
        } else if (degree >= 247.4 && degree < 292.5) {
            direction ="W";
        } else if (degree >= 292.5 && degree < 22.5) {
            direction ="NW";
        }

        return String.format(context.getString(windFormat), windSpeed, direction);
    }
}