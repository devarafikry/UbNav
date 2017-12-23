package devfikr.skripsi.ubnav.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import devfikr.skripsi.ubnav.R;
import devfikr.skripsi.ubnav.data.DatabaseContract;

public class NavigationTypeChooserActivity extends AppCompatActivity {

//    @BindView(R.id.btn_nav)
//    Button btn_nav;
//    @BindView(R.id.btn_)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_type_chooser);
        ButterKnife.bind(this);
        getSupportActionBar().setTitle("Pilih Menu Navigasi");
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

    @OnClick(R.id.btn_nav_walk)
    public void navWalk(){
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.putExtra(NavigationActivity.KEY_NAV_TYPE, DatabaseContract.PathColumns.CATEGORY_WALKING);
        intent.putExtra(NavigationActivity.KEY_TITLE, "Navigasi Pejalan Kaki");
        startActivity(intent);
    }

    @OnClick(R.id.btn_nav_motor)
    public void navMotor(){
        Intent intent = new Intent(this, NavigationMotorActivity.class);
        intent.putExtra(NavigationActivity.KEY_NAV_TYPE, DatabaseContract.PathColumns.CATEGORY_MOTORCYCLE);
        intent.putExtra(NavigationActivity.KEY_TITLE, "Navigasi Kendaraan Bermotor");
        startActivity(intent);
    }

    @OnClick(R.id.btn_nav_interchange)
    public void navInterchange(){
        Intent intent = new Intent(this, NavigationInterchangeActivity.class);
        intent.putExtra(NavigationActivity.KEY_TITLE, "Navigasi Dengan Interchange");
//        intent.putExtra(NavigationActivity.KEY_NAV_TYPE, DatabaseContract.PathColumns.CATEGORY_MOTORCYCLE);
//        intent.putExtra(NavigationActivity.KEY_PATH_IN_OUT, DatabaseContract.PathColumns.CATEGORY_INBOUND);
        startActivity(intent);
    }
}
