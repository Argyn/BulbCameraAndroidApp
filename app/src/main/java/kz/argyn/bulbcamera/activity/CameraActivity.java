package kz.argyn.bulbcamera.activity;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import kz.argyn.bulbcamera.fragments.NavigationDrawerFragment;
import kz.argyn.bulbcamera.R;
import kz.argyn.bulbcamera.fragments.BulbModeFragment;

public class CameraActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private FragmentManager frgManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentTransaction frgTransaction = getFragmentManager().beginTransaction();
        frgTransaction.replace(R.id.container, new BulbModeFragment());
        frgTransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.camera, menu);
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

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        //FragmentManager fragmentManager = getFragmentManager();
        //fragmentManager.beginTransaction()
          //      .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
         //       .commit();
        switch(position) {
            case 1:
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, new BulbModeFragment())
                        .commit();
                break;
            default:
                break;
        }
    }
}
