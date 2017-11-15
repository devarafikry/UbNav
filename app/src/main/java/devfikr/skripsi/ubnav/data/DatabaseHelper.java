package devfikr.skripsi.ubnav.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.android.gms.maps.model.LatLng;

import timber.log.Timber;

/**
 * Created by Fikry-PC on 11/11/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ubgraf.db";
    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_TABLE_GATES = String.format("CREATE TABLE %s"
                    +" (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s TEXT,%s INTEGER DEFAULT 1)",
            DatabaseContract.TABLE_GATE,
            DatabaseContract.GatesColumns._ID,
            DatabaseContract.GatesColumns.COLUMN_NAME,
            DatabaseContract.GatesColumns.COLUMN_OPEN_TIME,
            DatabaseContract.GatesColumns.COLUMN_CLOSE_TIME,
            DatabaseContract.GatesColumns.COLUMN_AVAILABLE
    );
    private static final String SQL_CREATE_TABLE_INTERCHANGES = String.format("CREATE TABLE %s"
                    +" (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s TEXT, %s INTEGER,%s INTEGER,%s TEXT,%s TEXT)",
            DatabaseContract.TABLE_INTERCHANGE,
            DatabaseContract.InterchangeColumns._ID,
            DatabaseContract.InterchangeColumns.COLUMN_NAME,
            DatabaseContract.InterchangeColumns.COLUMN_DESCRIPTION,
            DatabaseContract.InterchangeColumns.COLUMN_INTERCHANGE_CATEGORY,
            DatabaseContract.InterchangeColumns.COLUMN_AVAILABLE,
            DatabaseContract.InterchangeColumns.COLUMN_LAT,
            DatabaseContract.InterchangeColumns.COLUMN_LNG
    );
    private static final String SQL_CREATE_TABLE_POINTS = String.format("CREATE TABLE %s"
                    +" (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s DOUBLE, %s DOUBLE, %s INTEGER DEFAULT 0, " +
                    " FOREIGN KEY ("+ DatabaseContract.PointColumns.COLUMN_GATES_CATEGORY+")" +
                    " REFERENCES "+DatabaseContract.TABLE_GATE +"(_ID)" +
                    ")",
            DatabaseContract.TABLE_POINTS,
            DatabaseContract.PointColumns._ID,
            DatabaseContract.PointColumns.COLUMN_LAT,
            DatabaseContract.PointColumns.COLUMN_LNG,
            DatabaseContract.PointColumns.COLUMN_GATES_CATEGORY
    );
    private static final String SQL_CREATE_TABLE_PATHS = String.format("CREATE TABLE %s"
                    +" (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s INTEGER, %s INTEGER, %s INTEGER," +
                    " FOREIGN KEY ("+ DatabaseContract.PathColumns.COLUMN_START_POINT+")" +
                    " REFERENCES "+DatabaseContract.TABLE_POINTS +"(_ID)," +
                    " FOREIGN KEY ("+ DatabaseContract.PathColumns.COLUMN_END_POINT+")" +
                    " REFERENCES "+DatabaseContract.TABLE_POINTS +"(_ID))",
            DatabaseContract.TABLE_PATHS,
            DatabaseContract.PathColumns._ID,
            DatabaseContract.PathColumns.COLUMN_START_POINT,
            DatabaseContract.PathColumns.COLUMN_END_POINT,
            DatabaseContract.PathColumns.COLUMN_CATEGORY
    );


    private final Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Timber.d("GATES : "+ SQL_CREATE_TABLE_GATES);
        Timber.d("INTERCHANGES : "+ SQL_CREATE_TABLE_INTERCHANGES);
        Timber.d("POINTS : "+ SQL_CREATE_TABLE_POINTS);
        Timber.d("PATHS : "+ SQL_CREATE_TABLE_PATHS);

        db.execSQL(SQL_CREATE_TABLE_GATES);
        db.execSQL(SQL_CREATE_TABLE_INTERCHANGES);
        db.execSQL(SQL_CREATE_TABLE_POINTS);
        db.execSQL(SQL_CREATE_TABLE_PATHS);
        addGerbangVeteran(db);
//        loadDemoTask(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TABLE_POINTS);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TABLE_PATHS);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TABLE_GATE);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseContract.TABLE_INTERCHANGE);
        onCreate(db);
    }

    private void addGerbangVeteran(SQLiteDatabase db) {
        LatLng gerbangVeteranMasuk = new LatLng(-7.956213, 112.613298);
        ContentValues values = new ContentValues();
        values.put(DatabaseContract.PointColumns.COLUMN_LAT, gerbangVeteranMasuk.latitude);
        values.put(DatabaseContract.PointColumns.COLUMN_LNG, gerbangVeteranMasuk.longitude);
        values.put(DatabaseContract.PointColumns.COLUMN_GATES_CATEGORY, 1);
//        ContentValues values = new ContentValues();
//        values.put(TaskColumns.DESCRIPTION, mContext.getResources().getString(R.string.demo_task));
//        values.put(TaskColumns.IS_COMPLETE, 0);
//        values.put(TaskColumns.IS_PRIORITY, 1);
//        values.put(TaskColumns.DUE_DATE, Long.MAX_VALUE);

        db.insertOrThrow(DatabaseContract.TABLE_POINTS, null, values);
    }
}
