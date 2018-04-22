package itcom.cartographer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final TextView tvEnterPinCode = (TextView) findViewById(R.id.tvEnterPinCode);
        final EditText etPinCode = (EditText) findViewById(R.id.etPinCode);
        final Button bConfirm = (Button) findViewById(R.id.bConfirm);
    }
}
