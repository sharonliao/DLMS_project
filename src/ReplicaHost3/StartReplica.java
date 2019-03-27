package ReplicaHost3;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

public class StartReplica {
	private static int portNum=6999;
	static Map<String, Integer> portlist = new HashMap<String, Integer>() {
		{
			put("MCG", 9999);
			put("CON", 8999);
			put("MON", 7999);
		};
	};
	public StartReplica(int portNumber) {
		this.portNum = portNumber;
		
	}
	public static void main(String[] args) {
		Runnable r1 = () -> {
			receive(portNum - 1);
		};
		Runnable CON = () -> {
			receive(portlist.get("CON")-1);
		};Runnable MCG = () -> {
			receive(portlist.get("MCG") - 1);
		};Runnable MON = () -> {
			receive(portlist.get("MON") - 1);
		};
		Thread udpServerThread = new Thread(r1);
		Thread CONServerThread = new Thread(CON);
		Thread MCGServerThread = new Thread(MCG);
		Thread MONServerThread = new Thread(MON);
		udpServerThread.start();
		CONServerThread.start();
		MCGServerThread.start();
		MONServerThread.start();
	}
	
	public static void receive(int portNum) {
		DatagramSocket aSocket = null;

		try {
			aSocket = new DatagramSocket(portNum);

			System.out.println("Replica " + portNum + " Started............");
			while (true) {
				byte[] buffer = new byte[1024];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String s = "";
				s = new String(request.getData()).trim();
				String[] m = s.split(",");
				StringBuilder itemname = new StringBuilder();
//				if (m[0].equals("borrow")) {
//					String replystr = borrowItem(m[1], m[2].substring(0, 7));
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("addtowaitlist")) {
//					String replystr = addToWaitlist(m[1], m[2]);
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("return")) {
//					String replystr = returnItem(m[1], m[2].substring(0, 7));
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("find")) {
//					String replystr = findItem(m[1], m[2]);
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("borrowsuccess")) {
//					String replystr = borrowRecord(m[1], m[2], m[3]);
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("checkuser")) {
//					String replystr = checkUser(m[1], m[2]);
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("setBorrowStatus")) {
//					String replystr = setBorrowRecord(m[1], m[2]);
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("borrowforexchange")) {
//					String replystr = borrowforExchange(m[1], m[2], m[3]);
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("returnforexchange")) {
//					String replystr = returnforexchange(m[1], m[2],m[3]);
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("rollbackborrow")) {
//					String replystr = rollbackBorrow(m[1], m[2]);
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("rollbackreturn")) {
//					String replystr = rollbackReturn(m[1], m[2]);
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("checkBookAvailable")) {
//					String replystr = checkBookAvailable(m[1], m[2], m[3],m[4]);
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("deleteborrowrecord")) {
//					String replystr = deleteborrowrecord(m[1], m[2]);
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				} else if (m[0].equals("checkReturn")) {
//					String replystr = checkReturn(m[1], m[2],m[3]);
//					byte[] replyb = replystr.getBytes();
//					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
//							request.getPort());
//					aSocket.send(reply);
//				}
//
//			}
//		} catch (SocketException e) {
//			System.out.println("Socket: " + e.getMessage());
//		} catch (IOException e) {
//			System.out.println("IO: " + e.getMessage());
//		} finally {
//			if (aSocket != null)
//				aSocket.close();
//		}
	}
	
	public static String sendMessage(byte[] message, int serverPort) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);
			aSocket.send(request);
			System.out.println("Request message sent from the client to server with port number " + serverPort + " is: "
					+ new String(request.getData()));
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			aSocket.receive(reply);
			System.out.println("Reply received from the server with port number " + serverPort + " is: "
					+ new String(reply.getData()));

			return new String(reply.getData()).trim();

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
		return null;
	}
}
