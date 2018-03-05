package itcom.cartographer.Database;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonToken;
import com.john.waveview.WaveView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private static class AsyncJSONReader extends AsyncTask<HashMap<String, Object>, Long, Boolean> {

        private WeakReference<ProcessJSON> activityReference;

        // only retain a weak reference to the activity
        AsyncJSONReader(ProcessJSON context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(HashMap<String, Object>[] hashMaps) {

            try {
                Context context = (Context) hashMaps[0].get("context");
                Uri uri = (Uri) hashMaps[0].get("file");

                // Using a custom input stream to get an estimate on the progress of the parsing
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                ProgressInputStream progressInputStream = new ProgressInputStream(inputStream);
                progressInputStream.addListener(new ProgressInputStream.Listener() {
                    @Override
                    public void onProgressChanged(int percentage, long bytesRead, int size) {
                        publishProgress((long) percentage, bytesRead, (long) size);
                        System.out.println(percentage);
                    }
                });

                // Parsing of the JSON file
                com.google.gson.stream.JsonReader jsonReader = new com.google.gson.stream.JsonReader(new InputStreamReader(progressInputStream));

                // Check if the file is correct and starts with an object
                if (jsonReader.hasNext() && jsonReader.peek().equals(JsonToken.BEGIN_OBJECT)) { // the very first object, parent of all objects in the file
                    jsonReader.beginObject(); // consume the opening brackets of the first object
                    if (jsonReader.hasNext() && jsonReader.peek().equals(JsonToken.NAME)) { // the array of the object, namely "locations". this contains all further objects in an array
                        String firstObjectName = jsonReader.nextName(); // consume the name of the location object
                        if (firstObjectName.equals("locations") && jsonReader.hasNext() && jsonReader.peek().equals(JsonToken.BEGIN_ARRAY)) { // the beginning of the "locations" array
                            jsonReader.beginArray(); // consume the opening brackets of the array

                            boolean hasAnotherObjectInArray = true;
                            while (hasAnotherObjectInArray) { // loop through every object in the array
                                if (jsonReader.peek().equals(JsonToken.BEGIN_OBJECT)) {
                                    jsonReader.beginObject();

                                    // go through each entry in the object
                                    while (jsonReader.hasNext()) {
                                        if (jsonReader.peek().equals(JsonToken.NAME)) {
                                            String nextName = jsonReader.nextName();
                                            switch (nextName) {
                                                case "timestampMs":
                                                    Log.i("timestampMs", jsonReader.nextString());
                                                    break;
                                                case "latitudeE7":
                                                    Log.i("latitudeE7", String.valueOf(jsonReader.nextLong()));
                                                    break;
                                                case "longitudeE7":
                                                    Log.i("longitudeE7", String.valueOf(jsonReader.nextLong()));
                                                    break;
                                                case "accuracy":
                                                    Log.i("accuracy", String.valueOf(jsonReader.nextInt()));
                                                    break;
                                                case "velocity":
                                                    Log.i("velocity", String.valueOf(jsonReader.nextInt()));
                                                    break;
                                                case "heading":
                                                    Log.i("heading", String.valueOf(jsonReader.nextInt()));
                                                    break;
                                                case "altitude":
                                                    Log.i("altitude", String.valueOf(jsonReader.nextInt()));
                                                    break;
                                                case "verticalAccuracy":
                                                    Log.i("verticalAccuracy", String.valueOf(jsonReader.nextInt()));
                                                    break;
                                                case "activity":
                                                    // sub array
                                                    Log.e("activity", "Skipping for now...");
                                                    jsonReader.skipValue();
                                                    break;
                                                default:
                                                    Log.i("Undefined name", "Undefined name found in JSON");
                                                    jsonReader.skipValue(); // skip the unknown value
                                                    break;
                                            }
                                        }
                                    }

                                    jsonReader.endObject(); // end the current object
                                    hasAnotherObjectInArray = jsonReader.peek().equals(JsonToken.BEGIN_OBJECT); // determines if there is another object coming up in the array or if it is done
                                }
                            }

                            if (jsonReader.peek().equals(JsonToken.END_ARRAY)) { // ends the array when it has reached it's end
                                jsonReader.endArray();
                            }
                        }
                    }

                    if (jsonReader.peek().equals(JsonToken.END_OBJECT)) { // ends the main object in the file
                        jsonReader.endObject();
                    }
                }
                jsonReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
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
        protected void onPostExecute(Boolean successful) {
            Log.i("Parsing successful: ", successful ? "true" : "false");
        }
    }
}