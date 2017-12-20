package ub.mobile.ap.ubnav.base;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Aryo on 3/11/2017.
 */

public class Point {
    public String id;
    public double latitude;
    public double longitude;

    private List<Point> shortestPath = new LinkedList<>();
    private Double distance = Double.MAX_VALUE;
    private Map<Point, Double> adjacentPoints = new HashMap<>();

    public Point(String id, double lat, double lng) {
        this.id = id;
        this.latitude = lat;
        this.longitude = lng;
    }

    public LatLng toLatLng() {
        return new LatLng(this.latitude, this.longitude);
    }

    public void addDestination(Point destination, Double distance) {
        this.adjacentPoints.put(destination, distance);
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Map<Point, Double> getAdjacentPoints() {
        return adjacentPoints;
    }

    public Double getDistance() {
        return distance;
    }

    public void setShortestPath(LinkedList<Point> shortestPath) {
        this.shortestPath = shortestPath;
    }

    public List<Point> getShortestPath() {
        return shortestPath;
    }

    @Override
    public String toString() {
        return this.id + ": " + this.latitude + ", " + this.longitude;
    }

    public Double heuristic = 0D;
    public Double g = 0D;
    public Double getF() {
        return g+heuristic;
    }

}
