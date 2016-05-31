package anisia.sunny;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE_DAY = 1;

    private boolean useTodayLayout = true;

    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView descriptionView;
        public final TextView minTempView;
        public final TextView maxTempView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecat_textview);
            minTempView = (TextView) view.findViewById(R.id.list_item_min_textview);
            maxTempView = (TextView) view.findViewById(R.id.list_item_max_textview);
        }
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0 && useTodayLayout) {
            return VIEW_TYPE_TODAY;
        } else {
            return VIEW_TYPE_FUTURE_DAY;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int viewId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                viewId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                viewId = R.layout.list_item_forecast;
                break;
            }
        }

        View view = LayoutInflater.from(context).inflate(viewId, parent, false);

        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);

        return view;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.
        ViewHolder holder = (ViewHolder) view.getTag();
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_CONDITION_ID);
        int viewType = getItemViewType(cursor.getPosition());
        int fallbackIcon = 0;
        String pref_icon = Utility.getPreferredIconPack(context);

        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                if(pref_icon.equalsIgnoreCase(context.getString(R.string.app_name))){
                    holder.iconView.setImageResource(Utility.getResourceForWeatherCondition(weatherId));
                } else {
                    fallbackIcon = Utility.getResourceForWeatherCondition(weatherId);
                }
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                if(pref_icon.equalsIgnoreCase(context.getString(R.string.app_name))){
                    holder.iconView.setImageResource(Utility.getSmallResourceForWeatherCondition(weatherId));
                } else {
                    fallbackIcon = Utility.getSmallResourceForWeatherCondition(weatherId);
                }
                break;
            }
        }

        if(!pref_icon.equalsIgnoreCase(context.getString(R.string.app_name))){
            Glide.with(mContext)
                    .load(Utility.getUrlForWeatherCondition(mContext, weatherId))
                    .error(fallbackIcon)
                    .crossFade()
                    .into(holder.iconView);
        }

        String forecastDesc = Utility.getStringForWeatherCondition(context, weatherId);
        holder.descriptionView.setText(forecastDesc);
        holder.descriptionView.setContentDescription(context.getString(R.string.a11y_forecast, forecastDesc));

        long dateInMillis = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        holder.dateView.setText(Utility.GetDayString(context, dateInMillis));

        double highD = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);
        String high = Utility.formatTemperature(context, highD);
        holder.maxTempView.setText(high);
        holder.maxTempView.setContentDescription(context.getString(R.string.a11y_max_temp, high));

        double mind = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        String min = Utility.formatTemperature(context, mind);
        holder.minTempView.setText(min);
        holder.minTempView.setContentDescription(context.getString(R.string.a11y_min_temp, min));

    }
}
