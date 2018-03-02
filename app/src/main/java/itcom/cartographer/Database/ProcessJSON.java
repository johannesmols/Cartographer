package itcom.cartographer.Database;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import itcom.cartographer.MainActivity;
import itcom.cartographer.PreferenceManager;
import itcom.cartographer.R;

public class ProcessJSON extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_json);
    }

    private void launchMainActivity() {
        new PreferenceManager(this).setFirstTimeLaunch(false);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
