package devfikr.skripsi.ubnav.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CustomCap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.lapism.searchview.SearchAdapter;
import com.lapism.searchview.SearchFilter;
import com.lapism.searchview.SearchHistoryTable;
import com.lapism.searchview.SearchItem;
import com.lapism.searchview.SearchView;

import java.security.Permission;
import java.util.ArrayList;
import java.util.LinkedList;
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
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.model.Point;
import devfikr.skripsi.ubnav.util.LatLngConverter;
import timber.log.Timber;

public class NavigationActivity
 extends AppCompatActivity implements OnMapReadyCallback,
         GoogleMap.OnMarkerClickListener,
         GoogleMap.OnMapClickListener{
    @BindView(R.id.bottom_sheet)
    LinearLayout layoutBottomSheet;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.tv_distance) TextView tv_distance;
    @BindView(R.id.btn_direction) Button btn_direction;
    @BindView(R.id.edit_panel) LinearLayout edit_panel;
    @BindView(R.id.searchView)
    SearchView mSearchView;
    private GoogleMap mMap;

    //path arraylist for storing paths from db
    private ArrayList<Path> paths = new ArrayList<>();
    //points arraylist for storing points from db
    private ArrayList<Point> points= new ArrayList<>();

    private ArrayList<devfikr.skripsi.ubnav.djikstra.base.Point> djikstraPoints = new ArrayList<>();
    private ArrayList<devfikr.skripsi.ubnav.djikstra.base.Path> djikstraPaths = new ArrayList<>();

    private LoaderManager.LoaderCallbacks<Cursor> pathsLoaderCallback;
    private LoaderManager.LoaderCallbacks<Cursor> pointsLoaderCallback;
    private LatLng selectedLatLng;
    private LatLng startLatLng;
    private Marker startMarker;
    private int LOADER_POINT_ID = 22;
    private int LOADER_PATH_ID = 33;

    private boolean isDebugMode = false;
    public static final String KEY_NAV_TYPE = "keyNavType";
    public static final String KEY_TITLE = "keyTitle";
    private int navCategory;
    private ArrayList<Marker> poiMarkers = new ArrayList<>();

    BottomSheetBehavior sheetBehavior;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if(getIntent() != null){
            navCategory = getIntent().getIntExtra(KEY_NAV_TYPE, DatabaseContract.PathColumns.CATEGORY_WALKING);
            String title = getIntent().getStringExtra(KEY_TITLE);
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mSearchView.setVisibility(View.VISIBLE);
        isDebugMode = sharedPreferences.getBoolean(getString(R.string.pref_debug_key),false);
        edit_panel.setVisibility(View.GONE);
        tv_name.setText("Silahkan pilih tujuan anda");
        tv_distance.setText(" ");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initPointsLoaderCallback();
        initPathsLoaderCallback();
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        btn_direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedLatLng!=null){
                    getDirection(selectedLatLng);
                }
            }
        });

        final SearchHistoryTable mHistoryDatabase = new SearchHistoryTable(NavigationActivity.this);

        if (mSearchView != null) {
            mSearchView.setVersionMargins(SearchView.VersionMargins.TOOLBAR_SMALL);
            mSearchView.setNavigationIcon(R.drawable.ic_search);
            mSearchView.setNavigationIconClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSearchView.open(true);
                }
            });
            mSearchView.setNavigationIconAnimation(true);
            mSearchView.setHint("Cari Tujuan...");
            mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
