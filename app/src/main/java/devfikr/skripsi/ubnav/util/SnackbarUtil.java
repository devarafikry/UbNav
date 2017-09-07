package devfikr.skripsi.ubnav.util;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by Fikry-PC on 9/5/2017.
 */

public class SnackbarUtil {
    public static void showSnackBar(View view, Snackbar snackbar, String message, int duration){
        if(snackbar != null){
            snackbar.dismiss();
        }
        snackbar = Snackbar.make(view, message, duration);
        snackbar.show();
    }
}
