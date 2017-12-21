package devfikr.skripsi.ubnav.command;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;
import devfikr.skripsi.ubnav.util.SnackbarUtil;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public class AddPointCommand implements Command {
    private Point selectedPoint;
    private DatabaseHelper mDbHelper;
    private ArrayList<Point> points;
    private ArrayList<Path> paths;
    private LatLng latLng;
    private Point addedPoint;
    private AddCommandCallback addCommandCallback;
    private View root_map;
    private Snackbar s;
    private int pathCategory;

    @Override
    public void execute() {
        addPoint(points, latLng);
        addCommandCallback.addCommandResult(points, paths, selectedPoint);
    }

    @Override
    public void undo() {
//        deletePoint(addedPoint.getId());
        deletePoint(addedPoint);
        addCommandCallback.addCommandResult(points, paths, selectedPoint);
    }

    @Override
    public void redo() {
        addPoint(points, latLng);
        addCommandCallback.addCommandResult(points, paths, selectedPoint);
    }

    @Override
    public ArrayList<Path> getPaths() {
        return paths;
    }

    @Override
    public ArrayList<Point> getPoints() {
        return points;
    }

    @Override
    public Point getSelectedPosition() {
        return selectedPoint;
    }

    public AddPointCommand(View view, Snackbar s, AddCommandCallback addCommandCallback, DatabaseHelper mDbHelper, ArrayList<Path> paths, ArrayList<Point> points, Point selectedPoint, LatLng latLng, int pathCategory) {
        this.mDbHelper = mDbHelper;
        this.points = points;
        this.latLng = latLng;
        this.paths = paths;
        this.selectedPoint = selectedPoint;
        this.addCommandCallback = addCommandCallback;
        this.root_map = view;
        this.s = s;
        this.pathCategory = pathCategory;
    }

    private void addPoint(ArrayList<Point> points, LatLng latLng){
        Point addedPoint = new Point(DatabaseOperationHelper.insertPointToDb(mDbHelper, latLng, pathCategory), latLng.latitude, latLng.longitude);
        points.add(addedPoint);
        this.addedPoint = addedPoint;

        addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, selectedPoint.getId(),
                addedPoint.getId(), pathCategory), selectedPoint, addedPoint);
    }
    public void deletePoint(Point pointToDelete) {
        long id = pointToDelete.getId();
        long idPathToBeDeleted = 0;
        Path pathToBeDeleted = null;
        for (Path path : paths){
            if(path.getStartLocation().getId() ==
                    id){
                SnackbarUtil.showSnackBar(root_map, s,
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
        points.remove(selectedPoint);

//        pointToDelete = null;
//        selectedMarker = null;
//        deletePathFromDb(mDbHelper, idPathToBeDeleted);
        new deletePathTask().execute(new Long[]{idPathToBeDeleted});
//        new deletePointTask().execute(new Long[]{idPathToBeDeleted});
//        deletePointFromDb(mDbHelper, id);
    }

    public void addPath(long pathId, Point startPosition,
                        Point endPosition){
        Path path = new Path(pathId,startPosition,
                endPosition
        );
        paths.add(path);
    }

    private class deletePathTask extends AsyncTask<Long, Void, Long> {
        @Override
        protected Long doInBackground(Long... longs) {
            long toBeDeletedId = longs[0];
            return DatabaseOperationHelper.deletePathFromDb(mDbHelper, toBeDeletedId);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);

        }
    }

    private class deletePointTask extends AsyncTask<Long, Void, Long> {
        @Override
        protected Long doInBackground(Long... longs) {
            long toBeDeletedId = longs[0];
            return DatabaseOperationHelper.deletePointFromDb(mDbHelper, toBeDeletedId);
        }

        @Override
        protected void onPostExecute(Long aLong) {
            super.onPostExecute(aLong);

        }
    }

}
