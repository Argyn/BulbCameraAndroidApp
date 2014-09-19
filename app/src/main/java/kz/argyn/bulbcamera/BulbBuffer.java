package kz.argyn.bulbcamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Created by argyn on 28/08/2014.
 */
public class BulbBuffer {

    private int[] pixels;
    private int width;
    private int height;
    private boolean firstPixels;

    public BulbBuffer(int width, int height) {
        pixels = new int[width*height];
        this.width = width;
        this.height = height;
        firstPixels = true;
    }

    public void append(byte[] bytes) {
        int[] tempInt = new int[pixels.length];
        Bitmap temp;

        try {
            temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            temp.getPixels(tempInt, 0, width, 0, 0, width, height);

            if(!firstPixels) {
                for (int i = 0; i < pixels.length; i++) {
                    if (pixels[i] < tempInt[i])
                        pixels[i] = tempInt[i];
                }
            } else {
                pixels = tempInt;
                firstPixels = false;
            }

        } catch(OutOfMemoryError ignored) { }

    }

    public int[] getPixels() {
        return pixels;
    }
}
