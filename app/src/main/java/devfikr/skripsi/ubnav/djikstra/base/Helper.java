package devfikr.skripsi.ubnav.djikstra.base;


import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Aryo on 3/20/2017.
 */

public class Helper {

    public static double calculateDistance(Point a, Point b) {
        Double distance = Math.sqrt(Math.pow((a.latitude - b.latitude),2) + Math.pow((a.longitude - b.longitude),2));
        return distance;
    }

    public static double calculateDistance(Point a, Double lat, Double lng) {
        Double distance = Math.sqrt(Math.pow((a.latitude - lat),2) + Math.pow((a.longitude - lng),2));
        return distance;
    }

    public static double calculateDistance(Point a, LatLng latLng) {
        Double distance = Math.sqrt(Math.pow((a.latitude - latLng.latitude),2) + Math.pow((a.longitude - latLng.longitude),2));
        return distance;
    }

}
