package kz.argyn.bulbcamera.thread;

import android.os.AsyncTask;

import kz.argyn.bulbcamera.helper.BulbBuffer;

/**
 * Created by argyn on 29/08/2014.
 */
public class BulbBufferAsyncTask extends AsyncTask<byte[], Void, Void> {

    private BulbBuffer buffer;

    public BulbBufferAsyncTask(BulbBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    protected Void doInBackground(byte[]... params) {
        buffer.append(params[0]);
        return null;
    }
}
