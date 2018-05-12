package itcom.cartographer.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Point;
import android.util.Log;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.PendingResult;
import com.google.maps.PlacesApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import itcom.cartographer.FavPlace;
import itcom.cartographer.FavPlaceResult;
import itcom.cartographer.Utils.CoordinateUtils;
import itcom.cartographer.Utils.PreferenceManager;

public class Database extends SQLiteOpenHelper {

    private Context _context;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "LocationHistory";

    private static final String TABLE_FAV_PLACES = "fav_places";
    private static final String FP_ID = "lh_id";
    private static final String FP_PLACE_ID = "place_id";
    private static final String FP_LATITUDE = "latitude";
    private static final String FP_LONGITUDE = "longitude";
    private static final String FP_NAME = "name";
    private static final String FP_ICON = "icon";
    private static final String FP_RATING = "rating";
    private static final String FP_PHOTO = "photo";
    private static final String FP_ADDRESS = "address";


    private static final String TABLE_LOCATION_HISTORY = "location_history";
    private static final String LH_ID = "lh_id";
    private static final String LH_TIMESTAMP = "lh_timestamp";
    private static final String LH_LATITUDE_E7 = "lh_latitude_e7";
    private static final String LH_LONGITUDE_E7 = "lh_longitude_e7";
    private static final String LH_ACCURACY = "lh_accuracy";
    private static final String LH_VELOCITY = "lh_velocity";
    private static final String LH_HEADING = "lh_heading";
    private static final String LH_ALTITUDE = "lh_altitude";
    private static final String LH_VERTICAL_ACCURACY = "lh_vertical_accuracy";

    private static final String TABLE_ACTIVITIES = "activities";
    private static final String AC_ID = "ac_id";
    private static final String AC_TIMESTAMP = "ac_timestamp";

    public Database(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        this._context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query = "CREATE TABLE " + TABLE_LOCATION_HISTORY + "(" +
                LH_ID + " INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                LH_TIMESTAMP + " BIGINT NOT NULL, " +
                LH_LATITUDE_E7 + " BIGINT NOT NULL, " +
                LH_LONGITUDE_E7 + " BIGINT NOT NULL, " +
                LH_ACCURACY + " INTEGER NOT NULL, " +
                LH_VELOCITY + " INTEGER, " +
                LH_HEADING + " INTEGER, " +
                LH_ALTITUDE + " INTEGER, " +
                LH_VERTICAL_ACCURACY + " INTEGER" +
                ");";
        System.out.println(query);
        sqLiteDatabase.execSQL(query);

        query = "CREATE TABLE " + TABLE_ACTIVITIES + "(" +
                AC_ID + " INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                AC_TIMESTAMP + " BIGINT NOT NULL" +
                ");";
        sqLiteDatabase.execSQL(query);
        String favPlacesTableQuery = "CREATE TABLE " + TABLE_FAV_PLACES + "(" +
                FP_PLACE_ID + " TEXT, " +
                FP_LATITUDE + " DOUBLE, " +
                FP_LONGITUDE + " DOUBLE, " +
                FP_NAME + " TEXT, " +
                FP_ICON + " TEXT, " +
                FP_RATING + " DOUBLE, " +
                FP_PHOTO + " TEXT, " +
                FP_ADDRESS + " TEXT, " +
                FP_ID + " INTEGER, " +
                "FOREIGN KEY(" + FP_ID + ") REFERENCES " + TABLE_LOCATION_HISTORY + "(" + LH_ID
                + "));";

        System.out.println(favPlacesTableQuery);
        sqLiteDatabase.execSQL(favPlacesTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION_HISTORY);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
        onCreate(sqLiteDatabase);
    }

    @Override // here I am trying to enable the foreign keys
    public void onOpen(SQLiteDatabase db) {
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    public int getDatapointCount() {
        SQLiteDatabase db = getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_LOCATION_HISTORY);
    }

    public int getActivitesCount() {
        SQLiteDatabase db = getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(db, TABLE_ACTIVITIES);
    }

