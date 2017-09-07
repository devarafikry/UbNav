package devfikr.skripsi.ubnav.util;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Fikry-PC on 9/6/2017.
 */

public class Distance {
    public static double pointToLineDistance(LatLng A, LatLng B, LatLng P) {
//        double normalLength = Math.sqrt((B.latitude-A.latitude)*(B.latitude-A.latitude)+(B.longitude-A.longitude)*(B.longitude-A.longitude));
//        return Math.abs((P.latitude-A.latitude)*(B.longitude-A.longitude)-(P.longitude-A.longitude)*(B.longitude-A.longitude))/normalLength;

        double normalLength = Math.sqrt((B.longitude-A.longitude)*(B.longitude-A.longitude)+(B.latitude-A.latitude)*(B.latitude-A.latitude));
        return Math.abs((P.longitude-A.longitude)*(B.latitude-A.latitude)-(P.latitude-A.latitude)*(B.latitude-A.latitude))/normalLength;
    }
}
