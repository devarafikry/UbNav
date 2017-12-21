package devfikr.skripsi.ubnav.djikstra.base;


import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Aryo on 3/12/2017.
 */

public class Path {

    public String id;
    public Point startPoint;
    public Point endPoint;
    public Double distance;

    /*
    public Path(String id, Point startPoint, Point endPoint) {
        this.id = id;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }
    */

    public Path(String id, String idStartPoint, double latStartPoint, double lngStartPoint, String idEndPoint, double latEndPoint, double lngEndPoint) {
        this.id = id;
        this.startPoint = new Point(idStartPoint, latStartPoint, lngStartPoint);
        this.endPoint = new Point(idEndPoint, latEndPoint, lngEndPoint);
        this.distance = Helper.calculateDistance(startPoint, endPoint);
    }

    /*
    public Path(Point startPoint, Point endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }

    public Path(String idStartPoint, double latStartPoint, double lngStartPoint, String idEndPoint, double latEndPoint, double lngEndPoint) {
        this.startPoint = new Point(idStartPoint, latStartPoint, lngStartPoint);
        this.endPoint = new Point(idEndPoint, latEndPoint, lngEndPoint);
    }
    */

    public LatLng[] getPoints() {
        return new LatLng[] { startPoint.toLatLng(), endPoint.toLatLng() };
    }

}
