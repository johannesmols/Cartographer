package itcom.cartographer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class UserAreaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_area);

        final TextView tvChangePinCode = (TextView) findViewById(R.id.tvChangePinCode);
        final EditText etEmail = (EditText) findViewById(R.id.etEmail);
        final TextView tvCurrentPinCode = (TextView) findViewById(R.id.tvCurrentPinCode);
        final EditText etPinCode = (EditText) findViewById(R.id.etPinCode);
        final TextView tvNewPinCode = (TextView) findViewById(R.id.tvNewPinCode);
        final EditText etNewPinCode = (EditText) findViewById(R.id.etNewPinCode);
    }
}
