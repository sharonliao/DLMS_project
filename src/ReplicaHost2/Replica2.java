package ReplicaHost2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Model.ReplicaPort;
import Model.logSetFormatter;

public class Replica2 {

	public Logger log;
	public DLMSImp conServer;
	public DLMSImp mcgServer;
	public DLMSImp monServer;

	public Replica2(Logger log, DLMSImp conServer, DLMSImp mcgServer, DLMSImp monServer) {
		super();
		this.log = log;
		this.conServer = conServer;
		this.mcgServer = mcgServer;
		this.monServer = monServer;
	}

	private static void startUdpServer(int udpPort, DLMSImp library) {
		// TODO Auto-generated method stub

	}

	public void startReplica(int replica2_Port) throws IOException {
		DatagramSocket socket = new DatagramSocket(replica2_Port);
		DatagramPacket packet = null;
		byte[] data = null;
		log.info(" Replica_2 Server Start");
		while (true) {
			data = new byte[1024];
			packet = new DatagramPacket(data, data.length);

			System.out.println("====== 1. Replica_2 starts ======");
			socket.receive(packet);

			String receiveMsg = new String(packet.getData(), 0, packet.getLength());
			log.info(" Replica_2 Server Receive Message: " + receiveMsg);
			
			String library=receiveMsg.split(":")[0].toLowerCase();
			 Thread thread = new Thread(new UdpServer(socket, packet, getLibrary(library)));
	            thread.start();
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

	public void main(String[] args) throws IOException {
		Logger replica2_log = Logger.getLogger("Repilca2.log");
		createLogger("Repilca2.log", replica2_log);
		Logger conserver2_log = Logger.getLogger("conserver2.log");
		createLogger("conserver2.log", conserver2_log);
		Logger mcgserver2_log = Logger.getLogger("mcgserver2.log");
		createLogger("mcgserver2.log", mcgserver2_log);
		Logger monserver2_log = Logger.getLogger("monserver2.log");
		createLogger("monserver2.log", monserver2_log);

		Replica2 replica2 = new Replica2(replica2_log, conServer, mcgServer, monServer);

		/*
		 * Runnable r1 = () -> { replica2.startUdpServer(1112, conServer); }; Runnable
		 * r2 = () -> { replica2.startUdpServer(2223, mcgServer); }; Runnable r3 = () ->
		 * { replica2.startUdpServer(3334, monServer); };
		 */
		Runnable r4 = () -> {
			try {
				replica2.startReplica(ReplicaPort.REPLICA_PORT.replica2);
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