    public void deleteAllLocationHistoryEntries() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_LOCATION_HISTORY, null, null);
    }

    public void deleteAllActivityEntries() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ACTIVITIES, null, null);
    }

    public void addLocationHistoryEntries(ArrayList<LocationHistoryObject> lhObjects) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "INSERT INTO " + TABLE_LOCATION_HISTORY + " VALUES (?,?,?,?,?,?,?,?,?);";
        SQLiteStatement statement = db.compileStatement(sql);
        db.beginTransaction();

        try {
            for (LocationHistoryObject lhObject : lhObjects) {
                statement.clearBindings();
                statement.bindLong(2, lhObject.getTimestampMs());
                statement.bindLong(3, lhObject.getLatitudeE7());
                statement.bindLong(4, lhObject.getLongitudeE7());
                statement.bindLong(5, lhObject.getAccuracy());
                statement.bindLong(6, lhObject.getVelocity());
                statement.bindLong(7, lhObject.getHeading());
                statement.bindLong(8, lhObject.getAltitude());
                statement.bindLong(9, lhObject.getVerticalAccuracy());
                statement.execute();
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("Database Error", e.getMessage());
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    /**
     * Get the very first entry of the database, sorted by timestamp
     *
     * @return the row as an object
     */
    public LocationHistoryObject getFirstChronologicalEntry() {
        return getFirstOrLastChronologicalEntry(true);
    }

    /**
     * Get the very last entry of the database, sorted by timestamp
     *
     * @return the row as an object
     */
    public LocationHistoryObject getLastChronologicalEntry() {
        return getFirstOrLastChronologicalEntry(false);
    }

    public LocationHistoryObject getFirstOrLastChronologicalEntry(boolean first) {
        LocationHistoryObject lhObject = new LocationHistoryObject();
        try {
            SQLiteDatabase db = getReadableDatabase();
            String query;

            if (first) {
                query = "SELECT * FROM " + TABLE_LOCATION_HISTORY + " ORDER BY " + LH_TIMESTAMP + " ASC LIMIT 1";
            } else {
                query = "SELECT * FROM " + TABLE_LOCATION_HISTORY + " ORDER BY " + LH_TIMESTAMP + " DESC LIMIT 1";
            }

            Cursor c = db.rawQuery(query, null);
            c.moveToFirst();

            while (!c.isAfterLast()) {
                lhObject.setTimestampMs(c.getLong(c.getColumnIndex(LH_TIMESTAMP)));
                lhObject.setLatitudeE7(c.getLong(c.getColumnIndex(LH_LATITUDE_E7)));
                lhObject.setLongitudeE7(c.getLong(c.getColumnIndex(LH_LONGITUDE_E7)));
                lhObject.setAccuracy(c.getInt(c.getColumnIndex(LH_ACCURACY)));
                lhObject.setVelocity(c.getInt(c.getColumnIndex(LH_VELOCITY)));
                lhObject.setHeading(c.getInt(c.getColumnIndex(LH_HEADING)));
                lhObject.setAltitude(c.getInt(c.getColumnIndex(LH_ALTITUDE)));
                lhObject.setVerticalAccuracy(c.getInt(c.getColumnIndex(LH_VERTICAL_ACCURACY)));

                c.moveToNext();
            }
            c.close();
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return lhObject;
    }

    public void generateFavouritePlaces() {
        SQLiteDatabase db = getWritableDatabase();
        PreferenceManager preferenceManager = new PreferenceManager(_context);
        System.out.println("11111111111111111111111111111111");
        String dateQuery = "SELECT " + LH_TIMESTAMP + " FROM " + TABLE_LOCATION_HISTORY + " LIMIT 2";
        Cursor dateCursor = db.rawQuery(dateQuery, null);
        dateCursor.moveToFirst();
        long latestDate = Long.parseLong(dateCursor.getString(dateCursor.getColumnIndex(LH_TIMESTAMP)));
        dateCursor.close();
        long queryEndDate = latestDate - 86400 * 14 * 1000;
        String query = "SELECT * FROM " +
                TABLE_LOCATION_HISTORY + " WHERE " + LH_TIMESTAMP + " BETWEEN " + queryEndDate +
                " AND " + latestDate + " ORDER BY " + LH_LATITUDE_E7;

        // select all entries that fit in the date range and save them in an array list
//        String query = "SELECT * FROM " +
//                TABLE_LOCATION_HISTORY + " WHERE " + LH_TIMESTAMP + " BETWEEN " + preferenceManager.getDateRangeStart().getTimeInMillis() +
//                " AND " + preferenceManager.getDateRangeEnd().getTimeInMillis();

        Cursor cursor = db.rawQuery(query, null);

        System.out.println("cursor cursor cursor");
        System.out.println(cursor.getCount());

        ArrayList<FavPlace> coordinates = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(LH_ID));
                int lat = cursor.getInt(cursor.getColumnIndex(LH_LATITUDE_E7));
                int lon = cursor.getInt(cursor.getColumnIndex(LH_LONGITUDE_E7));
                boolean isDuplicate = false;
                for (FavPlace p : coordinates) {
                    if (p.lat == (double) lat / 10000000 && p.lon == (double) lon / 10000000) {
                        System.out.println("duplicate");
                        p.incrementVisits();
                        isDuplicate = true;
                    }
                }
                if (!isDuplicate) {
                    coordinates.add(new FavPlace(id, (double) lat / 10000000, (double) lon /
                            10000000, 0));
                }
            }
        } finally {
            cursor.close();
        }

        int i = 0;
        for (FavPlace place : coordinates) {
            if (i < 100) {
                try {
                    System.out.println
                            ("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");

                    GeoApiContext context = new GeoApiContext.Builder()
                            .apiKey("AIzaSyC7w2p0ViSu2MRNbc_RlHRR7rScokSxUGE")
                            .build();

                    PlacesSearchResponse result = PlacesApi.nearbySearchQuery(context, new LatLng(
                            place.lat, place.lon)).radius
                            (1000).keyword("a").await();

                    ContentValues favPlaceRow = new ContentValues();
                    System.out.println(result.results);
                    int randomElementIndex = new Random().nextInt(result.results.length);
                    if (result.results.length > 0) {
                        favPlaceRow.put(FP_ID, place.id);
                        favPlaceRow.put(FP_PLACE_ID, result.results[randomElementIndex].placeId);
                        favPlaceRow.put(FP_LATITUDE, place.lat);
                        favPlaceRow.put(FP_LONGITUDE, place.lon);
                        favPlaceRow.put(FP_NAME, result.results[randomElementIndex].name);
                        favPlaceRow.put(FP_ICON, String.valueOf(result.results[randomElementIndex].icon));
                        favPlaceRow.put(FP_RATING, result.results[randomElementIndex].rating);
                        favPlaceRow.put(FP_PHOTO, String.valueOf(result.results[randomElementIndex].photos[0]));
                        favPlaceRow.put(FP_ADDRESS, result.results[randomElementIndex].vicinity);
                        db.insert(TABLE_FAV_PLACES, null, favPlaceRow);
                    }

                } catch (final Exception e) {
                    System.out.println("error in radius search API");
                    System.out.println(e);
                }
                i = i + 5;
            }
        }
    }

    public ArrayList<FavPlaceResult> getFavouritePlaces() {
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<FavPlaceResult> favPlacesList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAV_PLACES, null);
        try {
            while (cursor.moveToNext()) {
                favPlacesList.add(new FavPlaceResult(
                        cursor.getInt(cursor.getColumnIndex(FP_ID)),
                        cursor.getString(cursor.getColumnIndex(FP_PLACE_ID)),
                        cursor.getDouble(cursor.getColumnIndex(FP_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(FP_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndex(FP_NAME)),
                        cursor.getString(cursor.getColumnIndex(FP_ICON)),
                        cursor.getDouble(cursor.getColumnIndex(FP_RATING)),
                        cursor.getString(cursor.getColumnIndex(FP_PHOTO)),
                        cursor.getString(cursor.getColumnIndex(FP_ADDRESS))));
            }
        } finally {
            cursor.close();
        }
        return favPlacesList;
    }
}