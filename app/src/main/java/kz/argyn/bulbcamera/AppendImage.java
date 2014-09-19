package kz.argyn.bulbcamera;

/**
 * Created by argyn on 28/08/2014.
 */
public class AppendImage implements Runnable {
    private byte[] bytes;
    private BulbBuffer buffer;

    public AppendImage(byte[] bytes, BulbBuffer buffer) {
        this.bytes = bytes;
        this.buffer = buffer;

    }

    @Override
    public void run() {
        buffer.append(this.bytes);
    }
}
