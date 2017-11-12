package devfikr.skripsi.ubnav.model;

/**
 * Created by Fikry-PC on 9/6/2017.
 */

public class LatLng {
    String id;
    double latitude;
    double longitude;

    public LatLng(){

    }

    public LatLng(String id, double latitude, double longitude) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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
