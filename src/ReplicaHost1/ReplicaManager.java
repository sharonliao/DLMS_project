package ReplicaHost1;

import Model.*;
import FrontEnd.Timer;

import java.io.IOException;
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
    Replica1 replica1;
    HashMap<Integer, Message> holdBackQueue;
    //Queue<Message> holdBackQueue;
    Queue<Message> deliveryQueue;
    Queue<Message> historyQueue;


    ReplicaManager(Logger logger) {

        this.logger = logger;
        replicaId = 1;
        failureTimes = 0;
        latestFailureId = 0;
        holdBackQueue = new HashMap<>();
        deliveryQueue = new LinkedList<>();
        historyQueue = new LinkedList<>();
        replica1 = new Replica1(); //蹇呴』鍚姩replica1
        System.out.println(replica1.getClass());
    }

    /**
     * start rm listener
     *
     * @param RMPort
     * @throws Exception
     */
    public void startRMListener(int RMPort) throws Exception {
//		DatagramSocket asocket = new DatagramSocket(RMPort);
        DatagramPacket apocket = null;
        byte[] buf = null;
        logger.info("RM1 is listenning ");

        MulticastSocket asocket = new MulticastSocket(RMPort);
        asocket.joinGroup(InetAddress.getByName("224.0.0.1"));

        while (true) {
            buf = new byte[2000];
            apocket = new DatagramPacket(buf, buf.length);
            asocket.receive(apocket);
            String message = new String(apocket.getData()).trim();
            System.out.println("UDP receive : " + message);

            logger.info("RM1 receives message:" + message);

            String[] messageSplited = message.split(":");
            System.out.println("messageSplited[0]--" + messageSplited[0]);

            switch (messageSplited[0]) {
                case "SoftWareFailure":
                    recoverFromFailure(message); // from FE SoftWareFailure:seqID:replicaId
                    break;
                case "Crash":
                    recoverFromCrash(message); // from FE (if choose crash test, then set replica1 crashFree = fasle)
                    break;
                default:
                    moveToHoldBackQueue(message, asocket); //from Sequencer, normal operation message
                    break;
            }
        }
    }


    public void recoverFromFailure(String failureMsg) throws IOException {
        //SoftWareFailure:seqId:replicaID
        int failureReplica = Integer.parseInt(failureMsg.split(":")[1]);
        int msgSeqId = Integer.parseInt(failureMsg.split(":")[2]);
        if (failureReplica == replicaId) {
            logger.info("Replica " + failureReplica + " has failure");
            if (checkIfFailThreeTimes(msgSeqId)) {
                try{
                    replica1.fixBug();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

    public boolean checkIfFailThreeTimes(int msgId) {
        boolean rtn = false;
        if(failureTimes==0){
            failureTimes++;
            latestFailureId = msgId;

        }else if (msgId == latestFailureId+1){
            failureTimes++;
            latestFailureId ++;

        } else{
            failureTimes = 1;
            latestFailureId = msgId;

        }
        this.logger.info("failure time:" + failureTimes);
        if (failureTimes == 3) {
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
                //recoverFromCrash
                this.logger.info("Crash: Replica" + replicaId);
                restartReplica();
            } else {
                //check if replica is alive

                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void restartReplica() throws IOException {

        //before restart need to close replica1's ports
//		replica1.closeImpSocket();
//		replica1 = null;
//		System.gc();
        try{
            replica1 = new Replica1();
            replica1.historyQueue = this.historyQueue;
            replica1.recoverRplicaData();
            replica1.crashFree = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        logger.info("restart and recover replica2.");

    }


    /**
     * if the hold back queue does not contain the msg, then put it
     *
     * @param msg
     * @throws IOException
     */
    private void moveToHoldBackQueue(String msg, DatagramSocket aSocket) throws IOException {
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
        //seqId,FEaddr,(operation,userId......)
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
        String reply = "";

        if (replica1 != null) {
            if (msg.operationMsg.indexOf("listItem") != -1 && replica1.crashFree == false) {
                //if crashFree is false, then findItem operation crash the replica1, no msg return to FE
                //shut down server
                System.out.println("Replica1 shut downt");
                logger.info("Replica1 shut downt");
                replica1.closeImpSocket();
                replica1 = null;
                return;
            }

            if (msg.operationMsg.indexOf("listItem") != -1 && replica1.bugFree == false) {
                //if bugFree is false, then replica1 return wrong info on findItem operation
                //logger.info("Replica1 failure");
                reply = msg.seqId + ":" + this.replicaId + ":AAAAAAAA,AA=0";
            }else{
                try{
                    reply = msg.seqId + ":" + this.replicaId + ":" + replica1.executeMsg(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            System.out.println("reply:" + reply);
            DatagramSocket socket = null;
            socket = new DatagramSocket();
            sendToFE(socket, reply);
            logger.info("RM1 sends message to Replica1: " + msg.operationMsg + "; reply from Replica1: " + reply);
        }
    }


    private void sendToFE(DatagramSocket aSocket, String msgFromReplica) throws IOException {
        InetAddress address = InetAddress.getByName("localhost");
        byte[] data = msgFromReplica.getBytes();
        DatagramPacket aPacket = new DatagramPacket(data, data.length, address, FEPort.FE_PORT.RegistorPort);
        aSocket.send(aPacket);

        logger.info("RM1 sends message to FE:" + msgFromReplica);
        //aSocket.close();
    }


    public static void main(String[] args) throws IOException {
        Logger rmLogger = Logger.getLogger("RM1.log");
        rmLogger.setLevel(Level.ALL);

        FileHandler handler = new FileHandler("RM1.log");
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
