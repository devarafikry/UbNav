package devfikr.skripsi.ubnav.model;

/**
 * Created by Fikry-PC on 9/6/2017.
 */

public class Path {
    long id;
    Point startLocation;
    Point endLocation;

    public Path(long id, Point startLocation, Point endLocation) {
        this.id = id;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Point getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Point startLocation) {
        this.startLocation = startLocation;
    }

    public Point getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Point endLocation) {
        this.endLocation = endLocation;
    }
}
