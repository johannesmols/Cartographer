package itcom.cartographer.Database;

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
import java.util.Set;

import itcom.cartographer.FavPlace;
import itcom.cartographer.Utils.CoordinateUtils;

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
        sqLiteDatabase.execSQL(query);

        query = "CREATE TABLE " + TABLE_ACTIVITIES + "(" +
                AC_ID + " INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                AC_TIMESTAMP + " BIGINT NOT NULL" +
                ");";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATION_HISTORY);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
        onCreate(sqLiteDatabase);
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

    public ArrayList<PlacesSearchResponse> getFavouritePlaces() {
        String latestAddress;
        SQLiteDatabase db = getReadableDatabase();
        //Get the latest date from db
        String dateQuery = "SELECT " + LH_TIMESTAMP + " FROM " + TABLE_LOCATION_HISTORY + " LIMIT 2";
        Cursor dateCursor = db.rawQuery(dateQuery, null);
        dateCursor.moveToFirst();
        long latestDate = Long.parseLong(dateCursor.getString(dateCursor.getColumnIndex(LH_TIMESTAMP)));
        dateCursor.close();

        // subtract 1 week from the latest date to get a date range
        long queryEndDate = latestDate - 86400 * 7 * 1000;

        // select all entries that fit in the date range and save them in an array list
        String query = "SELECT " + LH_LATITUDE_E7 + ", " + LH_LONGITUDE_E7 + " FROM " +
                TABLE_LOCATION_HISTORY + " WHERE " + LH_TIMESTAMP + " BETWEEN " + queryEndDate +
                " AND " + latestDate + " ORDER BY " + LH_LATITUDE_E7;

        Cursor cursor = db.rawQuery(query, null);
        ArrayList<Point> coordinates = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                int lat = cursor.getInt(cursor.getColumnIndex(LH_LATITUDE_E7));
                int lon = cursor.getInt(cursor.getColumnIndex(LH_LONGITUDE_E7));
                coordinates.add(new Point(lat, lon));
            }
        } finally {
            cursor.close();
        }

        ArrayList<FavPlace> finalList = new ArrayList<>();
        Set<Point> uniquePlace = new HashSet<>();
        for (Point p : coordinates) {
            if (uniquePlace.add(p)) {
                finalList.add(new FavPlace(p.x, p.y, Collections.frequency(coordinates, p)));
                System.out.println("element: " + p
                        + ", count: " + Collections.frequency(coordinates, p));
            }
        }


        System.out.println(printed.toString());

//        ArrayList<ArrayList<Point>> outer = new ArrayList<>();
//        ArrayList<Point> inner = new ArrayList<>();
//        inner.add(coordinates.get(0));
//        outer.add(inner);
//
//        for (int i = 1; i < coordinates.size(); i++) {
//            Point firstItemOfLastArray = outer.get(outer.size() - 1).get(0);
//            if (CoordinateUtils.getDistanceBetweenTwoPoints(firstItemOfLastArray.x, firstItemOfLastArray.y, coordinates.get(i).x, coordinates.get(i).y) < 20) {
//                outer.get(outer.size() - 1).add(coordinates.get(i));
//            } else {
//                outer.add(new ArrayList<Point>());
//                outer.get(outer.size() - 1).add(coordinates.get(i));
//            }
//        }
//        ArrayList<Point> finalList = new ArrayList<>();
//
//        //clear duplicates
//        for (int k = 0; k < outer.size(); k++) {
//            for (int i = 0; i < outer.get(k).size(); i++) {
//                for (int j = i + 1; j < outer.get(k).size(); j++) {
//                    if (outer.get(k).get(i).equals(outer.get(k).get(j))) {
//                        outer.get(k).remove(j);
//                        j--;
//                    }
//                }
//            }
//            finalList.addAll(outer.get(k));
//        }
//        ArrayList<String> favouritePlaces = new ArrayList<>();
//
//        String CREATE_BOOK_TABLE = "CREATE TABLE books ( " +
//                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                "title TEXT, " +
//                "author TEXT, " +
//                "sales INTEGER )";
//
//        // create books table
//        db.execSQL(CREATE_BOOK_TABLE);
//        GeocodingResult[] results = null;
//        for (Point coordinate : finalList) {
//            //get your favourite locations as a readable address
//            try {
//                GeoApiContext context = new GeoApiContext.Builder()
//                        .apiKey("AIzaSyC7w2p0ViSu2MRNbc_RlHRR7rScokSxUGE")
//                        .build();
//                results = GeocodingApi.reverseGeocode(context, new LatLng(
//                        (double) coordinate.x / 10000000, (double) coordinate
//                        .y / 10000000)).await();
//                System.out.println(results[0].formattedAddress);
//                favouritePlaces.add(results[0].formattedAddress);
//            } catch (final Exception e) {
//                System.out.println(e.getMessage());
//            }
//        }


//        String CREATE_BOOK_TABLE = "CREATE TABLE books ( " +
//                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                "title TEXT, " +
//                "author TEXT, " +
//                "sales INTEGER )";
//
//        // create books table
//        db.execSQL(CREATE_BOOK_TABLE);
        ArrayList<PlacesSearchResponse> favouritePlaces = new ArrayList<>();
        System.out.println(finalList.get(0));

        for (FavPlace place : finalList) {
            try {
                GeoApiContext context = new GeoApiContext.Builder()
                        .apiKey("AIzaSyC7w2p0ViSu2MRNbc_RlHRR7rScokSxUGE")
                        .build();

                PlacesSearchResponse result = PlacesApi.nearbySearchQuery(context, new LatLng(
                        (double) place.x / 10000000, (double) place.y / 10000000)).radius(1000).keyword("a").await();

                System.out.println(result);

                favouritePlaces.add(result);

            } catch (final Exception e) {
                System.out.println(e);
            }
        }

        return favouritePlaces;
    }
}