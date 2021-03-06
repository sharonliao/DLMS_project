package ReplicaHost1;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import Model.*;

import java.util.Queue;

public class Replica1 {

    public Logger log;
    public DLMSImp conServer;
    public DLMSImp mcgServer;
    public DLMSImp monServer;
    public boolean bugFree = true;
    public boolean crashFree = true;
    public Queue<Message> historyQueue;

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

    public Replica1() {
        try {

            Logger conserver1_log = Logger.getLogger("conserver1.log");
            createLogger("conserver1.log", conserver1_log);

            Logger mcgserver1_log = Logger.getLogger("mcgserver1_log");
            createLogger("mcgserver1.log", mcgserver1_log);

            Logger monserver1_log = Logger.getLogger("monserver1.log");
            createLogger("monserver1.log", monserver1_log);


            conServer = new DLMSImp("CON", DLMS_Port.PORT.CON_PORT, conserver1_log);
            mcgServer = new DLMSImp("MCG", DLMS_Port.PORT.MCG_PORT, mcgserver1_log);
            monServer = new DLMSImp("MON", DLMS_Port.PORT.MON_PORT, monserver1_log);

            startServers();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String executeMsg(Message msg) {
        String result = "";
        String operation[] = msg.operationMsg.split(",");
        DLMSImp dlms = getLibrary(msg.libCode);
        System.out.println("msg.libCode ---" + msg.libCode);
        switch (operation[0]) {
            case ("addItem"):
                result = dlms.addItem(operation[1], operation[2], operation[3], Integer.parseInt(operation[4]));
                break;
            case ("removeItem"):
                result = dlms.removeItem(operation[1], operation[2], Integer.parseInt(operation[3]));
                break;
            case ("listItem"):
                result = dlms.listItemAvailability(operation[1]);
                break;
            case ("borrowItem"):
                result = dlms.borrowItem(operation[1], operation[2]);
                break;
            case ("addToWaitlist"):
                result = dlms.putInWaiting(operation[1], operation[2]);
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
                result = dlms.ex_putInWaiting(operation[1], operation[2], operation[3]);
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
        bugFree = true;
    }

    public void setBug() {
        conServer.bugFree = false;
        mcgServer.bugFree = false;
        monServer.bugFree = false;
        bugFree = false;
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
            System.out.println("recover --- " + msg.operationMsg);
            executeMsg(msg);
        }
    }

    public void startServers() {
        Runnable start_CON_UDP = () -> {
            try {
                conServer.udpServer();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        };

        Runnable start_MCG_UDP = () -> {
            try {
                mcgServer.udpServer();

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        };

        Runnable start_MON_UDP = () -> {
            try {
                monServer.udpServer();
            } catch (Exception e) {
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

    public void closeImpSocket() {
        conServer.aSocket.close();
        mcgServer.aSocket.close();
        monServer.aSocket.close();
    }

}

