package devfikr.skripsi.ubnav.commands;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public interface Command {
    void execute();
    void undo();
    void redo();

    ArrayList<Path> getPaths();
    ArrayList<Point> getPoints();
    Point getSelectedPosition();
}
