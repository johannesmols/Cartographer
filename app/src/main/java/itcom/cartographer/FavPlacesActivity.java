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
import java.util.concurrent.TimeUnit;

import itcom.cartographer.Database.Database;

public class FavPlacesActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    ArrayList<FavPlaceResult> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final Database db = new Database(this, null, null, 1);
        new Thread(new Runnable() {
            public void run() {
                System.out.println("second threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond threadsecond thread");
                db.generateFavouritePlaces();
            }
        }).start();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
            public void run() {
                System.out.println("Hello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello worldHello world");
                results = db.getFavouritePlaces();
            }
        }, 0, 10, TimeUnit.SECONDS);

        System.out.println(results);
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

//        for (FavPlaceResult result : results) {
//            //get your favourite locations as a readable address
//            LatLng position = new LatLng(result.results[0].geometry
//                    .location.lat, result.results[0].geometry
//                    .location.lng);
//            mMap.addMarker(new MarkerOptions().position(position)).setTitle(result.results[0].name);
//        }


        // Add a marker in Sydney and move the camera
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
