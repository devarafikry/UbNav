package devfikr.skripsi.ubnav.model;

/**
 * Created by Fikry-PC on 9/6/2017.
 */

public class Point {
    long id;
    double latitude;
    double longitude;

    public Point(){

    }

    public Point(long id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
