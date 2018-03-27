package devfikr.skripsi.ubnav.ui.activity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import devfikr.skripsi.ubnav.R;
import devfikr.skripsi.ubnav.commands.callback.AddOnePointCommandCallback;
import devfikr.skripsi.ubnav.commands.callback.AddPointCommandCallback;
import devfikr.skripsi.ubnav.commands.command.AddOnePointCommand;
import devfikr.skripsi.ubnav.commands.command.AddPathCommand;
import devfikr.skripsi.ubnav.commands.callback.AddPathCommandCallback;
import devfikr.skripsi.ubnav.commands.callback.AddPointBetweenPathCommandCallback;
import devfikr.skripsi.ubnav.commands.command.AddPointBetweenPathCommand;
import devfikr.skripsi.ubnav.commands.command.AddPointCommand;
import devfikr.skripsi.ubnav.commands.CommandManager;
import devfikr.skripsi.ubnav.commands.callback.DeleteCommandCallback;
import devfikr.skripsi.ubnav.commands.command.DeletePointCommand;
import devfikr.skripsi.ubnav.commands.callback.UpdateCommandCallback;
import devfikr.skripsi.ubnav.commands.command.UpdatePointCommand;
import devfikr.skripsi.ubnav.commands.helper.DatabaseOperationHelper;
import devfikr.skripsi.ubnav.data.DatabaseContract;
import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.CreateHistory;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;
import devfikr.skripsi.ubnav.util.LatLngConverter;
import devfikr.skripsi.ubnav.util.SnackbarUtil;
import timber.log.Timber;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnPolylineClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMarkerClickListener,
        AddPointCommandCallback,
        DeleteCommandCallback,
        UpdateCommandCallback,
        AddPathCommandCallback,
        AddOnePointCommandCallback,
        AddPointBetweenPathCommandCallback {

    public static final String KEY_PATH_IN_OUT = "keyPathInOut";
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

    private boolean isEditNode = false;
    private boolean isEditPolyline = false;
    private static final int EDIT_POLYLINE = 80;
    private static final int EDIT_NODE = 90;

    private boolean isJoinNode = false;
    private boolean isAddMarker = false;
    //selected position (the green point)
    private Point selectedPosition;
    //selected marker (the marker of selected position)
    private Marker selectedMarker;

    private Snackbar snackbar;
    private LoaderManager.LoaderCallbacks<Cursor> pointsLoaderCallback;
    private LoaderManager.LoaderCallbacks<Cursor> pathsLoaderCallback;
    private int LOADER_POINT_ID = 22;
    private int LOADER_PATH_ID = 33;
    public final static String KEY_PATH_TYPE = "pathTypeKey";
    public static final String KEY_TITLE = "keyTitle";

    private int PATH_CATEGORY;

    private CommandManager commandManager;
    @BindView(R.id.fab_add_marker) FloatingActionButton fab_add_marker;
    @BindView(R.id.btn_finish)
    Button btn_finish;
    @BindView(R.id.root_map)
    CoordinatorLayout root_map;
    Toast mToast;
    @BindView(R.id.fab_undo)
    FloatingActionButton fab_undo;
    @BindView(R.id.fab_redo)
    FloatingActionButton fab_redo;
    @BindView(R.id.fab_clear)

    FloatingActionButton fab_edit_status;
    @BindView(R.id.fab_join_node)
    FloatingActionButton fab_join_node;
    @BindView(R.id.bottom_sheet)
    LinearLayout layoutBottomSheet;
//    @BindView(R.id.fab_save)
//    FloatingActionButton fab_save;
    private DatabaseHelper mDbHelper;
    private Polyline selectedPolyline;
    private long selectedPolylineId;
    private Point polylineStartPoint;

    private void showMessage(String s){
        if(mToast != null){
            mToast.cancel();
        }
        mToast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
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
        layoutBottomSheet.setVisibility(View.GONE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDbHelper = new DatabaseHelper(this);
        mDbHelper.getWritableDatabase();

        if(getIntent()!=null){
            PATH_CATEGORY = getIntent().getIntExtra(KEY_PATH_TYPE, DatabaseContract.PathColumns.CATEGORY_WALKING);
            String title = getIntent().getStringExtra(KEY_TITLE);
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        initPointsLoaderCallback();
        initPathsLoaderCallback();

        commandManager = new CommandManager();
    }

    private void initPointsLoaderCallback(){
        pointsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri getPointUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI_POINTS,
                        PATH_CATEGORY);

                return new CursorLoader(
                        MapsActivity.this,
                        getPointUri,
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
                Uri getPathUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI_PATHS,
                        PATH_CATEGORY);

                return new CursorLoader(
                        MapsActivity.this,
                        getPathUri,
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



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getSupportLoaderManager().restartLoader(LOADER_POINT_ID, null, pointsLoaderCallback);
        mMap.setOnMapClickListener(this);
        mMap.setOnPolylineClickListener(this);
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);
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

    private void removePointMarker(Point point){
        LatLng latLng = new LatLng(point.getLatitude(), point.getLongitude());
        long pointId = point.getId();
        Marker toDeleteMarker = null;
        for (Marker marker:markers){
            if (Long.valueOf(marker.getTag().toString())
                    == pointId){
                toDeleteMarker = marker;
            }
        }
        markers.remove(toDeleteMarker);
        toDeleteMarker.remove();
        selectedMarker = null;
    }

    private void addVisualLatLng(long id,LatLng latLng){
        Point addedPoint =
               new Point(id, latLng.latitude, latLng.longitude);
        points.add(addedPoint);
    }

    @Override
    public void onMapClick(LatLng latLng) {
//        String pathId = generatePathId();
        if(isJoinNode){
            return;
        }
        if(isAddMarker){
            commandManager.doCommand(
                    new AddOnePointCommand(
                            this,
                            mDbHelper,
                            latLng,
                            PATH_CATEGORY));
            isAddMarker = false;
            fab_add_marker.setSelected(false);
        }
        else if(isEditNode && !isJoinNode){
            if(selectedPosition == null){
                SnackbarUtil.showSnackBar(root_map, snackbar,"Pilih point terlebih dahulu.", Snackbar.LENGTH_LONG);
            } else{
                commandManager.doCommand(
                        new AddPointCommand(
                                this,
                                mDbHelper,
                                selectedPosition,
                                latLng,
                                PATH_CATEGORY));

            }
        } else if(isEditPolyline){
            long id = (long)selectedPolyline.getTag();
            Path selectedPath = null;
            for (Path path : paths){
                if(path.getId() == id){
                    selectedPath = path;
                }
            }
            commandManager.doCommand(
                    new AddPointBetweenPathCommand(
                            this,
                            mDbHelper,
                            selectedPath,
                            latLng,
                            PATH_CATEGORY));
        }

        }


    private void generatePoint(){
        mMap.clear();
        markers.clear();
        polylines.clear();
        if(paths.size() == 0){
            selectedPosition = points.get(0);
            Timber.d("Cupu "+ points.get(0).getId());
            addPointMarker(selectedPosition, selectedPosition.getId(), true);
            switchSelectedMarker(findMarker(selectedPosition));
        }
        for(int i =0;i<points.size();i++){
            boolean havePath = false;
            Point point = points.get(i);
            for (int j=0;j<paths.size();j++){
                Path path = paths.get(j);
                if(path.getStartLocation() != null){
//                    if(path.getStartLocation().getId() == point.getId() ||
//                            path.getEndLocation().getId() == point.getId()){
//                        havePath = true;
//                    }
                } else{
                    DatabaseOperationHelper.deletePathFromDb(mDbHelper, path.getId());
                }
            }
//            if(!havePath){
//                addPointMarker(point, point.getId(), false);
//            }
        }
        for(int i =0;i<paths.size();i++){
            Path path = paths.get(i);
            boolean startPointMarkerCreated = false;
            boolean endPointMarkerCreated = false;
            for (Marker marker : markers){
                long pointId = Long.valueOf(marker.getTag().toString());
//                Timber.d("Marker "+pointId+" aman");
                if(path.getStartLocation() != null && path.getEndLocation() != null){
                    if(path.getStartLocation().getId() == pointId){
                        startPointMarkerCreated = true;
                    }
                    if (path.getEndLocation().getId() == pointId){
                        endPointMarkerCreated = true;
                    }
                } else{
                    DatabaseOperationHelper.deletePathFromDb(mDbHelper, path.getId());
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
            polyline.setEndCap( new CustomCap(
                    BitmapDescriptorFactory.fromResource(getPolylineCaps()), 10));
            polyline.setColor(getPolylineColor());
            polyline.setClickable(true);
            polyline.setTag(path.getId());
        }
        switchState(EDIT_NODE);
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
            polyline.setEndCap( new CustomCap(
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_green), 10));
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
        polyline.setClickable(true);
        polyline.setStartCap(new CustomCap(
                BitmapDescriptorFactory.fromResource(R.drawable.ic_round_green), 20));
        polyline.setColor(getResources().getColor(R.color.colorAccent));
    }


    @Override
    public void onMarkerDragStart(Marker marker) {
        switchSelectedMarker(marker);
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        long selectedPointId = Long.valueOf(marker.getTag().toString());
        for (Point point : points){
            if (point.getId() == selectedPointId){
                selectedPosition = point;
            }
        }
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        long selectedPointId = Long.valueOf(marker.getTag().toString());
        Timber.d("Visual Path Size:"+visualPaths.size());

        Point draggedPoint = null;
        for (Point point : points){
            if (point.getId() == selectedPointId){
                draggedPoint = point;
            }
        }
        addVisualLatLng(selectedPointId,marker.getPosition());

        //gambar visualisasi path yang digeser
        for (Path path : paths){
            if(path.getStartLocation().getId() == selectedPointId){
                for (Point point : points){
                    if(point.getId()==selectedPointId){
                        Path visualPath = new Path(
                                path.getId(), draggedPoint, path.getEndLocation());

                        visualPaths.add(visualPath);
                    }
                }
            } else if(path.getEndLocation().getId() ==selectedPointId){
                for (Point point : points){
                    if(point.getId() == (selectedPointId)){
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
        selectedMarker = marker;
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        commandManager.doCommand(
                new UpdatePointCommand(
                        this,
                        mDbHelper,
                        selectedPosition,
                new LatLng(
                        selectedPosition.getLatitude(), selectedPosition.getLongitude()),
                        marker.getPosition(),
                        PATH_CATEGORY));

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(isJoinNode){
            long toJoinId = Long.valueOf(marker.getTag().toString());
            Point toJoinPoint = null;
            for (Point point:points){
                if(point.getId() == toJoinId){
                    toJoinPoint = point;
                }
            }
            commandManager.doCommand(new AddPathCommand(
                    this,
                    mDbHelper,
                    selectedPosition,
                    toJoinPoint,
                    PATH_CATEGORY
            ));
            isJoinNode = false;
            marker.setTag(toJoinId);
            fab_join_node.setSelected(false);
        }
        else{
            switchState(EDIT_NODE);
            switchSelectedMarker(marker);
        }

//        generatePoint();
        return true;
    }

    private void switchSelectedMarker(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        String markerTag = marker.getTag().toString();
        if(selectedMarker != null && marker!=null){
            if(!(selectedMarker.getTag().toString().equals(markerTag))){
                selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            }
        }
        selectedMarker = marker;
        for (Point point : points){
            if(point.getId()==Long.valueOf(markerTag)){
                selectedPosition = point;
            }
        }
        selectedMarker.setTag(markerTag);
    }


    public void deletePoint(View view) {
        commandManager.doCommand(new DeletePointCommand(
                this,
                mDbHelper,
                selectedPosition,
                paths,
                PATH_CATEGORY));
    }

    private void generatePolylineFromPath(Path path){
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
        polyline.setEndCap( new CustomCap(
                BitmapDescriptorFactory.fromResource(getPolylineCaps()), 10));
        polyline.setColor(getPolylineColor());
        polyline.setClickable(true);
        polyline.setTag(path.getId());
    }

    private void removePolylinePath(Path path){
        Polyline toDeletePolyline = null;
        for (Polyline polyline : polylines){
            if (Long.valueOf(polyline.getTag().toString())
                    == path.getId()){
                toDeletePolyline = polyline;
            }
        }
        if(toDeletePolyline != null){
            polylines.remove(toDeletePolyline);
            toDeletePolyline.remove();
        }
    }

    private Marker findMarker(Point point){
        Marker thisMarker = null;
        if(markers != null && markers.size() > 0){
            for (Marker marker : markers){
                if (Long.valueOf(marker.getTag().toString())
                        == point.getId()){
                    thisMarker = marker;
                }
            }
        }
        return thisMarker;
    }

    @Override
    public void addCommandExecuteResult(Point addedPoint, Path addedPath) {
        this.points.add(addedPoint);
        this.paths.add(addedPath);
        addPointMarker(addedPoint, addedPoint.getId(), true);
        generatePolylineFromPath(addedPath);
        switchSelectedMarker(findMarker(addedPoint));
    }

    @Override
    public void addCommandUndoResult(Point removedPoint, Path removedPath) {
        removePointMarker(removedPoint);
        removePolylinePath(removedPath);
        selectedPosition = null;
//        switchSelectedMarker(findMarker(removedPoint));
    }

    @Override
    public void deleteCommandExecuteResult(Path removedPath, Point removedPoint, String message) {
//        selectedMarker = null;
        if(removedPoint==null){
            if(message != null){
                SnackbarUtil.showSnackBar(
                        root_map,
                        snackbar,
                        "Anda tidak bisa menghapus node ini!",
                        Snackbar.LENGTH_LONG
                );
            }
        } else{
            removePointMarker(removedPoint);
            removePolylinePath(removedPath);
        }
    }

    @Override
    public void deleteCommandUndoResult(Path recoveredPath, Point recoveredPoint) {
        addPointMarker(recoveredPoint, recoveredPoint.getId(), true);
        generatePolylineFromPath(recoveredPath);
    }

    @Override
    public void updateCommandExecuteResult(Point updatedPoint, LatLng updatedLatLng) {
        Marker toMoveMarker = null;
//        switchSelectedMarker(findMarker(updatedPoint));
        for (Marker marker: markers){
            if(Long.valueOf(marker.getTag().toString())
                    == updatedPoint.getId()){
                toMoveMarker = marker;
            }
        }
        toMoveMarker.setPosition(updatedLatLng);

        for (Point point : points){
            if(point.getId() == updatedPoint.getId()){
                point.setLatitude(updatedLatLng.latitude);
                point.setLongitude(updatedLatLng.longitude);
            }
        }
        ArrayList<Path> updatedPaths = new ArrayList<>();
        for (Path path : paths){
            if(path.getStartLocation().getId()
                    == updatedPoint.getId() ||
                    path.getEndLocation().getId() == updatedPoint.getId()){
                updatedPaths.add(path);
            }
        }

        for (Path path : updatedPaths){
            ArrayList<Polyline> updatedPolylines = new ArrayList<>();
            for (Polyline polyline : polylines){
                if(Long.valueOf(polyline.getTag().toString())
                        == path.getId()){
                    updatedPolylines.add(polyline);
                }
            }

            for (Polyline polyline : updatedPolylines){
                polyline.remove();
                polylines.remove(polyline);
            }
            generatePolylineFromPath(path);
        }

        visualPaths.clear();
        for (Polyline polyline : visualPolyline){
            polyline.remove();
        }

        switchSelectedMarker(findMarker(updatedPoint));
    }

    @Override
    public void updateCommandUndoResult(Point recoveredPoint, LatLng updatedLatLng) {
        Marker toMoveMarker = null;
        switchSelectedMarker(findMarker(recoveredPoint));
        for (Marker marker: markers){
            if(Long.valueOf(marker.getTag().toString())
                    == recoveredPoint.getId()){
                toMoveMarker = marker;
            }
        }
        toMoveMarker.setPosition(updatedLatLng);

        for (Point point : points){
            if(point.getId() == recoveredPoint.getId()){
                point.setLatitude(updatedLatLng.latitude);
                point.setLongitude(updatedLatLng.longitude);
            }
        }
        ArrayList<Path> updatedPaths = new ArrayList<>();
        for (Path path : paths){
            if(path.getStartLocation().getId()
                    == recoveredPoint.getId() ||
                    path.getEndLocation().getId() == recoveredPoint.getId()){
                updatedPaths.add(path);
            }
        }

        for (Path path : updatedPaths){
            ArrayList<Polyline> updatedPolylines = new ArrayList<>();
            for (Polyline polyline : polylines){
                if(Long.valueOf(polyline.getTag().toString())
                        == path.getId()){
                    updatedPolylines.add(polyline);
                }
            }

            for (Polyline polyline : updatedPolylines){
                polyline.remove();
                polylines.remove(polyline);
            }
            generatePolylineFromPath(path);
        }

        visualPaths.clear();
        for (Polyline polyline : visualPolyline){
            polyline.remove();
        }

        switchSelectedMarker(findMarker(recoveredPoint));
    }

    public void undoPath(View view) {
        commandManager.undo();
    }

    public void redoPath(View view) {
        commandManager.redo();
    }

    @Override
    public void addPointBetweenPathExecuteResult(Path removedPath, Path addedPath1, Path addedPath2, Point pointInBetween) {
        this.points.add(pointInBetween);
        this.paths.add(addedPath1);
        this.paths.add(addedPath2);

        addPointMarker(pointInBetween, pointInBetween.getId(), true);
        removePolylinePath(removedPath);
        generatePolylineFromPath(addedPath1);
        generatePolylineFromPath(addedPath2);

        selectedPolyline = null;
        switchState(EDIT_NODE);
        switchSelectedMarker(findMarker(pointInBetween));
//        switchSelectedMarker(findMarker(pointInBetween));
    }

    @Override
    public void addPointBetweenPathUndoResult(Path recoveredPath, Path deletedPath1, Path deletedPath2, Point deletedPointInBetween) {
        this.points.remove(deletedPointInBetween);
        this.paths.remove(deletedPath1);
        this.paths.remove(deletedPath2);

        removePointMarker(deletedPointInBetween);
        removePolylinePath(deletedPath1);
        removePolylinePath(deletedPath2);

        generatePolylineFromPath(recoveredPath);
    }


    @Override
    public void addPointBetweenPathRedoResult(Path removedPath, Path addedPath1, Path addedPath2, Point pointInBetween) {
        this.points.add(pointInBetween);
        this.paths.add(addedPath1);
        this.paths.add(addedPath2);

        addPointMarker(pointInBetween, pointInBetween.getId(), true);
        removePolylinePath(removedPath);
        generatePolylineFromPath(addedPath1);
        generatePolylineFromPath(addedPath2);

        selectedPolyline = null;
        switchState(EDIT_NODE);
        switchSelectedMarker(findMarker(pointInBetween));
//        switchSelectedMarker(findMarker(pointInBetween));
    }


    @Override
    public void addPathCommandExecuteResult(Path addedPath, long pointToId) {
        generatePolylineFromPath(addedPath);
        Marker selectedMarker = null;
        paths.add(addedPath);
        for (Marker marker : markers){
            if (Long.valueOf(marker.getTag().toString()) == pointToId){
                selectedMarker = marker;
            }
        }

        switchSelectedMarker(selectedMarker);
    }

    @Override
    public void addPathCommandUndoResult(Path removedPath, long removedPointToId) {
        paths.remove(removedPath);
        removePolylinePath(removedPath);
    }

    @Override
    public void addOnePointCommandExecuteResult(Point addedPoint) {
        points.add(addedPoint);
        addPointMarker(addedPoint, addedPoint.getId(), true);
        switchSelectedMarker(findMarker(addedPoint));
        selectedPosition = addedPoint;
    }

    @Override
    public void addOnePointCommandUndoResult(Point removedPoint) {
        removePointMarker(removedPoint);
    }

    public void joinNode(View view) {
        if(isJoinNode){
            isJoinNode = false;
            view.setSelected(false);
        } else{
            this.isAddMarker = false;
            fab_add_marker.setSelected(false);
            this.isJoinNode = true;
            view.setSelected(true);
        }
    }


    public void addMarker(View view) {
        if(isAddMarker){
            isAddMarker = false;
            view.setSelected(false);
        } else{
            this.isAddMarker = true;
            fab_join_node.setSelected(false);
            this.isJoinNode = false;
            view.setSelected(true);
        }
    }

    public void resetPath(View view) {
        final int pathCategory = PATH_CATEGORY;
        String jenis = null;
        switch (pathCategory){
            case DatabaseContract.PathColumns.CATEGORY_WALKING:
                jenis = "JALUR BERJALAN";
                break;
            case DatabaseContract.PathColumns.CATEGORY_MOTORCYCLE:
                jenis = "JALUR BERMOTOR";
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Hapus seluruh jalur di "+jenis+" ?");
        builder.setMessage("Seluruh titik dan jalur akan dihapus dan tidak bisa dikembalikan.");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DatabaseOperationHelper.deleteAllResources(mDbHelper, pathCategory);
                getSupportLoaderManager().restartLoader(LOADER_POINT_ID, null, pointsLoaderCallback);
            }
        });
        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private int getPolylineCaps(){
        if(PATH_CATEGORY == DatabaseContract.PathColumns.CATEGORY_WALKING){
            return R.drawable.ic_arrow_red;
        } else if(PATH_CATEGORY == DatabaseContract.PathColumns.CATEGORY_MOTORCYCLE){
            return R.drawable.ic_arrow_blue;
        } else{
            return 0;
        }
    }
    private int getPolylineColor(){
        if(PATH_CATEGORY == DatabaseContract.PathColumns.CATEGORY_WALKING){
            return getResources().getColor(android.R.color.holo_red_dark);
        } else if(PATH_CATEGORY == DatabaseContract.PathColumns.CATEGORY_MOTORCYCLE){
            return getResources().getColor(android.R.color.holo_blue_dark);
        } else{
            return 0;
        }
    }
}
