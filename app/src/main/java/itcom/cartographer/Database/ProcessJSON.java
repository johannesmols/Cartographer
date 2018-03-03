package itcom.cartographer.Database;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.john.waveview.WaveView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import itcom.cartographer.MainActivity;
import itcom.cartographer.PreferenceManager;
import itcom.cartographer.R;

public class ProcessJSON extends AppCompatActivity {

    private Gson gson = new GsonBuilder().create();

    private WaveView waveView;
    private TextView progressTextView;

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
        progressTextView = findViewById(R.id.process_json_progress_text);

        // Read intent to get the Uri of the JSON file
        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.intent_intro_to_json_uri))) {
            Uri file = Uri.parse(intent.getStringExtra(getString(R.string.intent_intro_to_json_uri)));
            parseJSON(file);
        }
    }

    private void parseJSON(Uri file) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("context", this);
        params.put("file", file);
        AsyncJSONReader asyncJSONReader = new AsyncJSONReader(this);
        asyncJSONReader.execute(params);
    }

    /**
     * Launch the main activity when the parsing is done
     */
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

    /**
     * AsyncTask to handle the reading of the file without interrupting the main thread
     */
    private static class AsyncJSONReader extends AsyncTask<HashMap<String, Object>, Long, LinkedTreeMap> {

        private WeakReference<ProcessJSON> activityReference;

        // only retain a weak reference to the activity
        AsyncJSONReader(ProcessJSON context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected LinkedTreeMap doInBackground(HashMap<String, Object>[] hashMaps) {
            LinkedTreeMap linkedTreeMap = new LinkedTreeMap();
            try {
                Context context = (Context) hashMaps[0].get("context");
                Uri uri = (Uri) hashMaps[0].get("file");

                InputStream inputStream = context.getContentResolver().openInputStream(uri);

                ProgressInputStream progressInputStream = new ProgressInputStream(inputStream);
                progressInputStream.addListener(new ProgressInputStream.Listener() {
                    @Override
                    public void onProgressChanged(int percentage, long bytesRead, int size) {
                        publishProgress((long) percentage, bytesRead, (long) size);
                        System.out.println(percentage);
                    }
                });
                Reader reader = new BufferedReader(new InputStreamReader(progressInputStream));
                linkedTreeMap = new Gson().fromJson(reader, LinkedTreeMap.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return linkedTreeMap;
        }

        @Override
        protected void onProgressUpdate(Long... values) {
            // get a reference to the activity if it is still there
            ProcessJSON activity = activityReference.get();
            if (activity == null || activity.isFinishing())
                return;

            if (values.length == 3) {
                int progress = Integer.parseInt(String.valueOf(values[0]));
                if (progress == 0) {
                    progress = 1;
                }
                activity.waveView.setProgress(progress);

                activity.progressTextView.setText(activity.getString(R.string.json_processor_progress_text, values[1] / 1024, values[2] / 1024));
            }
        }

        @Override
        protected void onPostExecute(LinkedTreeMap aLinkedTreeMap) {
            System.out.println(aLinkedTreeMap.toString());
        }
    }
}