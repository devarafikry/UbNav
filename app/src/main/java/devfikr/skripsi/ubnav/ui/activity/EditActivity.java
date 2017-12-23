package devfikr.skripsi.ubnav.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import devfikr.skripsi.ubnav.R;
import devfikr.skripsi.ubnav.data.DatabaseContract;

public class EditActivity extends AppCompatActivity {

    @BindView(R.id.btn_edit_walk)
    ImageView btn_edit_walk;
    @BindView(R.id.btn_edit_motor) ImageView btn_edit_motorcycle;
//    @BindView(R.id.btn_edit_car) Button btn_edit_car;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        getSupportActionBar().setTitle("Pilih Menu Graf");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
