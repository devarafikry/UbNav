package devfikr.skripsi.ubnav.ui.activity;

import android.content.ContentUris;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import devfikr.skripsi.ubnav.R;
import devfikr.skripsi.ubnav.data.DatabaseContract;
import devfikr.skripsi.ubnav.djikstra.Dijkstra;
import devfikr.skripsi.ubnav.djikstra.base.ConstantLocationDataManager;
import devfikr.skripsi.ubnav.djikstra.base.Graph;
import devfikr.skripsi.ubnav.djikstra.base.Helper;
import devfikr.skripsi.ubnav.djikstra.base.PointOfInterest;
import devfikr.skripsi.ubnav.model.Interchange;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;
import devfikr.skripsi.ubnav.util.LatLngConverter;
import timber.log.Timber;

public class NavigationInterchangeActivity
 extends FragmentActivity implements OnMapReadyCallback,
         GoogleMap.OnMarkerClickListener,
         GoogleMap.OnMapClickListener{
    @BindView(R.id.bottom_sheet)
    LinearLayout layoutBottomSheet;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_distance) TextView tv_distance;
    @BindView(R.id.btn_direction) Button btn_direction;
    @BindView(R.id.edit_panel) LinearLayout edit_panel;

    private GoogleMap mMap;

    //path arraylist for storing paths from db
    private ArrayList<Path> inBoundPaths = new ArrayList<>();
    //points arraylist for storing points from db
    private ArrayList<Point> inBoundPoints = new ArrayList<>();
    //path arraylist for storing paths from db
    private ArrayList<Path> outBoundPaths = new ArrayList<>();
    //points arraylist for storing points from db
    private ArrayList<Point> outBoundPoints = new ArrayList<>();
    //path arraylist for storing paths from db
    private ArrayList<Path> walkingPaths = new ArrayList<>();
    //points arraylist for storing points from db
    private ArrayList<Point> walkingPoints = new ArrayList<>();
    private ArrayList<Interchange> interchanges = new ArrayList<>();

    private ArrayList<devfikr.skripsi.ubnav.djikstra.base.Point> djikstraInBoundPoints = new ArrayList<>();
    private ArrayList<devfikr.skripsi.ubnav.djikstra.base.Path> djikstraInBoundPaths = new ArrayList<>();
    private ArrayList<devfikr.skripsi.ubnav.djikstra.base.Point> djikstraOutBoundPoints = new ArrayList<>();
    private ArrayList<devfikr.skripsi.ubnav.djikstra.base.Path> djikstraOutBoundPaths = new ArrayList<>();
    private ArrayList<devfikr.skripsi.ubnav.djikstra.base.Point> djikstraWalkingPoint = new ArrayList<>();
    private ArrayList<devfikr.skripsi.ubnav.djikstra.base.Path> djikstraWalkingPath = new ArrayList<>();

    private LoaderManager.LoaderCallbacks<Cursor> inBoundPathsLoaderCallback;
    private LoaderManager.LoaderCallbacks<Cursor> inBoundPointsLoaderCallback;
    private LoaderManager.LoaderCallbacks<Cursor> outBoundPathsLoaderCallback;
    private LoaderManager.LoaderCallbacks<Cursor> outBoundPointsLoaderCallback;
    private LoaderManager.LoaderCallbacks<Cursor> walkingPointsLoaderCallback;
    private LoaderManager.LoaderCallbacks<Cursor> walkingPathsLoaderCallback;
    private LoaderManager.LoaderCallbacks<Cursor> interchangePointsLoaderCallback;

    private LatLng selectedLatLng;
    private LatLng startLatLng;
    private Marker startMarker;
    private int LOADER_INBOUND_POINT_ID = 22;
    private int LOADER_INBOUND_PATH_ID = 33;
    private int LOADER_OUTBOUND_POINT_ID = 44;
    private int LOADER_OUTBOUND_PATH_ID = 55;
    private int LOADER_WALKING_POINT_ID = 66;
    private int LOADER_WALKING_PATH_ID = 77;
    private int LOADER_INTERCHANGE_ID = 88;
    private boolean isDebugMode = false;
    public static final String KEY_NAV_TYPE = "keyNavType";
    public static final String KEY_PATH_IN_OUT = "keyPathInOut";
    private int navCategory;
    private int navInOutCategory;

    BottomSheetBehavior sheetBehavior;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(getIntent() != null){
            navCategory = getIntent().getIntExtra(KEY_NAV_TYPE, DatabaseContract.PathColumns.CATEGORY_WALKING);
            navInOutCategory = getIntent().getIntExtra(KEY_PATH_IN_OUT, DatabaseContract.PathColumns.CATEGORY_ALLBOUND);
        }
        isDebugMode = sharedPreferences.getBoolean(getString(R.string.pref_debug_key),false);
        edit_panel.setVisibility(View.GONE);
        tv_name.setText("Silahkan pilih tujuan anda");
        tv_distance.setText(" ");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initInBoundPointsLoaderCallback();
        initInBoundPathsLoaderCallback();
        initOutBoundPointsLoaderCallback();
        initOutBoundPathsLoaderCallback();
        initWalkingPointsLoaderCallback();
        initWalkingPathsLoaderCallback();
        initInterchangesLoadersCallback();
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        btn_direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedLatLng!=null){
                    getDirection(selectedLatLng);
                }
            }
        });


    }

    private void initInBoundPointsLoaderCallback(){
        inBoundPointsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri getPointUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI_POINTS,
                        DatabaseContract.PathColumns.CATEGORY_MOTORCYCLE);

                return new CursorLoader(
                        NavigationInterchangeActivity.this,
                        ContentUris.withAppendedId(getPointUri,
                                DatabaseContract.PathColumns.CATEGORY_INBOUND),
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
                    NavigationInterchangeActivity.this.inBoundPoints = points;
                    getSupportLoaderManager().restartLoader(LOADER_INBOUND_PATH_ID, null, inBoundPathsLoaderCallback);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }
    private void initInBoundPathsLoaderCallback(){
        inBoundPathsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri getPathUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI_PATHS,
                        DatabaseContract.PathColumns.CATEGORY_MOTORCYCLE);

                return new CursorLoader(
                        NavigationInterchangeActivity.this,
                        ContentUris.withAppendedId(getPathUri,
                                DatabaseContract.PathColumns.CATEGORY_INBOUND),
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
                        for (Point point : inBoundPoints){
                            if (point.getId() == startPointId){
                                startPoint = point;
                            }
                            if (point.getId() == endPointId){
                                endPoint = point;
                            }
                        }
//                        if(startPoint == null){
//                            startPoint
//                        }
                        Path path = new Path(path_id, startPoint, endPoint);
                        cursorPaths.add(path);
                    }
                    inBoundPaths = cursorPaths;
                    getSupportLoaderManager().restartLoader(LOADER_OUTBOUND_POINT_ID, null, outBoundPointsLoaderCallback);

