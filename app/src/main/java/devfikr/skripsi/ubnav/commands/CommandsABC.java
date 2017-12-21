package devfikr.skripsi.ubnav.command;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;
import devfikr.skripsi.ubnav.util.SnackbarUtil;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public class CommandsABC {
    private Point selectedPosition;
    private DatabaseHelper mDbHelper;
    private View root_map;
    private Snackbar snackbar;
    private Marker selectedMarker;

    public void addPointBetweenPath(ArrayList<Path> paths, ArrayList<Point> points,
                                    Path selectedPath, LatLng latLng){
        long id = selectedPath.getId();
        for (Path path : paths){
            if (path.getId() == id){
                selectedPath = path;
                paths.remove(path);
                break;
            }
        }
        DatabaseOperationHelper.deletePathFromDb(mDbHelper, id);
//        addPoint(points, latLng);
        Point addedPoint = points.get(points.size()-1);

//        //insert path 1
//        addPath(paths, insertPathToDb(mDbHelper, selectedPath.getStartLocation().getId(),
//                addedPoint.getId()),
//                selectedPath.getStartLocation(), addedPoint);
//        //insert path 2
//        addPath(paths, insertPathToDb(mDbHelper, addedPoint.getId(),
//                selectedPath.getEndLocation().getId()),
//                addedPoint, selectedPath.getEndLocation());
        selectedPosition = addedPoint;
    }

//    private void updatePoint(ArrayList<Point> points, long selectedPointId, LatLng position) {
//        Point updatedPoint = new Point(selectedPointId, position.latitude, position.longitude);
//        DatabaseOperationHelper.updatePointToDb(mDbHelper, selectedPointId, position);
//        for (int i =0;i<points.size();i++){
//            if (points.get(i).getId() == selectedPointId){
//                points.set(i, updatedPoint);
//            }
//        }
//    }
    //point yang dapat dihapus adalah point yang tidak menjadi startNode pada suatu path
    public void deletePoint(ArrayList<Path> paths, ArrayList<Point> points) {
        long id = selectedPosition.getId();
        long idPathToBeDeleted = 0;
        Path pathToBeDeleted = null;
        for (Path path : paths){
            if(path.getStartLocation().getId() ==
                    id){
                SnackbarUtil.showSnackBar(root_map, snackbar,
                        "Anda tidak bisa menghapus node ini", Snackbar.LENGTH_LONG);
                return;
            }

            if(path.getEndLocation().getId() ==
                    id){
                idPathToBeDeleted = path.getId();
                pathToBeDeleted = path;
            }
        }
        paths.remove(pathToBeDeleted);
        points.remove(selectedPosition);

        selectedPosition = null;
        selectedMarker = null;
        DatabaseOperationHelper.deletePathFromDb(mDbHelper, idPathToBeDeleted);
        DatabaseOperationHelper.deletePointFromDb(mDbHelper, id);
    }


}
