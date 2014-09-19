package kz.argyn.bulbcamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;

/**
 * Created by argyn on 25/08/2014.
 */
public class GenerateImage implements Runnable {

    private int width;
    private int height;
    private int totalImages;
    private int[][] imagePixels;
    private ArrayList<Bitmap> bitmapList;

    private int[] newImagePixels;
    private Context context;
    private int[] rawPixels;
    private Bitmap bitmap;
    private int inSampleSize;
    private BitmapFactory.Options options;

    public GenerateImage(Context context, ArrayList<Bitmap> bitmapList) throws Exception {
        if(bitmapList.size()==0)
            throw new Exception("Empty images list passed");


        this.context = context;

        if(bitmapList.size()>0) {
            width = bitmapList.get(0).getWidth();
            height = bitmapList.get(0).getHeight();
            Log.d("width", String.valueOf(width));
            Log.d("height", String.valueOf(height));
            totalImages = bitmapList.size();
            this.bitmapList = bitmapList;
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.outWidth = width;
            options.outHeight = height;
            options.inSampleSize = ImageFactory.calculateInSampleSize(options, 1200, 2100);

        }

        newImagePixels = new int[width*height];
        rawPixels = new int[width*height];
        imagePixels = new int[width*height][totalImages];
    }

    @Override
    public void run() {
        for(int i=0; i<width*height; i++) {
            for(int k=0; k<totalImages; k++) {
                bitmapList.get(k).getPixels(rawPixels, 0, width, 0, 0, width, height);
                imagePixels[i][k] = rawPixels[i];
            }
            Arrays.sort(imagePixels[i]);
            newImagePixels[i] = imagePixels[i][totalImages-1];
        }

        //DisplayMetrics metrics = new DisplayMetrics();
        //((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        //Bitmap resultBitmap = ImageFactory.getBitmapFromPixels(newImagePixels, width, height, metrics);

        Log.d("Result", "Finished");
    }
}
