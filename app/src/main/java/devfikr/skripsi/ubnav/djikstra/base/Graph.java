package ub.mobile.ap.ubnav.base;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Aryo on 3/20/2017.
 */

public class Graph {

    private Set<Point> points = new HashSet<>();

    public void addPoint(Point point) {
        this.points.add(point);
    }

    public void setPoints(Set<Point> points) {
        this.points = points;
    }

    public Set<Point> getPoints() {
        return this.points;
    }

    public static Set<Point> build(ArrayList<Point> points, ArrayList<Path> paths) {
        Set<Point> setPoints = new HashSet<>();
        for(Point p: points) {
            for(Path path: paths) {
                if(p.latitude == path.startPoint.latitude && p.longitude == path.startPoint.longitude)
                    p.addDestination(findPoint(path.endPoint, points), Helper.calculateDistance(p, path.endPoint));
                else if(p.latitude == path.endPoint.latitude && p.longitude == path.endPoint.longitude)
                    p.addDestination(findPoint(path.startPoint, points), Helper.calculateDistance(p, path.startPoint));
            }
            setPoints.add(p);
        }
        for(Point p: setPoints) {
            Log.d("UBNav", p.id + ": " + p.getAdjacentPoints().size());
        }
        return setPoints;
    }

    private static Point findPoint(Point p, ArrayList<Point> points) {
        for(Point tp: points) {
            if(p.latitude == tp.latitude && p.longitude == tp.longitude) {
                return tp;
            }
        }
        return null;
    }

    //public ArrayList<Point> getPoints() {
    //    return this.points;
    //}

}
