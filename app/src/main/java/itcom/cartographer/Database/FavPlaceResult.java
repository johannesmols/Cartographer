package itcom.cartographer.Database;

/**
 * An object that represents a favourite place result (looked up on Google Maps)
 */
public class FavPlaceResult {
    public int id;
    public String place_id;
    public double lat;
    public double lon;
    public String name;
    public String icon;
    public double rating;
    public String photo;
    public String address;

    public FavPlaceResult(int id, String place_id, double lat, double lon, String name, String icon, double rating, String photo, String address) {
        this.id = id;
        this.place_id = place_id;
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.icon = icon;
        this.address = address;
        this.photo = photo;
        this.rating = rating;
    }
}
