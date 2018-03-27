package devfikr.skripsi.ubnav.ui.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
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

public class AddInterchangeActivity extends AppCompatActivity implements OnMapReadyCallback,
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
    private  Marker selectedMarker;
    private LatLng selectedLatLng;
          BottomSheetBehavior sheetBehavior;



    @BindView(R.id.root_map)
    CoordinatorLayout root_map;
    Toast mToast;

    @BindView(R.id.bottom_sheet)
    LinearLayout layoutBottomSheet;
    @BindView(R.id.edt_name)
          EditText edt_name;
    @BindView(R.id.edt_description) EditText edt_description;
    @BindView(R.id.spn_category)
          Spinner spn_category;
    @BindView(R.id.sw_available)
          Switch sw_available;
    @BindView(R.id.btn_submit)
    Button btn_submit;
    @BindView(R.id.btn_delete) Button btn_delete;
    @BindView(R.id.btn_update) Button btn_update;

//    @BindView(R.id.fab_save)
//    FloatingActionButton fab_save;
    private DatabaseHelper mDbHelper;

    private String name, description;
    private int category;
    private int available;

    private boolean isTyping = false;
    private Marker toBeAddedMarker = null;
          private long toUpdateInterchangeId;
          private Marker toBeDeletedMarker;

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
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//        layoutBottomSheet.setVisibility(View.GONE);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mDbHelper = new DatabaseHelper(this);
        mDbHelper.getWritableDatabase();

        getSupportActionBar().setTitle("Kelola Interchange");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initPointsLoaderCallback();
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
        if(isTyping == false){
            btn_submit.setText("Daftarkan Interchange");
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            this.selectedLatLng = latLng;
            isInsert(true);
            isTyping = true;
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("New Position");
            this.toBeAddedMarker = mMap.addMarker(markerOptions);
        } else{
            invalidateTyping();
        }
    }

    private void isInsert(boolean b){
        if (b){
            btn_submit.setVisibility(View.VISIBLE);
            btn_update.setVisibility(View.GONE);
        } else{
            btn_submit.setVisibility(View.GONE);
            btn_update.setVisibility(View.VISIBLE);
        }
    }

    private void invalidateTyping(){
        if(toBeAddedMarker != null){
            toBeAddedMarker.remove();
        }
        edt_name.setText("");
        edt_description.setText("");
        btn_delete.setVisibility(View.GONE);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        isTyping = false;
    }

    private void generatePoint(){
        mMap.clear();
        markers.clear();

        for(int i =0;i<interchanges.size();i++){
            Interchange interchange = interchanges.get(i);
            addInterchangeMarker(interchange);
        }
    }

    private void selectMarker(Marker marker){
        if(selectedMarker != null) {
            selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            selectedMarker = marker;
            selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        } else{
            selectedMarker = marker;
            selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        selectMarker(marker);
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
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_LAT, marker.getPosition().latitude);
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_LNG, marker.getPosition().longitude);

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
        selectMarker(marker);

        if(isTyping){
            invalidateTyping();
        } else{
            isTyping = true;
            isInsert(false);
            long interchangeId = (Long)marker.getTag();
            Interchange inter = null;
            for(int i =0;i<interchanges.size();i++){
                if(interchanges.get(i).getId() == interchangeId){
                    inter = interchanges.get(i);
                }
            }
            this.toBeDeletedMarker = marker;
            selectedLatLng = marker.getPosition();
            this.toUpdateInterchangeId = inter.getId();
            edt_name.setText(inter.getName());
            edt_description.setText(inter.getDescription());
            spn_category.setSelection(inter.getCategory());
            if(inter.getAvailable() == 0){
                sw_available.setChecked(false);
            } else{
                sw_available.setChecked(true);
            }
            btn_submit.setText("Update Interchange");
            btn_delete.setVisibility(View.VISIBLE);
            sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        return true;
    }


    public void deleteInterchange(View view) {
        long toBeDeletedId = toUpdateInterchangeId;
        mDbHelper.getWritableDatabase()
                .delete(
                        DatabaseContract.TABLE_INTERCHANGE,
                        "_id=?",
                        new String[]{String.valueOf(toBeDeletedId)}
                );
        toBeDeletedMarker.remove();
        selectedMarker = null;
        mMap.clear();
        getSupportLoaderManager().restartLoader(LOADER_POINT_ID, null, pointsLoaderCallback);
        invalidateTyping();
    }
    public void submitInterchange(View view) {
        this.name = edt_name.getText().toString();
        this.description = edt_description.getText().toString();
        this.category = spn_category.getSelectedItemPosition();
        if(sw_available.isChecked()){
            this.available = 1;
        } else{
            this.available = 0;
        }
        ContentValues cv = new ContentValues();

        cv.put(DatabaseContract.InterchangeColumns.COLUMN_NAME, name);
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_DESCRIPTION, description);
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_AVAILABLE, available);
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_INTERCHANGE_CATEGORY, category);
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_LAT, selectedLatLng.latitude);
        cv.put(DatabaseContract.InterchangeColumns.COLUMN_LNG, selectedLatLng.longitude);
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
                selectedLatLng.latitude,
                selectedLatLng.longitude,
                id
        );
        addInterchangeMarker(interchange);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        selectedMarker = null;
        mMap.clear();
        getSupportLoaderManager().restartLoader(LOADER_POINT_ID, null, pointsLoaderCallback);
        invalidateTyping();
    }

          public void updateInterchange(View view) {
              this.name = edt_name.getText().toString();
              this.description = edt_description.getText().toString();
              this.category = spn_category.getSelectedItemPosition();
              if(sw_available.isChecked()){
                  this.available = 1;
              } else{
                  this.available = 0;
              }
              ContentValues cv = new ContentValues();

              cv.put(DatabaseContract.InterchangeColumns.COLUMN_NAME, name);
              cv.put(DatabaseContract.InterchangeColumns.COLUMN_DESCRIPTION, description);
              cv.put(DatabaseContract.InterchangeColumns.COLUMN_AVAILABLE, available);
              cv.put(DatabaseContract.InterchangeColumns.COLUMN_INTERCHANGE_CATEGORY, category);
              cv.put(DatabaseContract.InterchangeColumns.COLUMN_LAT, selectedLatLng.latitude);
              cv.put(DatabaseContract.InterchangeColumns.COLUMN_LNG, selectedLatLng.longitude);
              mDbHelper.getWritableDatabase()
                      .update(
                              DatabaseContract.TABLE_INTERCHANGE,
                              cv,
                      "_id="+toUpdateInterchangeId,
                              null
                      );

              sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
              selectedMarker = null;
              mMap.clear();
              getSupportLoaderManager().restartLoader(LOADER_POINT_ID, null, pointsLoaderCallback);
              invalidateTyping();
          }
      }
