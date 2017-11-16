package devfikr.skripsi.ubnav;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public interface UpdateCommandCallback {
    void updateCommandResult(ArrayList<Point> points, ArrayList<Path> paths, Point selectedPoint, Point draggedPoint);
}
