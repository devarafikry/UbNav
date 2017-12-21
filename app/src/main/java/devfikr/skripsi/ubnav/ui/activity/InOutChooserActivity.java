package devfikr.skripsi.ubnav.ui.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import butterknife.ButterKnife;
import butterknife.OnClick;
import devfikr.skripsi.ubnav.R;
import devfikr.skripsi.ubnav.data.DatabaseContract;

public class InOutChooserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_out_chooser);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.btn_in)
    public void inboundEdit(){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.KEY_PATH_TYPE, DatabaseContract.PathColumns.CATEGORY_MOTORCYCLE);
        intent.putExtra(MapsActivity.KEY_PATH_IN_OUT, DatabaseContract.PathColumns.CATEGORY_INBOUND);
        startActivity(intent);
    }

    @OnClick(R.id.btn_out)
    public void outBoundEdit(){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.KEY_PATH_TYPE, DatabaseContract.PathColumns.CATEGORY_MOTORCYCLE);
        intent.putExtra(MapsActivity.KEY_PATH_IN_OUT, DatabaseContract.PathColumns.CATEGORY_OUTBOUND);
        startActivity(intent);
    }
}