//                    generatePolylineForAllNodes(inBoundPaths);

                    LatLng gerbangVeteranMasuk = new LatLng(-7.956213, 112.613298);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gerbangVeteranMasuk,15));
//                    convertDjikstraResources();
                    mMap.setOnMarkerClickListener(NavigationInterchangeActivity.this);
//                    selectedPosition = points.get(points.size()-1);
                } else{

                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }
    private void initOutBoundPointsLoaderCallback(){
        outBoundPointsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri getPointUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI_POINTS,
                        DatabaseContract.PathColumns.CATEGORY_MOTORCYCLE);

                return new CursorLoader(
                        NavigationInterchangeActivity.this,
                        ContentUris.withAppendedId(getPointUri,
                                DatabaseContract.PathColumns.CATEGORY_OUTBOUND),
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
                    NavigationInterchangeActivity.this.outBoundPoints = points;
                    getSupportLoaderManager().restartLoader(LOADER_OUTBOUND_PATH_ID, null, outBoundPathsLoaderCallback);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }
    private void initOutBoundPathsLoaderCallback(){
        outBoundPathsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri getPathUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI_PATHS,
                        DatabaseContract.PathColumns.CATEGORY_MOTORCYCLE);

                return new CursorLoader(
                        NavigationInterchangeActivity.this,
                        ContentUris.withAppendedId(getPathUri,
                                DatabaseContract.PathColumns.CATEGORY_OUTBOUND),
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
                        for (Point point : outBoundPoints){
                            if (point.getId() == startPointId){
                                startPoint = point;
                            }
                            if (point.getId() == endPointId){
                                endPoint = point;
                            }
                        }
//                        if(startPoint == null){
//                            startPoint
//                        }
                        Path path = new Path(path_id, startPoint, endPoint);
                        cursorPaths.add(path);
                    }
                    outBoundPaths = cursorPaths;
                    getSupportLoaderManager().restartLoader(LOADER_WALKING_POINT_ID, null, walkingPointsLoaderCallback);
                    LatLng gerbangVeteranMasuk = new LatLng(-7.956213, 112.613298);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gerbangVeteranMasuk,15));
                    convertDjikstraResources();
                    mMap.setOnMarkerClickListener(NavigationInterchangeActivity.this);
