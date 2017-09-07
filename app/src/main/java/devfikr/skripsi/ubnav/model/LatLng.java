package devfikr.skripsi.ubnav.model;

/**
 * Created by Fikry-PC on 9/6/2017.
 */

public class LatLng {
    double latitude;
    double longitude;

    public LatLng(){

    }

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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
