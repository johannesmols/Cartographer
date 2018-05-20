package itcom.cartographer.Utils;

public class CoordinateUtils {

    public static double getDistanceBetweenTwoPoints(int latitude1, int longitude1, int latitude2,
                                                     int longitude2) {
        double lat1 = (double) latitude1 / 10000000;
        double lon1 = (double) longitude1 / 10000000;
        double lat2 = (double) latitude2 / 10000000;
        double lon2 = (double) longitude2 / 10000000;

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        return Math.sqrt(distance);
    }
}
