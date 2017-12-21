package devfikr.skripsi.ubnav.commands.callback;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.model.Path;

/**
 * Created by Fikry-PC on 12/21/2017.
 */

public interface AddPathCommandCallback {
    void addPathCommandResult(ArrayList<Path> paths, long pointToId);
}
