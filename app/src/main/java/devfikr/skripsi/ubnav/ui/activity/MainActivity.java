package devfikr.skripsi.ubnav.ui.activity;

import android.support.v4.app.Fragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import devfikr.skripsi.ubnav.R;
import devfikr.skripsi.ubnav.ui.fragment.SettingFragment;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_edit)
    TextView btn_edit;
    @BindView(R.id.btn_nav) TextView btn_nav;
    @BindView(R.id.btn_setting) TextView btn_setting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Timber.plant(new Timber.DebugTree());
    }

    @OnClick(R.id.btn_edit)
    public void editPath(){
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_nav)
    public void navigation(){
        Intent intent = new Intent(MainActivity.this, NavigationTypeChooserActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.btn_setting)
    public void setting(){
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }
}
