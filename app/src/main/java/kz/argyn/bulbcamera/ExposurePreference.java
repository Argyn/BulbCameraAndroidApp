package kz.argyn.bulbcamera;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;

/**
 * Created by Argyn on 17.08.2014.
 */
public class ExposurePreference extends DialogPreference {

    private SharedPreferences preferences;

    public ExposurePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(kz.argyn.bulbcamera.R.layout.exposure_dialog_preference);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);


    }

    @Override
    public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        builder.setTitle("Exposure level");
        builder.setPositiveButton(null, null);
        builder.setNeutralButton(null, null);
        builder.setNegativeButton(null, null);
        super.onPrepareDialogBuilder(builder);
    }

    @Override
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        SeekBar seekBar = (SeekBar) view.findViewById(kz.argyn.bulbcamera.R.id.seekBar);
        /*Camera camera = Camera.open();
        Camera.Parameters params = camera.getParameters();
        camera.release();*/

        seekBar.setMax(12);
        int level = preferences.getInt(Preferences.EXPOSURE_LEVEL, 0);
        seekBar.setProgress(level);

        SeekBar.OnSeekBarChangeListener exposureSeekBarChangeListener =
                                                            new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    preferences.edit().putInt(Preferences.EXPOSURE_LEVEL, i).apply();

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        seekBar.setOnSeekBarChangeListener(exposureSeekBarChangeListener);
    }






}
