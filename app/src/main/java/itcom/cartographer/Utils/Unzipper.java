package itcom.cartographer.Utils;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Toast;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import es.dmoral.toasty.Toasty;
import itcom.cartographer.R;

/**
 * Unzips a given file using the zip4j library
 */
public class Unzipper {

    private Context context;

    public Unzipper(Context context) {
        this.context = context;
    }

    /**
     * Unzip an archive
     * @param file the .zip archive
     * @return if the operation was successful
     */
    public boolean unzip(Uri file) {
        try {
            String filePath = getPath(context, file);

            // Build destination path to be in the same directory as the zip file, by taking the zip file path and removing the last part
            if (filePath != null) {
                String[] destinationArray = filePath.split("(?=/)");
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < destinationArray.length - 1; i++) { // loop until the second last item, so the file name is omitted
                    stringBuilder.append(destinationArray[i]);
                }
                stringBuilder.append("/");
                String destination = stringBuilder.toString();

                // Check for read file permission
                Permissions permissions = new Permissions(context);
                if (!permissions.checkForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    permissions.askForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Permissions.RequestCodes.WRITE_EXTERNAL_STORAGE);
                    if (!permissions.checkForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Toasty.error(context, context.getString(R.string.toast_permission_denied), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }

                ZipFile zipFile = new ZipFile(filePath);
                if (zipFile.isEncrypted()) {
                    throw new ZipException("File is encrypted");
                }
                zipFile.extractAll(destination);
            } else {
                Toasty.error(context, context.getString(R.string.toast_zip_error), Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (ZipException e) {
            e.printStackTrace();
        }

        return true;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * This is a method from an StackOverflow post: https://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author Paul Burke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // to-do: handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * This is a method from an StackOverflow post: https://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * This is a method from an StackOverflow post: https://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * This is a method from an StackOverflow post: https://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * This is a method from an StackOverflow post: https://stackoverflow.com/questions/20067508/get-real-path-from-uri-android-kitkat-new-storage-access-framework?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
