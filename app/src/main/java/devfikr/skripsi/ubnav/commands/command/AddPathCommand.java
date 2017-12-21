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
 * Created by Fikry-PC on 12/21/2017.
 */

public class AddPathCommand implements Command {
    private Point selectedPoint;
    private DatabaseHelper mDbHelper;
    private ArrayList<Point> points;
    private ArrayList<Path> paths;
    private Path addedPath;
    private Point toJoinPoint;
    private AddPathCommandCallback addPathCommand;
    private View root_map;
    private Snackbar s;
    private int pathCategory;

    @Override
    public void execute() {
        addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, selectedPoint.getId(),
                toJoinPoint.getId(), pathCategory), selectedPoint, toJoinPoint);
        addPathCommand.addPathCommandResult(paths, toJoinPoint.getId());
    }

    @Override
    public void undo() {
        deletePath(addedPath);
        addPathCommand.addPathCommandResult(paths, toJoinPoint.getId());
    }

    @Override
    public void redo() {
        addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, selectedPoint.getId(),
                toJoinPoint.getId(), pathCategory), selectedPoint, toJoinPoint);
        addPathCommand.addPathCommandResult(paths, toJoinPoint.getId());
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

    public AddPathCommand(View view, Snackbar s, AddPathCommandCallback addPathCommand, DatabaseHelper mDbHelper, ArrayList<Path> paths, Point selectedPoint, Point toJoinPoint, int pathCategory) {
        this.mDbHelper = mDbHelper;
        this.paths = paths;
        this.toJoinPoint = toJoinPoint;
        this.selectedPoint = selectedPoint;
        this.addPathCommand = addPathCommand;
        this.root_map = view;
        this.s = s;
        this.pathCategory = pathCategory;
    }

    public void deletePath(Path pathToDelete) {
        long id = pathToDelete.getId();
        paths.remove(pathToDelete);
        new deletePathTask().execute(new Long[]{id});
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
}
