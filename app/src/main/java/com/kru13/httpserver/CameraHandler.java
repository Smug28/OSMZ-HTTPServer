package com.kru13.httpserver;

import android.content.Context;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by smuggler on 07.03.17.
 */

public class CameraHandler {
    private static String TAG = "CAMERA_HANDLER";
    //private Context mContext;
    public static Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/camera_feed.jpg");
            try {
                if (!pictureFile.createNewFile()){
                    Log.d(TAG, "Error creating media file, check storage permissions: ");
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }
        }
    };

    public CameraHandler(){
        //mContext = context.getApplicationContext();
    }

    public static void takePicture(){
        getCameraInstance().takePicture(null, null, mPicture);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

}