//                    selectedPosition = points.get(points.size()-1);
                } else{

                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }
    private void initWalkingPointsLoaderCallback(){
        walkingPointsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri getPointUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI_POINTS,
                        DatabaseContract.PathColumns.CATEGORY_WALKING);

                return new CursorLoader(
                        NavigationInterchangeActivity.this,
                        ContentUris.withAppendedId(getPointUri,
                                DatabaseContract.PathColumns.CATEGORY_ALLBOUND),
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
                    NavigationInterchangeActivity.this.walkingPoints = points;
                    getSupportLoaderManager().restartLoader(LOADER_WALKING_PATH_ID, null, walkingPathsLoaderCallback);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }
    private void initWalkingPathsLoaderCallback(){
        walkingPathsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri getPathUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI_PATHS,
                        DatabaseContract.PathColumns.CATEGORY_WALKING);

                return new CursorLoader(
                        NavigationInterchangeActivity.this,
                        ContentUris.withAppendedId(getPathUri,
                                DatabaseContract.PathColumns.CATEGORY_ALLBOUND),
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
                        for (Point point : walkingPoints){
                            if (point.getId() == startPointId){
                                startPoint = point;
                            }
                            if (point.getId() == endPointId){
                                endPoint = point;
                            }
                        }
//                        if(startPoint == null){
//                            startPoint
//                        }
                        Path path = new Path(path_id, startPoint, endPoint);
                        cursorPaths.add(path);
                    }
                    walkingPaths = cursorPaths;
                    getSupportLoaderManager().restartLoader(LOADER_INTERCHANGE_ID, null, interchangePointsLoaderCallback);

//                    generatePolylineForAllNodes(inBoundPaths);

                    LatLng gerbangVeteranMasuk = new LatLng(-7.956213, 112.613298);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gerbangVeteranMasuk,15));
//                    convertDjikstraResources();
                    mMap.setOnMarkerClickListener(NavigationInterchangeActivity.this);
