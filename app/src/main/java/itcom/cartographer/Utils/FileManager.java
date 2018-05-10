package itcom.cartographer.Utils;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

@SuppressLint("Registered")
public class FileManager extends AppCompatActivity {
    /**
     * 25/4/2018 (Ricardo Comment)
     * This method was created originally to delete the JSON file downloaded in the internal storage
     * of the device. We made it generic so we can delete files from the app directory, the
     * database directory or a external storage directory.
     * It contains a list in case we want to delete more than one file at the same time.
     */

    public void deleteThisFiles(Uri uri, List<Uri> uriList){ //call this method with a single URI or a list of its kind(set it to null if none)
        /*
        For security reasons, Android does not allow an app to delete files created by another app, it will result in a permission denied
        --> Link https://developer.android.com/training/articles/security-tips.html#userid <--
        This method won't work in all the cases for now.
         */

        if(uri != null) {
            uriList.add(uri);
        }

        try{
            ArrayList<Uri> uriNotDeleted = null;
            int counter = 0;
            for(int x = 0; x < uriList.size(); x++) {
                try {
                    File fDelete = new File(uriList.get(x).getPath());//creates a File object with the URI path
                    if(fDelete.exists()){
                        FileInputStream fis = new FileInputStream(new File(fDelete.getAbsolutePath()));
                        deleteFile(fis.toString());//tries to delete the file
                        fis.close();//close the FIS
                        counter++;
                        Toast.makeText(this, counter + " of " + uriList.size(), Toast.LENGTH_LONG).show();
                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                    Toast.makeText(this, "File not found: " + uriList.get(x).toString(), Toast.LENGTH_LONG).show();
                    if(uriNotDeleted == null){//avoid NullPointException
                        uriNotDeleted = new ArrayList<>();
                    }
                    uriNotDeleted.add(uriList.get(x));
                }
            }//end for loop
            //Prompt the proper text
            if(counter == 0){
                Toasty.success(this, "Couldn't delete files").show();
            }else if(counter == 1){
                if(counter == uriList.size()){
                    Toasty.success(this, "File deleted").show();
                }else{
                    Toasty.success(this, counter + "/" + uriList.size() +"Files deleted").show();
                }
            }else if(counter >= 2){
                Toasty.success(this, counter + "/" + uriList.size() +"Files deleted").show();
            }

        }catch(NullPointerException npe){//catch the case "uriNotDeleted" might remain null.
            System.out.print("No files remaining");
            npe.printStackTrace();
        }
    }//end method

    protected void keyNoteGenerator(){
        KeyStore keyStore;
    }
}


