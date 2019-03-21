package Sequencer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import Model.RMPort;
import Model.SequencerPort;

public class Sequencer {

	private Integer sequenceNumber;

	public Sequencer() {
		this.sequenceNumber = 0;
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
			socket.receive(packet);

			String FEHostAddress = packet.getAddress().getHostAddress();
			String receiveMessage = new String(packet.getData(), 0, packet.getLength());

			synchronized (this.sequenceNumber) {
				String sendMessage = this.sequenceNumber.toString() + ":" + FEHostAddress + ":" + receiveMessage;
				this.sequenceNumber++;
				multicastMessage(sendMessage, socket);
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

		socket.send(sendPacket1);
		socket.send(sendPacket2);
		socket.send(sendPacket3);
	}

	public static void main(String[] args) throws IOException {
		Sequencer sequencer = new Sequencer();
		sequencer.receiveMessage(SequencerPort.SEQUENCER_PORT.sequencerPort);
	}
}
