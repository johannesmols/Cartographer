package itcom.cartographer;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;


public class RegisterRequest extends StringRequest{

    private static final String RegisterRequestUrl = "http://inflowing-electrici.000webhostapp.com/Register.php";

    private Map<String,String> params;

    public RegisterRequest(String Name, String Email, int Pin, Response.Listener<String> listener){
        super(Method.POST, RegisterRequestUrl, listener, null);
        params = new HashMap<>();
        params.put("Name", Name);
        params.put("Email", Email);
        params.put("Pin", Pin + "");

    }

    @Override
    public Map<String, String> getParams() {
        return params;
    }
}