//                    selectedPosition = points.get(points.size()-1);
                } else{

                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }
    private void initInterchangesLoadersCallback(){
        interchangePointsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri getPointUri = DatabaseContract.CONTENT_URI_INTERCHANGES;

                return new CursorLoader(
                        NavigationInterchangeActivity.this,
                        getPointUri,
                        null,
                        null,
                        null,
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                ArrayList<Interchange> interchangeData
                        = new ArrayList<>();
                if(cursor != null){
                    for (int i=0;i<cursor.getCount();i++){
                        cursor.moveToPosition(i);
                        double latitude = DatabaseContract.getColumnDouble(
                                cursor, DatabaseContract.InterchangeColumns.COLUMN_LAT);
                        double longitude = DatabaseContract.getColumnDouble(
                                cursor, DatabaseContract.InterchangeColumns.COLUMN_LNG);
                        long id = DatabaseContract.getColumnLong(
                                cursor, DatabaseContract.InterchangeColumns._ID);
                        String name = DatabaseContract.getColumnString(
                                cursor, DatabaseContract.InterchangeColumns.COLUMN_NAME
                        );
                        String description = DatabaseContract.getColumnString(
                                cursor, DatabaseContract.InterchangeColumns.COLUMN_DESCRIPTION
                        );
                        int category = DatabaseContract.getColumnInt(
                                cursor, DatabaseContract.InterchangeColumns.COLUMN_INTERCHANGE_CATEGORY
                        );
                        int available = DatabaseContract.getColumnInt(
                                cursor, DatabaseContract.InterchangeColumns.COLUMN_AVAILABLE
                        );
                        Interchange interchange = new Interchange(
                                name,
                                description,
                                category,
                                available,
                                latitude,
                                longitude,
                                id
                        );
                        interchangeData.add(interchange);
                    }
                    NavigationInterchangeActivity.this.interchanges = interchangeData;
//                    getSupportLoaderManager().restartLoader(LOADER_WALKING_PATH_ID, null, walkingPathsLoaderCallback);
                    generatePolylineForAllNodes(inBoundPaths, outBoundPaths, walkingPaths, interchanges);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }
    private void generatePOI(){
        ArrayList<PointOfInterest> pois = ConstantLocationDataManager.fillPois();
        for (PointOfInterest poi:pois) {
            MarkerOptions markerOptions = new MarkerOptions().position(poi.toLatLng()).title(poi.getName());
            Marker marker = mMap.addMarker(markerOptions);
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            marker.setTag(poi.getName());
            marker.setTitle(poi.getName());
        }
    }

    private void generateInBoundPathFromStartTo(LatLng latLng) {
        djikstraInBoundPaths.clear();
        djikstraInBoundPoints.clear();
        djikstraOutBoundPaths.clear();
        djikstraOutBoundPoints.clear();
        convertDjikstraResources();
        Dijkstra dijkstra = new Dijkstra();
        Graph graph = new Graph();
        devfikr.skripsi.ubnav.djikstra.base.Point pointStart =
                new devfikr.skripsi.ubnav.djikstra.base.Point(
                        "Start",
                        startLatLng.latitude,
                        startLatLng.longitude
                );
        devfikr.skripsi.ubnav.djikstra.base.Point pointDest =
                new devfikr.skripsi.ubnav.djikstra.base.Point(
                        "Destination",
                        latLng.latitude,
                        latLng.longitude
                );
        Set<devfikr.skripsi.ubnav.djikstra.base.Point> pointsSet;

        pointsSet
                = Graph.build(djikstraInBoundPoints, djikstraInBoundPaths);

//        pointDest.setDistance(Double.MAX_VALUE);
//        for(devfikr.skripsi.ubnav.djikstra.base.Point point : pointsSet){
//            point.addDestination(pointDest, Double.MAX_VALUE);
//        }
        devfikr.skripsi.ubnav.djikstra.base.Point closestPointFromStart =
                getClosestNode(pointStart, pointsSet);
        Interchange closesInterchangeFromDest =
                getClosestInterchange(pointDest, interchanges);
        devfikr.skripsi.ubnav.djikstra.base.Point interchangePoint =
                new devfikr.skripsi.ubnav.djikstra.base.Point(
                        "Interchange",
                        closesInterchangeFromDest.getLat(),
                        closesInterchangeFromDest.getLng()
                );
        devfikr.skripsi.ubnav.djikstra.base.Point closestPointFromDest =
                getClosestNode(pointDest, pointsSet);
//        closestPointFromDest.addDestination(pointDest, Double.MAX_VALUE);
        graph.setPoints(pointsSet);
        try {
//            Timber.d("Djikstra point size :"+djikstraPoints.size());
//            Timber.d("Djikstra path size :"+djikstraPaths.size());

            Graph djikstraGraph;


                djikstraGraph = dijkstra.calculateShortestVectorPathFrom(
                        graph,
                        closestPointFromDest,
                        djikstraInBoundPaths
                );

            generatePath(djikstraGraph,closestPointFromStart, pointStart, closestPointFromDest, interchangePoint, pointDest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateOutBoundPathFromStartTo(LatLng latLng) {
        djikstraInBoundPaths.clear();
        djikstraInBoundPoints.clear();
        djikstraOutBoundPaths.clear();
        djikstraOutBoundPoints.clear();
        convertDjikstraResources();
        Dijkstra dijkstra = new Dijkstra();
        Graph graph = new Graph();
        devfikr.skripsi.ubnav.djikstra.base.Point pointStart =
                new devfikr.skripsi.ubnav.djikstra.base.Point(
                        "Start",
                        startLatLng.latitude,
                        startLatLng.longitude
                );
        devfikr.skripsi.ubnav.djikstra.base.Point pointDest =
                new devfikr.skripsi.ubnav.djikstra.base.Point(
                        "Destination",
                        latLng.latitude,
                        latLng.longitude
                );
        Set<devfikr.skripsi.ubnav.djikstra.base.Point> pointsSet;

        pointsSet
                = Graph.build(djikstraOutBoundPoints, djikstraOutBoundPaths);

//        pointDest.setDistance(Double.MAX_VALUE);
//        for(devfikr.skripsi.ubnav.djikstra.base.Point point : pointsSet){
//            point.addDestination(pointDest, Double.MAX_VALUE);
//        }
        devfikr.skripsi.ubnav.djikstra.base.Point closestPointFromStart =
                getClosestNode(pointStart, pointsSet);
        Interchange closesInterchangeFromDest =
                getClosestInterchange(pointDest, interchanges);
        devfikr.skripsi.ubnav.djikstra.base.Point interchangePoint =
                new devfikr.skripsi.ubnav.djikstra.base.Point(
                        "Interchange",
                        closesInterchangeFromDest.getLat(),
                        closesInterchangeFromDest.getLng()
                );
        devfikr.skripsi.ubnav.djikstra.base.Point closestPointFromDest =
                getClosestNode(interchangePoint, pointsSet);
//        closestPointFromDest.addDestination(pointDest, Double.MAX_VALUE);
        graph.setPoints(pointsSet);
        try {
//            Timber.d("Djikstra point size :"+djikstraPoints.size());
//            Timber.d("Djikstra path size :"+djikstraPaths.size());

            Graph djikstraGraph;


                djikstraGraph = dijkstra.calculateShortestVectorPathFrom(
                        graph,
                        closestPointFromDest,
                        djikstraOutBoundPaths
                );

            generateOutBoundPath(djikstraGraph, closestPointFromStart, pointStart, closestPointFromDest, interchangePoint, pointDest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void generateWalkingPathFromInterchange(LatLng interchange, LatLng dest) {
        djikstraWalkingPath.clear();
        djikstraWalkingPoint.clear();

        convertDjikstraResources();
        Dijkstra dijkstra = new Dijkstra();
        Graph graph = new Graph();
        devfikr.skripsi.ubnav.djikstra.base.Point pointStart =
                new devfikr.skripsi.ubnav.djikstra.base.Point(
                        "Start",
                        interchange.latitude,
                        interchange.longitude
                );
        devfikr.skripsi.ubnav.djikstra.base.Point pointDest =
                new devfikr.skripsi.ubnav.djikstra.base.Point(
                        "Destination",
                        dest.latitude,
                        dest.longitude
                );
        Set<devfikr.skripsi.ubnav.djikstra.base.Point> pointsSet;

        pointsSet
                = Graph.build(djikstraWalkingPoint, djikstraWalkingPath);

//        pointDest.setDistance(Double.MAX_VALUE);
//        for(devfikr.skripsi.ubnav.djikstra.base.Point point : pointsSet){
//            point.addDestination(pointDest, Double.MAX_VALUE);
//        }
        devfikr.skripsi.ubnav.djikstra.base.Point closestPointFromStart =
                getClosestNode(pointStart, pointsSet);
        devfikr.skripsi.ubnav.djikstra.base.Point closestPointFromDest =
                getClosestNode(pointDest, pointsSet);
//        closestPointFromDest.addDestination(pointDest, Double.MAX_VALUE);
        graph.setPoints(pointsSet);
        try {
//            Timber.d("Djikstra point size :"+djikstraPoints.size());
//            Timber.d("Djikstra path size :"+djikstraPaths.size());

            Graph djikstraGraph;


            djikstraGraph = dijkstra.calculateShortestPathFrom(
                    graph,
                    closestPointFromDest
            );

            generateWalkingPath(djikstraGraph, closestPointFromStart, pointStart, closestPointFromDest, pointDest, interchange);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private devfikr.skripsi.ubnav.djikstra.base.Point getClosestNode(
            devfikr.skripsi.ubnav.djikstra.base.Point pointTo,
            Set<devfikr.skripsi.ubnav.djikstra.base.Point> pointsSet) {
        devfikr.skripsi.ubnav.djikstra.base.Point closestNode = null;
        Double lowestDistance = Double.MAX_VALUE;
        for (devfikr.skripsi.ubnav.djikstra.base.Point point: pointsSet) {
            Double pointDistance = Helper.calculateDistance(pointTo, point);
            if (pointDistance < lowestDistance) {
                Timber.d(pointDistance+" < "+lowestDistance);
                lowestDistance = pointDistance;
                closestNode = point;
            }
        }
        return closestNode;
    }

    private Interchange getClosestInterchange(
            devfikr.skripsi.ubnav.djikstra.base.Point pointTo,
            ArrayList<Interchange> interchanges) {
        Interchange closestInterchange = null;
        Double lowestDistance = Double.MAX_VALUE;
        for (Interchange interchange: interchanges) {
            LatLng latLng = new LatLng(interchange.getLat(), interchange.getLng());
            double interchangeDistance = Helper.calculateDistance(pointTo, latLng);
            if(interchangeDistance < lowestDistance){
                lowestDistance = interchangeDistance;
                closestInterchange = interchange;
            }
        }
        return closestInterchange;
    }

    private void generatePolylineFromTwoPoint(
            devfikr.skripsi.ubnav.djikstra.base.Point pointA,
            devfikr.skripsi.ubnav.djikstra.base.Point pointB
    ){
        PolylineOptions polylineOptionsStart = new PolylineOptions();
        polylineOptionsStart.add(pointA.toLatLng());
        polylineOptionsStart.add(pointB.toLatLng());
        Polyline polylineStart = mMap.addPolyline(polylineOptionsStart);
        polylineStart.setColor(getResources().getColor(android.R.color.holo_green_dark));
        polylineStart.setStartCap( new CustomCap(
                BitmapDescriptorFactory.fromResource(R.drawable.ic_round_green), 20));
    }

    private void generatePolylineFromTwoPointWalking(
            devfikr.skripsi.ubnav.djikstra.base.Point pointA,
            devfikr.skripsi.ubnav.djikstra.base.Point pointB
    ){
        PolylineOptions polylineOptionsStart = new PolylineOptions();
        polylineOptionsStart.add(pointA.toLatLng());
        polylineOptionsStart.add(pointB.toLatLng());
        Polyline polylineStart = mMap.addPolyline(polylineOptionsStart);
        polylineStart.setColor(getResources().getColor(android.R.color.holo_orange_dark));
        polylineStart.setStartCap( new CustomCap(
                BitmapDescriptorFactory.fromResource(R.drawable.ic_round_orange), 20));
    }

    private void generatePolylineForAllNodes(
            ArrayList<Path> inBoundPaths,
            ArrayList<Path> outBoundPaths,
            ArrayList<Path> walkingPaths,
            ArrayList<Interchange> interchanges
    ){
        generateInterchanges();

        if(isDebugMode){
           for (Path path:inBoundPaths){
               PolylineOptions polylineOptions = new PolylineOptions();
               polylineOptions.add(
                       LatLngConverter.convertToGoogleLatLng(path.getStartLocation())
               );
               polylineOptions.add(
                       LatLngConverter.convertToGoogleLatLng(path.getEndLocation())
               );
               Polyline polylineStart = mMap.addPolyline(polylineOptions);
               polylineStart.setColor(getResources().getColor(android.R.color.holo_red_dark));
//               polylineStart.setEndCap( new CustomCap(
//                       BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_red), 20));
               polylineStart.setEndCap( new CustomCap(
                       BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_red), 10));
           }

           for (Path path:outBoundPaths){
               PolylineOptions polylineOptions = new PolylineOptions();
               polylineOptions.add(
                       LatLngConverter.convertToGoogleLatLng(path.getStartLocation())
               );
               polylineOptions.add(
                       LatLngConverter.convertToGoogleLatLng(path.getEndLocation())
               );
               Polyline polylineStart = mMap.addPolyline(polylineOptions);
               polylineStart.setColor(getResources().getColor(R.color.colorPrimary));
//               polylineStart.setEndCap( new CustomCap(
//                       BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_red), 20));
               polylineStart.setEndCap( new CustomCap(
                       BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_blue), 10));
           }

           for (Path path:walkingPaths){
               PolylineOptions polylineOptions = new PolylineOptions();
               polylineOptions.add(
                       LatLngConverter.convertToGoogleLatLng(path.getStartLocation())
               );
               polylineOptions.add(
                       LatLngConverter.convertToGoogleLatLng(path.getEndLocation())
               );
               Polyline polylineStart = mMap.addPolyline(polylineOptions);
               polylineStart.setColor(getResources().getColor(android.R.color.holo_purple));
//               polylineStart.setEndCap( new CustomCap(
//                       BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_red), 20));
               polylineStart.setEndCap( new CustomCap(
                       BitmapDescriptorFactory.fromResource(R.drawable.ic_arrow_purple), 10));
           }

           generateInterchanges();
       }
    }

    private void generateInterchanges(){
        for (Interchange interchange : interchanges){
            LatLng latLng = new LatLng(interchange.getLat(),interchange.getLng());
            MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_park));
            Marker marker = mMap.addMarker(markerOptions);
            marker.setTitle(interchange.getName());
            marker.setTag(interchange.getId());
        }
    }

    private void generatePath(Graph graph,
                              devfikr.skripsi.ubnav.djikstra.base.Point closestStartPoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point startPoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point closestDestinationPoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point interchangePoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point destPoint
                              ){

        boolean isFoundShortestPath = false;
        Set<devfikr.skripsi.ubnav.djikstra.base.Point> navPoint =
                graph.getPoints();
        PolylineOptions polylineOptions = new PolylineOptions();
        Timber.d("Djikstra Points "+navPoint.size());
        List<devfikr.skripsi.ubnav.djikstra.base.Point> shortestPath = null;
        for (devfikr.skripsi.ubnav.djikstra.base.Point point: navPoint) {
            if(point.id.equals(closestStartPoint.id)){
                Timber.d("Zilong");
//                polylineOptions.add(point.toLatLng());
                generatePolylineFromTwoPoint(startPoint, closestStartPoint);
                shortestPath =
                        point.getShortestPath();
                if(shortestPath.size() == 0){
                    isFoundShortestPath = false;
                } else{
                    isFoundShortestPath = true;
                }
//                polylineOptions.add(point.toLatLng());

            }
        }
        if(isFoundShortestPath){
            for (devfikr.skripsi.ubnav.djikstra.base.Point p: shortestPath) {
//                    mMap.addMarker(new MarkerOptions().position(p.toLatLng()).title(p.id));
                polylineOptions.add(p.toLatLng());
            }
            polylineOptions.add(closestStartPoint.toLatLng());

            generatePolylineFromTwoPoint(closestDestinationPoint, interchangePoint);

            Polyline polyline = mMap.addPolyline(polylineOptions);
            polyline.setStartCap( new CustomCap(
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_round_green), 20));
            polyline.setColor(getResources().getColor(android.R.color.holo_green_dark));
            LatLng latLng = polylineOptions.getPoints().get(polylineOptions.getPoints().size()-1);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
            generateWalkingPathFromInterchange(interchangePoint.toLatLng(), destPoint.toLatLng());
        } else{
            generateOutBoundPathFromStartTo(destPoint.toLatLng());
        }
    }

    private void generateWalkingPath(Graph graph,
                              devfikr.skripsi.ubnav.djikstra.base.Point closestStartPoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point startPoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point closestDestinationPoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point destPoint,
                                     LatLng interchangeLatLng
    ){

        boolean isFoundShortestPath = false;
        Set<devfikr.skripsi.ubnav.djikstra.base.Point> navPoint =
                graph.getPoints();
        PolylineOptions polylineOptions = new PolylineOptions();
        Timber.d("Djikstra Points "+navPoint.size());
        List<devfikr.skripsi.ubnav.djikstra.base.Point> shortestPath = null;
        for (devfikr.skripsi.ubnav.djikstra.base.Point point: navPoint) {
            if(point.id.equals(closestStartPoint.id)){
                Timber.d("Zilong");
//                generatePolylineFromTwoPoint(startPoint, closestStartPoint);
                shortestPath =
                        point.getShortestPath();
                if(shortestPath.size() == 0){
                    isFoundShortestPath = false;
                } else{
                    isFoundShortestPath = true;
                }
//                polylineOptions.add(point.toLatLng());

            }
        }
        if(isFoundShortestPath){
            for (devfikr.skripsi.ubnav.djikstra.base.Point p: shortestPath) {
//                    mMap.addMarker(new MarkerOptions().position(p.toLatLng()).title(p.id));
                polylineOptions.add(p.toLatLng());
            }
            polylineOptions.add(interchangeLatLng);

            generatePolylineFromTwoPointWalking(closestDestinationPoint, destPoint);

            Polyline polyline = mMap.addPolyline(polylineOptions);
            polyline.setStartCap( new CustomCap(
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_round_orange), 20));
            polyline.setColor(getResources().getColor(android.R.color.holo_orange_dark));
            LatLng latLng = polylineOptions.getPoints().get(polylineOptions.getPoints().size()-1);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
        } else{
//            generateOutBoundPathFromStartTo(destPoint.toLatLng());
        }
    }

    private void generateOutBoundPath(Graph graph,
                              devfikr.skripsi.ubnav.djikstra.base.Point closestStartPoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point startPoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point closestDestinationPoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point interchangePoint,
                                      devfikr.skripsi.ubnav.djikstra.base.Point destPoint
    ){

        boolean isFoundShortestPath = false;
        Set<devfikr.skripsi.ubnav.djikstra.base.Point> navPoint =
                graph.getPoints();
        PolylineOptions polylineOptions = new PolylineOptions();
        Timber.d("Djikstra Points "+navPoint.size());
        List<devfikr.skripsi.ubnav.djikstra.base.Point> shortestPath = null;
        for (devfikr.skripsi.ubnav.djikstra.base.Point point: navPoint) {
            if(point.id.equals(closestStartPoint.id)){
                Timber.d("Zilong");
//                polylineOptions.add(point.toLatLng());
                generatePolylineFromTwoPoint(startPoint, closestStartPoint);
                shortestPath =
                        point.getShortestPath();
                if(shortestPath.size() == 0){
                    isFoundShortestPath = false;
                } else{
                    isFoundShortestPath = true;
                }
//                polylineOptions.add(point.toLatLng());

            }
        }
        if(isFoundShortestPath){
            for (devfikr.skripsi.ubnav.djikstra.base.Point p: shortestPath) {
//                    mMap.addMarker(new MarkerOptions().position(p.toLatLng()).title(p.id));
                polylineOptions.add(p.toLatLng());
            }
            polylineOptions.add(closestStartPoint.toLatLng());

            generatePolylineFromTwoPoint(closestDestinationPoint, interchangePoint);

            Polyline polyline = mMap.addPolyline(polylineOptions);
            polyline.setStartCap( new CustomCap(
                    BitmapDescriptorFactory.fromResource(R.drawable.ic_round_green), 20));
            polyline.setColor(getResources().getColor(android.R.color.holo_green_dark));
            LatLng latLng = polylineOptions.getPoints().get(polylineOptions.getPoints().size()-1);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
            generateWalkingPathFromInterchange(interchangePoint.toLatLng(), destPoint.toLatLng());
        } else{
//            generateOutBoundPathFromStartTo(destPoint.toLatLng());
        }
    }
    private void convertDjikstraResources(){
        for (Point point: inBoundPoints) {
            devfikr.skripsi.ubnav.djikstra.base.Point djikstraPoint =
                    new devfikr.skripsi.ubnav.djikstra.base.Point(
                            String.valueOf(point.getId()),
                            point.getLatitude(),
                            point.getLongitude());
            djikstraInBoundPoints.add(djikstraPoint);
        }
        for (Path path: inBoundPaths) {
            devfikr.skripsi.ubnav.djikstra.base.Path djikstraPath =
                    new devfikr.skripsi.ubnav.djikstra.base.Path(
                            String.valueOf(path.getId()),
                            String.valueOf(path.getStartLocation().getId()),
                            path.getStartLocation().getLatitude(),
                            path.getStartLocation().getLongitude(),
                            String.valueOf(path.getEndLocation().getId()),
                            path.getEndLocation().getLatitude(),
                            path.getEndLocation().getLongitude()
                            );
            djikstraInBoundPaths.add(djikstraPath);
        }
        for (Point point: outBoundPoints) {
            devfikr.skripsi.ubnav.djikstra.base.Point djikstraPoint =
                    new devfikr.skripsi.ubnav.djikstra.base.Point(
                            String.valueOf(point.getId()),
                            point.getLatitude(),
                            point.getLongitude());
            djikstraOutBoundPoints.add(djikstraPoint);
        }
        for (Path path: outBoundPaths) {
            devfikr.skripsi.ubnav.djikstra.base.Path djikstraPath =
                    new devfikr.skripsi.ubnav.djikstra.base.Path(
                            String.valueOf(path.getId()),
                            String.valueOf(path.getStartLocation().getId()),
                            path.getStartLocation().getLatitude(),
                            path.getStartLocation().getLongitude(),
                            String.valueOf(path.getEndLocation().getId()),
                            path.getEndLocation().getLatitude(),
                            path.getEndLocation().getLongitude()
                    );
            djikstraOutBoundPaths.add(djikstraPath);
        }

        for (Point point: walkingPoints) {
            devfikr.skripsi.ubnav.djikstra.base.Point djikstraPoint =
                    new devfikr.skripsi.ubnav.djikstra.base.Point(
                            String.valueOf(point.getId()),
                            point.getLatitude(),
                            point.getLongitude());
            djikstraWalkingPoint.add(djikstraPoint);
        }

        for (Path path: walkingPaths) {
            devfikr.skripsi.ubnav.djikstra.base.Path djikstraPath =
                    new devfikr.skripsi.ubnav.djikstra.base.Path(
                            String.valueOf(path.getId()),
                            String.valueOf(path.getStartLocation().getId()),
                            path.getStartLocation().getLatitude(),
                            path.getStartLocation().getLongitude(),
                            String.valueOf(path.getEndLocation().getId()),
                            path.getEndLocation().getLatitude(),
                            path.getEndLocation().getLongitude()
                    );
            djikstraWalkingPath.add(djikstraPath);
        }
    }



    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        tv_name.setText(marker.getTag().toString());
//        tv_distance
        this.selectedLatLng = marker.getPosition();
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        return true;
    }

    private void getDirection(LatLng latLng){
        mMap.clear();
        putPositionMarker(startLatLng);
        generatePOI();
        generatePolylineForAllNodes(inBoundPaths, outBoundPaths, walkingPaths, interchanges);
        generateInBoundPathFromStartTo(latLng);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng gerbangVeteranMasuk = new LatLng(-7.956413, 112.613298);
        this.startLatLng = gerbangVeteranMasuk;
        putPositionMarker(gerbangVeteranMasuk);
        getSupportLoaderManager().restartLoader(LOADER_INBOUND_POINT_ID, null, inBoundPointsLoaderCallback);

        generatePOI();
        mMap.setOnMapClickListener(this);

//        mMap.setOnPolylineClickListener(this);
//        mMap.setOnMarkerDragListener(this);
//        mMap.setOnMarkerClickListener(this);
    }

    private void putPositionMarker(LatLng latLng) {
        MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_direction_walk));
        Marker marker = mMap.addMarker(markerOptions);
        marker.setTag("Posisi Anda");
        marker.setTitle("Posisi Anda");
        this.startMarker = marker;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else{
            mMap.clear();
            generatePOI();
            generatePolylineForAllNodes(inBoundPaths, outBoundPaths, walkingPaths, interchanges);
            this.startLatLng = latLng;
            this.startMarker.remove();
            putPositionMarker(latLng);
        }
    }
}