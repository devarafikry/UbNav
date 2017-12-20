package devfikr.skripsi.ubnav;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;

import devfikr.skripsi.ubnav.data.DatabaseContract;
import devfikr.skripsi.ubnav.data.DatabaseHelper;

/**
 * Created by Fikry-PC on 11/16/2017.
 */

public class DatabaseOperationHelper {
    public static long deletePathFromDb(DatabaseHelper mDbHelper, long id){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(DatabaseContract.TABLE_PATHS, "_id="+id, null);
    }

    public static long deletePointFromDb(DatabaseHelper mDbHelper, long id){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(DatabaseContract.TABLE_POINTS, "_id="+id, null);
    }

    public static long updatePointToDb(DatabaseHelper mDbHelper, long id, LatLng latLng){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.PointColumns.COLUMN_LAT,latLng.latitude);
        cv.put(DatabaseContract.PointColumns.COLUMN_LNG,latLng.longitude);
        cv.put(DatabaseContract.PointColumns.COLUMN_GATES_CATEGORY, 0);
        return db.update(DatabaseContract.TABLE_POINTS, cv, "_id="+id, null);
    }

    public static long insertPointToDb(DatabaseHelper mDbHelper, LatLng latLng){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.PointColumns.COLUMN_LAT,latLng.latitude);
        cv.put(DatabaseContract.PointColumns.COLUMN_LNG,latLng.longitude);
        cv.put(DatabaseContract.PointColumns.COLUMN_GATES_CATEGORY, 0);
        return db.insert(DatabaseContract.TABLE_POINTS, null, cv);
    }

    public static long insertPathToDb(DatabaseHelper mDbHelper, long startPointId, long endPointId){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.PathColumns.COLUMN_START_POINT,startPointId);
        cv.put(DatabaseContract.PathColumns.COLUMN_END_POINT,endPointId);
        cv.put(DatabaseContract.PathColumns.COLUMN_CATEGORY, 0);
        return db.insert(DatabaseContract.TABLE_PATHS, null, cv);
    }

}
