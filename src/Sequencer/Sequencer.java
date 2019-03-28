package Sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Model.RMPort;
import Model.SequencerPort;
import Model.logSetFormatter;

public class Sequencer {

	private Integer sequenceNumber;
	private Logger log;

	public Sequencer(Logger log) {
		this.sequenceNumber = 0;
		this.log=log;
	}

	/**
	 * Receive request from FE  and packet the msg with sequencer number
	 * @param udpPort
	 * @throws IOException
	 */
	public void receiveMessage(int udpPort) throws IOException {
		DatagramSocket socket = new DatagramSocket(udpPort);
		DatagramPacket packet = null;
		byte[] data = null;
		//int count = 0;

		log.info("Sequencer starts! ");
		while (true) {
			data = new byte[1024];
			packet = new DatagramPacket(data, data.length);


			System.out.println("====== 1. Sequencer starts ======" );
			socket.receive(packet);
						
			String FEHostAddress = packet.getAddress().getHostAddress();
			String receiveMessage = new String(packet.getData(), 0, packet.getLength());
			log.info("Sequencer receive message: "+receiveMessage);
			
			synchronized (this.sequenceNumber) {
				String sendMessage = this.sequenceNumber.toString() + ":" + FEHostAddress + ":" + receiveMessage;
				this.sequenceNumber++;

				multicastMessage(sendMessage, RMPort.RM_PORT.rmPort1);
				
				//log.info("Sequencer multicasts message: "+sendMessage);
			}
			//count++;
			//System.out.println("Server Connected：" + count);
			//InetAddress address = packet.getAddress();
			//System.out.println("Server IP：" + address.getHostAddress());
		}
	}

	/**
	 * multicast message to rms
	 */
	private void multicastMessage(String msg,int sPort) throws IOException {
		DatagramSocket aSocket = null;
		String returnMsg ="";
		try {
			System.out.println("Client Started........");
			aSocket = new DatagramSocket();
			byte [] message = msg.getBytes();

			InetAddress aHost = InetAddress.getByName("224.0.0.1");
			int serverPort = sPort;
			DatagramPacket request = new DatagramPacket(message,message.length, aHost, serverPort);
			aSocket.send(request);
			log.info("Sequencer multicasts message: "+msg);
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
//		//the host address of replica
//		InetAddress address = InetAddress.getByName("localhost");
//
//		byte[] data = packageMessage.getBytes();
//		DatagramPacket sendPacket1 = new DatagramPacket(data, data.length, address, RMPort.RM_PORT.rmPort1); // 6001
//		DatagramPacket sendPacket2 = new DatagramPacket(data, data.length, address, RMPort.RM_PORT.rmPort2); // 6002
//		DatagramPacket sendPacket3 = new DatagramPacket(data, data.length, address, RMPort.RM_PORT.rmPort3); // 6003
//
//		System.out.println("====== 2. Sequencer multicasts message to RMS.======" );
//		socket.send(sendPacket1);
//		socket.send(sendPacket2);
//		socket.send(sendPacket3);
	}

	public static void main(String[] args) throws IOException {
		Logger log=Logger.getLogger("Sequencer.log");
		log.setLevel(Level.ALL);
		FileHandler handler=new FileHandler("Sequencer.log");
		handler.setFormatter(new logSetFormatter());
		log.addHandler(handler);
		
		Sequencer sequencer = new Sequencer(log);
		sequencer.receiveMessage(SequencerPort.SEQUENCER_PORT.sequencerPort);
	}
}
