package kz.argyn.bulbcamera.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import kz.argyn.bulbcamera.AlbumStorageDirFactory;
import kz.argyn.bulbcamera.BaseAlbumDirFactory;
import kz.argyn.bulbcamera.BulbBuffer;
import kz.argyn.bulbcamera.CameraHelper;
import kz.argyn.bulbcamera.ExpositionCompensationSeekBarListener;
import kz.argyn.bulbcamera.ImageFactory;
import kz.argyn.bulbcamera.R;
import kz.argyn.bulbcamera.ShowImageActivity;
import kz.argyn.bulbcamera.views.VerticalSeekBar;

public class BulbModeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private CameraHelper cameraHelper;
    private PopupWindow settingsPopupWindow;
    private LinearLayout evPopUpLayout;
    private SeekBar exposureCompensationSeekBar;
    private AlbumStorageDirFactory mAlbumStorageDirFactory;
    private BulbBuffer buffer;
    private Thread counterThread;

    public static CameraFragment newInstance() {
        CameraFragment fragment = new CameraFragment();
        return fragment;
    }

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

        mAlbumStorageDirFactory = new BaseAlbumDirFactory();

        final View v = inflater.inflate(R.layout.fragment_bulb_mode, container, false);

        final FrameLayout shootingInProgressFrameLayout = (FrameLayout)
                        v.findViewById(R.id.shooting_in_process);
        shootingInProgressFrameLayout.setVisibility(View.INVISIBLE);

        View evPopupView = inflater.inflate(R.layout.ev_setting_popup, null);

        exposureCompensationSeekBar = (SeekBar)evPopupView.findViewById(R.id.ev_setting_seek_bar);

        cameraHelper = new CameraHelper();

        SurfaceHolder.Callback surfaceCallBack = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                cameraHelper.cameraOpen();
                cameraHelper.setCameraPreviewDisplay(surfaceHolder);
                cameraHelper.startPreview();
                exposureCompensationSeekBar.setMax(cameraHelper.maxExposureCompensation()*2);
                exposureCompensationSeekBar.setProgress(cameraHelper.exposureCompensation()
                                                        +cameraHelper.maxExposureCompensation());
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        };

        SurfaceView surfaceView = (SurfaceView) v.findViewById(R.id.camera_surface);

        surfaceView.getHolder().addCallback(surfaceCallBack);

        exposureCompensationSeekBar.setOnSeekBarChangeListener(new ExpositionCompensationSeekBarListener(getActivity(), cameraHelper));

        settingsPopupWindow = new PopupWindow(evPopupView,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        Point p = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(p);
        settingsPopupWindow.setWidth(p.x);

        ImageView bulbSettingsButton = (ImageView) v.findViewById(R.id.bulb_settings_buttom);

        bulbSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(settingsPopupWindow.isShowing()) {
                    settingsPopupWindow.dismiss();
                } else {
                    settingsPopupWindow.showAsDropDown(v, -300, -400);
                }
            }
        });

        final TextView timerText = (TextView)v.findViewById(R.id.timerText);

        final Handler printCounter = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle data = msg.getData();
                if(data.containsKey("counter")) {
                    timerText.setVisibility(View.VISIBLE);
                    timerText.setText(String.valueOf(data.getInt("counter")));
                }
                else
                    Log.d("Message","There is no key counter");
            }
        };



        FrameLayout startShooting = (FrameLayout) v.findViewById(R.id.take_picture_button);

        Thread counterThread2;

        startShooting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Event", "Start shooting");


                if(!cameraHelper.isBulbModeInProgress()) {
                    // start bulb mode

                    buffer = new BulbBuffer(cameraHelper.getPictureSize().width, cameraHelper.getPictureSize().height);
                    try {
                        counterThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int counter = 0;
                                while(!Thread.currentThread().isInterrupted()) {
                                    try {
                                        Thread.currentThread().sleep(1000);
                                        counter++;
                                        Bundle bundle = new Bundle();
                                        bundle.putInt("counter", counter);
                                        Log.d("Message", "Sending message");
                                        Message msg = new Message();
                                        msg.setData(bundle);
                                        printCounter.sendMessage(msg);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        Thread.currentThread().interrupt();
                                    }
                                }
                            }
                        });

                        counterThread.start();

                        v.findViewById(R.id.timerText).setVisibility(View.VISIBLE);
                    } catch(Exception ignored) {}

                    cameraHelper.startBulb(buffer);

                    v.findViewById(R.id.start_bulb).setVisibility(View.INVISIBLE);
                    v.findViewById(R.id.stop_bulb).setVisibility(View.VISIBLE);
                    shootingInProgressFrameLayout.setVisibility(View.VISIBLE);
                } else {
                    // stop bulb mode
                    v.findViewById(R.id.start_bulb).setVisibility(View.VISIBLE);
                    v.findViewById(R.id.stop_bulb).setVisibility(View.INVISIBLE);
                    View timerText = v.findViewById(R.id.timerText);
                    timerText.setVisibility(View.INVISIBLE);
                    ((TextView)timerText).setText("0");
                    shootingInProgressFrameLayout.setVisibility(View.INVISIBLE);

                    counterThread.interrupt();
                    cameraHelper.stopBulb();
                    DisplayMetrics metrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
                    Bitmap finalBitmap = ImageFactory.getBitmapFromPixels(buffer.getPixels(), cameraHelper.getPictureSize().width,
                                            cameraHelper.getPictureSize().height, metrics);
                    try {
                        File imageFile = createImageFile();
                        //galleryAddPic(imageFile);
                        OutputStream output = new FileOutputStream(imageFile.getAbsolutePath());
                        finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, output);
                        output.close();

                        Intent intent = new Intent(getActivity(), ShowImageActivity.class);
                        intent.putExtra("photo", imageFile.getAbsolutePath());
                        Log.d("path", imageFile.getAbsolutePath());
                        startActivity(intent);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            }
        });



        return v;

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private String getAlbumName() {
        return getString(R.string.album_name);
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", storageDir.getAbsolutePath());
                        Log.d("CameraSample", "get to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp;
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, ".jpg", albumF);
        return imageF;
    }

    @Override
    public void onPause() {
        super.onPause();
        //cameraHelper = null;
    }

    private void galleryAddPic(File imageFile) {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

}
