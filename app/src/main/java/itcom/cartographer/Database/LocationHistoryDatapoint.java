package itcom.cartographer.Database;

public class LocationHistoryDatapoint {

    private long timestampMs;
    private long latitudeE7;
    private long longitudeE7;
    private short accuracy;

    public LocationHistoryDatapoint() {

    }

    public LocationHistoryDatapoint(long timestampMs, long latitudeE7, long longitudeE7, short accuracy) {
        this.timestampMs = timestampMs;
        this.latitudeE7 = latitudeE7;
        this.longitudeE7 = longitudeE7;
        this.accuracy = accuracy;
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

    public short getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(short accuracy) {
        this.accuracy = accuracy;
    }
}
