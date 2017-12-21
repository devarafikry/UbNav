package devfikr.skripsi.ubnav.commands.callback;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public interface AddPointBetweenPathCommandCallback {
    void addPointBetweenPathResult(ArrayList<Point> points, ArrayList<Path> paths, Point selectedPoint);
}
