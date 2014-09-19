package kz.argyn.bulbcamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.SeekBar;

/**
 * Created by argyn on 31/08/2014.
 */
public class ExpositionCompensationSeekBarListener implements SeekBar.OnSeekBarChangeListener {
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private CameraHelper cameraHelper;

    public ExpositionCompensationSeekBarListener(Context context, CameraHelper cameraHelper) {
        settings = context.getSharedPreferences(Settings.SETTINGS_FILE, 0);
        editor = settings.edit();
        this.cameraHelper = cameraHelper;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int value = progress - cameraHelper.maxExposureCompensation();
        cameraHelper.setExposureCompensation(value);

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
