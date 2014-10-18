package kz.argyn.bulbcamera.helper;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kz.argyn.bulbcamera.R;
import kz.argyn.bulbcamera.factory.AlbumStorageDirFactory;
import kz.argyn.bulbcamera.factory.BaseAlbumDirFactory;
import kz.argyn.bulbcamera.factory.FroyoAlbumDirFactory;

/**
 * Created by argyn on 03/10/2014.
 */
public class FileHelper {

    /**
     * Returns the dir in which images should be stored
     * @param albumName
     * @return
     */
    private static File getAlbumDir(String albumName) {

        // initializing album dir factory
        AlbumStorageDirFactory mAlbumStorageDirFactory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }


        File storageDir = null;

        // check if we have external media storage mounted
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            // we are good, getting album storage dir
            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(albumName);

            // if any problems occur, return null
            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        return null;
                    }
                }
            }
        }

        return storageDir;
    }

    /**
     * Creates image file and returns it
     * @return
     */
    public static File createImageFile(String albumName) {
        //  current timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        // file name
        String imageFileName = "IMG_" + timeStamp;

        // getting album dir
        File albumDir = getAlbumDir(albumName);

        // creating file
        File imageFile = null;

        try {
            imageFile = File.createTempFile(imageFileName, ".jpg", albumDir);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return imageFile;
    }

    /**
     * Sends broadcast message to gallery asking to add image file
     * @param context
     * @param imageFile
     */
    public static void galleryAddPic(Context context, File imageFile) {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
}
