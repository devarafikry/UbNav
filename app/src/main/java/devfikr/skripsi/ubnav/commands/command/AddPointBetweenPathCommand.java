package devfikr.skripsi.ubnav.commands.command;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.commands.Command;
import devfikr.skripsi.ubnav.commands.helper.DatabaseOperationHelper;
import devfikr.skripsi.ubnav.commands.callback.AddPointBetweenPathCommandCallback;
import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public class AddPointBetweenPathCommand implements Command {
    private DatabaseHelper mDbHelper;
    private LatLng latLng;
    private Point addedPoint;
    private AddPointBetweenPathCommandCallback addPointBetweenPathCommandCallback;
    private Path selectedPath;
    private Path createdPath1;
    private Path createdPath2;
    private int pathCategory;

    public AddPointBetweenPathCommand(
            AddPointBetweenPathCommandCallback addPointBetweenPathCommandCallback,
            DatabaseHelper mDbHelper,
            Path selectedPath,
            LatLng latLng,
            int pathCategory) {
        this.mDbHelper = mDbHelper;
        this.addPointBetweenPathCommandCallback = addPointBetweenPathCommandCallback;
        this.selectedPath = selectedPath;
        this.latLng = latLng;
        this.pathCategory = pathCategory;
    }

    @Override
    public void execute() {
        addPointBetweenPath(selectedPath, latLng);
        addPointBetweenPathCommandCallback.addPointBetweenPathExecuteResult(selectedPath, createdPath1, createdPath2, addedPoint);
    }

    @Override
    public void undo() {
        new deletePointTask().execute(new Long[]{addedPoint.getId()});
        new deletePathTask().execute(new Long[]{createdPath1.getId()});
        new deletePathTask().execute(new Long[]{createdPath2.getId()});

        long recoveredPathId = DatabaseOperationHelper.insertPathToDb(mDbHelper, selectedPath.getStartLocation().getId(),
                selectedPath.getEndLocation().getId(), pathCategory);
        Path recoveredPath = addPath(recoveredPathId,
                selectedPath.getStartLocation(), selectedPath.getEndLocation());
        this.selectedPath = recoveredPath;
        addPointBetweenPathCommandCallback.addPointBetweenPathUndoResult(recoveredPath, createdPath1, createdPath2, addedPoint);
    }

    @Override
    public void redo() {
        addPointBetweenPath(selectedPath, latLng);
        addPointBetweenPathCommandCallback.addPointBetweenPathRedoResult(selectedPath, createdPath1, createdPath2, addedPoint);
    }

    private void addPoint(LatLng latLng){
        Point addedPoint = new Point(DatabaseOperationHelper.insertPointToDb(mDbHelper, latLng, pathCategory), latLng.latitude, latLng.longitude);
        this.addedPoint = addedPoint;
    }
    public Path addPath(long pathId, Point startPosition,
                        Point endPosition){
        Path path = new Path(pathId,startPosition,
                endPosition
        );
        return path;
    }
    public void addPointBetweenPath(Path selectedPath, LatLng latLng){
        long id = selectedPath.getId();

        new deletePathTask().execute(new Long[]{id});
        addPoint(latLng);
//        Point addedPoint = points.get(points.size()-1);

//        //insert path 1
        createdPath1 = addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, selectedPath.getStartLocation().getId(),
                addedPoint.getId(), pathCategory),
                selectedPath.getStartLocation(), addedPoint);
//        //insert path 2
        createdPath2 = addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, addedPoint.getId(),
                selectedPath.getEndLocation().getId(), pathCategory),
                addedPoint, selectedPath.getEndLocation());
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
