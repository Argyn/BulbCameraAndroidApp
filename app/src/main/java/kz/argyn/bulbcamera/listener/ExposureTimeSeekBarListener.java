package kz.argyn.bulbcamera.listener;

import android.widget.SeekBar;
import android.widget.TextView;

import kz.argyn.bulbcamera.helper.CameraHelper;

/**
 * Created by argyn on 19/09/2014.
 */
public class ExposureTimeSeekBarListener implements SeekBar.OnSeekBarChangeListener {

    private TextView exposureTimeLabel;
    private CameraHelper cameraHelper;

    public ExposureTimeSeekBarListener(TextView exposureTimeLabel, CameraHelper cameraHelper) {
        this.exposureTimeLabel = exposureTimeLabel;
        this.cameraHelper = cameraHelper;
    }
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if(progress!=0)
            exposureTimeLabel.setText(String.valueOf((double)progress*30/60));
        else
            exposureTimeLabel.setText("∞");

        cameraHelper.setExposureTime(progress*30);


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
