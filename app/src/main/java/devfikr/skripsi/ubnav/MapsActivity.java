package devfikr.skripsi.ubnav;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import devfikr.skripsi.ubnav.data.DatabaseContract;
import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.CreateHistory;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;
import devfikr.skripsi.ubnav.util.LatLngConverter;
import devfikr.skripsi.ubnav.util.SnackbarUtil;
import timber.log.Timber;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMarkerClickListener{

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    private GoogleMap mMap;

    //path arraylist for storing paths from db
    private ArrayList<Path> paths = new ArrayList<>();
    //path arraylist for storing visual paths for dragging functionality
    private ArrayList<Path> visualPaths = new ArrayList<>();
    //polyline arraylist for storing polyline drew when point dragged
    private ArrayList<Polyline> visualPolyline = new ArrayList<>();
    //points arraylist for storing points from db
    private ArrayList<Point> points = new ArrayList<>();
    //marker arraylist for storing marker
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<Polyline> polylines = new ArrayList<>();

    private ArrayList<CreateHistory> undo_history = new ArrayList<>();
    private ArrayList<CreateHistory> redo_history = new ArrayList<>();

    //value of normal size of latlng (point)
    private int latlngsNormalSize = 0;

    private boolean isEditNode = false;
    private boolean isEditPolyline = false;
    private static final int EDIT_POLYLINE = 80;
    private static final int EDIT_NODE = 90;

    private int editedPolylineId;
    //selected position (the green point)
    private Point selectedPosition;
    //selected marker (the marker of selected position)
    private Marker selectedMarker;
    private LatLng editedLatLng;
    private Polyline editedPolyline;
    private Snackbar snackbar;
    private LoaderManager.LoaderCallbacks<Cursor> pointsLoaderCallback;
    private LoaderManager.LoaderCallbacks<Cursor> pathsLoaderCallback;
    private int LOADER_POINT_ID = 22;
    private int LOADER_PATH_ID = 33;
    @BindView(R.id.btn_finish)
    Button btn_finish;
    @BindView(R.id.root_map)
    FrameLayout root_map;
    Toast mToast;
    @BindView(R.id.fab_undo)
    FloatingActionButton fab_undo;
    @BindView(R.id.fab_redo)
    FloatingActionButton fab_redo;
    @BindView(R.id.fab_clear)
    FloatingActionButton fab_clear;
    @BindView(R.id.fab_edit_status)
    FloatingActionButton fab_edit_status;

//    @BindView(R.id.fab_save)
//    FloatingActionButton fab_save;
    private DatabaseHelper mDbHelper;
    private Polyline selectedPolyline;
    private long selectedPolylineId;
    private Point polylineStartPoint;

    private long deletePathFromDb(long id){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(DatabaseContract.TABLE_PATHS, "_id="+id, null);
    }

    private long deletePointFromDb(long id){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(DatabaseContract.TABLE_POINTS, "_id="+id, null);
    }

    private long updatePointToDb(long id, LatLng latLng){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.PointColumns.COLUMN_LAT,latLng.latitude);
        cv.put(DatabaseContract.PointColumns.COLUMN_LNG,latLng.longitude);
        cv.put(DatabaseContract.PointColumns.COLUMN_GATES_CATEGORY, 0);
        return db.update(DatabaseContract.TABLE_POINTS, cv, "_id="+id, null);
    }

    private long insertPointToDb(LatLng latLng){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.PointColumns.COLUMN_LAT,latLng.latitude);
        cv.put(DatabaseContract.PointColumns.COLUMN_LNG,latLng.longitude);
        cv.put(DatabaseContract.PointColumns.COLUMN_GATES_CATEGORY, 0);
        return db.insert(DatabaseContract.TABLE_POINTS, null, cv);
    }

    private long insertPathToDb(long startPointId, long endPointId){
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.PathColumns.COLUMN_START_POINT,startPointId);
        cv.put(DatabaseContract.PathColumns.COLUMN_END_POINT,endPointId);
        cv.put(DatabaseContract.PathColumns.COLUMN_CATEGORY, 0);
        return db.insert(DatabaseContract.TABLE_PATHS, null, cv);
    }

    private void showMessage(String s){
        if(mToast != null){
            mToast.cancel();
        }
        mToast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        mToast.show();
    }

    private void addHistory(int id, LatLng latLng){
        CreateHistory createHistory = new CreateHistory(id, latLng);
        undo_history.add(createHistory);
    }
    private void addHistory(){
        CreateHistory createHistory = null;
        undo_history.add(createHistory);
    }

    private void addRedoHistory(int id, LatLng latLng){
        CreateHistory createHistory = new CreateHistory(id, latLng);
        redo_history.add(createHistory);
    }

    private void switchState(int i){
        switch (i){
            case EDIT_NODE:
                isEditNode = true;
                isEditPolyline = false;
                if(selectedPolyline != null){
                    selectedPolyline.setStartCap(new CustomCap(
                            BitmapDescriptorFactory.fromResource(R.drawable.ic_round_black), 20));
                    selectedPolyline.setColor(getResources().getColor(android.R.color.black));
                }
                break;
            case EDIT_POLYLINE:
                isEditNode = false;
                isEditPolyline = true;
                if (selectedMarker!= null){
                    selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDbHelper = new DatabaseHelper(this);
        mDbHelper.getWritableDatabase();

        initPointsLoaderCallback();
        initPathsLoaderCallback();

    }

    private void initPointsLoaderCallback(){
        pointsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                return new CursorLoader(
                        MapsActivity.this,
                        DatabaseContract.CONTENT_URI_POINTS,
                        null,
                        null,
                        null,
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                ArrayList<Point> points
                        = new ArrayList<>();
               if(cursor != null){

                   for (int i=0;i<cursor.getCount();i++){
                       cursor.moveToPosition(i);
                       double latitude = DatabaseContract.getColumnDouble(
                               cursor, DatabaseContract.PointColumns.COLUMN_LAT);
                       double longitude = DatabaseContract.getColumnDouble(
                               cursor, DatabaseContract.PointColumns.COLUMN_LNG);
                       long id = DatabaseContract.getColumnLong(
                               cursor, DatabaseContract.PointColumns._ID);
                       Point point = new Point(
                               id, latitude, longitude
                       );
                       points.add(point);
                   }
                   MapsActivity.this.points = points;
//                   generatePoint();
                   getSupportLoaderManager().restartLoader(LOADER_PATH_ID, null, pathsLoaderCallback);
               }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }

    private void initPathsLoaderCallback(){
        pathsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                return new CursorLoader(
                        MapsActivity.this,
                        ContentUris.withAppendedId(DatabaseContract.CONTENT_URI_PATHS,
                                DatabaseContract.PathColumns.CATEGORY_WALKING),
                        null,
                        null,
                        null,
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                ArrayList<Path> cursorPaths = new ArrayList<>();
                if(cursor != null){

                    for (int i=0;i<cursor.getCount();i++){
                        cursor.moveToPosition(i);
                        //IF USING COMPLEX QUERYYYYYY (I THINK NOT?)
//                        long path_id = DatabaseContract.getColumnLong(
//                                cursor, DatabaseContract.PathColumns._ID
//                        );
//                        long start_point_id = DatabaseContract.getColumnLong(
//                                cursor, DatabaseContract.PathColumns.COLUMN_START_POINT_ID
//                        );
//                        double start_point_latitude = DatabaseContract.getColumnDouble(
//                                cursor, DatabaseContract.PathColumns.COLUMN_START_POINT_LAT);
//                        double start_point_longitude = DatabaseContract.getColumnDouble(
//                                cursor, DatabaseContract.PathColumns.COLUMN_START_POINT_LNG);
//                        int start_point_category = DatabaseContract.getColumnInt(
//                                cursor, DatabaseContract.PathColumns.COLUMN_START_POINT_CATEGORY);
//                        long end_point_id = DatabaseContract.getColumnLong(
//                                cursor, DatabaseContract.PathColumns.COLUMN_START_POINT_ID
//                        );
//                        double end_point_latitude = DatabaseContract.getColumnDouble(
//                                cursor, DatabaseContract.PathColumns.COLUMN_END_POINT_LAT);
//                        double end_point_longitude = DatabaseContract.getColumnDouble(
//                                cursor, DatabaseContract.PathColumns.COLUMN_END_POINT_LNG);
//                        int end_point_category = DatabaseContract.getColumnInt(
//                                cursor, DatabaseContract.PathColumns.COLUMN_END_POINT_CATEGORY);
//                        devfikr.skripsi.ubnav.model.Point startLatLng = new devfikr.skripsi.ubnav.model.Point(
//                                start_point_id, start_point_latitude, start_point_longitude
//                        );
//                        devfikr.skripsi.ubnav.model.Point endLatLng = new devfikr.skripsi.ubnav.model.Point(
//                                end_point_id, end_point_latitude, end_point_longitude
//                        );
                        long path_id = DatabaseContract.getColumnLong(
                                cursor, DatabaseContract.PathColumns._ID
                        );
                        int startPointId = DatabaseContract.getColumnInt(
                                cursor, DatabaseContract.PathColumns.COLUMN_START_POINT
                        );
                        int endPointId = DatabaseContract.getColumnInt(
                                cursor, DatabaseContract.PathColumns.COLUMN_END_POINT
                        );
                        Point startPoint = null;
                        Point endPoint = null;
                        for (Point point : points){
                            if (point.getId() == startPointId){
                                startPoint = point;
                            }
                            if (point.getId() == endPointId){
                                endPoint = point;
                            }
                        }
                        Path path = new Path(path_id, startPoint, endPoint);
                        cursorPaths.add(path);
                    }
                    paths = cursorPaths;
                    LatLng gerbangVeteranMasuk = new LatLng(-7.956213, 112.613298);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gerbangVeteranMasuk,15));
//                    selectedPosition = points.get(points.size()-1);
                    generatePoint();
                } else{

                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }


    private void addPath(long pathId, Point startPosition,
                         Point endPosition){
        Path path = new Path(pathId,startPosition,
                endPosition
                );
        paths.add(path);
    }

    private long generatePathId(){
        return 1;
    }

    private long generateLatLngId(){
        return 1;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        addGerbangVeteran();
        // Add a marker in Sydney and move the camera


//        Point gerbangVeteranKeluar = new Point(-7.956165, 112.613413);
//        locations.add(gerbangVeteranKeluar);
//        mMap.addMarker(new MarkerOptions().position(gerbangVeteranKeluar).title("Gerbang Veteran Keluar")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gerbangVeteranKeluar,15));
        getSupportLoaderManager().restartLoader(LOADER_POINT_ID, null, pointsLoaderCallback);
        mMap.setOnMapClickListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    private void insertNode(int id, LatLng latLng){

    }

    private void addPointMarker(Point point, long latlngid, boolean isSelected){
        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("New Position");
        Marker marker = mMap.addMarker(markerOptions);
        marker.setDraggable(true);
        markers.add(marker);
        if(selectedPosition != null){
            if(point.getId() == selectedPosition.getId()){
                selectedMarker = marker;
            }
        }
        if(isSelected){
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }
        marker.setTag(latlngid);
    }

    private void addPoint(LatLng latLng){
        Point addedPoint = new Point(insertPointToDb(latLng), latLng.latitude, latLng.longitude);
        points.add(addedPoint);
        latlngsNormalSize = points.size();
    }

    private void updatePoint(long selectedPointId, LatLng position) {
        Point updatedPoint = new Point(selectedPointId, position.latitude, position.longitude);
        updatePointToDb(selectedPointId, position);
        for (int i =0;i<points.size();i++){
            if (points.get(i).getId() == selectedPointId){
                points.set(i, updatedPoint);
            }
        }
    }

    private void addVisualLatLng(long id,LatLng latLng){
        Point addedPoint =
               new Point(id, latLng.latitude, latLng.longitude);
        points.add(addedPoint);
    }

    @Override
    public void onMapClick(LatLng latLng) {
//        String pathId = generatePathId();
        if(isEditNode){
            if(selectedPosition == null){
                SnackbarUtil.showSnackBar(root_map, snackbar,"Pilih point terlebih dahulu.", Snackbar.LENGTH_LONG);
            } else{
                addPoint(latLng);
                Point addedPoint = points.get(points.size()-1);
                addPath(insertPathToDb(selectedPosition.getId(), addedPoint.getId()),
                        selectedPosition, addedPoint);
                addPointMarker(addedPoint, addedPoint.getId(), true);
                selectedPosition = addedPoint;

                generatePoint();
            }
        } else if(isEditPolyline){
            long id = (long)selectedPolyline.getTag();
            Path selectedPath = null;
            for (Path path : paths){
                if (path.getId() == id){
                    selectedPath = path;
                    paths.remove(path);
                    break;
                }
            }
            deletePathFromDb(id);
            addPoint(latLng);
            Point addedPoint = points.get(points.size()-1);

            //insert path 1
            addPath(insertPathToDb(selectedPath.getStartLocation().getId(),
                    addedPoint.getId()),
                    selectedPath.getStartLocation(), addedPoint);
            //insert path 2
            addPath(insertPathToDb(addedPoint.getId(),
                    selectedPath.getEndLocation().getId()),
                    addedPoint, selectedPath.getEndLocation());
            selectedPosition = addedPoint;
            switchState(EDIT_NODE);
            generatePoint();
//            showMessage(String.valueOf(id));
//            selectedPolylineId = (long)polyline.getTag();
//        polylineStartPoint = polyline.getPoints().get(0);
//            selectedPolyline = polyline;
//            selectedPolyline.setTag(id);
        }


//        if(isCreateNewPath){
//            mMap.clear();
//            if(isEditPolyline){
//                    if (isInsertNewNode) {
//                        int id = editedPolylineId+1;
//                        locations.add(id, latLng);
//                        generatePoint();
//                        isEditPolyline = false;
//                        Log.e("Dafuq e", "Found @ "+latLng.latitude+" "+latLng.longitude);
//                    }
//                    else if(isDragPath){
//                        addRedoHistory(editedPolylineId, latLng);
//                        locations.set(editedPolylineId, latLng);
//                        generatePoint();
//                        isEditPolyline = false;
//                    }
//                    else if(isDeletePath){
//                        locations.remove(editedPolylineId);
//                        isEditPolyline = false;
//                        generatePoint();
//                    }
////                }
//
//            }
//            else{
//                addHistory();
////            mMap.addMarker(new MarkerOptions().position(latLng).title("New Marker")
////            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_point)));
//
//                Path path = new Path(LatLngConverter.convertToLocalLatLng(selectedNode),
//                        LatLngConverter.convertToLocalLatLng(latLng));
//                paths.add(path);
//                selectedNode = latLng;
//                generatePoint();
//            }
//
////            polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
////            polyline.setColor(COLOR_BLACK_ARGB);
        }

//    }

    private void generatePoint(){
        mMap.clear();
        markers.clear();
        polylines.clear();
        if(paths.size() == 0){
            selectedPosition = points.get(0);
            addPointMarker(selectedPosition, selectedPosition.getId(), true);
        }
        for(int i =0;i<paths.size();i++){
            Path path = paths.get(i);
            boolean startPointMarkerCreated = false;
            boolean endPointMarkerCreated = false;
            for (Marker marker : markers){
                long pointId = Long.valueOf(marker.getTag().toString());
//                Timber.d("Marker "+pointId+" aman");
                if(path.getStartLocation().getId() == pointId){
                    startPointMarkerCreated = true;
                }
                if (path.getEndLocation().getId() == pointId){
                    endPointMarkerCreated = true;
                }
            }
            if(!startPointMarkerCreated){
                if(path.getStartLocation() == selectedPosition){
                addPointMarker(path.getStartLocation(), path.getStartLocation().getId(),true);
                } else{
                    addPointMarker(path.getStartLocation(), path.getStartLocation().getId(), false);
                }
            }
            if(!endPointMarkerCreated){
                if(path.getEndLocation() == selectedPosition){
                    addPointMarker(path.getEndLocation(), path.getEndLocation().getId(),true);
                } else{
                    addPointMarker(path.getEndLocation(), path.getEndLocation().getId(),false);
                }
            }

            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.add(LatLngConverter.convertToGoogleLatLng(path.getStartLocation()))
                    .add(LatLngConverter.convertToGoogleLatLng(path.getEndLocation()));
            Polyline polyline = mMap.addPolyline(polylineOptions);
            polylines.add(polyline);
            if(selectedPolyline != null){
                if(path.getId() == selectedPolylineId){
                    selectedPolyline = polyline;
                }
            }
            polyline.setStartCap( new CustomCap(
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_round_black), 20));
            polyline.setClickable(true);
            polyline.setTag(path.getId());
        }

    }
    private void generateVisualizationPolyline(){
        if(visualPolyline.size()>0){
            for (Polyline polyline : visualPolyline){
                polyline.remove();
            }
            visualPolyline.clear();
        }
        for(Path path : visualPaths){
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.add(LatLngConverter.convertToGoogleLatLng(path.getStartLocation()))
                    .add(LatLngConverter.convertToGoogleLatLng(path.getEndLocation()));
            Polyline polyline = mMap.addPolyline(polylineOptions);
            polyline.setColor(getResources().getColor(R.color.colorAccent));
            visualPolyline.add(polyline);
            polyline.setStartCap( new CustomCap(
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_round_black), 20));
            polyline.setClickable(true);
        }
        visualPaths.clear();
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        switchState(EDIT_POLYLINE);
        if(selectedPolyline != null){
            selectedPolyline.setStartCap(new CustomCap(
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_round_black), 20));
            selectedPolyline.setColor(getResources().getColor(android.R.color.black));
        }

        long id = (long)polyline.getTag();
        showMessage(String.valueOf(id));
        selectedPolylineId = (long)polyline.getTag();
//        polylineStartPoint = polyline.getPoints().get(0);
        selectedPolyline = polyline;
        selectedPolyline.setTag(id);
        addHistory(editedPolylineId, editedLatLng);
        polyline.setClickable(true);
        polyline.setStartCap(new CustomCap(
                BitmapDescriptorFactory.fromResource(R.drawable.ic_round_green), 20));
        polyline.setColor(getResources().getColor(R.color.colorAccent));
//        if(isEditPolyline == false){
//
//        } else{
//            if(id == editedPolylineId){
//                Timber.d("Lat :"+polyline.getPoints().get(0).latitude+" Lng :"+polyline.getPoints().get(0).longitude);
//            }
//        }
    }

//    public void undoPath(View view) {
//        if(undo_history != null){
//            int lastIdHistory = undo_history.size()-1;
//            //undo edited path
//            if(undo_history.get(lastIdHistory) != null){
//                CreateHistory createHistory = undo_history.get(lastIdHistory);
//                Timber.d(String.valueOf(createHistory.getIndex()));
//                locations.set(createHistory.getIndex(), createHistory.getLatLng());
//                undo_history.remove(lastIdHistory);
//                generatePoint();
//            } else{
//                int lastIdLocation = locations.size()-1; //undo added path
//                if(locations.size()>1){
//                    if(locations.get(lastIdLocation) != null){
//                        CreateHistory createHistory = new CreateHistory(lastIdLocation, locations.get(lastIdLocation));
//                        redo_history.add(createHistory);
//                        locations.remove(lastIdLocation);
//                        generatePoint();
//                    }
//                }
//            }
//        }
//    }
//
//    public void redoPath(View view) {
//        if(redo_history != null){
//            if(redo_history.size()>0){
//                int redoLastIndex = redo_history.size()-1;
//                CreateHistory redoHistory = redo_history.get(redoLastIndex);
//                if(locations.size() <= redoHistory.getIndex()){
//                    locations.add(redoHistory.getLatLng());
//                } else{
//                    locations.set(redoHistory.getIndex(),redoHistory.getLatLng());
//                }
////                addHistory(editedPolylineId, editedLatLng);
//                redo_history.remove(redoLastIndex);
//                generatePoint();
//            }
//        }
//    }

//    public void clearPath(View view) {
//        mMap.clear();
//        isEditPolyline = false;
//        undo_history.clear();
//        redo_history.clear();
//        locations.clear();
//
//        Point gerbangVeteranMasuk = new Point(-7.956213, 112.613298);
//        locations.add(gerbangVeteranMasuk);
//        mMap.addMarker(new MarkerOptions().position(gerbangVeteranMasuk).title("Gerbang Veteran Masuk")
//                .icon(BitmapDescriptorFactory.fromResource(R.drawable.gate)));
//    }


    @Override
    public void onMarkerDragStart(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        long selectedPointId = Long.valueOf(marker.getTag().toString());
        Timber.d("Visual Path Size:"+visualPaths.size());
        int lastPointId = points.size()-1;

        if(points.size()>latlngsNormalSize){
            points.remove(lastPointId);
        }
        addVisualLatLng(selectedPointId,marker.getPosition());

        //gambar visualisasi path yang digeser
        for (Path path : paths){
            if(path.getStartLocation().getId() == selectedPointId){
                for (Point point : points){
                    if(point.getId()==selectedPointId){
                        Point draggedPoint = points.get(lastPointId);
//                        path.setStartLocation(draggedPoint);
                        Path visualPath = new Path(
                                path.getId(), draggedPoint, path.getEndLocation());

                        visualPaths.add(visualPath);
                    }
                }
            } else if(path.getEndLocation().getId() ==selectedPointId){
                for (Point point : points){
                    if(point.getId() == (selectedPointId)){
                        Point draggedPoint = points.get(lastPointId);
//                        path.setStartLocation(draggedPoint);
                        Path visualPath = new Path(
                                path.getId(), path.getStartLocation(), draggedPoint);

                        visualPaths.add(visualPath);
                    }
                }
            }
        }
        generateVisualizationPolyline();
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        long selectedPointId = Long.valueOf(marker.getTag().toString());
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//        addPoint(marker.getPosition());
        //update point di database
        updatePoint(selectedPointId, marker.getPosition());
        switchSelectedMarker(marker);
        int lastLatLngsId = points.size()-1;

        if(selectedPosition.getId()== selectedPointId){
            selectedPosition = points.get(lastLatLngsId);
        }
        int i =0;
        //implementasi kan visualisasi path ke path sebenarnya setelah di geser
        for (Path path : paths){
            if(path.getStartLocation().getId()==selectedPointId){
                for (Point point : points){
                    if(point.getId()==selectedPointId){
                        Point draggedPoint = points.get(lastLatLngsId);
//                        path.setStartLocation(draggedPoint);
                        Path visualPath = new Path(
                                path.getId(), draggedPoint, path.getEndLocation());
                        paths.set(i, visualPath);
//                        visualPaths.add(visualPath);
                    }
                }
            } else if(path.getEndLocation().getId()==selectedPointId){
                for (Point point : points){
                    if(point.getId()==selectedPointId){
                        Point draggedPoint = points.get(lastLatLngsId);
//                        path.setStartLocation(draggedPoint);
                        Path visualPath = new Path(
                                path.getId(), path.getStartLocation(), draggedPoint);
                        paths.set(i, visualPath);
//                        visualPaths.add(visualPath);
                    }
                }
            }
            i++;
        }
        generatePoint();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        switchState(EDIT_NODE);
        switchSelectedMarker(marker);
//        generatePoint();
        return true;
    }

    private void switchSelectedMarker(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        if(selectedMarker != null){
            if(!(selectedMarker.getTag().toString().equals(marker.getTag().toString()))){
                selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
        }
        selectedMarker = marker;
        for (Point point : points){
            if(point.getId()==Long.valueOf(marker.getTag().toString())){
                selectedPosition = point;
            }
        }
    }

    //point yang dapat dihapus adalah point yang tidak menjadi startNode pada suatu path
    public void deletePoint(View view) {
        long id = selectedPosition.getId();
        long idPathToBeDeleted = 0;
        Path pathToBeDeleted = null;
        for (Path path : paths){
            if(path.getStartLocation().getId() ==
                    id){
                SnackbarUtil.showSnackBar(root_map, snackbar,
                        "Anda tidak bisa menghapus node ini", Snackbar.LENGTH_LONG);
                return;
            }

            if(path.getEndLocation().getId() ==
                    id){
                idPathToBeDeleted = path.getId();
                pathToBeDeleted = path;
            }
        }
        paths.remove(pathToBeDeleted);
        points.remove(selectedPosition);

        selectedPosition = null;
        selectedMarker = null;
        deletePathFromDb(idPathToBeDeleted);
        deletePointFromDb(id);
        generatePoint();
    }

//    public void finishEditor(View view) {
//        mDatabase.child(getString(R.string.node_path)).setValue(null);
//        for (int i=0;i<locations.size();i++){
//            if(i+1 < locations.size()){
//                devfikr.skripsi.ubnav.model.Point startLocation =
//                        LatLngConverter.convertToLocalLatLng(locations.get(i));
//                devfikr.skripsi.ubnav.model.Point endLocation =
//                        LatLngConverter.convertToLocalLatLng(locations.get(i+1));
//                Path path = new Path(startLocation, endLocation);
//                mDatabase.child(getString(R.string.node_path)).push().setValue(path);
//            }
//        }
//        createPath(view);
//    }

//    public void exitWatchPath(View view) {
//        isWatchMode = false;
//        fab_exit_watch_path.setVisibility(View.GONE);
//        fab_watch_path.setVisibility(View.VISIBLE);
//
//        clearPath(view);
//    }
}
