package devfikr.skripsi.ubnav.commands.command;

import android.support.design.widget.Snackbar;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.commands.Command;
import devfikr.skripsi.ubnav.commands.helper.DatabaseOperationHelper;
import devfikr.skripsi.ubnav.commands.callback.UpdateCommandCallback;
import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public class UpdatePointCommand implements Command {
    private Point selectedPoint;
    private DatabaseHelper mDbHelper;
    private UpdateCommandCallback updateCommandCallback;
    private LatLng latLngEnd;
    private LatLng latLngStart;
    private Point draggedPoint;
    private int pathCategory;

    public UpdatePointCommand(
            UpdateCommandCallback updateCommandCallback,
            DatabaseHelper mDbHelper,
            Point selectedPoint,
            LatLng latLngStart,
            LatLng latLngEnd,
            int pathCategory) {
        this.mDbHelper = mDbHelper;
        this.selectedPoint = selectedPoint;
        this.updateCommandCallback = updateCommandCallback;
        this.latLngEnd = latLngEnd;
        this.latLngStart = latLngStart;
        this.pathCategory = pathCategory;
    }

    @Override
    public void execute() {
        updatePoint(selectedPoint.getId(), latLngEnd);
        updateCommandCallback.updateCommandExecuteResult(draggedPoint, latLngEnd);
    }

    @Override
    public void undo() {
        updatePoint(selectedPoint.getId(), latLngStart);
        updateCommandCallback.updateCommandUndoResult(draggedPoint, latLngStart);
    }

    @Override
    public void redo() {
        updatePoint(selectedPoint.getId(), latLngEnd);
        updateCommandCallback.updateCommandExecuteResult(draggedPoint, latLngEnd);
    }

    private void updatePoint(long selectedPointId, LatLng position) {
        Point updatedPoint = new Point(selectedPointId, position.latitude, position.longitude);
        draggedPoint = updatedPoint;
        DatabaseOperationHelper.updatePointToDb(mDbHelper, selectedPointId, position, pathCategory);
    }
}
