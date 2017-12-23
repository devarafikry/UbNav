package devfikr.skripsi.ubnav.commands.callback;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public interface AddPointCommandCallback {
    void addCommandExecuteResult(Point addedPoint, Path addedPath);
    void addCommandUndoResult(Point removedPoint, Path removedPath);
}
