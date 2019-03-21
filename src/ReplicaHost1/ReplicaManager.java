package ReplicaHost1;

import java.io.IOException;

public class ReplicaManager {

	/**
	 * start rm listener
	 * @param RMPort
	 * @throws Exception
	 */
	public void startRMListener(int RMPort) throws Exception {

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
