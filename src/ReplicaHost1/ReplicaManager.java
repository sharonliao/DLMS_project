package ReplicaHost1;

import Model.FEPort;
import Model.Message;
import Model.RMPort;
import Model.ReplicaPort;
import FrontEnd.Timer;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplicaManager {
	Logger logger;
	int replicaId = 1;
	int failureTimes = 0;
	int latestFailureId = 0;
	int seqNum;
	HashMap<Integer,Message> holdBackQueue;
	//Queue<Message> holdBackQueue;
	Queue<Message> deliveryQueue;
	Queue<Message> historyQueue;



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
		logger.info("RM is listenning ");

		while (true){
			buf = new byte[2000];
			apocket = new DatagramPacket(buf,buf.length);
			asocket.receive(apocket);
			String message = new String(apocket.getData()).trim();
			System.out.println("UDP receive : " + message);

			String[] messageSplited = message.split("");

			switch (messageSplited[0]){
				case "Failure" : recoverFromFailure(message); // from FE
					             break;
				case "recoverFromCrash": recoverFromCrash(message); // from FE
					             break;
				default: moveToHoldBackQueue(message); //from Sequencer, normal operation message
					     break;
			}
		}
	}


	public void recoverFromFailure(String failureMsg) throws IOException {
		logger.info("Replica "+ replicaId + " has failure");
		//检查是否连续出错三次
		int msgId = 0;//注意修改 取到真正的msgId来比较是否连续错了三次
		if(checkIfFailThreeTimes(msgId)){
			// inform replica to reply correct meaasge
			InetAddress address = InetAddress.getByName("localhost");
			byte[] data = "Failure".getBytes();


			DatagramPacket apacket = new DatagramPacket(data,data.length,address, RMPort.RM_PORT.rmPort1_failure);
			DatagramSocket asocket = new DatagramSocket();
			asocket.send(apacket);
		}
	}

	public boolean checkIfFailThreeTimes(int msgId){
		boolean rtn = false;
		if (msgId+1 == latestFailureId){
			failureTimes ++;
		}else {
			latestFailureId = msgId;
			failureTimes = 0;
		}
		if(failureTimes >= 3){
			// tell the replica correct the reply
			rtn = true;
			failureTimes = 0;
		}
		return rtn;
	}


	public void recoverFromCrash(String msg){
		int creshNum = Integer.parseInt(msg.split(":")[0]);
		try{
			if(creshNum == replicaId){
				//recoverFromCrash
				this.logger.info("Crash: Replica" +replicaId );
				restartReplica();
			}else {
				//check if replica is alive
			}
		}catch (IOException e){
			e.printStackTrace();
		}

	}

	private void restartReplica() throws IOException{
		Runnable replica1 = () -> {
			try {
				Replica1.main(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		Thread thread = new Thread(replica1);
		thread.start();
	}






	/**
	 * if the hold back queue does not contain the msg, then put it 
	 * @param msg
	 * @throws IOException
	 */
	private void moveToHoldBackQueue(String msg) throws IOException {
		int id = Integer.parseInt(msg.split(":")[0]);
		if (!holdBackQueue.containsKey(id)) {
			Message message = splitMessge(msg);
			holdBackQueue.put(id,message);
		}
		moveToDeliveryQueue();
	}

	/**
	 * check the msg's seqId and put the msg to the delivery queue
	 * @throws IOException
	 */
	private void moveToDeliveryQueue() throws IOException {
		if(holdBackQueue.size() != 0){
			if(holdBackQueue.containsKey(this.seqNum)){
				Message message = holdBackQueue.get(this.seqNum);
				if( !this.deliveryQueue.contains(message)){
					this.deliveryQueue.offer(message);
					this.holdBackQueue.remove(this.seqNum);
					this.seqNum ++;
					checkAndExecuteMessage();
					moveToDeliveryQueue();
				}
			}
		}
	}

	public Message splitMessge(String message){
		Message msg = new Message();
		//记得修改数据
		msg.seqId = Integer.parseInt(message.split(",")[0]);
		msg.feHostAddr = message.split(",")[0];
		msg.operationMsg = message.split(",")[0];
		msg.libCode = message.split(",")[0];
		return msg;
	}

	/**
	 * check sequencer number and delivery number, then ready to send msg
	 * @throws IOException
	 */
	private void checkAndExecuteMessage() throws IOException {
		Message message = this.deliveryQueue.peek();
		if(message != null){
			message = this.deliveryQueue.poll();
			sendToReplicaAndGetReply(message);
			historyQueue.offer(message);
			checkAndExecuteMessage();
		}
	}
	
	/**
	 * Send the message to the replica and receive the reply
	 * @param msg
	 * @throws IOException
	 */
	private void sendToReplicaAndGetReply(Message msg) throws IOException{
		InetAddress address = InetAddress.getByName("localhost");
		String sendMsg = msg.operationMsg;
		byte[] data = sendMsg.getBytes();
		DatagramPacket aPacket = new DatagramPacket(data,data.length,address, ReplicaPort.REPLICA_PORT.replica1);
		DatagramSocket aSocket = new DatagramSocket(RMPort.RM_PORT.rmPort1);
		aSocket.send(aPacket);
		byte[] buff = new byte[2000];
		DatagramPacket replyPacket  = new DatagramPacket(buff,buff.length);

		Timer timer = new Timer(aSocket,false);
		Thread thread = new Thread(timer);
		thread.start();

		aSocket.receive(replyPacket);
		String reply = new String(aPacket.getData()).trim();
		sendToFE(aSocket,reply);
	}


	private void sendToFE(DatagramSocket aSocket, String msgFromReplica) throws IOException{
		InetAddress address = InetAddress.getByName("localhost");
		String msg = this.replicaId + ":" + msgFromReplica;
		byte[] data = msg.getBytes();
		DatagramPacket aPacket = new DatagramPacket(data,data.length,address, FEPort.FE_PORT.FEPort);
		aSocket.send(aPacket);
		aSocket.close();//如果不colse会怎么样
	}


//	private void crashListener(int crashPort){
//		logger.info("RM crash listener is started......");
//		try {
//			DatagramSocket ascoket = new DatagramSocket(crashPort);
//			while (true){
//				byte[] buff = new byte[2000];
//				DatagramPacket aPacket = new DatagramPacket(buff,buff.length);
//				ascoket.receive(aPacket);
//				String reply = new String(aPacket.getData()).trim();
//			}
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//	}


	
	public static void main(String[] args) {
		Logger rmLogger = Logger.getLogger("RM1.log");
		rmLogger.setLevel(Level.ALL);

		ReplicaManager rm = new ReplicaManager(rmLogger);

		Runnable TaskListener = () ->{
			try{
				rm.startRMListener(RMPort.RM_PORT.rmPort1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

//		Runnable CrashListener = () ->{
//			try{
//
//			}
//
//		};



	}

}
