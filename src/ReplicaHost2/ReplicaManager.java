package ReplicaHost2;

import Model.FEPort;
import Model.Message;
import Model.RMPort;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplicaManager {
	Logger logger;
	int replicaId;
	int failureTimes ;
	int latestFailureId ;
	int seqNum;
	Replica2 replica2;
	HashMap<Integer,Message> holdBackQueue;
	//Queue<Message> holdBackQueue;
	Queue<Message> deliveryQueue;
	Queue<Message> historyQueue;



	ReplicaManager(Logger logger){

		this.logger = logger;
		replicaId = 2;
		failureTimes = 0;
		latestFailureId = 0;
		holdBackQueue = new HashMap<>();
		deliveryQueue = new LinkedList<>();
		historyQueue = new LinkedList<>();
		replica2 = new Replica2(); //蹇呴』鍚姩replica1
		System.out.println(replica2.getClass());
	}

	/**
	 * start rm listener
	 * @param RMPort
	 * @throws Exception
	 */
	public void startRMListener(int RMPort) throws Exception {
//		DatagramSocket asocket = new DatagramSocket(RMPort);
		DatagramPacket apocket = null;
		byte[] buf = null;
		logger.info("RM is listenning ");

		MulticastSocket asocket = new MulticastSocket(RMPort);
		asocket.joinGroup(InetAddress.getByName("224.0.0.1"));
		

		while (true){
			buf = new byte[2000];
			apocket = new DatagramPacket(buf,buf.length);
			asocket.receive(apocket);
			String message = new String(apocket.getData()).trim();
			System.out.println("UDP receive : " + message);

			String[] messageSplited = message.split(":");
			System.out.println("messageSplited[0]--" + messageSplited[0]);

			switch (messageSplited[0]){
				case "Failure" : recoverFromFailure(message); // from FE
					             break;
				case "recoverFromCrash": recoverFromCrash(message); // from FE
					             break;
				default: moveToHoldBackQueue(message,asocket); //from Sequencer, normal operation message
					     break;
			}
		}
	}


	public void recoverFromFailure(String failureMsg) throws IOException {
		logger.info("Replica "+ replicaId + " has failure");
		//妫�鏌ユ槸鍚﹁繛缁嚭閿欎笁娆�
		int msgId = 0;//娉ㄦ剰淇敼 鍙栧埌鐪熸鐨刴sgId鏉ユ瘮杈冩槸鍚﹁繛缁敊浜嗕笁娆�
		if(checkIfFailThreeTimes(msgId)){
			replica2.fixBug();
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
		//Replica1.main(null);
		//restart 涔嬪墠瑕佹妸replica1鐨勭鍙ｅ叏閮藉叧鎺夛紝涓嶇劧udp浼氭姤閿�
		replica2 = null;
		System.gc();

		replica2 = new Replica2();
		replica2.historyQueue = this.historyQueue;
		replica2.recoverRplicaData();
	}


	/**
	 * if the hold back queue does not contain the msg, then put it 
	 * @param msg
	 * @throws IOException
	 */
	private void moveToHoldBackQueue(String msg,DatagramSocket aSocket) throws IOException {
		System.out.println("moveToHoldBackQueue --" + msg);
		int id = Integer.parseInt(msg.split(":")[0]);
		if (!holdBackQueue.containsKey(id)) {
			Message message = splitMessge(msg);
			holdBackQueue.put(id,message);
		}
		moveToDeliveryQueue(aSocket);
	}

	/**
	 * check the msg's seqId and put the msg to the delivery queue
	 * @throws IOException
	 */
	private void moveToDeliveryQueue(DatagramSocket aSocket) throws IOException {
		System.out.println("moveToDeliveryQueue --" );
		if(holdBackQueue.size() != 0){
			if(holdBackQueue.containsKey(this.seqNum)){
				Message message = holdBackQueue.get(this.seqNum);
				if( !this.deliveryQueue.contains(message)){
					this.deliveryQueue.offer(message);
					this.holdBackQueue.remove(this.seqNum);
					this.seqNum ++;
					checkAndExecuteMessage(aSocket);
					moveToDeliveryQueue(aSocket);
				}
			}
		}
	}

	public Message splitMessge(String message){
		Message msg = new Message();
		//seqId,FEaddr,(operation,userId......)
		//璁板緱淇敼鏁版嵁
		String[] msgArry = message.split(":");
		msg.seqId = Integer.parseInt(msgArry[0]);
		msg.feHostAddr = msgArry[1];
		msg.operationMsg = msgArry[2];
		msg.libCode = msg.operationMsg.split(",")[1].substring(0,3);
		return msg;
	}

	/**
	 * check sequencer number and delivery number, then ready to send msg
	 * @throws IOException
	 */
	private void checkAndExecuteMessage(DatagramSocket aSocket) throws IOException {
		System.out.println("checkAndExecuteMessage --" );
		Message message = this.deliveryQueue.peek();
		if(message != null){
			message = this.deliveryQueue.poll();
			sendToReplicaAndGetReply(message,aSocket);
			historyQueue.offer(message);
			checkAndExecuteMessage(aSocket);
		}
	}
	
	/**
	 * Send the message to the replica and receive the reply
	 * @param msg
	 * @throws IOException
	 */
	private void sendToReplicaAndGetReply(Message msg,DatagramSocket aSocket) throws IOException{
		System.out.println("sendToReplicaAndGetReply");
		String reply = this.replicaId + ":" + replica2.executeMsg(msg);
		System.out.println("reply:"+reply);
		DatagramSocket socket = null;
		socket = new DatagramSocket();
		sendToFE(socket,reply);

	}


	private void sendToFE(DatagramSocket aSocket, String msgFromReplica) throws IOException{
		System.out.println("sendToFE");
		InetAddress address = InetAddress.getByName("localhost");
		byte[] data = msgFromReplica.getBytes();
		DatagramPacket aPacket = new DatagramPacket(data,data.length,address, FEPort.FE_PORT.RegistorPort);
		aSocket.send(aPacket);
		//aSocket.close();//濡傛灉涓峜olse浼氭�庝箞鏍�
	}


	public static void main(String[] args) {
		Logger rmLogger = Logger.getLogger("RM2.log");
		rmLogger.setLevel(Level.ALL);

		ReplicaManager rm = new ReplicaManager(rmLogger);

		Runnable TaskListener = () ->{
			try{
				rm.startRMListener(RMPort.RM_PORT.rmPort1);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		Thread Thread2 = new Thread(TaskListener);
		Thread2.start();

		try{
			Thread.sleep(3000);
		}catch (Exception e){
			e.printStackTrace();
		}

	}

}
