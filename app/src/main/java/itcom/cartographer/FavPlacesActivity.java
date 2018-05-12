package itcom.cartographer;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.PlacesSearchResponse;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import itcom.cartographer.Database.Database;

public class FavPlacesActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    static ScheduledFuture<?> scheduleFavPlacesGetter;
    ArrayList<FavPlaceResult> results;
    final Database db = new Database(this, null, null, 1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        results = db.getFavouritePlaces();

        if (results.size() < 1) {
            new Thread(new Runnable() {
                public void run() {
                    db.generateFavouritePlaces();
                    scheduleFavPlacesGetter.cancel(false);
                }
            }).start();

            scheduleFavPlacesGetter = executor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    results = db.getFavouritePlaces();
                    // TODO: trigger update of map
                }
            }, 0, 10, TimeUnit.SECONDS);
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        for (FavPlaceResult result : results) {
            LatLng position = new LatLng(result.lat, result.lon);
            mMap.addMarker(new MarkerOptions().position(position)).setTitle(result.name);
        }


        // Add a marker in Cph and move the camera
        LatLng cph = new LatLng(55.679, 12.572);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cph, 12));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {

                System.out.println(marker.getTitle());
            }
        });
    }
}
