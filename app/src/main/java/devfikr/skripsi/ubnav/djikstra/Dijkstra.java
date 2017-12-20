package ub.mobile.ap.ubnav.dijkstra;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import ub.mobile.ap.ubnav.base.Graph;
import ub.mobile.ap.ubnav.base.IRoute;
import ub.mobile.ap.ubnav.base.Point;

/**
 * Created by Aryo on 3/12/2017.
 */

public class Dijkstra implements IRoute {

    @Override
    public Graph calculateShortestPathFrom(Graph graph, Point source, Point destination) throws Exception {

        source.setDistance(0D);

        Set<Point> settledPoints = new HashSet<>();
        Set<Point> unsettledPoints = new HashSet<>();
        unsettledPoints.add(source);

        while (unsettledPoints.size() != 0) {
            Point currentPoint = getLowestDistancePoint(unsettledPoints);
            unsettledPoints.remove(currentPoint);
            for (Map.Entry<Point, Double> adjacencyPair:
                    currentPoint.getAdjacentPoints().entrySet()) {
                Point adjacentPoint = adjacencyPair.getKey();
                Double edgeWeight = adjacencyPair.getValue();
                if (!settledPoints.contains(adjacentPoint)) {
                    calculateMinimumDistance(adjacentPoint, edgeWeight, currentPoint);
                    unsettledPoints.add(adjacentPoint);
                }
            }
            settledPoints.add(currentPoint);
        }
        graph.setPoints(settledPoints);
        return graph;
    }

    private static Point getLowestDistancePoint(Set < Point > unsettledPoints) {
        Point lowestDistancePoint = null;
        Double lowestDistance = Double.MAX_VALUE;
        for (Point point: unsettledPoints) {
            Double pointDistance = point.getDistance();
            if (pointDistance < lowestDistance) {
                lowestDistance = pointDistance;
                lowestDistancePoint = point;
            }
        }
        return lowestDistancePoint;
    }

    private static void calculateMinimumDistance(Point evaluationPoint,
                                                 Double edgeWeight, Point sourcePoint) {
        Double sourceDistance = sourcePoint.getDistance();
        if (sourceDistance + edgeWeight < evaluationPoint.getDistance()) {
            evaluationPoint.setDistance(sourceDistance + edgeWeight);
            LinkedList<Point> shortestPath = new LinkedList<>(sourcePoint.getShortestPath());
            shortestPath.add(sourcePoint);
            evaluationPoint.setShortestPath(shortestPath);
        }
    }

}
