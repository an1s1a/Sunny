package anisia.sunny.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

/**
 * Created by Utente on 17/02/2016.
 */
public class TestProvider extends AndroidTestCase{

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    /*
       This helper function deletes all records from both database tables using the ContentProvider.
       It also queries the ContentProvider to make sure that the database has been successfully
       deleted, so it cannot be used until the Query and Delete functions have been written
       in the ContentProvider.

       Students: Replace the calls to deleteAllRecordsFromDB with this one after you have written
       the delete functionality in the ContentProvider.
    */

    public void deleteAllRecordsFromProvider() {
        mContext.getContentResolver().delete(WeatherContract.WeatherEntry.CONTENT_URI,
                null, null);
        mContext.getContentResolver().delete(WeatherContract.LocationEntry.CONTENT_URI,
                null, null);

        Cursor cursor = mContext.getContentResolver().query(WeatherContract.WeatherEntry.CONTENT_URI,
                null, null, null, null);

        assertEquals("Error: Records not deleted from Weather table during delete", 0, cursor.getCount());

        cursor.close();
    }

    public void deleteAllRecordsFromDB() {
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        database.delete(WeatherContract.WeatherEntry.TABLE_NAME, null, null);
        database.delete(WeatherContract.LocationEntry.TABLE_NAME, null, null);
        database.close();
    }

    public void deleteAllRecords() {
        deleteAllRecordsFromDB();
    }


    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        deleteAllRecords();
    }

    static private final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    static ContentValues[] createBulkInsertWeatherValues(long locationRowID) {
        long currentTestDate = TestUtilities.TEST_DATE;
        long millisecondsInADAay = 1000*60*60*24;
        ContentValues[] contentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, currentTestDate+=millisecondsInADAay) {
            ContentValues weatherValues = new ContentValues();
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_LOC_KEY, locationRowID);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DATE, currentTestDate);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_DEGREES,1.1);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_HUMIDITY,1.2+0.1*(float)i);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_PRESSURE,1.3 - 0.01 * (float)i);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP, 75+i);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP, 65-i);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC, "Asteroids");
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WIND_SPEED, 5.5 +0.2 *(float)i);
            weatherValues.put(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID, 321);
        }
        return contentValues;
    }


}
