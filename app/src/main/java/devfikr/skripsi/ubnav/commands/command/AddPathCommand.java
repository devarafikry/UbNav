package devfikr.skripsi.ubnav.commands.command;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.commands.Command;
import devfikr.skripsi.ubnav.commands.helper.DatabaseOperationHelper;
import devfikr.skripsi.ubnav.commands.callback.AddPathCommandCallback;
import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;

/**
 * Created by Fikry-PC on 12/21/2017.
 */

public class AddPathCommand implements Command {
    private Point currentPoint;
    private Point toJoinPoint;
    private DatabaseHelper mDbHelper;
    private Path addedPath;
    private AddPathCommandCallback addPathCommand;
    private int pathCategory;

    @Override
    public void execute() {
        addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, currentPoint.getId(),
                toJoinPoint.getId(), pathCategory), currentPoint, toJoinPoint);
        addPathCommand.addPathCommandExecuteResult(addedPath, toJoinPoint.getId());
    }

    @Override
    public void undo() {
        deletePath(addedPath);
        addPathCommand.addPathCommandUndoResult(addedPath, toJoinPoint.getId());
    }

    @Override
    public void redo() {
        addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, currentPoint.getId(),
                toJoinPoint.getId(), pathCategory), currentPoint, toJoinPoint);
        addPathCommand.addPathCommandExecuteResult(addedPath, toJoinPoint.getId());
    }


    public AddPathCommand(
            AddPathCommandCallback addPathCommand,
            DatabaseHelper mDbHelper,
            Point currentPoint,
            Point toJoinPoint,
            int pathCategory) {
        this.mDbHelper = mDbHelper;
        this.toJoinPoint = toJoinPoint;
        this.currentPoint = currentPoint;
        this.addPathCommand = addPathCommand;
        this.pathCategory = pathCategory;
    }

    public void deletePath(Path pathToDelete) {
        long id = pathToDelete.getId();
        new deletePathTask().execute(new Long[]{id});
    }

    public void addPath(long pathId, Point startPosition,
                        Point endPosition){
        Path path = new Path(pathId,startPosition,
                endPosition
        );
        addedPath = path;
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
