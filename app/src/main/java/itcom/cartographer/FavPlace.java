package itcom.cartographer;

/**
 * Created by Boris on 4/24/2018.
 */

public class FavPlace {
    public double x;
    public double y;
    public int count;

    public FavPlace(double x, double y, int count) {
        this.x = x;
        this.y = y;
        this.count = count;
    }

    private void incrementCount() {
        this.count++;
    }
}
