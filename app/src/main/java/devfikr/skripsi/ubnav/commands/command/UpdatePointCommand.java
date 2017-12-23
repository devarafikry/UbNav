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
    private Point lastPointAfterDelete;
    private DatabaseHelper mDbHelper;
    private ArrayList<Point> points;
    private ArrayList<Path> paths;
    private UpdateCommandCallback updateCommandCallback;
    private View root_map;
    private Snackbar s;
    private LatLng latLngEnd;
    private LatLng latLngStart;
    private Point draggedPoint;
    private int pathCategory;

    public UpdatePointCommand(View view, Snackbar s, UpdateCommandCallback updateCommandCallback, DatabaseHelper mDbHelper, ArrayList<Path> paths, ArrayList<Point> points, Point selectedPoint,LatLng latLngStart, LatLng latLngEnd, int pathCategory) {
        this.mDbHelper = mDbHelper;
        this.points = points;
        this.paths = paths;
        this.selectedPoint = selectedPoint;
        this.updateCommandCallback = updateCommandCallback;
        this.root_map = view;
        this.s = s;
        this.latLngEnd = latLngEnd;
        this.latLngStart = latLngStart;
        this.pathCategory = pathCategory;
    }

    @Override
    public void execute() {
        updatePoint(selectedPoint.getId(), latLngEnd);
        updateCommandCallback.updateCommandResult(points, paths, draggedPoint, draggedPoint);
    }

    @Override
    public void undo() {
        updatePoint(selectedPoint.getId(), latLngStart);
        updateCommandCallback.updateCommandResult(points, paths, draggedPoint, draggedPoint);
    }

    @Override
    public void redo() {
        updatePoint(selectedPoint.getId(), latLngEnd);
        updateCommandCallback.updateCommandResult(points, paths, selectedPoint, draggedPoint);
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

    private void updatePoint(long selectedPointId, LatLng position) {
        Point updatedPoint = new Point(selectedPointId, position.latitude, position.longitude);
        draggedPoint = updatedPoint;
        DatabaseOperationHelper.updatePointToDb(mDbHelper, selectedPointId, position, pathCategory);
        for (int i =0;i<points.size();i++){
            if (points.get(i).getId() == selectedPointId){
                points.set(i, updatedPoint);
            }
        }
    }
}
