package devfikr.skripsi.ubnav.commands.command;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.commands.Command;
import devfikr.skripsi.ubnav.commands.helper.DatabaseOperationHelper;
import devfikr.skripsi.ubnav.commands.callback.DeleteCommandCallback;
import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;
import devfikr.skripsi.ubnav.util.SnackbarUtil;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public class DeletePointCommand implements Command {
    private Point selectedPoint;
    private Point lastPointAfterDelete;
    private DatabaseHelper mDbHelper;
    private ArrayList<Point> points;
    private ArrayList<Path> paths;
    private DeleteCommandCallback deleteCommandCallback;
    private View root_map;
    private Snackbar s;
    private int pathCategory;

    public DeletePointCommand(View view, Snackbar s, DeleteCommandCallback deleteCommandCallback, DatabaseHelper mDbHelper, ArrayList<Path> paths, ArrayList<Point> points, Point selectedPoint, int pathCategory) {
        this.mDbHelper = mDbHelper;
        this.points = points;
        this.paths = paths;
        this.selectedPoint = selectedPoint;
        this.deleteCommandCallback = deleteCommandCallback;
        this.root_map = view;
        this.s = s;
        this.pathCategory = pathCategory;
    }

    @Override
    public void execute() {
        deletePoint(selectedPoint);
        deleteCommandCallback.deleteCommandResult(points, paths, null);
    }

    @Override
    public void undo() {
        addPoint(points, new LatLng(selectedPoint.getLatitude(), selectedPoint.getLongitude()));
        deleteCommandCallback.deleteCommandResult(points, paths, selectedPoint);
    }

    @Override
    public void redo() {
        deletePoint(selectedPoint);
        deleteCommandCallback.deleteCommandResult(points, paths, null);
    }

    @Override
    public ArrayList<Path> getPaths() {
        return null;
    }

    @Override
    public ArrayList<Point> getPoints() {
        return null;
    }

    @Override
    public Point getSelectedPosition() {
        return null;
    }
    private void addPoint(ArrayList<Point> points, LatLng latLng){
        Point addedPoint = new Point(DatabaseOperationHelper.insertPointToDb(mDbHelper, latLng, pathCategory), latLng.latitude, latLng.longitude);
        points.add(addedPoint);
        this.selectedPoint = addedPoint;

        addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, lastPointAfterDelete.getId(),
                addedPoint.getId(), pathCategory), lastPointAfterDelete, addedPoint);
    }
    public void addPath(long pathId, Point startPosition,
                        Point endPosition){
        Path path = new Path(pathId,startPosition,
                endPosition
        );
        paths.add(path);
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

//        selectedPoint = lastPointAfterDelete
        paths.remove(pathToBeDeleted);
        points.remove(selectedPoint);

//        pointToDelete = null;
//        selectedMarker = null;
//        deletePathFromDb(mDbHelper, idPathToBeDeleted);
        new deletePathTask().execute(new Long[]{idPathToBeDeleted});
        new deletePointTask().execute(new Long[]{id});
//        new deletePointTask().execute(new Long[]{idPathToBeDeleted});
//        deletePointFromDb(mDbHelper, id);
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
