package ReplicaHost1;

import Model.RMPort;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

public class ReplicaManager {
	Logger logger;
	int replicaId = 1;
	int replicaPort = 5555;

	ReplicaManager(Logger logger){
		this.logger = logger;
	}

	/**
	 * start rm listener
	 * @param RMPort
	 * @throws Exception
	 */
	public void startRMListener(int RMPort) throws Exception {
		DatagramSocket asocket = new DatagramSocket(RMPort);
		DatagramPacket apocket = null;
		byte[] buf = null;
		logger.info("RM is listenning ……");

		while (true){
			buf = new byte[2000];
			apocket = new DatagramPacket(buf,buf.length);
			asocket.receive(apocket);
			String message = new String(apocket.getData()).trim();
			System.out.println("UDP receive : " + message);

			String[] messageSplited = message.split("");

			switch (messageSplited[0]){
				case "Failure" : recoverFromFailure(); // from FE
					             break;
				case "recoverFromCrash": recoverFromCrash(); // from FE
					             break;
				default: moveToDeliveryQueue(message); //from Sequencer, normal operation message
				                 break;
			}
		}
	}


	public void recoverFromFailure() throws IOException {
		logger.info("Replica "+ replicaId + " has failure");
		// inform replica to reply correct meaasge
		InetAddress address = InetAddress.getByName("localhost");
		byte[] data = "Failure".getBytes();
		DatagramPacket apacket = new DatagramPacket(data,data.length,address, RMPort.RM_PORT.rmPort1_failure);
		DatagramSocket asocket = new DatagramSocket();
		asocket.send(apacket);
	}


	public void recoverFromCrash(){

	}


	/**
	 * if the hold back queue does not contain the msg, then put it 
	 * @param recvMsg
	 * @throws IOException
	 */
	private void moveToHoldBackQueue(String recvMsg) throws IOException {

	}

	/**
	 * check the msg's seqId and put the msg to the delivery queue
	 * @param recvMsg
	 * @throws IOException
	 */
	private void moveToDeliveryQueue(String recvMsg) throws IOException {

	}

	/**
	 * check sequencer number and delivery number, then ready to send msg
	 * @throws IOException
	 */
	private void checkAndExecuteMessage() throws IOException {

		
	}
	
	/**
	 * Send the message to the replica and receive the reply
	 * @param msg
	 * @throws IOException
	 */
	private void sendToReplica(String msg) throws IOException{
		
	}

	
	public static void main(String[] args) {

	}

}
