package devfikr.skripsi.ubnav.commands.command;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.commands.Command;
import devfikr.skripsi.ubnav.commands.callback.AddOnePointCommandCallback;
import devfikr.skripsi.ubnav.commands.callback.AddPointCommandCallback;
import devfikr.skripsi.ubnav.commands.helper.DatabaseOperationHelper;
import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;
import devfikr.skripsi.ubnav.util.SnackbarUtil;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public class AddOnePointCommand implements Command {
    private DatabaseHelper mDbHelper;
    private LatLng latLng;
    private Point addedPoint;
    private AddOnePointCommandCallback addOnePointCommandCallback;
    private int pathCategory;

    @Override
    public void execute() {
        addPoint(latLng);
        addOnePointCommandCallback.addOnePointCommandExecuteResult(addedPoint);
    }

    @Override
    public void undo() {
        deletePoint(addedPoint);
        addOnePointCommandCallback.addOnePointCommandUndoResult(addedPoint);
    }

    @Override
    public void redo() {
        addPoint(latLng);
        addOnePointCommandCallback.addOnePointCommandExecuteResult(addedPoint);
    }

    public AddOnePointCommand(
            AddOnePointCommandCallback addOnePointCommandCallback,
            DatabaseHelper mDbHelper,
            LatLng latLng,
            int pathCategory) {
        this.mDbHelper = mDbHelper;
        this.latLng = latLng;
        this.addOnePointCommandCallback = addOnePointCommandCallback;
        this.pathCategory = pathCategory;
    }

    private void addPoint(LatLng latLng){
        Point addedPoint = new Point(DatabaseOperationHelper.insertPointToDb(mDbHelper, latLng, pathCategory), latLng.latitude, latLng.longitude);
        this.addedPoint = addedPoint;
    }
    public void deletePoint(Point pointToDelete) {
        long id = pointToDelete.getId();
        new deletePointTask().execute(new Long[]{id});
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
