package ReplicaHost1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.RemoteException;

public class UdpServer implements Runnable{


    DatagramSocket socket ;
    DatagramPacket packet = null;
    DLMSImp dlms;

    public UdpServer(DatagramSocket socket,DatagramPacket packet, DLMSImp dlms) {
        this.packet = packet;
        this.socket = socket;
        this.dlms = dlms;
    }


    @Override
    public void run() {
        // TODO Auto-generated method stub


        try {
            byte[] receiveMsg = new byte[1024];
            byte[] replyMsg = null;

            while (true) {
                DatagramPacket request = new DatagramPacket(receiveMsg, receiveMsg.length);
                socket.receive(request);
                String message = new String(request.getData(), 0, request.getLength()).split("\n")[0];

                String operation[] = message.split(",");

                switch (operation[0]){

                    case ("addItem"):
                        String addResult=dlms.addItem(operation[1], operation[2], operation[3], Integer.parseInt(operation[4]));
                        replyMsg=addResult.getBytes();
                        break;
                    case ("removeItem"):
                        String removeResult=dlms.removeItem(operation[1], operation[2], Integer.parseInt(operation[3]));
                        replyMsg=removeResult.getBytes();
                        break;
                    case ("listItem"):
                        String listResult=dlms.listItemAvailability(operation[1]);
                        replyMsg=listResult.getBytes();
                        break;
                    case ("borrowItem"):
                        String borrowResult=dlms.borrowItem(operation[1],operation[2]);
                        replyMsg=borrowResult.getBytes();
                        break;
                    case (" addToWaitlist"):
                        String addToWaitlistResult=dlms.putInWaiting(operation[1],operation[2]);
                        replyMsg=addToWaitlistResult.getBytes();
                        break;
                    case ("findItem"):
                        String findResult=dlms.findItem(operation[1],operation[2]);
                        replyMsg=findResult.getBytes();
                        break;
                    case ("returnItem"):
                        String returnResult=dlms.returnItem(operation[1],operation[2]);
                        replyMsg=returnResult.getBytes();
                        break;
                    case ("exchangeItem"):
                        String exchangeResult=dlms.exchangeItem(operation[1],operation[2],operation[3]);
                        replyMsg=exchangeResult.getBytes();
                        break;
                    case ("addToWaitlistforExchagne"):
                        String newexchangeResult=dlms.ex_putInWaiting(operation[1],operation[2],operation[3]);
                        replyMsg=newexchangeResult.getBytes();
                        break;
                    default:
                        System.out.println("\nERROR: Invalid input please try again.");
                        break;

                }

                DatagramPacket replyPacket = new DatagramPacket(replyMsg, 0,replyMsg.length, request.getAddress(), request.getPort());
                socket.send(replyPacket);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

