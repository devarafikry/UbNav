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
    private Point selectedPoint;
    private DatabaseHelper mDbHelper;
    private ArrayList<Point> points;
    private ArrayList<Path> paths;
    private LatLng latLng;
    private Point addedPoint;
    private AddPointBetweenPathCommandCallback addPointBetweenPathCommandCallback;
    private View root_map;
    private Snackbar s;
    private Path selectedPath;
    private Path createdPath1;
    private Path createdPath2;
    private int pathCategory;
    private int inOutCategory;

    public AddPointBetweenPathCommand(View view, Snackbar s, AddPointBetweenPathCommandCallback addPointBetweenPathCommandCallback, DatabaseHelper mDbHelper, ArrayList<Path> paths, ArrayList<Point> points, Point selectedPoint, Path selectedPath, LatLng latLng, int pathCategory, int inOutCategory) {
        this.mDbHelper = mDbHelper;
        this.points = points;
        this.paths = paths;
        this.selectedPoint = selectedPoint;
        this.addPointBetweenPathCommandCallback = addPointBetweenPathCommandCallback;
        this.root_map = view;
        this.s = s;
        this.selectedPath = selectedPath;
        this.latLng = latLng;
        this.pathCategory = pathCategory;
        this.inOutCategory = inOutCategory;
    }

    @Override
    public void execute() {
        addPointBetweenPath(selectedPath, latLng);
        addPointBetweenPathCommandCallback.addPointBetweenPathResult(points, paths, selectedPoint);
    }

    @Override
    public void undo() {
        new deletePathTask().execute(new Long[]{createdPath1.getId()});
        new deletePathTask().execute(new Long[]{createdPath2.getId()});
        paths.remove(createdPath1);
        paths.remove(createdPath2);
        long addedPathId = DatabaseOperationHelper.insertPathToDb(mDbHelper, selectedPath.getStartLocation().getId(),
                selectedPath.getEndLocation().getId(), pathCategory, inOutCategory);
        addPath(addedPathId,
                selectedPath.getStartLocation(), selectedPath.getEndLocation());
        for (Path path : paths){
            if (path.getId() == addedPathId){
                selectedPath = path;
            }
        }
        addPointBetweenPathCommandCallback.addPointBetweenPathResult(points, paths, selectedPoint);
    }

    @Override
    public void redo() {
        addPointBetweenPath(selectedPath, latLng);
        addPointBetweenPathCommandCallback.addPointBetweenPathResult(points, paths, selectedPoint);
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
        Point addedPoint = new Point(DatabaseOperationHelper.insertPointToDb(mDbHelper, latLng, pathCategory, inOutCategory), latLng.latitude, latLng.longitude);
        points.add(addedPoint);
        this.addedPoint = addedPoint;
    }
    public void addPath(long pathId, Point startPosition,
                        Point endPosition){
        Path path = new Path(pathId,startPosition,
                endPosition
        );
        paths.add(path);
    }
    public void addPointBetweenPath(Path selectedPath, LatLng latLng){
        long id = selectedPath.getId();
//        for (Path path : paths){
//            if (path.getId() == id){
//                selectedPath = path;
//                paths.remove(path);
//                break;
//            }
//        }
        paths.remove(selectedPath);
//        deletePathFromDb(mDbHelper, id);
        new deletePathTask().execute(new Long[]{id});
        addPoint(points, latLng);
        Point addedPoint = points.get(points.size()-1);

//        //insert path 1
        addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, selectedPath.getStartLocation().getId(),
                addedPoint.getId(), pathCategory, inOutCategory),
                selectedPath.getStartLocation(), addedPoint);
        createdPath1 = paths.get(paths.size()-1);
//        //insert path 2
        addPath(DatabaseOperationHelper.insertPathToDb(mDbHelper, addedPoint.getId(),
                selectedPath.getEndLocation().getId(), pathCategory, inOutCategory),
                addedPoint, selectedPath.getEndLocation());
        createdPath2 = paths.get(paths.size()-1);
        selectedPoint = addedPoint;
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
