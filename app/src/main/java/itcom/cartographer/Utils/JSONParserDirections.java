package itcom.cartographer.Utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * This class contains one method "mkRequest", it sends the start and destination points.
 * This method returns a JSONObject which will be used to obtain the routes.
 */
public class JSONParserDirections {

    private String charset = "UTF-8";
    private String paramsString;
    private StringBuilder result;
    private StringBuilder sbParams;
    private HttpURLConnection connection;
    private DataOutputStream dataOutputStream;
    private URL urlObject;
    private JSONObject jsonObject = null;

    public JSONObject mkRequest(String url, String method, HashMap<String, String> params, boolean encode){

        sbParams = new StringBuilder(); //stands for "String Builder Params".
        int x = 0;
        for(String key: params.keySet()){ //encondes the string to be sent.
            try{
                if(x != 0){
                    sbParams.append("&");
                }
                if (encode){
                    sbParams.append(key).append("=").append(URLEncoder.encode(params.get(key), charset));
                }else{
                    sbParams.append(key).append("=").append(params.get(key));
                }
            }catch(UnsupportedEncodingException uee){
                uee.printStackTrace();
            }
            x++;
        }
        if(method.equals(("POST"))){ //This will try establish connection and send directly the params (NOTE: not used, this has been deprecated)
            try{
                urlObject = new URL(url);
                connection = (HttpURLConnection) urlObject.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Accept-Charset", charset);
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.connect();

                paramsString = sbParams.toString();

                dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.writeBytes(paramsString);
                dataOutputStream.flush();
                dataOutputStream.close();

            }catch(IOException e){
                e.printStackTrace();
            }
        }else if(method.equals("GET")){ //this will try to put the params into the url to make the request and establish connection.
            if(sbParams.length() != 0){
                url += "?" + sbParams.toString();
            }
            Log.d("JSONParser", "full GET url: " + url); //shows the URL and the information in it that google send back.

            try{
                urlObject = new URL(url);
                connection = (HttpURLConnection) urlObject.openConnection();
                connection.setDoOutput(false);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept-Charset", charset);
                connection.setConnectTimeout(15000);
                connection.connect();

            }catch(IOException e){
                e.printStackTrace();
            }
        }
        try{ //this will try to read the page to which the request have been made.
            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            result = new StringBuilder();
            while((line = reader.readLine()) != null){
                result.append(line);
            }
            Log.d("JSONParser", "result: " + result.toString()); //shows the information from the URL.
        }catch(IOException e){
            e.printStackTrace();
        }
        connection.disconnect(); //remember to disconnect every time a request has been made

        try{
            jsonObject = new JSONObject(result.toString()); // Puts the data in the JSONObject
        }catch(JSONException je){
            Log.e("JSONParser", "Error parsing data "+ je.toString());
        }
        return jsonObject;
    }

}
