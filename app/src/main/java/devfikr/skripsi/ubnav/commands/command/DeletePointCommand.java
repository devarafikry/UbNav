package devfikr.skripsi.ubnav.commands.command;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.commands.Command;
import devfikr.skripsi.ubnav.commands.helper.DatabaseOperationHelper;
import devfikr.skripsi.ubnav.commands.callback.DeleteCommandCallback;
import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public class DeletePointCommand implements Command {
    private Point currentPoint;
    private Point toBeDeletedPoint;
    private DatabaseHelper mDbHelper;
    private DeleteCommandCallback deleteCommandCallback;
    private int pathCategory;
    private Path removedPath;
    private ArrayList<Path> paths;
    private String message = null;

    public DeletePointCommand(
            DeleteCommandCallback deleteCommandCallback,
            DatabaseHelper mDbHelper,
            Point currentPoint,
            ArrayList<Path> paths,
            int pathCategory) {
        this.mDbHelper = mDbHelper;
        this.toBeDeletedPoint = currentPoint;
        this.deleteCommandCallback = deleteCommandCallback;
        this.pathCategory = pathCategory;
        this.paths = paths;
    }

    @Override
    public void execute() {
        deletePoint(toBeDeletedPoint);
        deleteCommandCallback.deleteCommandExecuteResult(removedPath, toBeDeletedPoint, message);
    }

    @Override
    public void undo() {
        recoverPoint();
        deleteCommandCallback.deleteCommandUndoResult(removedPath, toBeDeletedPoint);
    }

    @Override
    public void redo() {
        deletePoint(toBeDeletedPoint);
        deleteCommandCallback.deleteCommandExecuteResult(removedPath, toBeDeletedPoint, message);
    }

    private void recoverPoint(){
        LatLng deletedLatLng = new LatLng(toBeDeletedPoint.getLatitude(), toBeDeletedPoint.getLongitude());
        Point recoveredPoint = new Point(
                DatabaseOperationHelper.insertPointToDb(mDbHelper, deletedLatLng, pathCategory),
                deletedLatLng.latitude,
                deletedLatLng.longitude);
        this.toBeDeletedPoint = recoveredPoint;
        addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, removedPath.getStartLocation().getId(),
                recoveredPoint.getId(), pathCategory), removedPath.getStartLocation(), recoveredPoint);
//        removedPath = addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, toBeDeletedPoint.getId(),
//                recoveredPoint.getId(), pathCategory), toBeDeletedPoint, recoveredPoint);
    }

    public Path addPath(long pathId, Point startPosition,
                        Point endPosition){
        Path path = new Path(pathId,startPosition,
                endPosition
        );
        return path;
    }
    public void deletePoint(Point pointToDelete) {
        this.toBeDeletedPoint = pointToDelete;
        long pointToDeleteId = pointToDelete.getId();
        long idPathToBeDeleted = 0;
        for (Path path : paths){
            if(path.getStartLocation().getId() ==
                    pointToDeleteId){
                toBeDeletedPoint = null;
                this.message = "Anda tidak bisa menghapus node ini.";
                return;
            }

            if(path.getEndLocation().getId() ==
                    pointToDeleteId){
                idPathToBeDeleted = path.getId();
                this.removedPath = path;
            }
        }

        new deletePathTask().execute(new Long[]{idPathToBeDeleted});
        new deletePointTask().execute(new Long[]{pointToDeleteId});
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
