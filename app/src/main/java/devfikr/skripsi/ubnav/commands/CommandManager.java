package devfikr.skripsi.ubnav.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public class CommandManager {
    private List<Command> commands = Collections.emptyList();
    private int nextPointer = 0;

    private Point selectedPoint;
    private DatabaseHelper mDbHelper;


    private ArrayList<Point> points;
    private ArrayList<Path> paths;

    private void updateValue(ArrayList<Point> points, ArrayList<Path> paths, Point selectedPoint){
        this.selectedPoint = selectedPoint;
        this.points = points;
        this. paths = paths;
    }

    public void doCommand(Command command){
        List<Command> newList = new ArrayList<>(nextPointer + 1);

        for(int k = 0; k < nextPointer; k++) {
            newList.add(commands.get(k));
        }

        newList.add(command);

        commands = newList;
        nextPointer++;

        // Do the command here, or return it to whatever called this to be done, or maybe it has already been done by now or something
        // (I can only guess on what your code currently looks like...)
        command.execute();
        updateValue(command.getPoints(), command.getPaths(), command.getSelectedPosition());
    }

    public boolean canUndo() {
        return nextPointer > 0;
    }

    public void undo() {
        if(canUndo()) {
            nextPointer--;
            Command commandToUndo = commands.get(nextPointer);
            // Undo the command, or return it to whatever called this to be undone, or something
            commandToUndo.undo();
        } else {
//            throw new IllegalStateException("Cannot undo");
        }
    }

    public boolean canRedo() {
        return nextPointer < commands.size();
    }

    public void redo() {
        Command commandToDo;
        if(canRedo()) {
            commandToDo = commands.get(nextPointer);
            nextPointer++;
            // Do the command, or return it to whatever called this to be re-done, or something
            commandToDo.execute();
        } else {
//            throw new IllegalStateException("Cannot redo");
        }
    }


    public Point getSelectedPoint() {
        return selectedPoint;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public ArrayList<Path> getPaths() {
        return paths;
    }
}
