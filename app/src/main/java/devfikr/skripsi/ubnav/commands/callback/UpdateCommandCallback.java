package devfikr.skripsi.ubnav.commands.callback;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public interface UpdateCommandCallback {
    void updateCommandExecuteResult(Point updatedPoint, LatLng updatedLatLng);
    void updateCommandUndoResult(Point recoveredPoint, LatLng updatedLatLng);
}
