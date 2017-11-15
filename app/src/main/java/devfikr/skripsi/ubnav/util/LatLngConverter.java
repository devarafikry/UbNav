package devfikr.skripsi.ubnav.util;

import com.google.android.gms.maps.model.LatLng;

import devfikr.skripsi.ubnav.model.Point;

/**
 * Created by Fikry-PC on 9/6/2017.
 */

public class LatLngConverter {
    public static LatLng convertToGoogleLatLng(Point localPoint){
        LatLng latLng = new LatLng(
                localPoint.getLatitude(),
                localPoint.getLongitude()
        );
        return latLng;
    }

    public static Point convertToLocalLatLng(long id, LatLng googleLatLng){
        Point point = new Point(id,
                googleLatLng.latitude,
                googleLatLng.longitude
        );
        return point;
    }
}
