package devfikr.skripsi.ubnav.commands.helper;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;

import devfikr.skripsi.ubnav.data.DatabaseContract;
import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.Path;

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

    public static long updatePointToDb(DatabaseHelper mDbHelper, long id, LatLng latLng, int pathCategory){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.PointColumns.COLUMN_LAT,latLng.latitude);
        cv.put(DatabaseContract.PointColumns.COLUMN_LNG,latLng.longitude);
        cv.put(DatabaseContract.PointColumns.COLUMN_GATES_CATEGORY, 0);
        cv.put(DatabaseContract.PointColumns.COLUMN_PATH_CATEGORY, pathCategory);
        return db.update(DatabaseContract.TABLE_POINTS, cv, "_id="+id, null);
    }

    public static long insertPointToDb(DatabaseHelper mDbHelper, LatLng latLng, int pathCategory){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.PointColumns.COLUMN_LAT,latLng.latitude);
        cv.put(DatabaseContract.PointColumns.COLUMN_LNG,latLng.longitude);
        cv.put(DatabaseContract.PointColumns.COLUMN_GATES_CATEGORY, 0);
        cv.put(DatabaseContract.PointColumns.COLUMN_PATH_CATEGORY, pathCategory);
        return db.insert(DatabaseContract.TABLE_POINTS, null, cv);
    }

    public static long insertPathToDb(DatabaseHelper mDbHelper, long startPointId, long endPointId, int pathCategory){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.PathColumns.COLUMN_START_POINT,startPointId);
        cv.put(DatabaseContract.PathColumns.COLUMN_END_POINT,endPointId);
        cv.put(DatabaseContract.PathColumns.COLUMN_CATEGORY, pathCategory);
        return db.insert(DatabaseContract.TABLE_PATHS, null, cv);
    }

    public static void deleteAllResources(DatabaseHelper mDbHelper, int pathCategory){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(DatabaseContract.TABLE_POINTS,
                DatabaseContract.PointColumns.COLUMN_PATH_CATEGORY+"="+pathCategory,
                null);
        db.delete(DatabaseContract.TABLE_PATHS,
                DatabaseContract.PathColumns.COLUMN_CATEGORY+"="+pathCategory,
                null);

        LatLng gerbangVeteranMasuk = new LatLng(-7.956213, 112.613298);

        ContentValues values1 = new ContentValues();
        values1.put(DatabaseContract.PointColumns.COLUMN_LAT, gerbangVeteranMasuk.latitude);
        values1.put(DatabaseContract.PointColumns.COLUMN_LNG, gerbangVeteranMasuk.longitude);
        values1.put(DatabaseContract.PointColumns.COLUMN_GATES_CATEGORY, 1);
        values1.put(DatabaseContract.PointColumns.COLUMN_PATH_CATEGORY, pathCategory);
        db.insertOrThrow(DatabaseContract.TABLE_POINTS, null, values1);
    }
}
