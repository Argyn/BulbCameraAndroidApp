package kz.argyn.bulbcamera;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import kz.argyn.bulbcamera.R;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ShowImageActivity extends Activity {
    private boolean actionBarVisible = true;
    private File imageFile;
    private Bitmap imageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        ImageView imageView = (ImageView) findViewById(R.id.photo);
        String imagePath = (String)getIntent().getExtras().get("photo");
        imageFile = new File(imagePath);

        imageBitmap = BitmapFactory.decodeFile(imagePath);

        imageView.setImageBitmap(imageBitmap);

        final ActionBar mActionBar = getActionBar();
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.show_image_layout);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(actionBarVisible) {
                    mActionBar.hide();
                    actionBarVisible = false;
                } else {
                    mActionBar.show();
                    actionBarVisible = true;
                }
            }
        });

        galleryAddPic(imageFile);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.show_image_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id) {
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void galleryAddPic(final File imageFile) {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        Uri contentUri = Uri.fromFile(imageFile);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }
}
