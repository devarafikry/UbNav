package devfikr.skripsi.ubnav.djikstra.base;

import java.util.ArrayList;

/**
 * Created by Aryo on 3/20/2017.
 */

public interface IRoute {
    Graph calculateShortestPathFrom(Graph graph, Point source) throws Exception;
    Graph calculateShortestVectorPathFrom(Graph graph, Point source, ArrayList<Path> paths) throws Exception;
    Graph shortestPath(Graph graph, Point source, Point destination) throws Exception;
}
