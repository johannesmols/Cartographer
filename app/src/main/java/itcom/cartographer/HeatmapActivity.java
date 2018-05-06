package itcom.cartographer;

import android.graphics.Color;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import itcom.cartographer.Database.Database;
import itcom.cartographer.Utils.DirectionsAsync;

//DEMO: https://github.com/googlemaps/android-maps-utils/blob/master/demo/src/com/google/maps/android/utils/demo/HeatmapsDemoActivity.java
//COLORS: https://convertingcolors.com/

/**
 * This class is used to instantiate the map and set a Heat Map on top of it.
 *
 * The Heat Map uses the Google Maps Android API (no limit) solely.
 */

public class HeatmapActivity extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private List<LatLng> latLngList;
    public static Polyline line;
    private HashMap<LatLng, ArrayList<LatLng>> latLngDir;
    private AsyncTask<LatLng, Void, List<LatLng>> directionsAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        addAutocompleteFragment();
        //For resource reasons, the database should be created here, before the callback onMapReady,
        //and the list must be populated at this point, otherwise it will throw a NullPointException.
        Database db = new Database(this, null, null, 1);
        latLngList = db.getLatLng();
        latLngDir = db.getLatLngTimed();
        db.close();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *
     * Please be sure you are using a device which can use google play services.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng copenhagen = new LatLng(55.650498, 12.555349);

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(copenhagen));//set the camera in Copenhagen

        /*addHeatMap();*///add the heat map Tile Overlay on top of the map
        addPolylines();

    }

    private void addHeatMap(){
        // Create the gradient.
        int[] colors = {
                Color.argb(0, 0, 255, 255),// transparent (alpha determines opacity)
                Color.argb(255 / 3 * 2, 0, 255, 255), //soft blue
                Color.rgb(0, 191, 255), //sky blue
                Color.rgb(0, 0, 127), //deep blue
                Color.rgb(255, 0, 0) //red
        };

        float[] startPoints = {
                0.0f, 0.10f, 0.20f, 0.60f, 1.0f
        };
        Gradient gradient = new Gradient(colors, startPoints);
        try{
            if(mProvider == null && latLngList != null){//  if the provider is empty it will fill it with the data and add it to the overlay
                mProvider = new HeatmapTileProvider.Builder()
                        .data(latLngList)//must be a collection
                        .radius(50)
                        .gradient(gradient)
                        .build();
                mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));// Add a tile overlay to the map, using the heat map tile provider.
            }else{
                // Change the data for the new one and clears the previous Tile Overlay (heat map)
                if (mProvider != null) {
                    mProvider.setData(latLngList);
                }
                mOverlay.clearTileCache();

            }
        }catch(IllegalArgumentException iae){
            iae.printStackTrace();
            Toast.makeText(this, "No places to show", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * This is a fragment containing an autocomplete text field that uses the google places API KEY
     *  and the google places package.
     *  It moves and lock the camera to the result selected from the text field.
     */
    private void addAutocompleteFragment(){
        //Obtain the PlaceAutocompleteFragment and sets a listener on it. When a place is selected,
        // set and lock the camera on the location
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager()
                .findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                Log.i("Place", "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                Log.i("Error", "An error occurred: " + status);
            }
        });//end listener
    }//end method

    private void addPlaces(){

        try{
            Hashtable< LatLng, Integer> hashTable = new Hashtable<>();
            for(LatLng aLatLngList: latLngList) {
                if (!hashTable.containsKey(aLatLngList)) {
                    hashTable.put(aLatLngList, +1);
                } else {
                    hashTable.put(aLatLngList, 1);
                }
            }
            List<Map.Entry<LatLng, Integer>> list = new LinkedList<>(hashTable.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<LatLng, Integer>>() {
                @Override
                public int compare(Map.Entry<LatLng, Integer> o1, Map.Entry<LatLng, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });

            for(int x = 0; x < 5; x++){
                if(list.get(x) != null){
                    Geocoder geocoder= new Geocoder(this, Locale.getDefault());
                    geocoder.getFromLocation( list.get(0).getKey().latitude, list.get(0).getKey().longitude, 1);
                    if(Geocoder.isPresent()){
                        mMap.addMarker(new MarkerOptions().position(list.get(x).getKey()).title(geocoder.toString()));
                    }
                }
            }
            }catch(IOException ioe){
                ioe.printStackTrace();
                Toast.makeText(this, "No location found", Toast.LENGTH_LONG).show();
            }
    }

    private void addPolylines(){
        for(LatLng position : latLngDir.keySet() ){
            List<LatLng> list = latLngDir.get(position);
            LatLng temp = position;
            for(int x = 0; x < list.size(); x++){
                directionsAsync = new DirectionsAsync().execute(temp, list.get(x));
                temp = list.get(x);
            }
        }
    }
}
