package devfikr.skripsi.ubnav.ui.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import devfikr.skripsi.ubnav.R;
import devfikr.skripsi.ubnav.data.DatabaseContract;
import devfikr.skripsi.ubnav.data.DatabaseHelper;
import devfikr.skripsi.ubnav.model.Interchange;
import timber.log.Timber;

public class AddInterchangeActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerDragListener,
        GoogleMap.OnMarkerClickListener
      {


    private GoogleMap mMap;


    private ArrayList<Interchange> interchanges = new ArrayList<>();
    //marker arraylist for storing marker
    private ArrayList<Marker> markers = new ArrayList<>();

    private Snackbar snackbar;
    private LoaderManager.LoaderCallbacks<Cursor> pointsLoaderCallback;
    private int LOADER_POINT_ID = 22;
    private int LOADER_PATH_ID = 33;


    @BindView(R.id.root_map)
    CoordinatorLayout root_map;
    Toast mToast;

    @BindView(R.id.bottom_sheet)
    LinearLayout layoutBottomSheet;
//    @BindView(R.id.fab_save)
//    FloatingActionButton fab_save;
    private DatabaseHelper mDbHelper;

    private void showMessage(String s){
        if(mToast != null){
            mToast.cancel();
        }
        mToast = Toast.makeText(this, s, Toast.LENGTH_SHORT);
        mToast.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.plant(new Timber.DebugTree());
        setContentView(R.layout.activity_add_interchange);
        ButterKnife.bind(this);
        layoutBottomSheet.setVisibility(View.GONE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDbHelper = new DatabaseHelper(this);
        mDbHelper.getWritableDatabase();

        initPointsLoaderCallback();
    }

    private void initPointsLoaderCallback(){
        pointsLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                Uri getPointUri = DatabaseContract.CONTENT_URI_INTERCHANGES;

                return new CursorLoader(
                        AddInterchangeActivity.this,
                        getPointUri,
                        null,
                        null,
                        null,
                        null
                );
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
                ArrayList<Interchange> interchanges
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
                       interchanges.add(interchange);
                       addInterchangeMarker(interchange);
                   }
                   AddInterchangeActivity.this.interchanges = interchanges;
//                   generatePoint();
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
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);

        LatLng gerbangVeteranMasuk = new LatLng(-7.956213, 112.613298);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gerbangVeteranMasuk,15));
    }

    private void addInterchangeMarker(Interchange interchange){
        LatLng latLng = new LatLng(interchange.getLat(), interchange.getLng());
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("New Position");
        Marker marker = mMap.addMarker(markerOptions);
        marker.setDraggable(true);
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markers.add(marker);
        marker.setTag(interchange.getId());
    }

    @Override
    public void onMapClick(LatLng latLng) {
        ContentValues cv = new ContentValues();
        String name = "Parkiran";
        String description = "Parkiran untuk uji coba";
        int available = 1;
        int category = DatabaseContract.InterchangeColumns.CATEGORY_MOTORCYCLE_AND_CAR;
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_NAME, name);
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_DESCRIPTION, description);
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_AVAILABLE, available);
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_INTERCHANGE_CATEGORY, category);
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_LAT, latLng.latitude);
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_LNG, latLng.longitude);
        Long id = mDbHelper.getWritableDatabase()
                .insert(
                        DatabaseContract.TABLE_INTERCHANGE,
                        null,
                        cv
                );
        Interchange interchange = new Interchange(
                name,
                description,
                category,
                available,
                latLng.latitude,
                latLng.longitude,
                id
        );
        addInterchangeMarker(interchange);
    }


    private void generatePoint(){
        mMap.clear();
        markers.clear();

        for(int i =0;i<interchanges.size();i++){
            Interchange interchange = interchanges.get(i);
            addInterchangeMarker(interchange);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        long selectedInterchangeId = Long.valueOf(marker.getTag().toString());
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        long selectedInterchangeId = Long.valueOf(marker.getTag().toString());
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        long selectedInterchangeId = Long.valueOf(marker.getTag().toString());
        Interchange interchange = null;
        for (Interchange inter : interchanges){
            if (inter.getId() == selectedInterchangeId){
                interchange = inter;
            }
        }
        ContentValues cv = new ContentValues();
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_NAME, interchange.getName());
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_DESCRIPTION, interchange.getDescription());
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_AVAILABLE, interchange.getAvailable());
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_INTERCHANGE_CATEGORY, interchange.getCategory());
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_LAT, interchange.getLat());
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_LNG, interchange.getLng());

        mDbHelper.getWritableDatabase()
                .update(
                        DatabaseContract.TABLE_INTERCHANGE,
                        cv,
                        "_id="+selectedInterchangeId,
                        null
                );
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        return true;
    }


    public void deleteInterchange(View view) {

    }
      }
