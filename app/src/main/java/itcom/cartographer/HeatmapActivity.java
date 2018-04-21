package itcom.cartographer;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;


import java.util.List;

import itcom.cartographer.Database.Database;

//DEMO: https://github.com/googlemaps/android-maps-utils/blob/master/demo/src/com/google/maps/android/utils/demo/HeatmapsDemoActivity.java
//COLORS: https://convertingcolors.com/

/**
 * This class is used to instantiate the map and set a Heat Map on top of it.
 *
 * The Heat Map uses the Google Maps Android API (no limit) solely.
 */

public class HeatmapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private List<LatLng> latLngList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);// Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment.getMapAsync(this);

        addAutocompleteFragment();
        //For resource reasons, the database should be created here, before the callback onMapReady,
        //and the list must be populated at this point, otherwise it will throw a NullPointException.
        Database db = new Database(this, null, null, 1);
        latLngList = db.getLatLng();
        db.close();//**Remember to close the database when it's no longer needed
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

        addHeatMap();//add the heat map Tile Overlay on top of the map
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


        if(mProvider == null){//  if the provider is empty it will fill it with the data and add it to the overlay
            mProvider = new HeatmapTileProvider.Builder()
                    .data(latLngList)
                    .radius(50)
                    .gradient(gradient)
                    .build();
            mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));// Add a tile overlay to the map, using the heat map tile provider.
        }else{
            // Change the data for the new one and clears the previous Tile Overlay (heat map)
            mProvider.setData(latLngList);
            mOverlay.clearTileCache();
        }
    }
    private void addAutocompleteFragment(){
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
        });
    }
}
