package ub.mobile.ap.ubnav.base;

/**
 * Created by Aryo on 3/20/2017.
 */

public interface IRoute {
    Graph calculateShortestPathFrom(Graph graph, Point source, Point destination) throws Exception;
}
