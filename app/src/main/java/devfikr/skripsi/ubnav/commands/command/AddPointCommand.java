package devfikr.skripsi.ubnav.commands.command;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.commands.Command;
import devfikr.skripsi.ubnav.commands.helper.DatabaseOperationHelper;
import devfikr.skripsi.ubnav.commands.callback.AddPointCommandCallback;
import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;
import devfikr.skripsi.ubnav.util.SnackbarUtil;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public class AddPointCommand implements Command {
    private DatabaseHelper mDbHelper;
    private LatLng latLng;
    private Point addedPoint;
    private Point currentPoint;
    private Path addedPath;
    private AddPointCommandCallback addPointCommandCallback;
    private Snackbar s;
    private int pathCategory;

    @Override
    public void execute() {
        addPoint(latLng);
        addPointCommandCallback.addCommandExecuteResult(addedPoint,addedPath);
    }

    @Override
    public void undo() {
//        deletePoint(addedPoint.getId());
        deletePoint(addedPoint);
        addPointCommandCallback.addCommandUndoResult(addedPoint,addedPath);
    }

    @Override
    public void redo() {
        addPoint(latLng);
        addPointCommandCallback.addCommandExecuteResult(addedPoint,addedPath);
    }

    public AddPointCommand(
            AddPointCommandCallback addPointCommandCallback,
            DatabaseHelper mDbHelper,
            Point currentPoint,
            LatLng latLng,
            int pathCategory) {
        this.mDbHelper = mDbHelper;
        this.latLng = latLng;
        this.addPointCommandCallback = addPointCommandCallback;
        this.currentPoint = currentPoint;
        this.s = s;
        this.pathCategory = pathCategory;
    }

    private void addPoint(LatLng latLng){
        Point addedPoint = new Point(DatabaseOperationHelper.insertPointToDb(mDbHelper, latLng, pathCategory), latLng.latitude, latLng.longitude);
        this.addedPoint = addedPoint;
        addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, currentPoint.getId(),
                addedPoint.getId(), pathCategory), currentPoint, addedPoint);
    }

    public void deletePoint(Point pointToDelete) {
        long id = pointToDelete.getId();
        long idPathToBeDeleted = addedPath.getId();

        new deletePathTask().execute(new Long[]{idPathToBeDeleted});
        new deletePointTask().execute(new Long[]{id});
    }

    public void addPath(long pathId, Point startPosition,
                        Point endPosition){
        Path path = new Path(pathId,startPosition,
                endPosition
        );
        this.addedPath = path;
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
