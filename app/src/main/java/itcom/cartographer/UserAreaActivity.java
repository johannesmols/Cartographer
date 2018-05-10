package itcom.cartographer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class UserAreaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        final TextView tvHello = (TextView) findViewById(R.id.tvHello);
        final TextView tvName = (TextView) findViewById(R.id.tvName);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");

        tvName.setText(name);
    }
}
