package itcom.cartographer;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class RegisterRequest extends StringRequest{

    private static final String RegisterRequestUrl = "http://cartographer.tech/Register.php";

    private Map<String,String> params;

    public RegisterRequest(String name, String email, int pin, Response.Listener<String> listener){
        super(Method.POST, RegisterRequestUrl, listener, null);
        params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        params.put("pin", pin + "");

    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
