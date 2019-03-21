package ReplicaHost2;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;

public class Replica2 implements Runnable{
	private DLMSImpl dlms;
	private DatagramPacket packet;
	private DatagramSocket socket;

	public Replica2(DatagramPacket packet,DatagramSocket socket,DLMSImpl dlms) {
		super();
		this.dlms=dlms;
		this.packet=packet;
		this.socket=socket;
	}
	
	@Override
	public void run() {

		System.out.println("========DLMS Replica2 start========");
		
		
		/*
		 * Runnable r1 = () -> { receive(1112); }; Runnable r2 = () -> { receive(2223);
		 * }; Runnable r3 = () -> { receive(3334); };
		 * 
		 * Thread Thread1 = new Thread(r1); Thread Thread2 = new Thread(r2); Thread
		 * Thread3 = new Thread(r3); Thread1.start(); Thread2.start(); Thread3.start();
		 */
	}
	

}
