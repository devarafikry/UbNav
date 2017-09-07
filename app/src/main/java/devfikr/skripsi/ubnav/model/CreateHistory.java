package devfikr.skripsi.ubnav.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Fikry-PC on 9/6/2017.
 */

public class CreateHistory {
    int index;
    LatLng latLng;

    public CreateHistory(int index, LatLng latLng) {
        this.index = index;
        this.latLng = latLng;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
