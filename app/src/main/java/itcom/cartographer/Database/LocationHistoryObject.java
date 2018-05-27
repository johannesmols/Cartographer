package itcom.cartographer.Database;

/**
 * An object that represents an entry in the location history file from Google. The same format is used in the database.
 */
public class LocationHistoryObject {

    private long timestampMs;
    private long latitudeE7;
    private long longitudeE7;
    private int accuracy;
    private int velocity;
    private int heading;
    private int altitude;
    private int verticalAccuracy;

    public LocationHistoryObject(long timestampMs, long latitudeE7, long longitudeE7, int accuracy, int velocity, int heading, int altitude, int verticalAccuracy) {
        this.timestampMs = timestampMs;
        this.latitudeE7 = latitudeE7;
        this.longitudeE7 = longitudeE7;
        this.accuracy = accuracy;
        this.velocity = velocity;
        this.heading = heading;
        this.altitude = altitude;
        this.verticalAccuracy = verticalAccuracy;
    }

    public LocationHistoryObject() {
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public void setTimestampMs(long timestampMs) {
        this.timestampMs = timestampMs;
    }

    public long getLatitudeE7() {
        return latitudeE7;
    }

    public void setLatitudeE7(long latitudeE7) {
        this.latitudeE7 = latitudeE7;
    }

    public long getLongitudeE7() {
        return longitudeE7;
    }

    public void setLongitudeE7(long longitudeE7) {
        this.longitudeE7 = longitudeE7;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public int getVelocity() {
        return velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public int getHeading() {
        return heading;
    }

    public void setHeading(int heading) {
        this.heading = heading;
    }

    public int getAltitude() {
        return altitude;
    }

    public void setAltitude(int altitude) {
        this.altitude = altitude;
    }

    public int getVerticalAccuracy() {
        return verticalAccuracy;
    }

    public void setVerticalAccuracy(int verticalAccuracy) {
        this.verticalAccuracy = verticalAccuracy;
    }
}
