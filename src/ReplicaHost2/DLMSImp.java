package ReplicaHost2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

import Model.Item;

public class DLMSImp {

	public HashMap<String, Item> map;
	public HashMap<String, List<String>> UserBorrow;
	public Map<String, Queue<String>> WaitList;
	public HashMap<String, Item> temp;
	public Logger log;
	public String library;

	String answer = "";

	public DLMSImp(String library, Logger log) {
		this.library = library;
		this.log = log;
		map = new HashMap<>();
		temp = new HashMap<>();
		UserBorrow = new HashMap<>();
		WaitList = new HashMap<>();

		Runnable r1 = () -> {
			receive(1112);
		};
		Runnable r2 = () -> {
			receive(2223);
		};
		Runnable r3 = () -> {
			receive(3334);
		};

		Thread thread1 = new Thread(r1);
		Thread thread2 = new Thread(r2);
		Thread thread3 = new Thread(r3);
		thread1.start();
		thread2.start();
		thread3.start();

		if (library.equals("CON")) {
			map.put("CON1111", new Item("CON1111", "Test for Concordia", 3));
			map.put("CON2222", new Item("CON2222", "Math", 5));
			map.put("CON3333", new Item("CON3333", "French", 1));

		} else if (library.equals("MCG")) {
			map.put("MCG1111", new Item("MCG1111", "Test for Mcgill", 8));
			map.put("MCG2222", new Item("MCG2222", "Math", 1));
			map.put("MCG3333", new Item("MCG3333", "French", 1));

		} else if (library.equals("MON")) {
			map.put("MON1111", new Item("MON1111", "Test for Mcgill", 5));
			map.put("MON2222", new Item("MON2222", "Math", 2));
			map.put("MON3333", new Item("MON3333", "French", 1));
		}

	}



	public void receive(int portNumber) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(portNumber);
			byte[] buffer = new byte[1000];
			if (portNumber == 1112) {
				System.out.println("Concordia UdpServer Started............");
			} else if (portNumber == 2223) {
				System.out.println("Mcgill UdpServer Started............");
			} else if (portNumber == 3334) {
				System.out.println("Montreal UdpServer Started............");
			}
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String str = new String(request.getData(), 0, request.getLength());
				String answer = "";

				
				
				

				DatagramPacket reply = new DatagramPacket(answer.getBytes(), answer.length(), request.getAddress(),
						request.getPort());
				aSocket.send(reply);
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null) {
				aSocket.close();
			}
		}
	}

	public String sendMessage(int portNumber, String str) {
		DatagramSocket aSocket = null;
		try {
			System.out.println("Client Started........");
			aSocket = new DatagramSocket();
			byte[] message = str.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");

			DatagramPacket request = new DatagramPacket(message, str.length(), aHost, portNumber);

			aSocket.send(request);

			byte[] receiveMsg = new byte[1024];
			DatagramPacket reply = new DatagramPacket(receiveMsg, receiveMsg.length);
			aSocket.receive(reply);
			String info = new String(reply.getData(), 0, reply.getLength());

			System.out.println("Reply message sent from the client is : " + info);
			return info;

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null) {
				aSocket.close();
			}
		}
		return "";
	}

}
