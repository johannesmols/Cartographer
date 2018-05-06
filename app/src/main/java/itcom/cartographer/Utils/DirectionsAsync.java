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

import static itcom.cartographer.HeatmapActivity.line;
import static itcom.cartographer.HeatmapActivity.mMap;

public class DirectionsAsync extends AsyncTask<LatLng, Void, List<LatLng>> {

    JSONParserDirections jsonParserDirections;
    String DIRECTIONS_URL;

    @Override
    protected  void onPreExecute(){
        super.onPreExecute();
    }

    @Override
    protected List<LatLng> doInBackground(LatLng...params){
        LatLng start = params[0];
        LatLng end = params[1];

        HashMap<String, String> points = new HashMap<>();
        points.put("origin", start.latitude + "," + start.longitude);
        points.put("destination", end.latitude + "," + end.longitude);

        jsonParserDirections = new JSONParserDirections();

        DIRECTIONS_URL = "https://maps.googleapis.com/maps/api/directions/json";
        JSONObject jsonObject = jsonParserDirections.mkRequest(DIRECTIONS_URL, "GET", points, true );

        if(jsonObject == null) return null;

        try{
            List<LatLng> list;

            JSONArray routeArray= jsonObject.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolyline = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolyline.getString("points");
            list = PolyUtil.decode(encodedString);

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
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        for (int i = 0; i < pointsList.size(); i++) {
            LatLng point = pointsList.get(i);
            options.add(point);
        }
        mMap.addPolyline(options);
    }
}
