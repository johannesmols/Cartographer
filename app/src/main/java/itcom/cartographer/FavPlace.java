package itcom.cartographer;

/**
 * Created by Boris on 4/24/2018.
 */

public class FavPlace {
    public int id;
    public double lat;
    public double lon;
    public int visits;

    public FavPlace(int id, double lat, double lon, int visits) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.visits = visits;
    }
}
