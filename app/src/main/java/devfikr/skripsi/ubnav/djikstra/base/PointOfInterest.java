package devfikr.skripsi.ubnav.djikstra.base;

/**
 * Created by Aryo on 3/25/2017.
 */

public class PointOfInterest extends Point {

    private String name;

    public PointOfInterest(String id, double lat, double lng) {
        super(id, lat, lng);
        this.name = id;
    }

    public String getName() {
        return this.name;
    }

}
