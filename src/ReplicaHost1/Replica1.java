package ReplicaHost1;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Model.RMPort;
import Model.ReplicaPort;
import Model.logSetFormatter;


public class Replica1 {

	public Logger log;
	public DLMSImp conServer;
	public DLMSImp mcgServer;
	public DLMSImp monServer;
	public Boolean bugFree = true;

	enum DLMS_Port {
		PORT;
		final int CON_PORT = 7777;
		final int MCG_PORT = 8888;
		final int MON_PORT = 9999;
	}

	public Replica1(Logger log, DLMSImp conServer, DLMSImp mcgServer, DLMSImp monServer) {
		super();
		this.log = log;
		this.conServer = conServer;
		this.mcgServer = mcgServer;
		this.monServer = monServer;
	}

	private static void startUdpServer(int udpPort, DLMSImp library) {
		// TODO Auto-generated method stub
	}

	public void startReplica(int replica_Port) throws IOException {
		DatagramSocket socket = new DatagramSocket(replica_Port);
		DatagramPacket packet = null;
		byte[] data = null;
		log.info(" Replica_2 Server Start");
		while (true) {
			data = new byte[1024];
			packet = new DatagramPacket(data, data.length);

			System.out.println("====== 1. Replica_1 starts ======");
			socket.receive(packet);

			String receiveMsg = new String(packet.getData(), 0, packet.getLength());
			log.info(" Replica_2 Server Receive Message: " + receiveMsg);

			String library=receiveMsg.split(":")[0].toLowerCase();
			Thread thread = new Thread(new UdpServer(socket, packet, getLibrary(library)));
			thread.start();
		}
	}

	//if this port receive message from Rm, then recover the failure
	public void startFailureListening(int port) throws IOException{
		DatagramSocket aSocket = new DatagramSocket(port);
		byte[] buff = new byte[2000];
		DatagramPacket aPacket = new DatagramPacket(buff,buff.length);
		while (true){
			aSocket.receive(aPacket);
			if(aPacket.getData() != null){
				conServer.bugFree = true;
				mcgServer.bugFree = true;
				monServer.bugFree = true;
			}
		}
	}


	private DLMSImp getLibrary(String library) {
		if (library.equalsIgnoreCase("con"))
			return this.conServer;
		else if(library.equalsIgnoreCase("mcg"))
			return this.mcgServer;
		else
			return this.monServer;
	}

	private static void createLogger(String log_name, Logger logger) throws IOException {
		logger.setLevel(Level.ALL);
		FileHandler handler = new FileHandler(log_name);
		handler.setFormatter(new logSetFormatter());
		logger.addHandler(handler);
	}

	public static void main(String[] args) throws IOException {
		Logger replica1_log = Logger.getLogger("Repilca1.log");
		createLogger("Repilca1.log", replica1_log);
		Logger conserver1_log = Logger.getLogger("conserver1.log");
		createLogger("conserver1.log", conserver1_log);
		Logger mcgserver1_log = Logger.getLogger("mcgserver1.log");
		createLogger("mcgserver1.log", mcgserver1_log);
		Logger monserver1_log = Logger.getLogger("monserver1.log");
		createLogger("monserver1.log", monserver1_log);

		DLMSImp conServer = new DLMSImp("CON",DLMS_Port.PORT.CON_PORT);
		DLMSImp mcgServer = new DLMSImp("MCG",DLMS_Port.PORT.MCG_PORT);
		DLMSImp monServer = new DLMSImp("MON",DLMS_Port.PORT.MON_PORT);



		Replica1 replica1 = new Replica1(replica1_log, conServer, mcgServer, monServer);

		/*
		 * Runnable r1 = () -> { replica2.startUdpServer(1112, conServer); }; Runnable
		 * r2 = () -> { replica2.startUdpServer(2223, mcgServer); }; Runnable r3 = () ->
		 * { replica2.startUdpServer(3334, monServer); };
		 */
		Runnable r4 = () -> {
			try {
				replica1.startReplica(ReplicaPort.REPLICA_PORT.replica1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};

		Runnable failureListener = () -> {
			try{
				replica1.startFailureListening(RMPort.RM_PORT.rmPort1_failure);
			}catch (Exception e){
				e.printStackTrace();
			}
		};

		/*
		 * Thread Thread1 = new Thread(r1); Thread Thread2 = new Thread(r2); Thread
		 * Thread3 = new Thread(r3);
		 */
		Thread Thread4 = new Thread(r4);
		/*
		 * Thread1.start(); Thread2.start(); Thread3.start();
		 */
		Thread4.start();

	}

}

