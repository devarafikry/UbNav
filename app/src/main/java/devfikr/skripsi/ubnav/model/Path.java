package devfikr.skripsi.ubnav.model;

import devfikr.skripsi.ubnav.model.LatLng;
/**
 * Created by Fikry-PC on 9/6/2017.
 */

public class Path {
    String id;
    LatLng startLocation;
    LatLng endLocation;

    public Path(String id, LatLng startLocation, LatLng endLocation) {
        this.id = id;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LatLng getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(LatLng startLocation) {
        this.startLocation = startLocation;
    }

    public LatLng getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(LatLng endLocation) {
        this.endLocation = endLocation;
    }
}
