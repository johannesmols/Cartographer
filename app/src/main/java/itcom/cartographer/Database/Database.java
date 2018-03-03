package itcom.cartographer.Database;

import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {

    private Context _context;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LocationHistory";

    private static final String TABLE_LOCATION_HISTORY = "location_history";
    private static final String LH_ID = "lh_id";
    private static final String LH_TIMESTAMP = "lh_timestamp";
    private static final String LH_LATITUDE_E7 = "lh_latitude_e7";
    private static final String LH_LONGITUDE_E7 = "lh_longitude_e7";
    private static final String LH_ACCURACY = "lh_accuracy";

    private static final String TABLE_ACTIVITIES = "activites";
    private static final String AC_ID = "ac_id";
    private static final String AC_TIMESTAMP = "ac_timestamp";

    public Database(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        this._context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_LOCATION_HISTORY + "(" +
                LH_ID +         " BIGINT UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                LH_TIMESTAMP    + " BIGINT NOT NULL, " +
                LH_LATITUDE_E7  + " BIGINT NOT NULL, " +
                LH_LONGITUDE_E7 + " BIGINT_NOT NULL, " +
                LH_ACCURACY     + " INTEGER" +
                ");";
        sqLiteDatabase.execSQL(query);

        query = "CREATE TABLE " + TABLE_ACTIVITIES + "(" +
                AC_ID +         " BIGINT UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                AC_TIMESTAMP +  " BIGINT NOT NULL" +
                ");";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION_HISTORY);
        onCreate(sqLiteDatabase);
    }

    public int getDatapointCount() {
        SQLiteDatabase db = getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_LOCATION_HISTORY);
    }

    public int getActivitesCount() {
        SQLiteDatabase db = getReadableDatabase();
        return (int)DatabaseUtils.queryNumEntries(db, TABLE_ACTIVITIES);
    }
}