package kz.argyn.bulbcamera.factory;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.DisplayMetrics;

/**
 * Created by argyn on 26/08/2014.
 */
public class ImageFactory {

    public static Bitmap getBitmapFromPixels(int[][] pixels, DisplayMetrics metrics) {
        int width = pixels.length;
        int height = pixels[0].length;

        int[] colors = new int[pixels.length*pixels[0].length];

        int counter = 0;
        for(int j=0; j<height; j++) {
            for(int i=0; i<width; i++) {
                colors[counter++] = pixels[i][j];
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(colors, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static Bitmap getBitmapFromPixels(int[] pixels, int width, int height, DisplayMetrics metrics) {
        Bitmap bitmap = Bitmap.createBitmap(pixels, 0, width, width, height, Bitmap.Config.ARGB_8888);
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
