package kz.argyn.bulbcamera.helper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * Created by argyn on 03/10/2014.
 */
public class BackCounter implements Runnable {

    public Handler printCounter;
    public int time;

    public BackCounter(Handler printCounter, int time) {
        this.printCounter = printCounter;
        this.time = time;
    }

    @Override
    public void run() {
        int counter = time;
        while(!Thread.currentThread().isInterrupted()) {
            try {
                Thread.currentThread();
                Thread.sleep(1000);
                counter--;
                Bundle bundle = new Bundle();
                bundle.putInt("counter", counter);
                Message msg = new Message();
                msg.setData(bundle);
                printCounter.sendMessage(msg);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }
}
