package itcom.cartographer;

/**
 * Created by Boris on 4/24/2018.
 */

public class FavPlaceResult {
    public int id;
    public String place_id;
    public String name;
    public String icon;
    public double rating;
    public String photo;
    public String address;

    public FavPlaceResult(int id, String place_id, String name, String icon, double rating, String photo, String address) {
        this.id = id;
        this.place_id = place_id;
        this.name = name;
        this.icon = icon;
        this.address = address;
        this.photo = photo;
        this.rating = rating;
    }
}
