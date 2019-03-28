package Test;

import Model.FEPort;
import Model.RMPort;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class Test_rm {

    public String udpClient(String msg, int sPort) {
        DatagramSocket aSocket = null;
        String returnMsg = "";
        try {
            System.out.println("Client Started........");
            aSocket = new DatagramSocket();
            byte[] message = msg.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = sPort;
            DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);
            aSocket.send(request);
            System.out.println("Request message sent from the client is : " + new String(request.getData()));
        } catch (Exception e) {
            System.out.println("udpClient error: " + e);
        }
        return returnMsg;
    }

    public void udpServer() {
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
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }

    public static void main(String[] args) {
        Test_udp_client testClient = new Test_udp_client();
        ArrayList<String> messages = new ArrayList<>();
        messages.add("0:localhost:addItem,CONM0001,CON9999,distributed,1");
        messages.add("1:localhost:listItem,CONM0001");
        messages.add("2:localhost:listItem,MONM0001");
        messages.add("3:localhost:listItem,MCGM0001");
        messages.add("4:localhost:borrowItem,CONU0001,CON1111");
        Runnable TaskListener = () -> {
            try {
                testClient.udpServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Thread Thread2 = new Thread(TaskListener);
        Thread2.start();
        for (String message : messages) {
            testClient.udpClient(message, RMPort.RM_PORT.rmPort1);
        }
        try {
            Thread.sleep(8000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String message1 = "5:localhost:exchangeItem,CONU0001,CON2222,CON1111";
        testClient.udpClient(message1, RMPort.RM_PORT.rmPort1);

        String message2 = "6:localhost:exchangeItem,CONU0001,MCG1111,CON2222";
        testClient.udpClient(message2, RMPort.RM_PORT.rmPort1);

        String message3 = "7:localhost:borrowItem,MCGU0001,CON3333";
        testClient.udpClient(message3, RMPort.RM_PORT.rmPort1);

        String message4 = "8:localhost:exchangeItem,MCGU0001,CON4444,CON3333";
        testClient.udpClient(message4, RMPort.RM_PORT.rmPort1);

        String message5 = "9:localhost:borrowItem,MCGU0002,CON2222";
        testClient.udpClient(message5, RMPort.RM_PORT.rmPort1);

        String message6 = "10:localhost:exchangeItem,MCGU0001,CON2222,CON4444";
        testClient.udpClient(message6, RMPort.RM_PORT.rmPort1);

        String message7 = "11:localhost:borowItem,MONU0001,CON3333";
        testClient.udpClient(message7, RMPort.RM_PORT.rmPort1);

        String message8 = "12:localhost:borowItem,MONU0001,MCG1111";
        testClient.udpClient(message8, RMPort.RM_PORT.rmPort1);

        String message9 = "13:localhost:exchangeItem,MONU0001,MCG2222,CON3333";
        testClient.udpClient(message9, RMPort.RM_PORT.rmPort1);

        String message10 = "14:localhost:removeItem,CONM0001,CON3333";
        testClient.udpClient(message10, RMPort.RM_PORT.rmPort1);

        String message11 = "15:localhost:exchangeItem,MONU0001,CON4444,CON3333";
        testClient.udpClient(message11, RMPort.RM_PORT.rmPort1);
    }


}
