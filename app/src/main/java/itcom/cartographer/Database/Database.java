package itcom.cartographer.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResponse;

import java.util.ArrayList;
import java.util.HashMap;

import itcom.cartographer.FavPlace;
import itcom.cartographer.FavPlaceResult;
import itcom.cartographer.Utils.PreferenceManager;
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
                LH_VERTICAL_ACCURACY + " INTEGER" + "" +
                ");" + "";
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
     * @return the row as an object
     */
    public LocationHistoryObject getFirstChronologicalEntry() {
        return getFirstOrLastChronologicalEntry(true);
    }

    /**
     * Get the very last entry of the database, sorted by timestamp
     * @return the row as an object
     */
    public LocationHistoryObject getLastChronologicalEntry() {
        return getFirstOrLastChronologicalEntry(false);
    }

    /**
     * Get the first or last object in the database, sorted by date
     * @param first determines if the first or last entry should be returned
     * @return the object
     */
    private LocationHistoryObject getFirstOrLastChronologicalEntry(boolean first) {
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

    /**
     * Get the latitude and the longitude form the database and transforms it into LatLng format for google maps
     * CAUTION!! this LatLng is NOT the same used for other purposes, this is coming from the google maps library.
     * @return the list.
     */
    public ArrayList<com.google.android.gms.maps.model.LatLng> getLatLng(){

        SQLiteDatabase db = getReadableDatabase();//get the database to read only
        PreferenceManager preferenceManager = new PreferenceManager(_context);
        String query = "SELECT " + LH_LATITUDE_E7 + ", " + LH_LONGITUDE_E7 +
                " FROM " + TABLE_LOCATION_HISTORY + " WHERE " + LH_TIMESTAMP +
                " BETWEEN " + preferenceManager.getDateRangeStart().getTimeInMillis() +
                " AND " + preferenceManager.getDateRangeEnd().getTimeInMillis(); //selects the latitude and the longitude from the table in the database and add them to the "query"

        Cursor cursor = db.rawQuery(query, null); //the cursor is used to iterate through the query
        ArrayList<com.google.android.gms.maps.model.LatLng> list = new ArrayList<>(); //Creates the list for the latitude and the longitude where to put the values from the "query"
        if((cursor != null && cursor.getCount() > 0)){ //if the query is not empty and bigger than only one element, the cursor will go thought the query
            cursor.moveToFirst();
            try{
                do{
                    int lat = cursor.getInt(cursor.getColumnIndex(LH_LATITUDE_E7));
                    int lng = cursor.getInt(cursor.getColumnIndex(LH_LONGITUDE_E7));
                    list.add(new com.google.android.gms.maps.model.LatLng((double)lat/1E7,(double) lng/1E7)); //Since the values in the database are coming "raw", they must be divided by 1E7 (10^7)
                    cursor.moveToNext();
                }while(!cursor.isAfterLast());
            }finally {
                cursor.close();
            }
        } //finish the loop
        return list;
    }

    public HashMap<com.google.android.gms.maps.model.LatLng,ArrayList<com.google.android.gms.maps.model.LatLng>> getLatLngTimed(){
        SQLiteDatabase db = getReadableDatabase();//get the database to read only
        PreferenceManager preferenceManager = new PreferenceManager(_context);

        String query = "SELECT " + LH_LATITUDE_E7 + ", " + LH_LONGITUDE_E7 + ", " + LH_TIMESTAMP +
                " FROM " + TABLE_LOCATION_HISTORY + " WHERE " + LH_TIMESTAMP +
                " BETWEEN " + preferenceManager.getDateRangeStart().getTimeInMillis() +
                " AND " + preferenceManager.getDateRangeEnd().getTimeInMillis();

        Cursor cursor = db.rawQuery(query, null);
        HashMap < com.google.android.gms.maps.model.LatLng,ArrayList < com.google.android.gms.maps.model.LatLng>> hashMap = new HashMap<>();
        if((cursor != null && cursor.getCount() > 0)){
            cursor.moveToFirst();
            double timeTemp = (double) cursor.getInt(cursor.getColumnIndex(LH_TIMESTAMP));
            try{
                do{
                    int lat = cursor.getInt(cursor.getColumnIndex(LH_LATITUDE_E7));
                    int lng = cursor.getInt(cursor.getColumnIndex(LH_LONGITUDE_E7));
                    com.google.android.gms.maps.model.LatLng StartPoint = new com.google.android.gms.maps.model.LatLng((double)lat/1E7,(double) lng/1E7);
                    ArrayList<com.google.android.gms.maps.model.LatLng> arrayList = new ArrayList<>();

                    while((cursor.getInt(cursor.getColumnIndex(LH_TIMESTAMP)) - timeTemp  <= 3.6E+6) && (!cursor.isAfterLast())){
                        timeTemp = (double) cursor.getInt(cursor.getColumnIndex(LH_TIMESTAMP));
                        int latEnd = cursor.getInt(cursor.getColumnIndex(LH_LATITUDE_E7));
                        int lngEnd = cursor.getInt(cursor.getColumnIndex(LH_LONGITUDE_E7));
                        com.google.android.gms.maps.model.LatLng point = new com.google.android.gms.maps.model.LatLng((double)latEnd/1E7,(double) lngEnd/1E7);
                        arrayList.add(point);
                        hashMap.put(StartPoint, arrayList);
                        cursor.moveToNext();
                    }
                    cursor.moveToNext();
                }while(!cursor.isAfterLast());
            }finally {
                cursor.close();
            }
        } //finish the loop
        return hashMap;
    }

    public void generateFavouritePlaces() {
        SQLiteDatabase db = getWritableDatabase();
        PreferenceManager preferenceManager = new PreferenceManager(_context);
//        String dateQuery = "SELECT " + LH_TIMESTAMP + " FROM " + TABLE_LOCATION_HISTORY + " LIMIT 2";
//        Cursor dateCursor = db.rawQuery(dateQuery, null);
//        dateCursor.moveToFirst();
//        long latestDate = Long.parseLong(dateCursor.getString(dateCursor.getColumnIndex(LH_TIMESTAMP)));
//        dateCursor.close();
//        long queryEndDate = latestDate - 86400 * 14 * 1000;
//        String query = "SELECT * FROM " +
//                TABLE_LOCATION_HISTORY + " WHERE " + LH_TIMESTAMP + " BETWEEN " + queryEndDate +
//                " AND " + latestDate + " ORDER BY " + LH_LATITUDE_E7;

        // select all entries that fit in the date range and save them in an array list
        String query = "SELECT * FROM " +
                TABLE_LOCATION_HISTORY + " WHERE " + LH_TIMESTAMP + " BETWEEN " + preferenceManager.getDateRangeStart().getTimeInMillis() +
                " AND " + preferenceManager.getDateRangeEnd().getTimeInMillis();

        Cursor cursor = db.rawQuery(query, null);
        ArrayList<FavPlace> coordinates = new ArrayList<>();
        boolean isDuplicate;
        try {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(LH_ID));
                int lat = cursor.getInt(cursor.getColumnIndex(LH_LATITUDE_E7));
                int lon = cursor.getInt(cursor.getColumnIndex(LH_LONGITUDE_E7));
                isDuplicate = false;
                for (FavPlace p : coordinates) {
                    if (p.lat == (double) lat / 10000000 && p.lon == (double) lon / 10000000) {
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

        for (FavPlace place : coordinates) {
                try {
                    GeoApiContext context = new GeoApiContext.Builder()
                            .apiKey("AIzaSyC7w2p0ViSu2MRNbc_RlHRR7rScokSxUGE")
                            .build();

                    PlacesSearchResponse result = PlacesApi.nearbySearchQuery(context, new LatLng(
                            place.lat, place.lon)).radius
                            (1000).keyword("a").await();

                    ContentValues favPlaceRow = new ContentValues();
                    if (result.results.length > 0) {
                        favPlaceRow.put(FP_ID, place.id);
                        favPlaceRow.put(FP_PLACE_ID, result.results[0].placeId);
                        favPlaceRow.put(FP_LATITUDE, place.lat);
                        favPlaceRow.put(FP_LONGITUDE, place.lon);
                        favPlaceRow.put(FP_NAME, result.results[0].name);
                        favPlaceRow.put(FP_ICON, String.valueOf(result.results[0].icon));
                        favPlaceRow.put(FP_RATING, result.results[0].rating);
                        favPlaceRow.put(FP_PHOTO, String.valueOf(result.results[0].photos[0]));
                        favPlaceRow.put(FP_ADDRESS, result.results[0].vicinity);
                        db.insert(TABLE_FAV_PLACES, null, favPlaceRow);
                        System.out.println("Inserted favourite place");
                    }
                } catch (final Exception e) {
                    Log.e("Radius API query error", e.getMessage());
                    e.printStackTrace();
                }
        }
    }

    public ArrayList<FavPlaceResult> getFavouritePlaces() {
        SQLiteDatabase db = getWritableDatabase();
        ArrayList<FavPlaceResult> favPlacesList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAV_PLACES, null);
        try {
            while (cursor.moveToNext()) {
                FavPlaceResult favPlace = new FavPlaceResult(
                        cursor.getInt(cursor.getColumnIndex(FP_ID)),
                        cursor.getString(cursor.getColumnIndex(FP_PLACE_ID)),
                        cursor.getDouble(cursor.getColumnIndex(FP_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(FP_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndex(FP_NAME)),
                        cursor.getString(cursor.getColumnIndex(FP_ICON)),
                        cursor.getDouble(cursor.getColumnIndex(FP_RATING)),
                        cursor.getString(cursor.getColumnIndex(FP_PHOTO)),
                        cursor.getString(cursor.getColumnIndex(FP_ADDRESS)));
                favPlacesList.add(favPlace);
            }
        } finally {
            cursor.close();
        }
        return favPlacesList;
    }
}