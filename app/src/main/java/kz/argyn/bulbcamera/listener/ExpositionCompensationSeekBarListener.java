package kz.argyn.bulbcamera.listener;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.SeekBar;

import kz.argyn.bulbcamera.helper.CameraHelper;

/**
 * Created by argyn on 31/08/2014.
 */
public class ExpositionCompensationSeekBarListener implements SeekBar.OnSeekBarChangeListener {
    private CameraHelper cameraHelper;

    public ExpositionCompensationSeekBarListener(Context context, CameraHelper cameraHelper) {
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
