package kz.argyn.bulbcamera.helper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by argyn on 03/10/2014.
 */
public class ForwardCounter implements Runnable {
    public Handler printCounter;

    public ForwardCounter(Handler printCounter) {
        this.printCounter = printCounter;
    }

    @Override
    public void run() {
        int counter = 0;
        while(!Thread.currentThread().isInterrupted()) {
            try {
                // ask to sleep for a second
                Thread.currentThread();
                Thread.sleep(1000);
                // increment counter
                counter++;
                // prepare bundle with counter
                Bundle bundle = new Bundle();
                bundle.putInt("counter", counter);
                // packing into message
                Message msg = new Message();
                msg.setData(bundle);
                // send the message to handler
                printCounter.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }
}
