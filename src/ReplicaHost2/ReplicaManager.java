package ReplicaHost2;

import Model.FEPort;
import Model.Message;
import Model.RMPort;
import Model.logSetFormatter;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplicaManager {
	Logger logger;
	int replicaId;
	int failureTimes;
	int latestFailureId;
	int seqNum;
	Replica2 replica2;
	HashMap<Integer, Message> holdBackQueue;
	// Queue<Message> holdBackQueue;
	Queue<Message> deliveryQueue;
	Queue<Message> historyQueue;
	private static final int MAXNUM = 5;
	private static final int TIMEOUT = 5000;

	ReplicaManager(Logger logger) {

		this.logger = logger;
		replicaId = 2;
		failureTimes = 0;
		latestFailureId = 0;
		holdBackQueue = new HashMap<>();
		deliveryQueue = new LinkedList<>();
		historyQueue = new LinkedList<>();
		replica2 = new Replica2(); // 必须启动replica1
		System.out.println(replica2.getClass());
	}

	/**
	 * start rm listener
	 * 
	 * @param RMPort
	 * @throws Exception
	 */
	public void startRMListener(int RMPort) throws Exception {
		// DatagramSocket asocket = new DatagramSocket(RMPort);
		DatagramPacket apocket = null;
		byte[] buf = null;
		logger.info("RM2 is listenning! ");

		MulticastSocket asocket = new MulticastSocket(RMPort);
		asocket.joinGroup(InetAddress.getByName("224.0.0.1"));

		while (true) {
			buf = new byte[2000];
			apocket = new DatagramPacket(buf, buf.length);
			asocket.receive(apocket);
			String message = new String(apocket.getData()).trim();
			asocket.send(apocket);
			System.out.println("UDP receive : " + message);

			logger.info("RM2 receives message:" + message);

			String[] messageSplited = message.split(":");
			System.out.println("messageSplited[0]--sequencerid==" + messageSplited[0]);

			switch (messageSplited[0]) {
			case "SoftWareFailure":
				recoverFromFailure(message); // from FE SoftWareFailure:seqID:replicaId
				break;
			case "Crash":
				recoverFromCrash(message); // from FE
				break;
			default:
				moveToHoldBackQueue(message, asocket); // from Sequencer, normal operation message
				break;
			}

		}
	}

	public void recoverFromFailure(String failureMsg) throws IOException {
		// SoftWareFailure:seqId:replicaID
		int failureReplica = Integer.parseInt(failureMsg.split(":")[2]);
		int msgSeqId = Integer.parseInt(failureMsg.split(":")[1]);
		if (failureReplica == replicaId) {
			logger.info("Replica " + failureReplica + " has failure");
			if (checkIfFailThreeTimes(msgSeqId)) {
				replica2.fixBug();
			}
		}
	}

	public boolean checkIfFailThreeTimes(int msgId) {
		boolean rtn = false;
		if (msgId + 1 == latestFailureId) {
			failureTimes++;
			this.logger.info("failure time:" + failureTimes);
		} else {
			latestFailureId = msgId;
			failureTimes = 0;
		}
		if (failureTimes >= 3) {
			// tell the replica correct the reply
			rtn = true;
			failureTimes = 0;
		}
		return rtn;
	}

	public void recoverFromCrash(String msg) {
		int creshNum = Integer.parseInt(msg.split(":")[1]);
		try {
			if (creshNum == replicaId) {
				// recoverFromCrash
				this.logger.info("Crash: Replica" + replicaId);
				restartReplica();
			} else {
				// check if replica is alive
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void restartReplica() throws IOException {
		// Replica1.main(null);
		// restart 之前要把replica1的端口全都关掉，不然udp会报错

		replica2.closeImpSocket();
		replica2 = null;
		System.gc();

		replica2 = new Replica2();
		replica2.historyQueue = this.historyQueue;
		replica2.recoverRplicaData();
		replica2.crashFree = true;
		logger.info("restart and recover replica2.");
	}

	/**
	 * if the hold back queue does not contain the msg, then put it
	 * 
	 * @param msg
	 * @throws IOException
	 */
	private void moveToHoldBackQueue(String msg, DatagramSocket aSocket) throws IOException {
		System.out.println("moveToHoldBackQueue --" + msg);
		int id = Integer.parseInt(msg.split(":")[0]);
		if (!holdBackQueue.containsKey(id)) {
			Message message = splitMessge(msg);
			holdBackQueue.put(id, message);
		}
		moveToDeliveryQueue(aSocket);
	}

	/**
	 * check the msg's seqId and put the msg to the delivery queue
	 * 
	 * @throws IOException
	 */
	private void moveToDeliveryQueue(DatagramSocket aSocket) throws IOException {
		System.out.println("moveToDeliveryQueue --");
		if (holdBackQueue.size() != 0) {
			if (holdBackQueue.containsKey(this.seqNum)) {
				Message message = holdBackQueue.get(this.seqNum);
				if (!this.deliveryQueue.contains(message)) {
					this.deliveryQueue.offer(message);
					this.holdBackQueue.remove(this.seqNum);
					this.seqNum++;
					checkAndExecuteMessage(aSocket);
					moveToDeliveryQueue(aSocket);
				}
			}
		}
	}

	public Message splitMessge(String message) {
		Message msg = new Message();
		// seqId,FEaddr,(operation,userId......)
		String[] msgArry = message.split(":");
		msg.seqId = Integer.parseInt(msgArry[0]);
		msg.feHostAddr = msgArry[1];
		msg.operationMsg = msgArry[2];
		msg.libCode = msg.operationMsg.split(",")[1].substring(0, 3);
		return msg;
	}

	/**
	 * check sequencer number and delivery number, then ready to send msg
	 * 
	 * @throws IOException
	 */
	private void checkAndExecuteMessage(DatagramSocket aSocket) throws IOException {
		System.out.println("checkAndExecuteMessage --");
		Message message = this.deliveryQueue.peek();
		if (message != null) {
			message = this.deliveryQueue.poll();
			sendToReplicaAndGetReply(message, aSocket);
			historyQueue.offer(message);
			checkAndExecuteMessage(aSocket);
		}
	}

	/**
	 * Send the message to the replica and receive the reply
	 * 
	 * @param msg
	 * @throws IOException
	 */
	private void sendToReplicaAndGetReply(Message msg, DatagramSocket aSocket) throws IOException {
		System.out.println("sendToReplicaAndGetReply");
		String reply = msg.seqId + ":" + this.replicaId + ":" + replica2.executeMsg(msg);
		System.out.println("reply:" + reply);
		sendToFE(aSocket, reply);
		logger.info("RM2 sends message to Replica2: " + msg.operationMsg + "; reply from Replica2: " + reply);

	}

	private void sendToFE(DatagramSocket aSocket, String msgFromReplica) {
		DatagramPacket reply = null;
		int send_count = 0;
		boolean revResponse = false;
		while (!revResponse && send_count < MAXNUM) {
			try {
				System.out.println("sendToFE");
				aSocket.setSoTimeout(TIMEOUT);
				InetAddress address = InetAddress.getByName("172.20.10.2");
				byte[] data = msgFromReplica.getBytes();
				DatagramPacket aPacket = new DatagramPacket(data, data.length, address, FEPort.FE_PORT.RegistorPort);
				aSocket.send(aPacket);
				byte[] buffer = new byte[1000];
				reply = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(reply);
				revResponse = true;
				logger.info("RM2 sends message to FE:" + msgFromReplica);
				// aSocket.close();//如果不colse会怎么样
			} catch (InterruptedIOException e) {
				send_count += 1;
				System.out.println("Time out," + (MAXNUM - send_count) + " more tries...");
			} catch (Exception e) {
				System.out.println("udpClient error: " + e);
			}
		}
	}

	public static void main(String[] args) throws IOException {
		Logger rmLogger = Logger.getLogger("RM2.log");
		rmLogger.setLevel(Level.ALL);
		FileHandler handler = new FileHandler("RM2.log");
		handler.setFormatter(new logSetFormatter());
		rmLogger.addHandler(handler);

		ReplicaManager rm = new ReplicaManager(rmLogger);

		Runnable TaskListener = () -> {
			try {
				rm.startRMListener(RMPort.RM_PORT.rmPort1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		Thread Thread2 = new Thread(TaskListener);
		Thread2.start();

	}

}
