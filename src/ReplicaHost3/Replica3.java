package ReplicaHost3;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Model.*;
import java.util.Queue;

public class Replica3 {

	public Logger log;
	public LibraryObj conServer;
	public LibraryObj mcgServer;
	public LibraryObj monServer;
	public Boolean bugFree = true;
	public boolean crashFree = true;
	public Queue<Message> historyQueue;
	//public static Replica1 replica1_Instance;

	enum DLMS_Port {
		PORT;
		final int CON_PORT = 3499;
		final int MCG_PORT = 3599;
		final int MON_PORT = 3699;
	}

	public Replica3(Logger log, LibraryObj conServer, LibraryObj mcgServer, LibraryObj monServer) {
		super();
		this.log = log;
		this.conServer = conServer;
		this.mcgServer = mcgServer;
		this.monServer = monServer;
	}
	public Replica3(){
		try{
			//Logger replica1_log = Logger.getLogger("Repilca1.log");
			//createLogger("Repilca1.log", replica1_log);
			//Logger conserver1_log = Logger.getLogger("conserver1.log");
			//createLogger("conserver.log", conserver1_log);
			//Logger mcgserver1_log = Logger.getLogger("mcgserver1.log");
			//createLogger("mcgserver1.log", mcgserver1_log);
			//Logger monserver1_log = Logger.getLogger("monserver1.log");
			//createLogger("monserver1.log", monserver1_log);
			conServer = new LibraryObj("CON",DLMS_Port.PORT.CON_PORT);
			mcgServer = new LibraryObj("MCG",DLMS_Port.PORT.MCG_PORT);
			monServer = new LibraryObj("MON",DLMS_Port.PORT.MON_PORT);
		}catch (Exception e){
			e.printStackTrace();
		}
		

		startServers();
	}


	public String executeMsg(Message msg){
		System.out.println("executeMsg");
		String result = "";
		String operation[] = msg.operationMsg.split(",");
		LibraryObj dlms = getLibrary(msg.libCode);
		System.out.println("msg.libCode------:" +msg.libCode);
		switch (operation[0]){
			case ("addItem"):
				result=dlms.addItem(operation[1], operation[2], operation[3], Integer.parseInt(operation[4]));
				break;
			case ("removeItem"):
				result=dlms.removeItem(operation[1], operation[2], Integer.parseInt(operation[3]));
				break;
			case ("listItem"):
				result=dlms.listItemAvailability(operation[1]);
				break;
			case ("borrowItem"):
				result=dlms.borrowItem(operation[1],operation[2]);
				break;
			case ("addToWaitlist"):
				result=dlms.addToWaitlist(operation[1],operation[2]);
				break;
			case ("findItem"):
				result=dlms.findItem(operation[1],operation[2]);
				break;
			case ("returnItem"):
				result=dlms.returnItem(operation[1],operation[2]);
				break;
			case ("exchangeItem"):
				result=dlms.exchange(operation[1],operation[2],operation[3]);
				break;
			case ("addToWaitlistforExchagne"):
				result=dlms.addToWaitlistforExchange(operation[1],operation[2],operation[3]);
				break;
			default:
				System.out.println("\nERROR: Invalid input please try again.");
				break;
		}
		return  result;
	}


	public void fixBug(){
//		conServer.bugFree = true;
//		mcgServer.bugFree = true;
//		monServer.bugFree = true;
		bugFree=true;
	}

	private LibraryObj getLibrary(String library) {
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

	public void recoverRplicaData(){
		while (historyQueue.size() > 0){
			Message msg = historyQueue.poll();
			System.out.println("msg---"+msg.operationMsg +"\n");
			executeMsg(msg);
		}
	}

	public void startServers(){
		Runnable start_CON_UDP = () -> {
			try{
				conServer.receive(3498);
			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};

		Runnable start_MCG_UDP = () -> {
			try{
				mcgServer.receive(3598);

			}catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		};

		Runnable start_MON_UDP = () -> {
			try{
				monServer.receive(3698);
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


}

