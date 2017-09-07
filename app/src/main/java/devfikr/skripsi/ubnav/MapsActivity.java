package devfikr.skripsi.ubnav;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
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
import devfikr.skripsi.ubnav.model.CreateHistory;
import devfikr.skripsi.ubnav.model.Path;
import devfikr.skripsi.ubnav.util.LatLngConverter;
import devfikr.skripsi.ubnav.util.SnackbarUtil;
import timber.log.Timber;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnPolylineClickListener{

    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    private GoogleMap mMap;
    private ArrayList<LatLng> locations = new ArrayList<>();
    private ArrayList<CreateHistory> undo_history = new ArrayList<>();
    private ArrayList<CreateHistory> redo_history = new ArrayList<>();

    private boolean isCreateNewPath = false;
    private boolean isEditPolyline = false;
    private boolean isDeletePath = false;
    private boolean isInsertNewNode = true;
    private boolean isDragPath = false;
    private boolean isWatchMode = false;

    private int editedPolylineId;
    private LatLng editedLatLng;
    private Polyline editedPolyline;
    private Snackbar snackbar;

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
    @BindView(R.id.fab_watch_path)
    FloatingActionButton fab_watch_path;
    @BindView(R.id.fab_exit_watch_path)
    FloatingActionButton fab_exit_watch_path;

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

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    private void addGerbangVeteran(){
        LatLng gerbangVeteranMasuk = new LatLng(-7.956213, 112.613298);
        locations.add(gerbangVeteranMasuk);
        mMap.addMarker(new MarkerOptions().position(gerbangVeteranMasuk).title("Gerbang Veteran Masuk")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.gate)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gerbangVeteranMasuk,15));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addGerbangVeteran();
        // Add a marker in Sydney and move the camera


