package ReplicaHost2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;

public class UdpServer implements Runnable {

	DatagramSocket socket;
	DatagramPacket packet;
	DLMSImp dlms;

	public UdpServer(DatagramSocket socket, DatagramPacket packet, DLMSImp dlms) {
		this.packet = packet;
		this.socket = socket;
		this.dlms = dlms;
	}

	@Override
	public void run() {
		InetAddress address = packet.getAddress();
		int port = packet.getPort();

		String message = new String(packet.getData(), 0, packet.getLength());

		byte[] replyMsg = new byte[1024];
		String reply = null;

		try {

			String operation[] = message.split(",");
			while (true) {

				switch (operation[0]) {

				case ("addItem"):
					reply = dlms.addItem(operation[1], operation[2], operation[3],
							Integer.parseInt(operation[4]));
					break;
				case ("removeItem"):
					reply = dlms.removeItem(operation[1], operation[2], Integer.parseInt(operation[3]));					
					break;
				case ("listItem"):
					reply = dlms.listItemAvailability(operation[1]);
					break;
				case ("borrowItem"):
					reply = dlms.borrowItem(operation[1], operation[2]);
					break;
				case (" addToWaitlist"):
					reply = dlms.addWaitList(operation[1], operation[2]);
					break;
				case ("findItem"):
					reply = dlms.findItem(operation[1], operation[2]);
					break;
				case ("returnItem"):
					reply = dlms.returnItem(operation[1], operation[2]);
					break;
				case ("exchangeItem"):
					reply = dlms.exchangeItem(operation[1], operation[2], operation[3]);
					break;
				case ("addToWaitlistforExchagne"):
					reply = dlms.newExchange(operation[1], operation[2], operation[3]);
					break;
				default:
					System.out.println("\nERROR: Invalid input please try again.");
					break;

				}
				replyMsg=reply.getBytes();
				DatagramPacket replyPacket = new DatagramPacket(replyMsg, 0, replyMsg.length, address, port);
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
