package Sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Model.RMPort;
import Model.logSetFormatter;

public class Test_Seq {

	private Integer sequenceNumber;
	private Logger log;

	public Test_Seq(Logger log) {
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
				multicastMessage(sendMessage, socket);
				
				log.info("Sequencer multicasts message: "+sendMessage);
			}
			//count++;
			//System.out.println("Server Connected：" + count);
			//InetAddress address = packet.getAddress();
			//System.out.println("Server IP：" + address.getHostAddress());
		}
	}

	/**
	 * multicast message to rms
	 * @param packageMessage
	 * @param socket
	 * @throws IOException
	 */
	private void multicastMessage(String packageMessage, DatagramSocket socket) throws IOException {
		
		//the host address of replica
		InetAddress address = InetAddress.getByName("localhost");

		byte[] data = packageMessage.getBytes();
		DatagramPacket sendPacket1 = new DatagramPacket(data, data.length, address, RMPort.RM_PORT.rmPort1); // 6001
		DatagramPacket sendPacket2 = new DatagramPacket(data, data.length, address, RMPort.RM_PORT.rmPort2); // 6002
		DatagramPacket sendPacket3 = new DatagramPacket(data, data.length, address, RMPort.RM_PORT.rmPort3); // 6003

		System.out.println("====== 2. Sequencer multicasts message to RMS.======" );
		socket.send(sendPacket1);
		socket.send(sendPacket2);
		socket.send(sendPacket3);
	}

	private static void sendMessage(int serverPort){
		DatagramSocket aSocket=null;
		try {
			System.out.println("client started......");
			aSocket = new DatagramSocket();
			byte[] message="0:listItem,CONM1111".getBytes();
			InetAddress aHost=InetAddress.getByName("localhost");
			DatagramPacket request=new DatagramPacket(message,message.length,aHost,serverPort);
			aSocket.send(request);
			System.out.println("request message sent from the udpserver1 to server with port number" + serverPort +  "is: " + new String(request.getData()));
			
			byte[] buffer= new byte[1000];
			DatagramPacket reply=new DatagramPacket(buffer,buffer.length);
			aSocket.receive(reply);
			System.out.println("reply reveived from the server with port number " + serverPort  + " is: " +new String(reply.getData()));
		}catch(SocketException e) {
			System.out.println("Socket;"+e.getMessage());
			
		}catch(IOException e) {
			System.out.println("IO;"+e.getMessage());
			
		}finally {
			if(aSocket!=null) {
				aSocket.close();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		Logger log=Logger.getLogger("Sequencer.log");
		log.setLevel(Level.ALL);
		FileHandler handler=new FileHandler("Sequencer.log");
		handler.setFormatter(new logSetFormatter());
		log.addHandler(handler);
		
		Test_Seq sequencer = new Test_Seq(log);
		//sequencer.receiveMessage(SequencerPort.SEQUENCER_PORT.sequencerPort);
		sequencer.sendMessage(RMPort.RM_PORT.rmPort1);
	}
}
