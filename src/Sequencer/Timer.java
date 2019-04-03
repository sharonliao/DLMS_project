package Sequencer;

import java.net.DatagramSocket;

public class Timer implements Runnable{

    volatile Boolean timeout;


    public Timer( Boolean timeout){
        this.timeout = timeout;
    }
    @Override
    public void run() {
        try {
            Thread.sleep(10000);
            timeout = true;
            //socket.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
