package itcom.cartographer.Utils;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import static itcom.cartographer.RoutesActivity.line;
import static itcom.cartographer.RoutesActivity.mMap;

/**
 * This class is used as an API, it will send a JSONObject and retrieve an encoded String, which
 * will be decoded to get the points to form the polyline and draw the route.
 */
public class DirectionsAsync extends AsyncTask<LatLng, Void, List<LatLng>> {

    JSONParserDirections jsonParserDirections;
    String DIRECTIONS_URL;

    @Override
    protected  void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected List<LatLng> doInBackground(LatLng...params){ //This method is the one in charge to talk to Google.
        LatLng start = params[0]; //starting point.
        LatLng end = params[1]; // destination point.

        HashMap<String, String> points = new HashMap<>();
        // NOTE: "origin" and "destination" must not be changed.
        points.put("origin", start.latitude + "," + start.longitude); // "origin" as key and coordinates as value.
        points.put("destination", end.latitude + "," + end.longitude); // "destination" as key and coordinates as value.

        jsonParserDirections = new JSONParserDirections();

        DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json"; //google API for directions.
        JSONObject jsonObject = jsonParserDirections.mkRequest(DIRECTIONS_URL, "GET", points, true ); //JSONObject with the routes.

        if(jsonObject == null) return null;

        try{
            List<LatLng> list; //list for the points of the route.

            JSONArray routeArray = jsonObject.getJSONArray("routes"); //array for the routes of the JSONObject obtained.
            JSONObject routes = routeArray.getJSONObject(0); //get the routes from then JSONObject.
            JSONObject overviewPolyline = routes.getJSONObject("overview_polyline"); //gets the encoded polyline.
            String encodedString = overviewPolyline.getString("points"); //the encoded polyline.
            list = PolyUtil.decode(encodedString); //the list containing the points of the route to make the polyline.

            return list;
        }catch(JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected  void onPostExecute(List<LatLng> pointsList){
        if(pointsList == null) return;
        if(line != null){
            line.remove();
        }
        PolylineOptions options = new PolylineOptions().width(5).color(Color.RED).geodesic(true); //attach the draw to the streets on the map.
        for (int i = 0; i < pointsList.size(); i++) {
            LatLng point = pointsList.get(i);
            options.add(point);
        }
        mMap.addPolyline(options); //adds the polyline (visual of the route)
    }
}
