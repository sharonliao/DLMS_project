package ReplicaHost2;

import java.io.IOException;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Model.Message;
import Model.logSetFormatter;

public class Replica2 {

	public Logger log;
	public DLMSImp conServer;
	public DLMSImp mcgServer;
	public DLMSImp monServer;
	public Boolean bugFree = true;
	public Queue<Message> historyQueue;
	// public static Replica1 replica1_Instance;

	enum DLMS_Port {
		PORT;
		final int CON_PORT = 1112;
		final int MCG_PORT = 2223;
		final int MON_PORT = 3334;
	}

	public Replica2(Logger log, DLMSImp conServer, DLMSImp mcgServer, DLMSImp monServer) {
		super();
		this.log = log;
		this.conServer = conServer;
		this.mcgServer = mcgServer;
		this.monServer = monServer;
	}

	public Replica2() {
		try {
			Logger replica2_log = Logger.getLogger("Repilca2.log");
			createLogger("Repilca2.log", replica2_log);
			Logger conserver2_log = Logger.getLogger("conserver2.log");
			createLogger("conserver2.log", conserver2_log);
			Logger mcgserver2_log = Logger.getLogger("mcgserver2.log");
			createLogger("mcgserver2.log", mcgserver2_log);
			Logger monserver2_log = Logger.getLogger("monserver2.log");
			createLogger("monserver2.log", monserver2_log);
		} catch (Exception e) {
			e.printStackTrace();
		}
		conServer = new DLMSImp("CON", DLMS_Port.PORT.CON_PORT);
		mcgServer = new DLMSImp("MCG", DLMS_Port.PORT.MCG_PORT);
		monServer = new DLMSImp("MON", DLMS_Port.PORT.MON_PORT);

		startServers();
	}

	public String executeMsg(Message msg) {
		//System.out.println("executeMsg" + msg);
		String result = "";
		String operation[] = msg.operationMsg.split(",");
		DLMSImp dlms = getLibrary(msg.libCode);

		//System.out.println("=======list map=====" + dlms.toString());
		switch (operation[0]) {
		case ("addItem"):
			result = dlms.addItem(operation[1], operation[2], operation[3], Integer.parseInt(operation[4]));
			break;
		case ("removeItem"):
			System.out.println("executeMsg===remove" + operation[1] + operation[2]);
			result = dlms.removeItem(operation[1], operation[2], Integer.parseInt(operation[3]));
			break;
		case ("listItem"):
			result = dlms.listItemAvailability(operation[1]);
			break;
		case ("borrowItem"):
			result = dlms.borrowItem(operation[1], operation[2]);
			break;
		case (" addToWaitlist"):
			result = dlms.addWaitList(operation[1], operation[2]);
			break;
		case ("findItem"):
			result = dlms.findItem(operation[1], operation[2]);
			break;
		case ("returnItem"):
			result = dlms.returnItem(operation[1], operation[2]);
			break;
		case ("exchangeItem"):
			result = dlms.exchangeItem(operation[1], operation[2], operation[3]);
			break;
		case ("addToWaitlistforExchagne"):
			result = dlms.newExchange(operation[1], operation[2], operation[3]);
			break;
		default:
			System.out.println("\nERROR: Invalid input please try again.");
			break;
		}
		return result;
	}

	public void fixBug() {
		conServer.bugFree = true;
		mcgServer.bugFree = true;
		monServer.bugFree = true;
	}

	private DLMSImp getLibrary(String library) {
		if (library.equalsIgnoreCase("con"))
			return this.conServer;
		else if (library.equalsIgnoreCase("mcg"))
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
	

	public void recoverRplicaData() {
		while (historyQueue.size() > 0) {
			Message msg = historyQueue.poll();
			System.out.println("msg---" + msg.operationMsg + "\n");
			executeMsg(msg);
		}
	}

	public void startServers(){
		Runnable start_CON_UDP = () -> {
			try{
				conServer.receive(1112);
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};

		Runnable start_MCG_UDP = () -> {
			try{
				mcgServer.receive(2223);

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};

		Runnable start_MON_UDP = () -> {
			try{
				monServer.receive(3334);
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};

		Thread Thread2 = new Thread(start_CON_UDP);
		Thread Thread3 = new Thread(start_MCG_UDP);
		Thread Thread4 = new Thread(start_MON_UDP);
		//Thread Thread5 = new Thread(failureListener);

		//Thread1.start();
		Thread2.start();
		Thread3.start();
		Thread4.start();
	}

	public static void main(String[] args) throws IOException {

		Replica2 replica2 = new Replica2();

		Logger replica2_log = Logger.getLogger("Repilca2.log");
		createLogger("Repilca2.log", replica2_log);
		Logger conserver2_log = Logger.getLogger("conserver2.log");
		createLogger("conserver2.log", conserver2_log);
		Logger mcgserver2_log = Logger.getLogger("mcgserver2.log");
		createLogger("mcgserver2.log", mcgserver2_log);
		Logger monserver2_log = Logger.getLogger("monserver2.log");
		createLogger("monserver2.log", monserver2_log);

		// Replica2 replica2 = new Replica2(replica2_log, conServer, mcgServer,
		// monServer);

	}
}
