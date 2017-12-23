package devfikr.skripsi.ubnav.data;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Fikry-PC on 11/11/2017.
 */

public class DatabaseContract {
    public static final String TABLE_POINTS = "points";
    public static final String TABLE_PATHS = "paths";
    public static final String TABLE_GATE = "gates";
    public static final String TABLE_INTERCHANGE = "interchanges";

    public static final class PointColumns implements BaseColumns {
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";
        public static final String COLUMN_PATH_CATEGORY = "pathCategory";
        public static final String COLUMN_GATES_CATEGORY = "category";
    }
    public static final class PathColumns implements BaseColumns {
        public static final String COLUMN_START_POINT = "startPoint";
        public static final String COLUMN_END_POINT = "endPoint";
        public static final String COLUMN_CATEGORY = "category";
//
//        public static final String COLUMN_START_POINT_ID = "startPointId";
//        public static final String COLUMN_START_POINT_LAT = "startPointLat";
//        public static final String COLUMN_START_POINT_LNG = "startPointLng";
//        public static final String COLUMN_START_POINT_CATEGORY = "startPointCategory";
//
//        public static final String COLUMN_END_POINT_ID = "endPointId";
//        public static final String COLUMN_END_POINT_LAT = "endPointLat";
//        public static final String COLUMN_END_POINT_LNG = "endPointLng";
//        public static final String COLUMN_END_POINT_CATEGORY = "endPointCategory";

        public static final int CATEGORY_WALKING = 0;
        public static final int CATEGORY_MOTORCYCLE = 1;
        public static final int CATEGORY_CAR = 2;
    }
    public static final class GatesColumns implements BaseColumns {
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_OPEN_TIME = "open_time";
        public static final String COLUMN_CLOSE_TIME = "close_time";
        public static final String COLUMN_AVAILABLE = "available";
    }
    public static final class InterchangeColumns implements BaseColumns {
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_INTERCHANGE_CATEGORY = "category";
        public static final String COLUMN_AVAILABLE = "available";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";

        public static final int CATEGORY_MOTORCYCLE = 0;
        public static final int CATEGORY_CAR = 1;
        public static final int CATEGORY_MOTORCYCLE_AND_CAR = 2;
    }

    public static final String CONTENT_AUTHORITY = "devfikr.skripsi.ubnav";

    public static final Uri CONTENT_URI_POINTS = new Uri.Builder().scheme("content")
            .authority(CONTENT_AUTHORITY)
            .appendPath(TABLE_POINTS)
            .build();
    public static final Uri CONTENT_URI_PATHS = new Uri.Builder().scheme("content")
            .authority(CONTENT_AUTHORITY)
            .appendPath(TABLE_PATHS)
            .build();
    public static final Uri CONTENT_URI_GATES = new Uri.Builder().scheme("content")
            .authority(CONTENT_AUTHORITY)
            .appendPath(TABLE_GATE)
            .build();
    public static final Uri CONTENT_URI_INTERCHANGES = new Uri.Builder().scheme("content")
            .authority(CONTENT_AUTHORITY)
            .appendPath(TABLE_INTERCHANGE)
            .build();
    /* Helpers to retrieve column values */
    public static String getColumnString(Cursor cursor, String columnName) {
        return cursor.getString( cursor.getColumnIndex(columnName) );
    }

    public static int getColumnInt(Cursor cursor, String columnName) {
        return cursor.getInt( cursor.getColumnIndex(columnName) );
    }

    public static long getColumnLong(Cursor cursor, String columnName) {
        return cursor.getLong( cursor.getColumnIndex(columnName) );
    }
    public static double getColumnDouble(Cursor cursor, String columnName) {
        return cursor.getDouble( cursor.getColumnIndex(columnName) );
    }
}
