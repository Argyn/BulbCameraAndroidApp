package kz.argyn.bulbcamera;

/**
 * Created by argyn on 05/09/2014.
 */
public class BulbBufferThread implements Runnable {
    private BulbBuffer buffer;
    private byte[] bytes;

    public BulbBufferThread(BulbBuffer buffer, byte[] bytes) {
        this.buffer = buffer;
        this.bytes = bytes;
    }

    @Override
    public void run() {
        buffer.append(bytes);
    }
}