//                    mHistoryDatabase.addItem(new SearchItem(query));
                    mSearchView.close(false);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

            mSearchView.setOnOpenCloseListener(new SearchView.OnOpenCloseListener() {
                @Override
                public boolean onClose() {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    return false;
                }

                @Override
                public boolean onOpen() {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    return false;
                }
            });

            mSearchView.setVoiceText("Sebutkan tujuan anda...");
            mSearchView.setOnVoiceIconClickListener(new SearchView.OnVoiceIconClickListener() {
                @Override
                public void onVoiceIconClick() {
                    int permissionCheck = ContextCompat.checkSelfPermission(NavigationActivity.this,
                            Manifest.permission.RECORD_AUDIO);
                    if(permissionCheck == PackageManager.PERMISSION_DENIED){
                        ActivityCompat.requestPermissions(NavigationActivity.this,
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                88);
                    }
                }
            });


            List<SearchItem> suggestionsList = new ArrayList<>();
            ArrayList<PointOfInterest> pois = ConstantLocationDataManager.fillPois();
            for(int i=0;i<pois.size();i++){
                suggestionsList.add(new SearchItem(pois.get(i).getName()));
            }
            SearchAdapter searchAdapter = new SearchAdapter(this, suggestionsList);

            searchAdapter.setOnSearchItemClickListener(new SearchAdapter.OnSearchItemClickListener() {
                @Override
                public void onSearchItemClick(View view, int position, String text) {
//                    mHistoryDatabase.addItem(new SearchItem(text));
                    Timber.d("Selected: "+text);
                    Marker selectedMarker = null;
                    for (Marker marker : poiMarkers){
                        if(marker.getTag() != null){
                            if(marker.getTag().toString().equals(
                                    text
                            )){
                                selectedMarker = marker;
                            }
                        }
                    }
                    getDirection(selectedMarker.getPosition());
                    tv_name.setText(text);
                    mSearchView.close(false);
                }
            });

            mSearchView.setAdapter(searchAdapter);
            searchAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SearchView.SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    mSearchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initPointsLoaderCallback(){
        pointsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri getPointUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI_POINTS,
                        navCategory);

                return new CursorLoader(
                        NavigationActivity.this,
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
                    NavigationActivity.this.points = points;
                    getSupportLoaderManager().restartLoader(LOADER_PATH_ID, null, pathsLoaderCallback);
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
            poiMarkers.add(marker);
        }
    }

    private void generatePathFromStartTo(LatLng latLng) {
        djikstraPoints.clear();
        djikstraPaths.clear();
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
                = Graph.build(djikstraPoints, djikstraPaths);

        devfikr.skripsi.ubnav.djikstra.base.Point closestPointFromStart =
                getClosestNode(pointStart, pointsSet);
        devfikr.skripsi.ubnav.djikstra.base.Point closestPointFromDest =
                getClosestNode(pointDest, pointsSet);
        graph.setPoints(pointsSet);
        try {
            Timber.d("Djikstra point size :"+djikstraPoints.size());
            Timber.d("Djikstra path size :"+djikstraPaths.size());

            Graph djikstraGraph;

            if(navCategory == DatabaseContract.PathColumns.CATEGORY_WALKING){
                djikstraGraph = dijkstra.calculateShortestPathFrom(
                        graph,
                        closestPointFromDest
                );
            } else{
                djikstraGraph = dijkstra.calculateShortestVectorPathFrom(
                        graph,
                        closestPointFromDest,
                        djikstraPaths
                );
            }
            generatePath(djikstraGraph,pointStart, closestPointFromStart, closestPointFromDest, pointDest);
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
    private devfikr.skripsi.ubnav.djikstra.base.Point getClosestNode(
            Set<devfikr.skripsi.ubnav.djikstra.base.Path> paths,
            devfikr.skripsi.ubnav.djikstra.base.Point pointTo,
            Set<devfikr.skripsi.ubnav.djikstra.base.Point> pointsSet) {
        devfikr.skripsi.ubnav.djikstra.base.Point closestNode = null;
        Double lowestDistance = Double.MAX_VALUE;

        for (devfikr.skripsi.ubnav.djikstra.base.Point point: pointsSet) {
            Double pointDistance = Helper.calculateDistance(pointTo, point);
//            for (devfikr.skripsi.ubnav.djikstra.base.Path path : paths){
//                if(path.startPoint)
//            }
            if (pointDistance < lowestDistance) {
                Timber.d(pointDistance+" < "+lowestDistance);
                lowestDistance = pointDistance;
                closestNode = point;
            }
        }
        return closestNode;
    }

    private devfikr.skripsi.ubnav.djikstra.base.Point getOutBoundNode(
            devfikr.skripsi.ubnav.djikstra.base.Point closestStart,
            devfikr.skripsi.ubnav.djikstra.base.Point pointTo,
            Set<devfikr.skripsi.ubnav.djikstra.base.Point> pointsSet) {
        devfikr.skripsi.ubnav.djikstra.base.Point closestNode = null;
        Double lowestDistance = Double.MAX_VALUE;
        for (devfikr.skripsi.ubnav.djikstra.base.Point point: pointsSet) {
            Double pointDistance = Helper.calculateDistance(pointTo, point);
//            boolean isBackward = false;
            if(!(point.getShortestPath().contains(closestStart))){
                if (pointDistance < lowestDistance) {
                    Timber.d(pointDistance+" < "+lowestDistance);
                    lowestDistance = pointDistance;
                    closestNode = point;
                }
            }
        }
        return closestNode;
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

    private void generatePolylineForAllNodes(
            ArrayList<Path> paths
    ){
       if(isDebugMode){
           for (Path path:paths){
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
       }
    }

    private void generatePath(Graph graph,
                              devfikr.skripsi.ubnav.djikstra.base.Point closestStartPoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point startPoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point closestDestinationPoint,
                              devfikr.skripsi.ubnav.djikstra.base.Point destPoint
                              ){

        Set<devfikr.skripsi.ubnav.djikstra.base.Point> navPoint =
                graph.getPoints();
        PolylineOptions polylineOptions = new PolylineOptions();
        Timber.d("Djikstra Points "+navPoint.size());

        for (devfikr.skripsi.ubnav.djikstra.base.Point point: navPoint) {
            if(point.id.equals(startPoint.id)){
                Timber.d("Zilong");
//                polylineOptions.add(point.toLatLng());
                generatePolylineFromTwoPoint(startPoint, closestStartPoint);
                List<devfikr.skripsi.ubnav.djikstra.base.Point> shortestPath =
                        point.getShortestPath();
                for (devfikr.skripsi.ubnav.djikstra.base.Point p: shortestPath) {
//                    mMap.addMarker(new MarkerOptions().position(p.toLatLng()).title(p.id));
                    polylineOptions.add(p.toLatLng());
                }
                polylineOptions.add(point.toLatLng());

            }
        }
        generatePolylineFromTwoPoint(closestDestinationPoint, destPoint);

        Polyline polyline = mMap.addPolyline(polylineOptions);
        polyline.setStartCap( new CustomCap(
                BitmapDescriptorFactory.fromResource(R.drawable.ic_round_green), 20));
        polyline.setColor(getResources().getColor(android.R.color.holo_green_dark));
        LatLng latLng = polylineOptions.getPoints().get(polylineOptions.getPoints().size()-1);
        float zoom = mMap.getCameraPosition().zoom;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destPoint.toLatLng(),zoom));
    }

    private void convertDjikstraResources(){
        for (Point point: points) {
            devfikr.skripsi.ubnav.djikstra.base.Point djikstraPoint =
                    new devfikr.skripsi.ubnav.djikstra.base.Point(
                            String.valueOf(point.getId()),
                            point.getLatitude(),
                            point.getLongitude());
            djikstraPoints.add(djikstraPoint);
        }
        for (Path path: paths) {
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
            djikstraPaths.add(djikstraPath);
        }
    }

    private void initPathsLoaderCallback(){
        pathsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri getPathUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI_PATHS,
                        navCategory);

                return new CursorLoader(
                        NavigationActivity.this,
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
//                        if(startPoint == null){
//                            startPoint
//                        }
                        Path path = new Path(path_id, startPoint, endPoint);
                        cursorPaths.add(path);
                    }
                    paths = cursorPaths;
                            generatePolylineForAllNodes(paths);
                    LatLng gerbangVeteranMasuk = new LatLng(-7.956213, 112.613298);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gerbangVeteranMasuk,15));
                    convertDjikstraResources();
                    mMap.setOnMarkerClickListener(NavigationActivity.this);
//                    selectedPosition = points.get(points.size()-1);
                } else{

                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {

            }
        };
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.showInfoWindow();
        float zoom = mMap.getCameraPosition().zoom;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(),zoom));
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
        generatePolylineForAllNodes(paths);
        generatePathFromStartTo(latLng);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng gerbangVeteranMasuk = new LatLng(-7.956413, 112.613298);
        this.startLatLng = gerbangVeteranMasuk;
        putPositionMarker(gerbangVeteranMasuk);
        getSupportLoaderManager().restartLoader(LOADER_POINT_ID, null, pointsLoaderCallback);
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
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else{
            mMap.clear();
            generatePOI();
            generatePolylineForAllNodes(paths);
            this.startLatLng = latLng;
            this.startMarker.remove();
            putPositionMarker(latLng);
        }
    }
}
