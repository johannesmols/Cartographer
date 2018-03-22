package itcom.cartographer;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.maps.model.GeocodingResult;

import java.util.ArrayList;

import itcom.cartographer.Database.Database;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Database db = new Database(this, null, null, 1);


        TextView textView = findViewById(R.id.hello_world);
        textView.setText("Entries: " + db.getDatapointCount());

        TextView latestAddress = findViewById(R.id.latest_address);
        latestAddress.setText(db.getFavouritePlaces().toString());
    }
}
