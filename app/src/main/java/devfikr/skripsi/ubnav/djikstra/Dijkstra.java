package devfikr.skripsi.ubnav.djikstra;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import devfikr.skripsi.ubnav.djikstra.base.Graph;
import devfikr.skripsi.ubnav.djikstra.base.IRoute;
import devfikr.skripsi.ubnav.djikstra.base.Path;
import devfikr.skripsi.ubnav.djikstra.base.Point;
import timber.log.Timber;


/**
 * Created by Aryo on 3/12/2017.
 */

public class Dijkstra implements IRoute {

    @Override
    public Graph calculateShortestPathFrom(Graph graph, Point source) throws Exception {

        source.setDistance(0D);
//        destination.setDistance(0D);

        Set<Point> settledPoints = new HashSet<>();
        Set<Point> unsettledPoints = new HashSet<>();
        unsettledPoints.add(source);
//        unsettledPoints.add(destination);

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

    @Override
    public Graph calculateShortestVectorPathFrom(Graph graph, Point source, ArrayList<Path> paths) throws Exception {

        source.setDistance(0D);
//        destination.setDistance(0D);

        Set<Point> settledPoints = new HashSet<>();
        Set<Point> unsettledPoints = new HashSet<>();
        unsettledPoints.add(source);
//        unsettledPoints.add(destination);

        while (unsettledPoints.size() != 0) {
            Point currentPoint = getLowestDistancePoint(unsettledPoints);
            unsettledPoints.remove(currentPoint);
            for (Map.Entry<Point, Double> adjacencyPair:
                    currentPoint.getAdjacentPoints().entrySet()) {
                Point adjacentPoint = adjacencyPair.getKey();
                Double edgeWeight = adjacencyPair.getValue();
                boolean isBackward = false;
                for (Path path : paths){
                    if(path.startPoint.id.equals(currentPoint.id)
                            &&
                            path.endPoint.id.equals(adjacentPoint.id)){
                        isBackward = true;
                    }
                }
                if (!settledPoints.contains(adjacentPoint) && !isBackward) {
                    calculateMinimumDistanceVector(adjacentPoint, edgeWeight, currentPoint, paths);
                    unsettledPoints.add(adjacentPoint);
                }
            }
            settledPoints.add(currentPoint);
        }
        graph.setPoints(settledPoints);
        return graph;
    }

    @Override
    public Graph shortestPath(Graph graph, Point source, Point destination) throws Exception {
        source.setDistance(0D);
        destination.setDistance(0D);

        Set<Point> settledPoints = new HashSet<>();
        Set<Point> unsettledPoints = new HashSet<>();
        unsettledPoints.add(source);
        unsettledPoints.add(destination);

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

    public static Point getLowestDistancePoint(Set < Point > unsettledPoints) {
        Point lowestDistancePoint = null;
        Double lowestDistance = Double.MAX_VALUE;
        for (Point point: unsettledPoints) {
            Double pointDistance = point.getDistance();
            if (pointDistance < lowestDistance) {
                Timber.d(pointDistance+" < "+lowestDistance);
                lowestDistance = pointDistance;
                lowestDistancePoint = point;
            }
        }
        return lowestDistancePoint;
    }
//
//    public static Point getLowestDistancePoint(Set < Point > unsettledPoints, Point notPermittedPoint) {
//        Point lowestDistancePoint = null;
//        Double lowestDistance = Double.MAX_VALUE;
//        for (Point point: unsettledPoints) {
//            Double pointDistance = point.getDistance();
//            if (pointDistance < lowestDistance &&
//                    !(point.id.equals(notPermittedPoint.id))) {
//                Timber.d(pointDistance+" < "+lowestDistance);
//                lowestDistance = pointDistance;
//                lowestDistancePoint = point;
//            }
//        }
//        return lowestDistancePoint;
//    }

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

    private static void calculateMinimumDistanceVector(Point evaluationPoint,
                                                 Double edgeWeight, Point sourcePoint, ArrayList<Path> paths) {
        Double sourceDistance = sourcePoint.getDistance();
//        boolean isBackward = false;
//        for (int i=0;i<paths.size();i++){
//            if(paths.get(i).startPoint.id.equals(sourcePoint.id)
//                    && paths.get(i).endPoint.id.equals(evaluationPoint.id)){
//                isBackward = true;
//            }
//        }
        if (sourceDistance + edgeWeight < evaluationPoint.getDistance()) {
            evaluationPoint.setDistance(sourceDistance + edgeWeight);
            LinkedList<Point> shortestPath = new LinkedList<>(sourcePoint.getShortestPath());
            shortestPath.add(sourcePoint);
            evaluationPoint.setShortestPath(shortestPath);
        }
    }

}