//        LatLng gerbangVeteranKeluar = new LatLng(-7.956165, 112.613413);
//        locations.add(gerbangVeteranKeluar);
//        mMap.addMarker(new MarkerOptions().position(gerbangVeteranKeluar).title("Gerbang Veteran Keluar")
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gerbangVeteranKeluar,15));
        mMap.setOnMapClickListener(this);
        mMap.setOnPolylineClickListener(this);
    }

    private void insertNode(int id, LatLng latLng){

    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(isCreateNewPath){
            mMap.clear();
            if(isEditPolyline){
                    if (isInsertNewNode) {
                        // If distance is less than 100 meters, this is your polyline
                        int id = editedPolylineId+1;
                        locations.add(id, latLng);
                        generatePoint();
                        isEditPolyline = false;
                        Log.e("Dafuq e", "Found @ "+latLng.latitude+" "+latLng.longitude);
                    }
                    else if(isDragPath){
                        addRedoHistory(editedPolylineId, latLng);
                        locations.set(editedPolylineId, latLng);
                        generatePoint();
                        isEditPolyline = false;
                    }
                    else if(isDeletePath){
                        locations.remove(editedPolylineId);
                        isEditPolyline = false;
                        generatePoint();
                    }
//                }

            }
            else{
                addHistory();
//            mMap.addMarker(new MarkerOptions().position(latLng).title("New Marker")
//            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_point)));
                locations.add(latLng);
                generatePoint();
            }

//            polyline.setWidth(POLYLINE_STROKE_WIDTH_PX);
//            polyline.setColor(COLOR_BLACK_ARGB);
        }

    }

    private void generatePoint(){
        mMap.clear();

        for(int i =0;i<locations.size();i++){
            if(i+1 <locations.size()){
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.add(locations.get(i)).add(locations.get(i+1));
                Polyline polyline = mMap.addPolyline(polylineOptions);
                if(i==0){
                    mMap.addMarker(new MarkerOptions().position(locations.get(i)).title("Gerbang Veteran Masuk")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.gate)));
                }
                polyline.setStartCap( new CustomCap(
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_round_black), 20));
                polyline.setClickable(true);
                polyline.setTag(i);
            }
        }
    }

    public void createPath(View view) {
        if(!isWatchMode){
            if(isCreateNewPath == false){
                isCreateNewPath = true;
                btn_finish.setVisibility(View.VISIBLE);
                fab_undo.setVisibility(View.VISIBLE);
                fab_redo.setVisibility(View.VISIBLE);
                fab_clear.setVisibility(View.VISIBLE);
                fab_edit_status.setVisibility(View.VISIBLE);
                fab_watch_path.setVisibility(View.GONE);
                SnackbarUtil.showSnackBar(root_map, snackbar, getString(R.string.select_location), Snackbar.LENGTH_LONG);
            } else{
                isCreateNewPath = false;
                fab_watch_path.setVisibility(View.VISIBLE);
                fab_edit_status.setVisibility(View.GONE);
                btn_finish.setVisibility(View.GONE);
                fab_clear.setVisibility(View.GONE);
                fab_undo.setVisibility(View.GONE);
                fab_redo.setVisibility(View.GONE);
                if(snackbar != null){
                    snackbar.dismiss();
                }
                clearPath(view);
            }
        }
        else{
            SnackbarUtil.showSnackBar(root_map, snackbar, getString(R.string.cant_edit), Snackbar.LENGTH_LONG);
        }

    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        if(isCreateNewPath){
            int id = (int)polyline.getTag();
            if(isEditPolyline == false){
                showMessage(String.valueOf(id));
                editedPolylineId = (int)polyline.getTag();
                editedLatLng = polyline.getPoints().get(0);
                editedPolyline = polyline;
                addHistory(editedPolylineId, editedLatLng);
                polyline.setClickable(false);
                isEditPolyline = true;
                polyline.setStartCap(new CustomCap(
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_round_green), 20));
                polyline.setColor(getResources().getColor(R.color.colorAccent));
            } else{
                if(id == editedPolylineId){
                    Timber.d("Lat :"+polyline.getPoints().get(0).latitude+" Lng :"+polyline.getPoints().get(0).longitude);
                }
            }
        }
    }

    public void undoPath(View view) {
        if(undo_history != null){
            int lastIdHistory = undo_history.size()-1;
            //undo edited path
            if(undo_history.get(lastIdHistory) != null){
                CreateHistory createHistory = undo_history.get(lastIdHistory);
                Timber.d(String.valueOf(createHistory.getIndex()));
                locations.set(createHistory.getIndex(), createHistory.getLatLng());
                undo_history.remove(lastIdHistory);
                generatePoint();
            } else{
                int lastIdLocation = locations.size()-1; //undo added path
                if(locations.size()>1){
                    if(locations.get(lastIdLocation) != null){
                        CreateHistory createHistory = new CreateHistory(lastIdLocation, locations.get(lastIdLocation));
                        redo_history.add(createHistory);
                        locations.remove(lastIdLocation);
                        generatePoint();
                    }
                }
            }
        }
    }

    public void redoPath(View view) {
        if(redo_history != null){
            if(redo_history.size()>0){
                int redoLastIndex = redo_history.size()-1;
                CreateHistory redoHistory = redo_history.get(redoLastIndex);
                if(locations.size() <= redoHistory.getIndex()){
                    locations.add(redoHistory.getLatLng());
                } else{
                    locations.set(redoHistory.getIndex(),redoHistory.getLatLng());
                }
//                addHistory(editedPolylineId, editedLatLng);
                redo_history.remove(redoLastIndex);
                generatePoint();
            }
        }
    }

    public void clearPath(View view) {
        mMap.clear();
        isEditPolyline = false;
        undo_history.clear();
        redo_history.clear();
        locations.clear();

        LatLng gerbangVeteranMasuk = new LatLng(-7.956213, 112.613298);
        locations.add(gerbangVeteranMasuk);
        mMap.addMarker(new MarkerOptions().position(gerbangVeteranMasuk).title("Gerbang Veteran Masuk")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.gate)));
    }

    public void changeEditor(View view) {
        if(isInsertNewNode){
            isInsertNewNode = false;
            isDeletePath = false;
            isDragPath = true;
            fab_edit_status.setImageResource(R.drawable.ic_drag_path);
        } else if(isDragPath){
            isInsertNewNode = false;
            isDragPath = false;
            isDeletePath = true;
            fab_edit_status.setImageResource(R.drawable.ic_delete);
        } else if(isDeletePath){
            isDeletePath = true;
            isDragPath = false;
            isInsertNewNode = true;
            fab_edit_status.setImageResource(R.drawable.ic_insert_node);
        }
    }

    public void watchPath(View view) {
        isWatchMode = true;
        fab_watch_path.setVisibility(View.GONE);
        fab_exit_watch_path.setVisibility(View.VISIBLE);
        mDatabase.child(getString(R.string.node_path)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Path path = dataSnapshot.getValue(Path.class);
                PolylineOptions polylineOptions = new PolylineOptions();
//                polylineOptions.add(locations.get(i)).add(locations.get(i+1));
                LatLng startLocation = LatLngConverter.convertToGoogleLatLng(path.getStartLocation());
                LatLng endLocation = LatLngConverter.convertToGoogleLatLng(path.getEndLocation());
                polylineOptions.add(startLocation).add(endLocation);
                Polyline polyline = mMap.addPolyline(polylineOptions);
                polyline.setStartCap( new CustomCap(
                        BitmapDescriptorFactory.fromResource(R.drawable.ic_round_black), 20));
                polyline.setClickable(true);
                polyline.setTag(dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void finishEditor(View view) {
        mDatabase.child(getString(R.string.node_path)).setValue(null);
        for (int i=0;i<locations.size();i++){
            if(i+1 < locations.size()){
                devfikr.skripsi.ubnav.model.LatLng startLocation =
                        LatLngConverter.convertToLocalLatLng(locations.get(i));
                devfikr.skripsi.ubnav.model.LatLng endLocation =
                        LatLngConverter.convertToLocalLatLng(locations.get(i+1));
                Path path = new Path(startLocation, endLocation);
                mDatabase.child(getString(R.string.node_path)).push().setValue(path);
            }
        }
        createPath(view);
    }

    public void exitWatchPath(View view) {
        isWatchMode = false;
        fab_exit_watch_path.setVisibility(View.GONE);
        fab_watch_path.setVisibility(View.VISIBLE);

        clearPath(view);
    }
}
