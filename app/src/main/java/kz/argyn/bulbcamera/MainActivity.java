/*package kz.argyn.bulbcamera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends Activity implements OnExpositionLevelChanged {

    private Camera camera;
    private Camera.Parameters cameraParams;
    private SurfaceView surfaceView;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private AlbumStorageDirFactory mAlbumStorageDirFactory;
    private ImageView imageView;
    private ImageButton settingsButton;
    private SharedPreferences preferences;
    private SeekBar seekBar;
    private TextView counterText;
    private ArrayList<Bitmap> imagesBytes;
    private int totalPhotosTaken;
    private Thread takePicturesThread;
    private boolean safeToTakePicture;


    private Button startButton;
    private Button stopButton;
    private Thread counterThread;
    private BulbBuffer buffer;
    private Camera.Size cameraSize;
    private NavigationDrawerFragment mNavigationDrawerFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(kz.argyn.bulbcamera.R.layout.activity_main);

        totalPhotosTaken = 0;

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        mAlbumStorageDirFactory = new BaseAlbumDirFactory();

        imageView = (ImageView) findViewById(R.id.imageView2);

        startButton = (Button) findViewById(kz.argyn.bulbcamera.R.id.button);
        stopButton = (Button)findViewById(R.id.button_stop);

        imagesBytes = new ArrayList<Bitmap>();
        safeToTakePicture = true;

        final Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                Log.d("Shutter", "Shutter opened");
            }
        };

        final Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                camera.startPreview();
                totalPhotosTaken++;
                safeToTakePicture = true;
                // async task to append buffer bytes
                new BulbBufferAsyncTask(buffer).execute(bytes);
            }
        };

        counterText = (TextView) findViewById(R.id.counter_text);

        final View.OnClickListener takePhotoListener = new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                startButton.setVisibility(View.INVISIBLE);
                stopButton.setVisibility(View.VISIBLE);
                // capture started
                final Handler printCounter = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        Bundle data = msg.getData();
                        if(data.containsKey("counter"))
                            counterText.setText(String.valueOf(data.getInt("counter")));
                        else
                            Log.d("Message","There is no key counter");
                    }
                };

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

                takePicturesThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(!Thread.interrupted()) {
                            if(safeToTakePicture) {
                                camera.takePicture(shutterCallback, null, null, pictureCallback);
                                safeToTakePicture = false;
                            }
                        }
                    }
                });

                takePicturesThread.start();
            }
        };

        SurfaceHolder.Callback surfaceCallBack = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                startButton.setOnClickListener(takePhotoListener);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

            }
        };


        initializeCamera();


        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        surfaceView.getHolder().addCallback(surfaceCallBack);


        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(24);

        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                cameraParams.setExposureCompensation(i-12);
                camera.setParameters(cameraParams);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Button stop", "Stop button called");
                takePicturesThread.interrupt();
                counterThread.interrupt();
                Log.d("Total pictures taken:", String.valueOf(imagesBytes.size()));

                int[] pixels = buffer.getPixels();

                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

                Bitmap resultBitmap = ImageFactory.getBitmapFromPixels(pixels, cameraSize.width, cameraSize.height, metrics);

                File imageFile = null;

                camera.release();

                try {
                    imageFile = createImageFile();
                    OutputStream output = new FileOutputStream(imageFile.getAbsolutePath());
                    Log.d("Compress", "Compress started");

                    resultBitmap.compress(Bitmap.CompressFormat.JPEG, 90, output);
                    output.close();
                    Log.d("Compress", "Compress finished");

                    Intent intent = new Intent(MainActivity.this, ShowImageActivity.class);
                    intent.putExtra("photo", imageFile.getAbsolutePath());
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


        // invoke navigation drawer
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("OnResume", "OnResume called");

    }

    @Override
    protected void onPause() {
        super.onPause();
        //camera.release();
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
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {

    }

    public void initializeCamera() {
        camera = Camera.open();
        cameraParams = camera.getParameters();
        int level = preferences.getInt(Preferences.EXPOSURE_LEVEL, 0);
        cameraParams.setExposureCompensation(level);
        cameraSize = cameraParams.getPictureSize();
        //cameraParams.setPictureSize(1000, 1200);
        cameraSize = cameraParams.getPictureSize();
        buffer = new BulbBuffer(cameraSize.width, cameraSize.height);
        Log.d("Camera parameters", cameraParams.flatten());
        Log.d("Max exposure compensation", String.valueOf(cameraParams.getMaxExposureCompensation()));
        //cameraParams.setFlashMode(cameraParams.FLASH_MODE_ON);
        camera.setParameters(cameraParams);
        //cameraParams.setRotation(90);
        camera.setDisplayOrientation(90);
        Log.d("Compensation step", String.valueOf(cameraParams.getExposureCompensationStep()));
        Log.d("Camera","Starting preview");
    }

    /**
     * On exposition level changed
     *
    @Override
    public void onChanged(int level) {
        cameraParams.setExposureCompensation(level);
        camera.setParameters(cameraParams);
    }
}*/
