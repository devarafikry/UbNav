package ub.mobile.ap.ubnav.base;

import ub.mobile.ap.ubnav.astar.AStar;
import ub.mobile.ap.ubnav.dijkstra.Dijkstra;

/**
 * Created by Aryo on 3/25/2017.
 */

public class NavigationLibrary {

    public static final int DIJKSTRA = 1;
    public static final int ASTAR = 2;

    public static IRoute getNavigationLibrary(int algorithm) throws Exception {
        switch (algorithm) {
            case NavigationLibrary.DIJKSTRA:
                return new Dijkstra();
            case NavigationLibrary.ASTAR:
                return new AStar();
        }
        throw new Exception("Algorithm not supported exception");
    }

}
