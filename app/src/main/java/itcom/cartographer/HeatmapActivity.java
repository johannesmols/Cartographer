package itcom.cartographer;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

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

public class HeatmapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private List<LatLng> latLngList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //For resource reasons, the database should be created here, before the callback onMapReady,
        //and the list must be populated at this point, otherwise it will throw a NullPointException.
        Database db = new Database(this, null, null, 1);
        latLngList = db.getLatLng();
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
        mMap = googleMap;
        //set the camera in Copenhagen
        LatLng copenhagen = new LatLng(55.650498, 12.555349);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(copenhagen));
        //add the heat map Tile Overlay on top of the map
        addHeatMap();
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

        // Create a heat map tile provider
        if(mProvider == null){
            mProvider = new HeatmapTileProvider.Builder()
                    .data(latLngList)
                    .radius(50)
                    .gradient(gradient)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
            mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }else{
            // Change the data for the new one and clears the previous Tile Overlay (heat map)
            mProvider.setData(latLngList);
            mOverlay.clearTileCache();
        }

    }
}
