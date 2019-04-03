package Sequencer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Model.RMPort;
import Model.SequencerPort;
import Model.logSetFormatter;
import Model.RMAddressInfo;

public class Sequencer {

    private Integer sequenceNumber;
    private Logger log;
    private static final int MAXNUM = 5;
    private static final int TIMEOUT = 2000;

    public Sequencer(Logger log) {
        this.sequenceNumber = 0;
        this.log = log;
    }

    /**
     * Receive request from FE and packet the msg with sequencer number
     *
     * @param udpPort
     * @throws IOException
     */
    public void receiveMessage(int udpPort) throws IOException {
        DatagramSocket socket = new DatagramSocket(udpPort);
        DatagramPacket packet = null;
        byte[] data = null;
        // int count = 0;
        System.out.println("====== 1. Sequencer starts ======");
        log.info("Sequencer starts! ");
        while (true) {
            data = new byte[1024];
            packet = new DatagramPacket(data, data.length);


            socket.receive(packet);

            String FEHostAddress = packet.getAddress().getHostAddress();
            String receiveMessage = new String(packet.getData(), 0, packet.getLength());
            log.info("Sequencer receive message: " + receiveMessage);

            synchronized (this.sequenceNumber) {
                String sendMessage = this.sequenceNumber.toString() + ":" + FEHostAddress + ":" + receiveMessage;
                this.sequenceNumber++;

                multicastMessage(sendMessage, socket);

                // log.info("Sequencer multicasts message: "+sendMessage);
            }
            // count++;
            // System.out.println("Server Connected锛�" + count);
            // InetAddress address = packet.getAddress();
            // System.out.println("Server IP锛�" + address.getHostAddress());
        }
    }

    /**
     * multicast message to rms
     */
    private void multicastMessage(String packageMessage, DatagramSocket socket) throws IOException {
        DatagramSocket aSocket = null;
        DatagramPacket reply = null;
        List list = new LinkedList();
        try {
            System.out.println("Client Started........");


            InetAddress address1 = InetAddress.getByName(RMAddressInfo.RM_ADDRESS_INFO.RM1address);
            InetAddress address2 = InetAddress.getByName(RMAddressInfo.RM_ADDRESS_INFO.RM2address);
            InetAddress address3 = InetAddress.getByName(RMAddressInfo.RM_ADDRESS_INFO.RM3address);

            byte[] data = packageMessage.getBytes();
            DatagramPacket[] packets = new DatagramPacket[3];
            packets[0] = new DatagramPacket(data, data.length, address1, RMPort.RM_PORT.rmPort1); // 6001
            packets[1] = new DatagramPacket(data, data.length, address2, RMPort.RM_PORT.rmPort2); // 6002
            packets[2] = new DatagramPacket(data, data.length, address3, RMPort.RM_PORT.rmPort3); // 6003

            System.out.println("====== 2. Sequencer multicasts message to RMS.======");
            for (int i = 0; i < 3; i++) {
                int send_count = 0;
                boolean tmp = false;
                while (!tmp && send_count < MAXNUM) {
                    try {
                        aSocket = new DatagramSocket();
                        aSocket.send(packets[i]);
                        byte[] buffer = new byte[1000];
                        reply = new DatagramPacket(buffer, buffer.length);
                        aSocket.receive(reply);
                        tmp = true;
                    } catch (InterruptedIOException e) {
                        send_count = 1;
                        System.out.println("Time out," + (MAXNUM - send_count) + "more times...");
                    } finally {
                        if(aSocket!=null){
                            aSocket.close();
                        }
                    }
                }


            }

//            socket.send(sendPacket1);
//            socket.send(sendPacket2);
//            socket.send(sendPacket3);
//            log.info("Sequencer multicasts message: " + packageMessage);
//
//            Timer timer = new Timer(false);
//            Thread thread = new Thread(timer);
//            thread.start();
//            while (list.size() < 3 && !timer.timeout) {
//                socket.receive(reply);
//                list.add(reply.getPort());
//            }
//
//            System.out.println("====== 3. Sequencer receives message from RMS.======" + list);
//            if (list.size() < 3) {
//                if (list.contains(RMPort.RM_PORT.rmPort1)) {
//                    socket.send(sendPacket1);
//                    System.out.println("====== 4. Sequencer re send message to RM1.======");
//                }
//                if (list.contains(RMPort.RM_PORT.rmPort2)) {
//                    socket.send(sendPacket2);
//                    System.out.println("====== 5. Sequencer re send message to RM2.======");
//                }
//                if (list.contains(RMPort.RM_PORT.rmPort3)) {
//                    socket.send(sendPacket3);
//                    System.out.println("====== 6. Sequencer re send message to RM3.======");
//                }
//            }


        } catch (Exception e) {
            System.out.println("udpClient error: " + e);
        }

    }


    public static void main(String[] args) throws IOException {
        Logger log = Logger.getLogger("Sequencer.log");
        log.setLevel(Level.ALL);
        FileHandler handler = new FileHandler("Sequencer.log");
        handler.setFormatter(new logSetFormatter());
        log.addHandler(handler);

        Sequencer sequencer = new Sequencer(log);
        sequencer.receiveMessage(SequencerPort.SEQUENCER_PORT.sequencerPort);
    }
}
