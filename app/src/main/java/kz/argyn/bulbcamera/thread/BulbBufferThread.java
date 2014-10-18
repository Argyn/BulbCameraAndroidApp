package kz.argyn.bulbcamera.thread;

import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

import java.io.ByteArrayOutputStream;

import kz.argyn.bulbcamera.helper.BulbBuffer;

/**
 * Created by argyn on 05/09/2014.
 */
public class BulbBufferThread implements Runnable {
    private BulbBuffer buffer;
    private byte[] bytes;
    private Camera.Parameters parameters;

    public BulbBufferThread(BulbBuffer buffer, byte[] bytes, Camera.Parameters parameters) {
        this.buffer = buffer;
        this.bytes = bytes;
        this.parameters = parameters;
    }

    @Override
    public void run() {
        int width = parameters.getPreviewSize().width;
        int height = parameters.getPreviewSize().height;
        YuvImage yuv = new YuvImage(bytes, parameters.getPreviewFormat(), width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuv.compressToJpeg(new Rect(0, 0 , width, height), 50, out);
        byte[] data = out.toByteArray();
        buffer.append(data);
    }
}
