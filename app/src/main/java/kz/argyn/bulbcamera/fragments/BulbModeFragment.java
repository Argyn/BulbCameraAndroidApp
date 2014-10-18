package kz.argyn.bulbcamera.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import kz.argyn.bulbcamera.helper.BackCounter;
import kz.argyn.bulbcamera.helper.BulbBuffer;
import kz.argyn.bulbcamera.helper.CameraHelper;
import kz.argyn.bulbcamera.helper.FileHelper;
import kz.argyn.bulbcamera.helper.ForwardCounter;
import kz.argyn.bulbcamera.listener.ExpositionCompensationSeekBarListener;
import kz.argyn.bulbcamera.listener.ExposureTimeSeekBarListener;
import kz.argyn.bulbcamera.factory.ImageFactory;
import kz.argyn.bulbcamera.R;

public class BulbModeFragment extends Fragment {
    private CameraHelper cameraHelper;
    private PopupWindow settingsPopupWindow;
    private BulbBuffer buffer;
    private Thread forwardCounterThread;
    private Thread backCounterThread;
    private View view;
    private File lastTakenPhotoFile;
    private boolean backCounterEnabled;
    private Handler backCounterHandler;
    private Runnable backCounterFinishRunnable;

    public BulbModeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflating the view
        final View v = inflater.inflate(R.layout.fragment_bulb_mode, container, false);

        // hiding shooting in process
        final FrameLayout shootingInProgressFrameLayout = (FrameLayout)
                        v.findViewById(R.id.shooting_in_process);
        shootingInProgressFrameLayout.setVisibility(View.INVISIBLE);

        // inflating pop up settings view
        final View settingsPopUpView = inflater.inflate(R.layout.ev_setting_popup, null);

        // SeekBar for adjusting exposure compensation value
        final SeekBar exposureCompensationSeekBar =
                        (SeekBar)settingsPopUpView.findViewById(R.id.ev_setting_seek_bar);
        SeekBar exposureTime = (SeekBar)settingsPopUpView.findViewById(R.id.exposure_time_setting);


        // initializing camera helper
        cameraHelper = new CameraHelper();

        // initializing surface view to preview camera image
        final SurfaceView surfaceView = (SurfaceView) v.findViewById(R.id.camera_surface);

        // surface holder callback
        SurfaceHolder.Callback surfaceCallBack = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                // opening a camera
                cameraHelper.cameraOpen();

                // passing surface holder
                cameraHelper.setCameraPreviewDisplay(surfaceHolder);

                // starting preview
                cameraHelper.startPreview();

                // settings max and progress for exposure compensation seek bar
                exposureCompensationSeekBar.setMax(cameraHelper.maxExposureCompensation()*2);
                exposureCompensationSeekBar.setProgress(cameraHelper.exposureCompensation()
                                                        +cameraHelper.maxExposureCompensation());

