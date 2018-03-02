package itcom.cartographer.Database;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.john.waveview.WaveView;

import itcom.cartographer.MainActivity;
import itcom.cartographer.PreferenceManager;
import itcom.cartographer.R;

public class ProcessJSON extends AppCompatActivity {

    private WaveView waveView;
    private int progress = 0;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_json);

        // Make status bar transparent
        changeStatusBarColor();
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        waveView = findViewById(R.id.wave_view);

        new Thread(new Runnable() {
            @Override
            public void run() {
                   while (progress < 100) {
                       progress++;
                       android.os.SystemClock.sleep(50);
                       handler.post(new Runnable() {
                           @Override
                           public void run() {
                                waveView.setProgress(progress);
                           }
                       });
                   }
                   launchMainActivity();
            }
        }).start();
    }

    private void launchMainActivity() {
        new PreferenceManager(this).setFirstTimeLaunch(false);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    /**
     * Making notification bar transparent
     */
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
