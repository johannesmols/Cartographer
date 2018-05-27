package itcom.cartographer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import itcom.cartographer.Database.Database;
import itcom.cartographer.Utils.DirectionsAsync;

/**
 * An activity that draws the most frequents routes on a map
 */
public class RoutesActivity extends FragmentActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    public static Polyline line;
    private HashMap<LatLng, ArrayList<LatLng>> latLngDir;
    private AsyncTask<LatLng, Void, List<LatLng>> directionsAsync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Database db = new Database(this, null, null, 1);
        latLngDir = db.getLatLngTimed();
        db.close();

        addAutocompleteFragment();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng copenhagen = new LatLng(55.650498, 12.555349);

        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(copenhagen));//set the camera in Copenhagen

        addPolyline();
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


    /**
     * This method draws a polyline following the directions between a starting point and a destination point.
     * The hashmap contains a KEY value containing a list, which contains all the coordinates with less than 1h between them.
     */
    private void addPolyline(){
        for(LatLng position : latLngDir.keySet() ){ //Iterates through the hashmap, for each entry KEY.
            List<LatLng> list = latLngDir.get(position); //gets the list from the KEY of the hashmap.
            LatLng temp = position; //this will be the tarting point.
            for(int x = 0; x < list.size(); x++){
                directionsAsync = new DirectionsAsync().execute(temp, list.get(x)); //request the directions between the starting point "temp" and the destination "list.get(x)".
                temp = list.get(x);// the starting point becomes the last destination.
            }
        }
    }
}
