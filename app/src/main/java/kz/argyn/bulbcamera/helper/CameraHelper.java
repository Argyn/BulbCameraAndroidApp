package kz.argyn.bulbcamera.helper;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.List;

import kz.argyn.bulbcamera.thread.BulbBufferThread;

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
    private int exposureTime;

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
            //parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
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

    /*public void startBulbVideo(final BulbBuffer buffer) {
        camera.unlock();
        MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    }*/

    public void startBulb(final BulbBuffer buffer) {

        Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {
                new Thread(new BulbBufferThread(buffer, data, parameters)).start();
            }
        };

        camera.setPreviewCallback(previewCallback);

        bulbModeInProgress = true;


    }

    public void stopBulb() {
        // stop bulb mode
        bulbModeInProgress = false;
        camera.setPreviewCallback(null);
    }

    public boolean isBulbModeInProgress() {
        return bulbModeInProgress;
    }

    public Camera.Size getPreviewSize() {
        return parameters.getPreviewSize();
    }

    public void setExposureTime(int exposureTime) {
        this.exposureTime = exposureTime;
    }

    public int getExposureTime() {
        return exposureTime;
    }

    public Camera.Parameters getParameters() {
        return parameters;
    }

    public Camera getCamera() {
        return camera;
    }

    /**
     * Chooses the best preview size and assigns it to camera
     * @param width The width of surface view
     * @param height The height of surface view
     */
    public void assignBestPreviewSize(int width, int height) {
        // getting list of supported preview sizes
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();

        // current preview size
        Camera.Size bestSize = parameters.getPreviewSize();

        // current preview size
        int previewWidth = bestSize.width;
        int previewHeight = bestSize.height;

        // sum of dimensions of surface view
        int surfaceDimensionsSum = width+height;

        // best difference
        int bestDiff = Math.abs(surfaceDimensionsSum-(previewWidth+previewHeight));

        // check each size if it is the nearest to surface view size
        for(Camera.Size size : supportedPreviewSizes) {
            int newDiff = Math.abs(surfaceDimensionsSum - (size.width+size.height));
            if(newDiff<bestDiff) {
                bestDiff = newDiff;
                bestSize = size;
            }

        }

        // setting the preview size
        parameters.setPreviewSize(bestSize.width, bestSize.height);

        try {
            // updating camera parameters
            camera.setParameters(parameters);
        } catch(RuntimeException ex) {
            // problem occured while setting preview size
            ex.printStackTrace();
        }
    }
}
