package devfikr.skripsi.ubnav.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.codekidlabs.storagechooser.StorageChooser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import devfikr.skripsi.ubnav.R;
import devfikr.skripsi.ubnav.data.DatabaseContract;
import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.util.SnackbarUtil;
import timber.log.Timber;

public class EditActivity extends AppCompatActivity {

    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 88;
    Snackbar s;
    @BindView(R.id.root_view) View root_view;
    @BindView(R.id.btn_edit_walk)
    ImageView btn_edit_walk;
    @BindView(R.id.btn_edit_motor) ImageView btn_edit_motorcycle;
    @BindView(R.id.btn_import_points) Button btn_import_points;
    @BindView(R.id.btn_import_paths) Button btn_import_paths;
    @BindView(R.id.btn_import_interchanges) Button btn_import_interchanges;
    @BindView(R.id.btn_export) Button btn_export;
    private final int PICKFILE_REQUEST_CODE = 99;
    private final int WRITE_STORAGE_PERMISSION_REQUEST_CODE = 77;

    //    @BindView(R.id.btn_edit_car) Button btn_edit_car;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("Pilih Menu Graf");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_import_interchanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importDatabaseInterchanges();
            }
        });

        btn_import_points.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importDataPoints();
            }
        });

        btn_import_paths.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                importDataPaths();
            }
        });

        btn_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportData();
            }
        });
    }

    public void showSnackBar(View view, String message, int duration, int warning){
        if(s != null){
            s.dismiss();
        }
        s = Snackbar.make(view, message, duration);
        s.setActionTextColor(getResources().getColor(android.R.color.white));
        if(warning == 1){
            View sbView = s.getView();
            sbView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        } else{
            View sbView = s.getView();
            sbView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
        }
        s.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case READ_STORAGE_PERMISSION_REQUEST_CODE:
                try {
                    requestPermissionForWriteExtertalStorage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public boolean checkPermissionForReadExtertalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    public void requestPermissionForReadExtertalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    public void requestPermissionForWriteExtertalStorage() throws Exception {
        try {
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_STORAGE_PERMISSION_REQUEST_CODE);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void exportData() {
        if(!checkPermissionForReadExtertalStorage()){
            try {
                requestPermissionForReadExtertalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(EditActivity.this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .build();

        chooser.show();

        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                Timber.d(path);
                exportAllDataToJson(path);
            }
        });
    }

    private void exportAllDataToJson(String path){
        createFilePoints(path);
        createFilePaths(path);
        createFileInterchanges(path);
        showSnackBar(root_view, "Data has been saved.", Snackbar.LENGTH_LONG, 0);
    }

    private void createFilePoints(String path){
        File file = new File(path+"/"+getString(R.string.points_json_name)+ ".json");
        try {
            Writer output = null;
            String dbPath = getDatabasePath(DatabaseHelper.DATABASE_NAME).toString();// Set path to your database
            output = new BufferedWriter(new FileWriter(file));
            JSONObject object = new JSONObject();
            String dbTable = DatabaseContract.TABLE_POINTS;//Set name of your table
            try {
                object.put(getString(R.string.points_json_name), getJSONArrayFromDb(dbPath, dbTable));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            output.write(object.toString());
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFilePaths(String path){
        File file = new File(path+"/"+getString(R.string.paths_json_name)+ ".json");
        try {
            Writer output = null;
            String dbPath = getDatabasePath(DatabaseHelper.DATABASE_NAME).toString();// Set path to your database
            output = new BufferedWriter(new FileWriter(file));
            JSONObject object = new JSONObject();
            String dbTable = DatabaseContract.TABLE_PATHS;//Set name of your table
            try {
                object.put(getString(R.string.paths_json_name), getJSONArrayFromDb(dbPath, dbTable));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            output.write(object.toString());
            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createFileInterchanges(String path){
        File file = new File(path+"/"+getString(R.string.interchange_json_name)+ ".json");
        try {
            Writer output = null;
            String dbPath = getDatabasePath(DatabaseHelper.DATABASE_NAME).toString();// Set path to your database
            output = new BufferedWriter(new FileWriter(file));
            JSONObject object = new JSONObject();
            String dbTable = DatabaseContract.TABLE_INTERCHANGE;//Set name of your table
            try {
                object.put(getString(R.string.interchange_json_name), getJSONArrayFromDb(dbPath, dbTable));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            output.write(object.toString());
            output.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void importDataPoints() {
        if(!checkPermissionForReadExtertalStorage()){
            try {
                requestPermissionForReadExtertalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(EditActivity.this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.FILE_PICKER)
                .build();

        chooser.show();

        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                Timber.d(path);
                importDatabasePoints(getJSONObjectFromFile(path));
            }
        });
    }

    private void importDataPaths() {
        if(!checkPermissionForReadExtertalStorage()){
            try {
                requestPermissionForReadExtertalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(EditActivity.this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.FILE_PICKER)
                .build();

        chooser.show();

        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                Timber.d(path);
                importDatabasePaths(getJSONObjectFromFile(path));
            }
        });
    }

    private void importDatabaseInterchanges() {
        if(!checkPermissionForReadExtertalStorage()){
            try {
                requestPermissionForReadExtertalStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(EditActivity.this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.FILE_PICKER)
                .build();

        chooser.show();

        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                Timber.d(path);
                importDatabaseInterchanges(getJSONObjectFromFile(path));
            }
        });
    }

    private JSONObject getJSONObjectFromFile(String path){
        File yourFile = new File(path);
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(yourFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String jsonStr = null;
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

            jsonStr = Charset.defaultCharset().decode(bb).toString();
            JSONObject object = new JSONObject(jsonStr);
            return object;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void importDatabasePoints(JSONObject pointsJSON){
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(DatabaseContract.TABLE_POINTS, null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DatabaseContract.TABLE_POINTS + "'");

        try {
            JSONArray pointsJSONArr = pointsJSON.getJSONArray(getString(R.string.points_json_name));
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                for (int i =0; i<pointsJSONArr.length() ; i++) {
                    JSONObject pointObj = pointsJSONArr.getJSONObject(i);
                    values.put(DatabaseContract.PointColumns._ID, pointObj.getString(DatabaseContract.PointColumns._ID));
                    values.put(DatabaseContract.PointColumns.COLUMN_LAT, pointObj.getString(DatabaseContract.PointColumns.COLUMN_LAT));
                    values.put(DatabaseContract.PointColumns.COLUMN_LNG, pointObj.getString(DatabaseContract.PointColumns.COLUMN_LNG));
                    values.put(DatabaseContract.PointColumns.COLUMN_GATES_CATEGORY, pointObj.getString(DatabaseContract.PointColumns.COLUMN_GATES_CATEGORY));
                    values.put(DatabaseContract.PointColumns.COLUMN_PATH_CATEGORY, pointObj.getString(DatabaseContract.PointColumns.COLUMN_PATH_CATEGORY));
                    db.insert(DatabaseContract.TABLE_POINTS, null, values);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                showSnackBar(root_view, "Points imported.", Snackbar.LENGTH_LONG, 0);
            }
        } catch (JSONException e) {
            showSnackBar(root_view, "Points import failed.", Snackbar.LENGTH_LONG, 1);
            e.printStackTrace();
        }
    }

    private void importDatabasePaths(JSONObject pathsJSON){
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(DatabaseContract.TABLE_PATHS, null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DatabaseContract.TABLE_PATHS + "'");

        try {
            JSONArray pathsJsonArr = pathsJSON.getJSONArray(getString(R.string.paths_json_name));
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                for (int i =0; i<pathsJsonArr.length() ; i++) {
                    JSONObject pathsObj = pathsJsonArr.getJSONObject(i);
                    values.put(DatabaseContract.PathColumns._ID, pathsObj.getString(DatabaseContract.PathColumns._ID));
                    values.put(DatabaseContract.PathColumns.COLUMN_START_POINT, pathsObj.getString(DatabaseContract.PathColumns.COLUMN_START_POINT));
                    values.put(DatabaseContract.PathColumns.COLUMN_END_POINT, pathsObj.getString(DatabaseContract.PathColumns.COLUMN_END_POINT));
                    values.put(DatabaseContract.PathColumns.COLUMN_CATEGORY, pathsObj.getString(DatabaseContract.PathColumns.COLUMN_CATEGORY));
                    db.insert(DatabaseContract.TABLE_PATHS, null, values);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                showSnackBar(root_view, "Paths imported.", Snackbar.LENGTH_LONG, 0);
            }
        } catch (JSONException e) {
            showSnackBar(root_view, "Paths import failed.", Snackbar.LENGTH_LONG, 1);
            e.printStackTrace();
        }
    }

    private void importDatabaseInterchanges(JSONObject interchangesJSON){
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        db.delete(DatabaseContract.TABLE_INTERCHANGE, null, null);
        db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DatabaseContract.TABLE_INTERCHANGE + "'");

        try {
            JSONArray interchangeJSONArr = interchangesJSON.getJSONArray(getString(R.string.interchange_json_name));
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                for (int i =0; i<interchangeJSONArr.length() ; i++) {
                    JSONObject interchangeObj = interchangeJSONArr.getJSONObject(i);
                    values.put(DatabaseContract.InterchangeColumns._ID, interchangeObj.getString(DatabaseContract.InterchangeColumns._ID));
                    values.put(DatabaseContract.InterchangeColumns.COLUMN_NAME, interchangeObj.getString(DatabaseContract.InterchangeColumns.COLUMN_NAME));
                    values.put(DatabaseContract.InterchangeColumns.COLUMN_DESCRIPTION, interchangeObj.getString(DatabaseContract.InterchangeColumns.COLUMN_DESCRIPTION));
                    values.put(DatabaseContract.InterchangeColumns.COLUMN_LAT, interchangeObj.getString(DatabaseContract.InterchangeColumns.COLUMN_LAT));
                    values.put(DatabaseContract.InterchangeColumns.COLUMN_LNG, interchangeObj.getString(DatabaseContract.InterchangeColumns.COLUMN_LNG));
                    values.put(DatabaseContract.InterchangeColumns.COLUMN_INTERCHANGE_CATEGORY, interchangeObj.getString(DatabaseContract.InterchangeColumns.COLUMN_INTERCHANGE_CATEGORY));
                    values.put(DatabaseContract.InterchangeColumns.COLUMN_AVAILABLE, interchangeObj.getString(DatabaseContract.InterchangeColumns.COLUMN_AVAILABLE));

                    db.insert(DatabaseContract.TABLE_INTERCHANGE, null, values);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
                showSnackBar(root_view, "Interchanges imported.", Snackbar.LENGTH_LONG, 0);
            }
        } catch (JSONException e) {
            showSnackBar(root_view, "Interchanges import failed.", Snackbar.LENGTH_LONG, 1);
            e.printStackTrace();
        }
    }


//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case PICKFILE_REQUEST_CODE:
//                Uri uri = data.getData();
//                Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
//                        DocumentsContract.getTreeDocumentId(uri));
//
//                DocumentFile documentFile = DocumentFile.fromTreeUri(this, docUri);
//                documentFile.createFile("application/json","paths.json");
//                for (DocumentFile file : documentFile.listFiles()) {
//
//                    if(file.isFile()){ // if it is sub directory
//                        // Do stuff with sub directory
//                        Timber.d(file.getUri().toString());
//
//                    }else{
//                        // Do stuff with normal file
//                    }
//
//
//                }
//
//                break;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    private JSONArray getJSONArrayFromDb(String dbPath, String tablePath)
    {

        String myPath = dbPath;

        String myTable = tablePath;

//        SQLiteDatabase myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase myDataBase = dbHelper.getReadableDatabase();
        String searchQuery = "SELECT  * FROM " + myTable;
//        Cursor cursor = myDataBase.rawQuery(searchQuery, null );
//        cursor = myDataBase.query()
        Cursor cursor = myDataBase.query(tablePath, null,null, null, null,null,null);
        JSONArray resultSet     = new JSONArray();

        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {

            int totalColumn = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for( int i=0 ;  i< totalColumn ; i++ )
            {
                if( cursor.getColumnName(i) != null )
                {
                    try
                    {
                        if( cursor.getString(i) != null )
                        {
                            if(cursor.getColumnName(i).equalsIgnoreCase("lat")){
                                rowObject.put(cursor.getColumnName(i) ,  cursor.getDouble(i) );
                            } else if(cursor.getColumnName(i).equalsIgnoreCase("lng")){
                                rowObject.put(cursor.getColumnName(i) ,  cursor.getDouble(i) );
                            } else if(cursor.getColumnName(i).equalsIgnoreCase("_id")){
                                rowObject.put(cursor.getColumnName(i) ,  cursor.getInt(i) );
                            } else if(cursor.getColumnName(i).equalsIgnoreCase("category")){
                                rowObject.put(cursor.getColumnName(i) ,  cursor.getInt(i) );
                            } else if(cursor.getColumnName(i).equalsIgnoreCase("available")){
                                rowObject.put(cursor.getColumnName(i) ,  cursor.getInt(i) );
                            } else if(cursor.getColumnName(i).equalsIgnoreCase("startPoint")){
                                rowObject.put(cursor.getColumnName(i) ,  cursor.getInt(i) );
                            } else if(cursor.getColumnName(i).equalsIgnoreCase("endPoint")){
                                rowObject.put(cursor.getColumnName(i) ,  cursor.getInt(i) );
                            } else if(cursor.getColumnName(i).equalsIgnoreCase("pathCategory")){
                                rowObject.put(cursor.getColumnName(i) ,  cursor.getInt(i) );
                            } else{
                                rowObject.put(cursor.getColumnName(i) ,  cursor.getString(i) );
                            }
                        }
                        else
                        {
                            rowObject.put( cursor.getColumnName(i) ,  "" );
                        }
                    }
                    catch( Exception e )
                    {
                    }
                }
            }
            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        return resultSet;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_edit_walk)
    public void editWalk(){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.KEY_PATH_TYPE, DatabaseContract.PathColumns.CATEGORY_WALKING);
        intent.putExtra(MapsActivity.KEY_TITLE, "Graf Pejalan Kaki");
        startActivity(intent);
    }
//    @OnClick(R.id.btn_edit_car)
//    public void editCar(){
//        Intent intent = new Intent(this, MapsActivity.class);
//        intent.putExtra(MapsActivity.KEY_PATH_TYPE, DatabaseContract.PathColumns.CATEGORY_CAR);
//        startActivity(intent);
//    }
    @OnClick(R.id.btn_edit_motor)
    public void editMotorcycle(){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.KEY_PATH_TYPE, DatabaseContract.PathColumns.CATEGORY_MOTORCYCLE);
        intent.putExtra(MapsActivity.KEY_TITLE, "Graf Kendaraan Bermotor");
        startActivity(intent);
    }

    @OnClick(R.id.btn_add_interchange)
    public void addInterchange(){
        Intent intent = new Intent(this, AddInterchangeActivity.class);
        startActivity(intent);
    }
}
