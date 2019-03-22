package FrontEnd;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import FrontEndAPP.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.regex.Pattern;

public class FrontEndObj extends FrontEndPOA {
	private ORB orb;
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final int MAXNUM = 5;
	private static final int TIMEOUT = 5000;
	private static Map<String, Integer> softwareFailCounter;
	String FEID;
	static int portNum;
	int portSeq;
	String logpath;
	String logmessage;

	static Map<String, String> RMIPAddresses = new HashMap<String, String>() {
		{
			put("RM1address", "localhost");
			put("RM2address", "localhost");
			put("RM3address", "localhost");
		};

	};
	
	static Map<String, Integer> RMAddresses = new HashMap<String, Integer>() {
		{
			put("RM1address", 1111);
			put("RM2address", 2222);
			put("RM3address", 3333);
		};

	};
	Map<String, Integer> portlist = new HashMap<String, Integer>() {
		{
			put("MCG", 9999);
			put("CON", 8999);
			put("MON", 7999);
		};
	};

	public static boolean isInteger(String str) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}

	public static void writeFile(String path, String content) {
		File writefile;
		try {
			writefile = new File(path);
			if (!writefile.exists()) {
				writefile.createNewFile();
				writefile = new File(path);
			}

			FileOutputStream txt = new FileOutputStream(writefile, true);
			Writer out = new OutputStreamWriter(txt, "utf-8");
			out.write(content);
			String newline = System.getProperty("line.separator");
			out.write(newline);
			out.close();
			txt.flush();
			txt.close();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public class MyException extends Exception {

		private static final long serialVersionUID = 1L;

		public MyException() {
			super();
		}

		public MyException(String message) {
			super(message);
		}

		public void testMyException() throws MyException {
			throw new MyException("Exchange failed");
		}

	}

	public void setORB(ORB orb_val) {
		orb = orb_val;
	}

	public FrontEndObj(String ID, int portNumber) {
		super();
		softwareFailCounter = new HashMap<String,Integer>();
        softwareFailCounter.put("1",0);
        softwareFailCounter.put("2",0);
        softwareFailCounter.put("3",0);
		this.FEID = ID;
		this.portNum = portNumber;
		Runnable r1 = () -> {
			receive(portNumber);
		};
		logpath = "C:/Users/Peachy/Projects/library_corba/server/" + FEID + "Server.log";
		Thread udpServerThread = new Thread(r1);
		udpServerThread.start();
		
	}
	
	
	public void sendMessage(String message) throws Exception {
        //TODO:Sequener Ip;
        int send_count = 0;
		boolean revResponse = false;
        DatagramSocket socket = null;
        DatagramPacket reply=null;
        while(!revResponse && send_count < MAXNUM) {
        	 try {
        		InetAddress address = InetAddress.getByName("localhost");
        		byte[] data = message.getBytes();
        		DatagramPacket sendPacket = new DatagramPacket(data, data.length, address, portSeq);
             	socket = new DatagramSocket();
             	socket.setSoTimeout(TIMEOUT);
             	socket.send(sendPacket);
             	byte[] buffer = new byte[1000];
				reply = new DatagramPacket(buffer, buffer.length);
				socket.receive(reply);
				revResponse = true;
             }catch (InterruptedIOException e) {
					send_count += 1;
					System.out.println("Time out," + (MAXNUM - send_count) + " more tries...");
             }
        }
       
	}

	public static String sendMessage(byte[] message, int serverPort) {
		DatagramPacket reply=null;
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			aSocket.setSoTimeout(TIMEOUT);
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);
			int send_count = 0;
			boolean revResponse = false;
			DatagramSocket socket = null;
			Map<String, String> resultSet = new HashMap<String, String>();
			int count = 0;
			while (!revResponse && send_count < MAXNUM) {	
				try {
					socket =new DatagramSocket(portNum);
					aSocket.send(request); 
					System.out.println("Request message sent from the client to server with port number " + serverPort + " is: "
							+ new String(request.getData()));
					byte[] buffer = new byte[1000];
					reply = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(reply);
					revResponse = true;
					while(count<3) {
						count = registerListener(socket,resultSet);
					}
				} catch (InterruptedIOException e) {
					send_count += 1;
					System.out.println("Time out," + (MAXNUM - send_count) + " more tries...");
				}
			}
			if (resultSet.size()<3) {
				tellRMCrash(resultSet);
			} else {
				return majority(resultSet);
			}

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
		return null;
	}


	private static int registerListener(DatagramSocket socket, Map<String, String> resultSet) {
		byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        try {
            socket.receive(packet);
            String result = new String(packet.getData(), 0 , packet.getLength());

            System.out.println("receive " + result);

            String[] res = result.split(":");
            resultSet.put(res[0], res[1]);

        } catch (SocketException e){

        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultSet.size();
	}

	public void receive(int portNum) {
		DatagramSocket aSocket = null;

		try {
			aSocket = new DatagramSocket(portNum);

			System.out.println("Server " + portNum + " Started............");
			while (true) {
				byte[] buffer = new byte[1024];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String receiveMessage = new String(request.getData(), 0, request.getLength());
				
				}
			
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}

	public void shutdown() {
		orb.shutdown(false);
	}
	
	@Override
	public String addItem(String managerID, String itemID, String itemName, int q) {
		Map<String, String> resultSet = new HashMap<>();
		DatagramSocket socket = null;
		int count = 0;
		try {
			socket = new DatagramSocket(portNum);
			String message = "addItem" + "," + managerID + "," + itemID + "," + itemName + "," + q;
			sendMessage(message);
			Timer timer = new Timer(socket, false);
			Thread thread = new Thread(timer);
			thread.start();
			while (count < 3 && !timer.timeout) {
				count = registerListener(socket, resultSet);
			}
		} catch (Exception e) {
//          e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
		if (resultSet.size() < 3) {
			tellRMCrash(resultSet);
		}
		int temp;
		String x = majority(resultSet);
		String[] m = x.split(",");
		if (m[1].equals("Ad0")) {
			return itemID + " " + itemName + " " + q + ": Add Successful";
		} else if (m[1].equals("Ad1")) {
			return itemID + " " + itemName + " " + q + ": Add Failed! The quantity is not available ";
		} else if (m[1].equals("Ad2")) {
			return itemID + " " + itemName + " " + q + ": The itemID and itemName don't match";
		} else if (m[1].equals("Ad3")) {
			return itemID + ": You can only add book whose prefix is same as the library you belonged";
		} else {
			return itemID + ": The itemID does not meet the requirement.";
		}

	}


	@Override
	public String removeItem(String managerID, String itemID, int quantity) {
		Map<String, String> resultSet = new HashMap<>();
		DatagramSocket socket = null;
		int count = 0;
		try {
			socket = new DatagramSocket(portNum);
			String message = "removeItem" + "," + managerID + "," + itemID + "," + quantity;
			sendMessage(message);
			Timer timer = new Timer(socket, false);
			Thread thread = new Thread(timer);
			thread.start();
			while (count < 3 && !timer.timeout) {
				count = registerListener(socket, resultSet);
			}
		} catch (Exception e) {
//          e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
		if (resultSet.size() < 3) {
			tellRMCrash(resultSet);
		}

		String x = majority(resultSet);
		String[] m = x.split(",");
		if (m[1].equals("Re0")) {
			return itemID + " " + quantity + ": Remove Successfully";
		} else if (m[1].equals("Re1")) {
			return itemID + " " + quantity + ": Decrease Successfully";
		} else if (m[1].equals("Re2")) {
			return itemID + " " + quantity + ": Remove Failed. The quantity is unvailable.";
		} else if (m[1].equals("Re3")) {
			return itemID + " " + quantity + ": Remove Failed: The itemID does not exist";
		} else {
			return itemID + " " + quantity + ": Remove Failed: Invalid operation. Please try again.";
		}
	}

	@Override
	public String listItemAvailability(String managerID) {
		Map<String, String> resultSet = new HashMap<>();
		DatagramSocket socket = null;
		int count = 0;
		try {
			socket = new DatagramSocket(portNum);
			String message = "listItem" + "," + managerID;
			sendMessage(message);
			Timer timer = new Timer(socket, false);
			Thread thread = new Thread(timer);
			thread.start();
			while (count < 3 && !timer.timeout) {
				count = registerListener(socket, resultSet);
			}
		} catch (Exception e) {
//          e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
		if (resultSet.size() < 3) {
			tellRMCrash(resultSet);
		}
		String x = majorityList(resultSet);
		String[] m = x.split(",");
		return m[1];
			
	}

	@Override
	public String borrowItem(String userID, String itemID) {
		Map<String, String> resultSet = new HashMap<>();
		DatagramSocket socket = null;
		int count = 0;
		try {
			socket = new DatagramSocket(portNum);
			String message = "borrowItem" + "," + userID + "," + itemID;
			sendMessage(message);
			Timer timer = new Timer(socket, false);
			Thread thread = new Thread(timer);
			thread.start();
			while (count < 3 && !timer.timeout) {
				count = registerListener(socket, resultSet);
			}
		} catch (Exception e) {
//          e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
		if (resultSet.size() < 3) {
			tellRMCrash(resultSet);
		}
		String x =  majority(resultSet);
		String[] m = x.split(",");
		if (m[1].equals("Br0")) {
			return itemID  + ": Borrow Successfully";
		} else if (m[1].equals("Br1")) {
			return itemID  + ": Borrow Failed: The itemID does not meet the requirement.";
		} else if (m[1].equals("Br2")) {
			return itemID  + ": Remove Failed. The item does not exist";
		} else if (m[1].equals("Br3")) {
			return itemID  + ": Remove Failed. The book is not available now. Do you want to be added to waitlist? Y/N";
		} else if (m[1].equals("Br4")){
			return itemID  + ": Remove Failed: You have borrowed this item and not returned yet";
		} else if (m[1].equals("Br5")){
			return itemID  + ": Remove Failed: You already borrowed another book in that library and not returned yet";
		} else{
			return itemID  + ": Remove Failed: You are already in a waitlist of that library";
		}
	}

	@Override
	public String findItem(String userID, String itemName) {
		Map<String, String> resultSet = new HashMap<>();
		DatagramSocket socket = null;
		int count = 0;
		try {
			socket = new DatagramSocket(portNum);
			String message = "findItem" + "," + userID+ "," +itemName;
			sendMessage(message);
			Timer timer = new Timer(socket, false);
			Thread thread = new Thread(timer);
			thread.start();
			while (count < 3 && !timer.timeout) {
				count = registerListener(socket, resultSet);
			}
		} catch (Exception e) {
//          e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
		if (resultSet.size() < 3) {
			tellRMCrash(resultSet);
		}
		String x = majorityList(resultSet);
		String[] m = x.split(",");
		return m[1];
	}

	@Override
	public String returnItem(String userID, String itemID) {
		Map<String, String> resultSet = new HashMap<>();
		DatagramSocket socket = null;
		int count = 0;
		try {
			socket = new DatagramSocket(portNum);
			String message = "returnItem" + "," + userID + "," + itemID;
			sendMessage(message);
			Timer timer = new Timer(socket, false);
			Thread thread = new Thread(timer);
			thread.start();
			while (count < 3 && !timer.timeout) {
				count = registerListener(socket, resultSet);
			}
		} catch (Exception e) {
//          e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
		if (resultSet.size() < 3) {
			tellRMCrash(resultSet);
		}
		String x =  majority(resultSet);
		String[] m = x.split(",");
		if (m[1].equals("Rtn0")) {
			return itemID  + ": Return Successfully";
		} else if (m[1].equals("Rtn1")) {
			return itemID  + ": Return Failed. The itemID does not meet the requirement.";
		} else if (m[1].equals("Rtn2")) {
			return itemID  + ": Return Failed. The item does not exist";
		} else {
			return itemID  + ": Return Failed. You haven't borrowed this item";
		}
	}

	@Override
	public String checkBorrowList(String userID) {
		byte[] message = ("checkBorrow" + "," + userID).getBytes();
		String x = sendMessage(message, portSeq);
		String[] m = x.split(",");
		return m[1];
	}

	@Override
	public String checkWaitList(String itemID) {
		byte[] message = ("checkWaitList" + "," + itemID).getBytes();
		String x = sendMessage(message, portSeq);
		String[] m = x.split(",");
		return m[1];
	}

	@Override
	public String addToWaitlist(String userID, String itemID) {
		Map<String, String> resultSet = new HashMap<>();
		DatagramSocket socket = null;
		int count = 0;
		try {
			socket = new DatagramSocket(portNum);
			String message = "addToWaitList" + "," + userID + "," + itemID;
			sendMessage(message);
			Timer timer = new Timer(socket, false);
			Thread thread = new Thread(timer);
			thread.start();
			while (count < 3 && !timer.timeout) {
				count = registerListener(socket, resultSet);
			}
		} catch (Exception e) {
//          e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
		if (resultSet.size() < 3) {
			tellRMCrash(resultSet);
		}
		String x =  majority(resultSet);
		String[] m = x.split(",");
		if (m[1].equals("Atw0")) {
			return itemID  + ": Add to waitlist Successfully";
		} else {
			return itemID  + ": Add to waitlist Failed. You're already in the waitlist.";
		}
	}

	@Override
	public String exchange(String studentID, String newItemID, String oldItemID) {
		Map<String, String> resultSet = new HashMap<>();
		DatagramSocket socket = null;
		int count = 0;
		try {
			socket = new DatagramSocket(portNum);
			String message = "exchangeItem" + "," + studentID + "," + newItemID+ "," + oldItemID;
			sendMessage(message);
			Timer timer = new Timer(socket, false);
			Thread thread = new Thread(timer);
			thread.start();
			while (count < 3 && !timer.timeout) {
				count = registerListener(socket, resultSet);
			}
		} catch (Exception e) {
//          e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
		if (resultSet.size() < 3) {
			tellRMCrash(resultSet);
		}
		String x =  majority(resultSet);
		String[] m = x.split(",");
		if (m[1].equals("Ex0")) {
			return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Successfully";
		} else if (m[1].equals("Ex1")) {
			return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Failed.";
		} else if (m[1].equals("Ex2")) {
			return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Failed. The book you want to return does not exist";
		} else if (m[1].equals("Ex3")) {
			return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Failed. The book you want to borrow does not exist";
		} else if (m[1].equals("Ex4")){
			return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Failed. The book is not available now. Do you want to be added to waitlist? Y/N";
		} else if (m[1].equals("Ex5")){
			return "New: " + newItemID + " Old: " + oldItemID + ": Exchange failed. You already borrowed the new book";
		} else if (m[1].equals("Ex6")){
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. You already borrowed another book in that library";
		} else if (m[1].equals("Ex7")){
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. You're already in the waitlist of the new book";
		} else if (m[1].equals("Ex8")){
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. You are already in a waitlist in that library";
		} else if (m[1].equals("Ex9")){
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. You can not exchange the same item";
		} else if (m[1].equals("Ex10")){
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. You haven't borrowed the old item";
		} else if (m[1].equals("Ex11")){
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. The old itemID does not meet the requirement";
		} else{
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. The new itemID does not meet the requirement";
		}
	}

	@Override
	public String addToWaitlistforExchange(String studentID, String newItemID, String oldItemID) {
		Map<String, String> resultSet = new HashMap<>();
		DatagramSocket socket = null;
		int count = 0;
		try {
			socket = new DatagramSocket(portNum);
			String message = "addToWaitlistforExchagne" + "," + studentID + "," + newItemID+ "," + oldItemID;
			sendMessage(message);
			Timer timer = new Timer(socket, false);
			Thread thread = new Thread(timer);
			thread.start();
			while (count < 3 && !timer.timeout) {
				count = registerListener(socket, resultSet);
			}
		} catch (Exception e) {
//          e.printStackTrace();
		} finally {
			if (socket != null)
				socket.close();
		}
		if (resultSet.size() < 3) {
			tellRMCrash(resultSet);
		}
		String x =  majority(resultSet);
		String[] m = x.split(",");
		if (m[1].equals("Ex0")) {
			return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Successfully";
		} else if (m[1].equals("AtwEx0")) {
			return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Successfully. Add to waitlist successfully.";
		} else if (m[1].equals("Ex1")) {
			return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Failed.";
		} else if (m[1].equals("Ex2")) {
			return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Failed. The book you want to return does not exist";
		} else if (m[1].equals("Ex3")) {
			return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Failed. The book you want to borrow does not exist";
		} else if (m[1].equals("Ex5")){
			return "New: " + newItemID + " Old: " + oldItemID + ": Exchange failed. You already borrowed the new book";
		} else if (m[1].equals("Ex6")){
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. You already borrowed another book in that library";
		} else if (m[1].equals("Ex7")){
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. You're already in the waitlist of the new book";
		} else if (m[1].equals("Ex8")){
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. You are already in a waitlist in that library";
		} else if (m[1].equals("Ex9")){
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. You can not exchange the same item";
		} else if (m[1].equals("Ex10")){
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. You haven't borrowed the old item";
		} else if (m[1].equals("Ex11")){
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. The old itemID does not meet the requirement";
		} else{
			return "New: " + newItemID + " Old: " + oldItemID
					+ ": Exchange failed. The new itemID does not meet the requirement";
		}
	}
	
	
	private static void tellRMCrash(Map<String, String> resultSet) {
        if (!resultSet.containsKey("1")) {
            String msg = "Crash"+"," +"1";
            sendReq(msg);
        } else if (!resultSet.containsKey("2")) {
            String msg = "Crash"+"," +"2";;
            sendReq(msg);
        } else if (!resultSet.containsKey("3")) {
            String msg = "Crash"+"," +"3";;
            sendReq(msg);
        } 
    }

    private static DatagramPacket packet(String rmAddress, byte[] data, int replica) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(rmAddress);
        return new DatagramPacket(data,0, data.length, address, replica);
    }

    private static void sendReq(String msg) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] data = msg.getBytes();
            multicastCrashMsg(socket,data);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private static void multicastCrashMsg(DatagramSocket socket, byte[] data){
        try {
            socket.send(packet(RMIPAddresses.get("RM1address"), data,RMAddresses.get("RM1address")));
            socket.send(packet(RMIPAddresses.get("RM2address"), data,RMAddresses.get("RM2address")));
            socket.send(packet(RMIPAddresses.get("RM3address"), data,RMAddresses.get("RM3address")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String majority(Map<String,String> resultSet) {
        Map<String,Integer> map = new HashMap<>();
        for(String s: resultSet.keySet()) {
        	String tmp = resultSet.get(s);
        	if(map.containsKey(tmp)) {
        		map.put(tmp, map.get(tmp) + 1);
        	}else {
        		map.put(tmp,1);
        	}
        }

        Integer vote = 0;
        String candidate = "";
        for(String s: map.keySet()) {
        	int tmp = map.get(s);
        	if(tmp>vote) {
        		candidate=s;
        		vote = tmp;
        	}
        }

        findSoftwareFail(candidate, vote, resultSet);

        return candidate;
    }
    
    private static String majorityList(Map<String,String> resultSet) {
        Map<HashMap<String, Integer>,Integer> map = new HashMap<>();
        Map<String,HashMap<String, Integer>> result = new HashMap<>();
        for(String s: resultSet.keySet()) {
        	String tmp = resultSet.get(s);
        	String s1[] = tmp.split("\\p{javaWhitespace}+");
        	HashMap<String, Integer> tmp1 = new HashMap<>();
        	for(int i =0; i <s1.length;i++) {
        		String s2[]=s1[i].split(",");
        		tmp1.put(s2[0], Integer.parseInt(s2[1]));
        	}
        	result.put(s, tmp1);
        	if(map.containsKey(tmp1)) {
        		map.put(tmp1, map.get(tmp1)+1);
        	}else {
        		map.put(tmp1, 1);
        	}
        }
        Integer vote = 0;
        HashMap<String, Integer> candidate =new HashMap<>();
        for(HashMap<String, Integer> s: map.keySet()) {
        	int tmp = map.get(s);
        	if(tmp>vote) {
        		candidate=s;
        		vote = tmp;
        	}
        }
        StringBuilder stringbuilder = new StringBuilder();
        for(String s: candidate.keySet()) {
        	stringbuilder.append(s+","+candidate.get(s)+"\n");
        }
        String returnresult = stringbuilder.toString();
        findSoftwareFailforHash(candidate, vote, result);

        return returnresult;
	}

    private static void findSoftwareFail(String candidate, Integer vote, Map<String, String> resultSet) {
        if (vote == 3)
            return;
        String failServerNum = null;
        for (Map.Entry<String, String> entry : resultSet.entrySet()){
            if (!entry.getValue().equals(candidate)){
                failServerNum = entry.getKey();
            }
        }
        if (null != failServerNum){
            for (Map.Entry<String,Integer> entry:
                 softwareFailCounter.entrySet()) {
                if (!entry.getKey().equals(failServerNum)){
                    entry.setValue(0);
                } else if(entry.getKey().equals(failServerNum)){
                    entry.setValue(entry.getValue() + 1);
                }
            }
        }
        if (softwareFailCounter.get(failServerNum) != null && softwareFailCounter.get(failServerNum) == 3){
            sendToRM(failServerNum);
        }
    }
    
    
    private static void findSoftwareFailforHash(HashMap<String, Integer> candidate, Integer vote, Map<String,HashMap<String, Integer>> resultSet) {
        if (vote == 3)
            return;
        String failServerNum = null;
        for(String s: resultSet.keySet()) {
        	if(!candidate.equals(resultSet.get(s))) {
        		failServerNum = s;
        	}
        }
        sendToRM(failServerNum);
    }

    private static void sendToRM(String crashServerNum) {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            String msg = "SoftWareFailure";

            byte[] data = msg.getBytes();

            if (crashServerNum.equals("1")){
                InetAddress address = InetAddress.getByName(RMIPAddresses.get("RM1address"));
                DatagramPacket packet = new DatagramPacket(data, 0, data.length,address,RMAddresses.get("RM1address") );
                socket.send(packet);
            } else if (crashServerNum.equals("2")){
                InetAddress address = InetAddress.getByName(RMIPAddresses.get("RM2address"));
                DatagramPacket packet = new DatagramPacket(data, 0, data.length,address,RMAddresses.get("RM2address"));
                socket.send(packet);
            } else{
                InetAddress address = InetAddress.getByName(RMIPAddresses.get("RM3address"));
                DatagramPacket packet = new DatagramPacket(data, 0, data.length,address,RMAddresses.get("RM3address") );
                socket.send(packet);
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        socket.close();
}
}