                // assign the best preview size
                cameraHelper.assignBestPreviewSize(surfaceView.getWidth(), surfaceView.getHeight());
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        };

        surfaceView.getHolder().addCallback(surfaceCallBack);


        // setting OnSeekBarChangeListener for exposure compensation seek bar
        exposureCompensationSeekBar.setOnSeekBarChangeListener(
                            new ExpositionCompensationSeekBarListener(getActivity(), cameraHelper));
        // TextView displaying the timer
        TextView exposureTimeLabel =
                            (TextView)settingsPopUpView.findViewById(R.id.exposure_time_text_value);
        // setting OnSeekBarChangeListener for exposure time seek bar
        exposureTime.setOnSeekBarChangeListener(
                new ExposureTimeSeekBarListener(exposureTimeLabel, cameraHelper));

        // initializing popup window
        settingsPopupWindow = new PopupWindow(settingsPopUpView,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        // getting screen dimensions
        Point p = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(p);
        settingsPopupWindow.setWidth(p.x);

        // settings onClickListener for settings button
        final ImageView bulbSettingsButton = (ImageView) v.findViewById(R.id.bulb_settings_buttom);

        bulbSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(settingsPopupWindow.isShowing()) {
                    settingsPopupWindow.dismiss();
                } else {
                    // showing popup window
                    //settingsPopupWindow.showAtLocation(bulbSettingsButton, Gravity.BOTTOM, 0, 0);
                    int[] loc_int = new int[2];
                    bulbSettingsButton.getLocationOnScreen(loc_int);
                    Rect location = new Rect();
                    location.left = 0;
                    location.bottom = loc_int[1]-settingsPopUpView.getHeight();

                    settingsPopupWindow.showAtLocation(bulbSettingsButton, Gravity.CENTER, location.left, -800);
                }
            }
        });

        // TextView which displays the timer
        final TextView timerText = (TextView)v.findViewById(R.id.timerText);

        // Handler to print the timer text
        final Handler printCounter = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle data = msg.getData();
                if(data.containsKey("counter")) {
                    timerText.setVisibility(View.VISIBLE);
                    timerText.setText(String.valueOf(data.getInt("counter")));
                }
            }
        };


        // FrameLayout which works as a button
        final ImageView startBulb = (ImageView) v.findViewById(R.id.start_bulb);

        // hiding timer view
        timerText.setVisibility(View.INVISIBLE);

        startBulb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d("Event", "Start shooting");

                // either handle start or stop
                // depending on bulb mode status
                if(!cameraHelper.isBulbModeInProgress()) {
                    /*** START BULB MODE ***/

                    // hide thumbinal image
                    findViewById(R.id.resultingImage).setVisibility(View.INVISIBLE);

                    // initializing BulbBuffer with appropriate dimensions
                    buffer = new BulbBuffer(cameraHelper.getPreviewSize().width,
                                            cameraHelper.getPreviewSize().height);

                    try {
                        // decide which counter to use
                        if(cameraHelper.getExposureTime()!=0) {
                            // starting back counter
                            backCounterThread = new Thread(new BackCounter(printCounter,
                                                            cameraHelper.getExposureTime()));
                            backCounterThread.start();
                            backCounterEnabled = true;
                        } else {
                            // starting forward counter
                            forwardCounterThread = new Thread(new ForwardCounter(printCounter));
                            forwardCounterThread.start();
                            backCounterEnabled = false;
                        }

                        // show timer text
                        v.findViewById(R.id.timerText).setVisibility(View.VISIBLE);

                    } catch(Exception ignored) {}

                    // start bulb
                    cameraHelper.startBulb(buffer);

                    // schedule camera stop bulb
                    if(cameraHelper.getExposureTime()!=0) {

                        android.os.Handler handler = new android.os.Handler();
                        // imitate click to stop bulb mode

                        // saving runnable for further use
                        backCounterFinishRunnable = new Runnable() {
                            @Override
                            public void run() {
                                if(cameraHelper.isBulbModeInProgress()) {
                                    startBulb.performClick();
                                }

                            }
                        };

                        // schedule post delayed after n seconds
                        handler.postDelayed(backCounterFinishRunnable,
                                            cameraHelper.getExposureTime()*1000);

                        // saving handler for further use
                        backCounterHandler = handler;
                    }

                    // change button to display stop bulb button
                    startBulb.setImageDrawable(getResources().getDrawable(R.drawable.bulb_mode_stop));

                    // show frame saying the shooting is in progress
                    shootingInProgressFrameLayout.setVisibility(View.VISIBLE);
                } else {
                    /** STOP BULB MODE **/

                    // in case if user waits for timer to finish
                    // but then stops before timer expires
                    // interrupt post delayed handler
                    if(backCounterEnabled)
                        backCounterHandler.removeCallbacks(backCounterFinishRunnable);


                    // changing bulb button
                    startBulb.setImageDrawable(getResources().getDrawable(R.drawable.bulb_mode_start));

                    // hiding timer
                    timerText.setVisibility(View.INVISIBLE);

                    // setting to 0
                    timerText.setText("0");

                    // hiding shooting in progress label
                    shootingInProgressFrameLayout.setVisibility(View.INVISIBLE);

                    if(cameraHelper.getExposureTime()!=0) {
                        // interrupting back counter
                        backCounterThread.interrupt();
                    } else {
                        // interrupting forward counter
                        forwardCounterThread.interrupt();
                    }

                    // stopping bulb mode shooting
                    cameraHelper.stopBulb();

                    // prepare resulting image
                    prepareResultingImage(buffer);
                }
            }
        });


        // saving view
        view = v;

        return v;

    }

    /**
     * Prepares the resulting image from BulbBuffer
     * @param buffer BulbBuffer holding pixels
     */
    public void prepareResultingImage(BulbBuffer buffer) {

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Bitmap finalBitmap = ImageFactory.getBitmapFromPixels(buffer.getPixels(),
                            cameraHelper.getPreviewSize().width,
                            cameraHelper.getPreviewSize().height, metrics);

        try {
            // creating image file
            lastTakenPhotoFile = FileHelper.createImageFile(
                                 getResources().getString(R.string.albumName));

            // save image file
            OutputStream output = new FileOutputStream(lastTakenPhotoFile.getAbsolutePath());

            // best quality
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);

            // closing output
            output.close();

            // displaying thumbinal in right bottom corner
            showThumbinalImage(lastTakenPhotoFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Show thumbinal file in the right bottom corner
     * @param file The resulting image file
     */
    private void showThumbinalImage(File file) {
        // decode file into bitmap and scale to 50x50
        Bitmap thumbinal = BitmapFactory.decodeFile(file.getAbsolutePath());
        thumbinal = Bitmap.createScaledBitmap(thumbinal, 50, 50, true);

        ImageView thumbinalImageView = (ImageView) findViewById(R.id.resultingImage);

        // setting onCLick Listener for thumbinal image view
        thumbinalImageView.setOnClickListener(thumbinalOnClickListener());

        // setting image bitmap to be displayed
        thumbinalImageView.setImageBitmap(thumbinal);

        // make it visible
        thumbinalImageView.setVisibility(View.VISIBLE);

        // send request to gallery to add image
        FileHelper.galleryAddPic(getActivity(), lastTakenPhotoFile);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    /**
     * Find view and returns it
     * @param id id of view to be searched
     * @return view
     */
    private View findViewById(int id) {
        return view.findViewById(id);
    }


    /**
     *
     * @return OnClickListener for thumbinal image
     */
    private View.OnClickListener thumbinalOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lastTakenPhotoFile!=null) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(lastTakenPhotoFile), "image/jpeg");
                    getActivity().startActivity(intent);
                }
            }
        };
    }
}
