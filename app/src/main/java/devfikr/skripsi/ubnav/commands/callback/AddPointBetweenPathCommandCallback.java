package devfikr.skripsi.ubnav.commands.callback;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public interface AddPointBetweenPathCommandCallback {
    void addPointBetweenPathExecuteResult(Path removedPath, Path addedPath1, Path addedPath2, Point pointInBetween);
    void addPointBetweenPathUndoResult(Path recoveredPath, Path deletedPath1, Path deletedPath2, Point deletedPointInBetween);
    void addPointBetweenPathRedoResult(Path removedPath, Path addedPath1, Path addedPath2, Point pointInBetween);
}
