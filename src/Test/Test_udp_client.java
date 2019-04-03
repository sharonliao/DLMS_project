package Test;

import Model.FEPort;
import Model.SequencerPort;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Created by liaoxiaoyun on 2019-03-27.
 */
public class Test_udp_client {

    public String udpClient(String msg,int sPort) {
        DatagramSocket aSocket = null;
        String returnMsg ="";
        try {
            System.out.println("Client Started........");
            aSocket = new DatagramSocket();
            byte [] message = msg.getBytes();

            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = sPort;
            DatagramPacket request = new DatagramPacket(message,message.length, aHost, serverPort);
            aSocket.send(request);
            System.out.println("Request message sent from the client is : "+ new String(request.getData()));
//            byte [] buffer = new byte[1000];
//            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
//
//            aSocket.receive(reply);
//            returnMsg = new String(reply.getData()).trim();
//            System.out.println("Reply received from the server is: "+ returnMsg);

        } catch(Exception e){
            System.out.println("udpClient error: "+e);
        }
        return returnMsg;
    }

    public void udpServer(){
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket(FEPort.FE_PORT.RegistorPort);


            System.out.println("Server Started............");
            while (true) {
                byte[] buffer = new byte[1000];
                String rtnMsg = "";
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);// request received
                String requestMsg = new String(request.getData()).trim();
                System.out.println("Request received from client: " + requestMsg);
            }
        }catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }


    public static void main(String[] args){
        Test_udp_client testClient = new Test_udp_client();

        ArrayList<String> messages = new ArrayList<>();
        messages.add("addItem,CONM0001,CON9999,distributed,4");
        messages.add("removeItem,CONM0001,CON1111,-1");
        messages.add("listItem,CONM0001");
        messages.add("borrowItem,CONU0001,MCG2222");
        Runnable TaskListener = () ->{
            try{
                testClient.udpServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        Thread Thread2 = new Thread(TaskListener);
        //Thread2.start();

        for(String message : messages){
            testClient.udpClient(message, SequencerPort.SEQUENCER_PORT.sequencerPort);
        }

        try{
            Thread.sleep(8000);
        }catch (Exception e){
            e.printStackTrace();
        }


        String message3 = "borrowItem,CONU0001,MON1111";
        testClient.udpClient(message3, SequencerPort.SEQUENCER_PORT.sequencerPort);
        
        
        String message4 = "exchangeItem,CONU0001,MON2222,MON1111";
        testClient.udpClient(message4,SequencerPort.SEQUENCER_PORT.sequencerPort);

        String message5 = "findItem,CONU0001,MON2222,MON1111";
        testClient.udpClient(message5,SequencerPort.SEQUENCER_PORT.sequencerPort);

    }



}
