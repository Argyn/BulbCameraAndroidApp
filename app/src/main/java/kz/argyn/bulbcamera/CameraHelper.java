package kz.argyn.bulbcamera;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by argyn on 29/08/2014.
 */
public class CameraHelper {
    private Camera camera;
    private Camera.Parameters parameters;
    private boolean isCameraOpen = false;
    private Camera.Size size;

    private int minExpositionValue;
    private int maxExpositionValue;
    private int exposureCompensation;
    private boolean safeToTakePicture = true;
    private Thread takePicturesThread;
    private boolean bulbModeInProgress = false;

    public CameraHelper() {
    }

    public void cameraOpen() {
        try {
            // open camera
            camera = Camera.open();

            camera.setDisplayOrientation(90);
            // changing flag to true
            isCameraOpen = true;
            // getting camera parameters
            parameters = camera.getParameters();

            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(parameters);

            // get camera size
            size = parameters.getPictureSize();

            minExpositionValue = parameters.getMinExposureCompensation();
            Log.d("Exposure comp", String.valueOf(parameters.getMaxExposureCompensation()));
            maxExpositionValue = parameters.getMaxExposureCompensation();

            exposureCompensation = parameters.getExposureCompensation();
        } catch(Exception ignored) {}
    }

    public Camera.Size getPictureSize() {
        return size;
    }

    public int getPictureWidth() {
        return size.width;
    }

    public int getPictureHeight() {
        return size.height;
    }

    public boolean isCameraOpen() {
        return isCameraOpen;
    }

    public boolean setCameraPreviewDisplay(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            return true;
        } catch(Exception exception) {
            return false;
        }
    }

    public void startPreview() {
        if(camera!=null) {
            camera.startPreview();

        }
    }

    public void release() {
        camera.release();
    }

    public void setExposureCompensation(int value) {
        parameters.setExposureCompensation(value);
        camera.setParameters(parameters);
    }

    public int minExposureCompensation() {
        return minExpositionValue;
    }

    public int maxExposureCompensation() {
        return maxExpositionValue;
    }

    public int exposureCompensation() {
        return exposureCompensation;
    }

    public void startBulb(final BulbBuffer buffer) {

        final Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                //Log.d("Shutter", "Shutter opened");
            }
        };

        final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                camera.startPreview();
                safeToTakePicture = true;
                // async task to append buffer bytes
                new BulbBufferAsyncTask(buffer).execute(bytes);
                //new Thread(new BulbBufferThread(buffer, bytes)).start();
            }
        };

        takePicturesThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()) {
                    if(safeToTakePicture) {
                        camera.takePicture(null, null, null, pictureCallback);
                        safeToTakePicture = false;
                    }
                }
            }
        });

        takePicturesThread.start();

        bulbModeInProgress = true;
    }

    public void stopBulb() {
        if(!takePicturesThread.isInterrupted())
            takePicturesThread.interrupt();
        bulbModeInProgress = false;
    }

    public boolean isBulbModeInProgress() {
        return bulbModeInProgress;
    }
}
