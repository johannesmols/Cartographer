package itcom.cartographer.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Handles preferences of the app
 */
public class PreferenceManager {
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Context context;

    // shared preferences mode
    private int PRIVATE_MODE = Context.MODE_PRIVATE;

    // Shared preferences file name
    private static final String PREF_NAME = "cartographer";

    // Preferences
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String DATE_RANGE_START = "DateRangeStart";
    private static final String DATE_RANGE_END = "DateRangeEnd";

    @SuppressLint("CommitPrefEdits")
    public PreferenceManager(Context context) {
        this.context = context;
        preferences = this.context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = preferences.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return preferences.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public void setDateRangeStart(Calendar date) {
        editor.putLong(DATE_RANGE_START, date.getTimeInMillis());
        editor.commit();
    }

    public Calendar getDateRangeStart() {
        long time = preferences.getLong(DATE_RANGE_START, 0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal;
    }

    public void setDateRangeEnd(Calendar date) {
        editor.putLong(DATE_RANGE_END, date.getTimeInMillis());
        editor.commit();
    }

    public Calendar getDateRangeEnd() {
        long time = preferences.getLong(DATE_RANGE_END, 0);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return cal;
    }
}
