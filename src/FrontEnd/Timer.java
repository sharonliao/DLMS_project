package FrontEnd;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;

public class Timer implements Runnable{

    volatile Boolean timeout;
    DatagramSocket socket;
    int time;

    public Timer(DatagramSocket socket ,Boolean timeout, int time){
        this.socket = socket;
        this.timeout = timeout;
        this.time = time;
    }
    @Override
    public void run() {
        try {
            Thread.sleep(time);
            timeout = true;
            socket.close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
