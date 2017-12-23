package devfikr.skripsi.ubnav.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Fikry-PC on 11/11/2017.
 */

public class DatabaseProvider extends ContentProvider{
    private static final int POINTS = 100;
    private static final int POINTS_WITH_CATEGORY_ID = 101;

    private static final int PATHS_WITH_CATEGORY_ID = 200;
    private static final int PATHS_WITH_ID = 201;

    private static final int GATES = 300;
    private static final int GATES_WITH_ID = 301;

    private static final int INTERCHANGES = 400;
    private static final int INTERCHANGES_WITH_ID = 401;

    private DatabaseHelper mDbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TABLE_POINTS,
                POINTS);
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TABLE_POINTS + "/#",
                POINTS_WITH_CATEGORY_ID);

//        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
//                DatabaseContract.TABLE_PATHS,
//                PATHS);
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TABLE_PATHS + "/#",
                PATHS_WITH_CATEGORY_ID);

//        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
//                DatabaseContract.TABLE_GATE,
//                GATES);
//        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
//                DatabaseContract.TABLE_GATE+ "/#",
//                GATES_WITH_ID);
//
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TABLE_INTERCHANGE,
                INTERCHANGES);
//        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
//                DatabaseContract.TABLE_INTERCHANGE+ "/#",
//                INTERCHANGES_WITH_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        int match = sUriMatcher.match(uri);

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor retCursor;

        switch (match){
            case PATHS_WITH_CATEGORY_ID:
//                SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
//                builder.setTables(
//                        DatabaseContract.TABLE_PATHS+" JOIN "+DatabaseContract.TABLE_POINTS+
//                                " ON "+DatabaseContract.TABLE_PATHS+"."+
//                                DatabaseContract.PathColumns.C
//
//                );
                String path_category_id = uri.getPathSegments().get(1);

                String mSelection = DatabaseContract.PathColumns.COLUMN_CATEGORY+"=?";
                String mSelectionArgs[] = {path_category_id};

                //COMPLEX QUERYYYYYYYY
//                Cursor cursor = db.rawQuery(
//                        "SELECT pa._id " +
//                                ",p._id AS "+ PathColumns.COLUMN_START_POINT_ID+
//                                ",p.lat AS "+ PathColumns.COLUMN_START_POINT_LAT+
//                                ",p.lng AS "+ PathColumns.COLUMN_START_POINT_LNG+
//                                ",p.category AS "+ PathColumns.COLUMN_START_POINT_CATEGORY+
//                                ",p2._id AS "+ PathColumns.COLUMN_END_POINT_ID+
//                                ",p2.lat AS "+ PathColumns.COLUMN_END_POINT_LAT+
//                                ",p2.lng AS "+ PathColumns.COLUMN_END_POINT_LNG+
//                                ",p2.category AS "+ PathColumns.COLUMN_END_POINT_CATEGORY+
//                                "FROM paths pa" +
//                                "JOIN points p ON p._id = pa.startPoint" +
//                                "JOIN points p2 ON p2._id = pa.endPoint" +
//                                "WHERE pa.category = ?", mSelectionArgs);

                Cursor cursor = db.query(
                        DatabaseContract.TABLE_PATHS,
                        null,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        null
                );
                cursor.setNotificationUri(getContext().getContentResolver(), uri);
                return cursor;
            case INTERCHANGES:
                Cursor cursorInterchanges = db.query(
                        DatabaseContract.TABLE_INTERCHANGE,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                return cursorInterchanges;
            case POINTS:
                Cursor cursor1 = db.query(
                        DatabaseContract.TABLE_POINTS,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                return cursor1;
            case POINTS_WITH_CATEGORY_ID:
                String point_category_id = uri.getPathSegments().get(1);

                String mSelectionP = DatabaseContract.PointColumns.COLUMN_PATH_CATEGORY+"=?";
                String mSelectionArgsP[] = {point_category_id};

                Cursor cursorP = db.query(
                        DatabaseContract.TABLE_POINTS,
                        null,
                        mSelectionP,
                        mSelectionArgsP,
                        null,
                        null,
                        null
                );
                cursorP.setNotificationUri(getContext().getContentResolver(), uri);
                return cursorP;
            default:
                throw new UnsupportedOperationException("Unknown Uri");
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
//        int match = sUriMatcher.match(uri);
//
//        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
//        switch (match){
//            case PATHS_WITH_CATEGORY_ID:
//                String path_category_id = uri.getPathSegments().get(1);
//
//                break;
//        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
