package devfikr.skripsi.ubnav.util;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Fikry-PC on 9/6/2017.
 */

public class LatLngConverter {
    public static LatLng convertToGoogleLatLng(devfikr.skripsi.ubnav.model.LatLng localLatLng){
        LatLng latLng = new LatLng(
                localLatLng.getLatitude(),
                localLatLng.getLongitude()
        );
        return latLng;
    }

    public static devfikr.skripsi.ubnav.model.LatLng convertToLocalLatLng(String id,LatLng googleLatLng){
        devfikr.skripsi.ubnav.model.LatLng latLng = new devfikr.skripsi.ubnav.model.LatLng(id,
                googleLatLng.latitude,
                googleLatLng.longitude
        );
        return latLng;
    }
}
