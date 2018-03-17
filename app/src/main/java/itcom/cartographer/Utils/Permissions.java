package itcom.cartographer.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * A class to handle permission detection and ask for permissions
 */
public class Permissions implements ActivityCompat.OnRequestPermissionsResultCallback {

    private Context context;

    public enum RequestCodes {
        WRITE_EXTERNAL_STORAGE (0);

        private final int requestCode;
        RequestCodes(int requestCode) {
            this.requestCode = requestCode;
        }
    }

    public Permissions(Context context) {
        this.context = context;
    }

    /**
     * Checks if the app has a permission granted
     * @param permission the permission
     * @return if the permission is granted
     */
    public boolean checkForPermission(String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Asks the user for a permission
     * @param permission the permission
     * @param requestCode the request code
     */
    public void askForPermission(String permission, RequestCodes requestCode) {
        ActivityCompat.requestPermissions((Activity) context, new String[]{permission}, requestCode.requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCodes.WRITE_EXTERNAL_STORAGE.requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission granted
            } else {
                // permission denied
            }
        }
    }
}
