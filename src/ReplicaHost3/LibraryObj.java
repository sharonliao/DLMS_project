package ReplicaHost3;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.CORBA.Object;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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

public class LibraryObj {
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String libraryID;
	int portNum;
	Map<String, Book> books = new HashMap<>();
	Map<String, HashMap<String, Record>> records = new HashMap<>();
	Map<String, HashMap<String, Record>> libRecords = new HashMap<>();
	Map<String, ArrayList<String>> waitlist = new HashMap<>();
	Map<String, Record> newItemRecordsClone = new HashMap<>();
	Map<String, Record> oldItemRecordsClone = new HashMap<>();
	HashMap<String, Record> personClone = new HashMap<>();
	ArrayList<String> queueClone = new ArrayList<String>();
	Book newBookClone;
	Book oldBookClone;
	String logpath;
	String logmessage;
	Map<String, String> accounts = new HashMap<String, String>() {
		{
			put("MCGU2000", "1q2w3e4r");
			put("MCGU2001", "1q2w3e4r");
			put("MCGM2000", "1q2w3e4r");
			put("CONU2000", "1q2w3e4r");
			put("CONU2001", "1q2w3e4r");
			put("CONM2000", "1q2w3e4r");
			put("MONU2000", "1q2w3e4r");
			put("MONU2001", "1q2w3e4r");
			put("MONM2000", "1q2w3e4r");

		};

	};

	Map<String, Integer> portlist = new HashMap<String, Integer>() {
		{
			put("MCG", 9999);
			put("CON", 8999);
			put("MON", 7999);
		};
	};
	public boolean bugFree = false;

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

	public LibraryObj(String libID, int portNumber) {
		super();
		this.libraryID = libID;
		this.portNum = portNumber;
		Runnable r1 = () -> {
			receive(portNumber - 1);
		};
		logpath = "E:/dlms/DLMS_project/server" + libraryID + "Server.log";
		Thread udpServerThread = new Thread(r1);
		udpServerThread.start();
	}

	public boolean logincheck(String a, String b) {
		if (accounts.containsKey(a)) {
			if (accounts.get(a).equals(b)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public static String sendMessage(byte[] message, int serverPort) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);
			aSocket.send(request);
			System.out.println("Request message sent from the client to server with port number " + serverPort + " is: "
					+ new String(request.getData()));
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			aSocket.receive(reply);
			System.out.println("Reply received from the server with port number " + serverPort + " is: "
					+ new String(reply.getData()));

			return new String(reply.getData()).trim();

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

	public void receive(int portNum) {
		DatagramSocket aSocket = null;

		try {
			aSocket = new DatagramSocket(portNum);

			System.out.println("Server " + portNum + " Started............");
			while (true) {
				byte[] buffer = new byte[1024];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String s = "";
				s = new String(request.getData()).trim();
				String[] m = s.split(",");
				StringBuilder itemname = new StringBuilder();
				if (m[0].equals("borrow")) {
					String replystr = borrowItem(m[1], m[2].substring(0, 7));
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("addtowaitlist")) {
					String replystr = addToWaitlist(m[1], m[2]);
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("return")) {
					String replystr = returnItem(m[1], m[2].substring(0, 7));
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("find")) {
					String replystr = findItem(m[1], m[2]);
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("borrowsuccess")) {
					String replystr = borrowRecord(m[1], m[2], m[3]);
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("checkuser")) {
					String replystr = checkUser(m[1], m[2]);
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("setBorrowStatus")) {
					String replystr = setBorrowRecord(m[1], m[2]);
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("borrowforexchange")) {
					String replystr = borrowforExchange(m[1], m[2], m[3]);
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("returnforexchange")) {
					String replystr = returnforexchange(m[1], m[2], m[3]);
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("rollbackborrow")) {
					String replystr = rollbackBorrow(m[1], m[2]);
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("rollbackreturn")) {
					String replystr = rollbackReturn(m[1], m[2]);
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("checkBookAvailable")) {
					String replystr = checkBookAvailable(m[1], m[2], m[3], m[4]);
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("deleteborrowrecord")) {
					String replystr = deleteborrowrecord(m[1], m[2]);
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				} else if (m[0].equals("checkReturn")) {
					String replystr = checkReturn(m[1], m[2], m[3]);
					byte[] replyb = replystr.getBytes();
					DatagramPacket reply = new DatagramPacket(replyb, replyb.length, request.getAddress(),
							request.getPort());
					aSocket.send(reply);
				}

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

	public String addItem(String managerID, String itemID, String itemName, int q) {
		synchronized (this) {
			managerID = managerID.trim();
			itemID = itemID.trim();
			itemName = itemName.trim();
			if (books.containsKey(itemID)) {
				Book bb = books.get(itemID);
				String tmp = bb.getBookName();
				if (tmp.equals(itemName)) {
					Quantity temp = bb.getQuantity();
					int tempquantity = temp.getQuantity();
					tempquantity = tempquantity + q;
					temp.setQuantity(tempquantity);
					bb.setQuantity(temp);
					logmessage = df.format(new Date()) + " Add Item " + managerID + " " + itemID + " " + itemName + " "
							+ q + " Successful ";
					writeFile(logpath, logmessage);
					searchWaitList(itemID);
					return "Ad0";

				} else {
					logmessage = df.format(new Date()) + " Add Item " + managerID + " " + itemID + " " + itemName + " "
							+ q + " Failed: The itemID and itemName don't match ";
					writeFile(logpath, logmessage);
					return "Ad1";
				}
			} else {
				Book temp1 = new Book(itemID, itemName, q);
				books.put(temp1.getitemID(), temp1);
				logmessage = df.format(new Date()) + " Add Item " + managerID + " " + itemID + " " + itemName + " " + q
						+ " Successful ";
				writeFile(logpath, logmessage);
				return "Ad0";

			}
		}
	}

	public String removeItem(String managerID, String itemID, int quantity) {
		if (books.containsKey(itemID)) {
			Book bb = books.get(itemID);
			Quantity tempquantity = bb.getQuantity();
			int temp = tempquantity.getQuantity();
			if (quantity == -1) {
				books.remove(itemID);
				if (waitlist.containsKey(itemID)) {
					waitlist.remove(itemID);
				}
				if (libRecords.containsKey(itemID)) {
					HashMap<String, Record> borrowlist = libRecords.get(itemID);
					for (String key : borrowlist.keySet()) {
						Record i = borrowlist.get(key);
						if (i.status.equals("Borrowed")) {
							String user = i.getUserID();
							if (user.substring(0, 3).equals(libraryID)) {
								HashMap<String, Record> person = records.get(i.getUserID());
								Record p = person.get(itemID);
								p.setStatus();
							} else {
								String x = "";
								int tmp;
								byte[] message = ("setBorrowStatus" + "," + user + "," + itemID).getBytes();
								tmp = portlist.get(user.substring(0, 3));
								x = sendMessage(message, tmp - 1);
							}
						}
					}
					libRecords.remove(itemID);
				}
				logmessage = df.format(new Date()) + " Delete Item " + managerID + " " + itemID + " " + quantity
						+ " Successful";
				writeFile(logpath, logmessage);
				return "Re0";
			} else if (temp >= quantity) {
				temp = temp - quantity;
				Quantity temp1 = bb.getQuantity();
				temp1.setQuantity(temp);
				bb.setQuantity(temp1);
				logmessage = df.format(new Date()) + " Remove Item " + managerID + " " + itemID + " " + quantity
						+ " Successful";
				writeFile(logpath, logmessage);
				return "Re1";
			} else {
				logmessage = df.format(new Date()) + " Remove Item " + managerID + " " + itemID + " " + quantity
						+ " Failed: The quantity is unavailable";
				writeFile(logpath, logmessage);
				return "Re2";
			}
		} else {
			logmessage = df.format(new Date()) + " Remove Item " + managerID + " " + itemID + " " + quantity
					+ " Failed: The itemID does not exist";
			writeFile(logpath, logmessage);
			return "Re3";
		}
	}

	public String setBorrowRecord(String userID, String itemID) {
		userID = userID.trim();
		itemID = itemID.trim();
		HashMap<String, Record> person = records.get(userID);
		Record p = person.get(itemID);
		System.out.println(person.get(itemID));
		p.setStatus();
		return "success";
	}

	public String listItemAvailability(String managerID) {
		StringBuilder booklist = new StringBuilder();
		if (!books.isEmpty()) {
			for (String key : books.keySet()) {
				Book temp = books.get(key);
				booklist.append(temp.getitemID() + " " + temp.getBookName() + " " + temp.getQuantity().getQuantity());
				booklist.append("\n");
			}
			logmessage = df.format(new Date()) + " List Available Books " + managerID + " Successful";
			writeFile(logpath, logmessage);
		} else {
			booklist.append("There are no books in the library");
			logmessage = df.format(new Date()) + " List Available Books " + managerID + " Failed: No books in library";
			writeFile(logpath, logmessage);
		}
		String result = booklist.toString();
		return result;
	}

	public void searchWaitList(String itemID) {
		synchronized (this) {
			if (waitlist.containsKey(itemID)) {
				int tmp;
				String y = "";
				String z = "";
				ArrayList<String> queue = waitlist.get(itemID);
				while (true) {
					Book bb = books.get(itemID);
					Quantity temp1 = bb.getQuantity();
					int temp = temp1.getQuantity();
					if (!((queue.isEmpty()) || (temp == 0))) {
						String user = queue.get(0);
						int j = queue.size();
						for (int i = 0; i < j - 1; i++) {
							queue.set(i, queue.get(i + 1));
						}
						queue.remove(queue.get(j - 1));
						if (user.substring(0, 3).equals(libraryID)) {
							String x = borrowItem(user, itemID);
							continue;
						} else {
							byte[] message = ("checkuser" + "," + user + "," + itemID).getBytes();
							tmp = portlist.get(user.substring(0, 3));
							y = sendMessage(message, tmp - 1);
							String[] m = y.split(",");
							if (m[0].equals("no")) {
								String x = borrowItem(user, itemID);
								byte[] message1 = x.getBytes();
								tmp = portlist.get(user.substring(0, 3));
								z = sendMessage(message1, tmp - 1);
								continue;
							} else {
								logmessage = df.format(new Date()) + " Borrow Item " + user + " " + itemID
										+ " Failed: The user has already borrowed book from that library and have not returned yet.";
								writeFile(logpath, logmessage);
								continue;
							}
						}
					}
					break;

				}
			}
		}
	}

	public String borrowItem(String userID, String itemID) {
		itemID = itemID.trim();
		if (itemID.substring(0, 3).equals(libraryID)) {
			if (libRecords.containsKey(itemID)) {
				HashMap<String, Record> record = libRecords.get(itemID);
				if (record.containsKey(userID) && (record.get(userID).getStatus().equals("Borrowed"))) {
					logmessage = df.format(new Date()) + " Borrow Item " + userID + " " + itemID
							+ " Failed: Borrow one book twice";
					writeFile(logpath, logmessage);
					if (userID.substring(0, 3).equals(libraryID)) {
						return "Br3";
					} else {
						return "borrowtwice";
					}
				}
			}
			if (!userID.substring(0, 3).equals(libraryID)) {
				if (checkIfUserInWaitList(userID)) {
					logmessage = df.format(new Date()) + " Borrow Item " + userID + " " + itemID
							+ " Failed: Already in a waitlist of another book";
					writeFile(logpath, logmessage);
					return "alreadyinwaitlist";
				}
				for (String key : libRecords.keySet()) {
					HashMap<String, Record> item = libRecords.get(key);
					if (item.containsKey(userID) && item.get(userID).getStatus().equals("Borrowed")) {
						logmessage = df.format(new Date()) + " Borrow Item " + userID + " " + itemID
								+ " Failed: Already borrow another book in that library";
						writeFile(logpath, logmessage);
						return "alreadyborrowanotherone";
					}
				}
				if (libRecords.containsKey(itemID)) {
					HashMap<String, Record> record = libRecords.get(itemID);
					if (record.containsKey(userID) && (record.get(userID).getStatus().equals("Borrowed"))) {
						logmessage = df.format(new Date()) + " Borrow Item " + userID + " " + itemID
								+ " Failed: Borrow one book twice";
						writeFile(logpath, logmessage);
						if (userID.substring(0, 3).equals(libraryID)) {
							return itemID + ": You can not borrow one book twice! Please return first.";
						} else {
							return "borrowtwice";
						}
					}
				}
			}
			if (books.containsKey(itemID)) {
				Book bb = books.get(itemID);
				Quantity temp1 = bb.getQuantity();
				int temp = temp1.getQuantity();
				if (temp != 0) {
					temp = temp - 1;
					temp1.setQuantity(temp);
					bb.setQuantity(temp1);
					libraryRecord(userID, itemID);
					if (userID.substring(0, 3).equals(libraryID)) {
						borrowRecord(userID, itemID, bb.getBookName());
						logmessage = df.format(new Date()) + " Borrow Item " + userID + " " + itemID + " Successful";
						writeFile(logpath, logmessage);
						return "Br0";
					} else {
						logmessage = df.format(new Date()) + " Borrow Item " + userID + " " + itemID + " Successful";
						writeFile(logpath, logmessage);
						return "borrowsuccess" + "," + userID + "," + itemID + "," + bb.getBookName();
					}

				} else {
					if (userID.substring(0, 3).equals(libraryID)) {
						logmessage = df.format(new Date()) + " Borrow Item " + userID + " " + itemID
								+ " Failed: The book is not available";
						writeFile(logpath, logmessage);
						return "Br2";
					} else {
						logmessage = df.format(new Date()) + " Borrow Item " + userID + " " + itemID
								+ " Failed: The book is not available";
						writeFile(logpath, logmessage);
						return "notavailable";
					}

				}
			} else {
				logmessage = df.format(new Date()) + " Borrow Item " + userID + " " + itemID
						+ " Failed: The itemID doesn't exist";
				writeFile(logpath, logmessage);
				return "Br1";
			}
		} else

		{
			String x = "";
			int temp;
			byte[] message = ("borrow" + "," + userID + "," + itemID).getBytes();
			temp = portlist.get(itemID.substring(0, 3));
			x = sendMessage(message, temp - 1);
			String[] m = x.split(",");
			if (m[0].equals("borrowsuccess")) {
				borrowRecord(m[1], m[2], m[3]);
				return itemID + ": Borrow Successfully!";
			} else if (m[0].equals("notavailable")) {
				return "Br2";

			} else if (m[0].equals("alreadyinwaitlist")) {

				return "Br5";

			} else if (m[0].contains("borrowtwice")) {
				return "Br3";
			} else if (m[0].contains("alreadyborrowanotherone")) {
				return "Br4";
			}

			return "Br1";
		}
	}

	public String borrowRecord(String userID, String itemID, String bookName) {
		userID = userID.trim();
		itemID = itemID.trim();
		bookName = bookName.trim();
		if (records.containsKey(userID)) {
			HashMap<String, Record> person = records.get(userID);
			Record p1 = new Record(itemID, bookName, df.format(new Date()), "Borrowed");
			person.put(p1.getitemID(), p1);
			records.put(userID, person);
		} else {
			HashMap<String, Record> person = new HashMap<String, Record>();
			Record p1 = new Record(itemID, bookName, df.format(new Date()), "Borrowed");
			person.put(p1.getitemID(), p1);
			records.put(userID, person);
		}
		return "success";
	}

	public void libraryRecord(String userID, String itemID) {
		userID = userID.trim();
		itemID = itemID.trim();
		if (libRecords.containsKey(itemID)) {
			HashMap<String, Record> mcgRecords = libRecords.get(itemID);
			Record p1 = new Record(userID, df.format(new Date()), "Borrowed");
			mcgRecords.put(p1.getUserID(), p1);
			libRecords.put(itemID, mcgRecords);
		} else {
			HashMap<String, Record> mcgRecords = new HashMap<String, Record>();
			Record p1 = new Record(userID, df.format(new Date()), "Borrowed");
			mcgRecords.put(p1.getUserID(), p1);
			libRecords.put(itemID, mcgRecords);
		}
	}

	public String findItem(String userID, String itemName) {
		userID = userID.trim();
		itemName = itemName.trim();
		StringBuilder stringBuilder = new StringBuilder();
		for (String key : books.keySet()) {
			Book temp = books.get(key);
			if (temp.getBookName().replaceAll(" ", "").equals(itemName.replaceAll(" ", ""))) {
				stringBuilder.append(temp.getitemID() + "," + temp.getQuantity().getQuantity() + "\n");
			}
		}
		if (userID.substring(0, 3).equals(libraryID)) {
			String x = "";
			int tmp[] = new int[2];
			int i = 0;
			for (String key : portlist.keySet()) {
				if (!key.equals(libraryID)) {
					tmp[i] = portlist.get(key);
					i++;
				}
			}
			String Message = "";
			Message = "find" + "," + userID + "," + itemName;
			byte[] message = Message.getBytes();
			x = sendMessage(message, tmp[0] - 1);
			String[] m = x.split(",");
			if (m[0].equals("has")) {
				stringBuilder.append(m[1] + " " + m[2] + "\n");
			}

			String x2 = "";
			x2 = sendMessage(message, tmp[1] - 1);
			String[] m2 = x2.split(",");
			if (m2[0].equals("has")) {
				stringBuilder.append(m2[1] + " " + m2[2] + "\n");
			}

			String result = stringBuilder.toString();
			if (result.equals("")) {
				logmessage = df.format(new Date()) + " Find Item " + userID + " " + itemName
						+ " Failed: The book does not exist";
				writeFile(logpath, logmessage);
			} else {
				logmessage = df.format(new Date()) + " Find Item " + userID + " " + itemName + " Successful";
				writeFile(logpath, logmessage);
			}
			return result;
		} else {
			String results = stringBuilder.toString();
			if (results.equals("")) {
				return "no";
			} else {
				return "has" + "," + results;
			}
		}
	}

	public String returnItem(String userID, String itemID) {
		itemID = itemID.trim();
		userID = userID.trim();
		if (itemID.substring(0, 3).equals(libraryID)) {
			if (books.containsKey(itemID)) {
				if (libRecords.containsKey(itemID)) {
					HashMap<String, Record> item = libRecords.get(itemID);
					if (item.containsKey(userID)) {
						Record i = item.get(userID);
						i.setStatus();
						if (userID.substring(0, 3).equals(libraryID)) {
							HashMap<String, Record> person = records.get(userID);
							Record p = person.get(itemID);
							p.setStatus();
						}
						Book bb = books.get(itemID);
						Quantity temp1 = bb.getQuantity();
						int temp = temp1.getQuantity();
						temp = temp + 1;
						temp1.setQuantity(temp);
						bb.setQuantity(temp1);

						logmessage = df.format(new Date()) + " Return Item " + userID + " " + itemID + " Successful";
						writeFile(logpath, logmessage);
						searchWaitList(itemID);
						return "Rtn0";
					} else {
						logmessage = df.format(new Date()) + " Return Item " + userID + " " + itemID
								+ " Failed: Is not allowed to return the book";
						writeFile(logpath, logmessage);
						return "Rtn2";
					}

				} else {
					logmessage = df.format(new Date()) + " Return Item " + userID + " " + itemID
							+ " Failed: Is not allowed to return the book";
					writeFile(logpath, logmessage);
					return "Rtn2";
				}
			} else {
				logmessage = df.format(new Date()) + " Return Item " + userID + " " + itemID
						+ " Failed: The book does not exist";
				writeFile(logpath, logmessage);
				return "Rtn1";
			}

		} else {
			String x = "";
			int tmp;
			byte[] message = ("return" + "," + userID + "," + itemID.substring(0, 7)).getBytes();
			tmp = portlist.get(itemID.substring(0, 3));
			x = sendMessage(message, tmp - 1);
			if (x.contains("Success")) {
				HashMap<String, Record> person = records.get(userID);
				Record p = person.get(itemID);
				p.setStatus();
			}
			return x;
		}
	}

	public String checkUser(String userID, String itemID) {
		synchronized (this) {
			if (records.containsKey(userID)) {
				HashMap<String, Record> person = records.get(userID);
				for (String key : person.keySet()) {
					if (key.substring(0, 3).equals(itemID.substring(0, 3))) {
						Record tmp = person.get(key);
						if (tmp.getStatus().equals("Borrowed")) {
							return "yes";
						}
						return "no";
					}
				}
				return "no";
			}
			return "no";
		}
	}

	public String checkBorrowList(String userID) {
		StringBuilder record = new StringBuilder();
		if (records.containsKey(userID)) {
			HashMap<String, Record> person = records.get(userID);
			for (String key : person.keySet()) {
				Record temp = person.get(key);
				record.append(temp.getitemID() + " " + temp.getBookName() + " " + temp.getTime() + " "
						+ temp.getStatus() + "\n");
			}
			logmessage = df.format(new Date()) + " Check Borrow List " + userID + " Successful";
			writeFile(logpath, logmessage);
		} else {
			logmessage = df.format(new Date()) + " Check Borrow List " + userID + " Failed: Have no borrow record";
			writeFile(logpath, logmessage);
			record.append("You have no borrow record");
		}
		String result = record.toString();
		return result;
	}

	public String checkWaitList(String itemID) {
		synchronized (this) {
			StringBuilder wait = new StringBuilder();
			if (waitlist.containsKey(itemID)) {
				ArrayList<String> queue = waitlist.get(itemID);
				Iterator<String> iter = queue.iterator();
				while (iter.hasNext()) {
					wait.append(iter.next() + "\n");
				}
			} else {
				wait.append("This book don't have a waitlist");
			}
			String result = wait.toString();
			return result;

		}
	}

	public boolean checkIfUserInWaitList(String userID) {
		synchronized (this) {
			if (!waitlist.isEmpty()) {
				for (String key : waitlist.keySet()) {
					ArrayList<String> queue = waitlist.get(key);
					if (queue.contains(userID)) {
						return true;
					}
				}
				return false;
			}
			return false;
		}
	}

	public String addToWaitlist(String userID, String itemID) {
		userID = userID.trim();
		itemID = itemID.trim();
		if (itemID.substring(0, 3).equals(libraryID)) {
			if (waitlist.containsKey(itemID)) {
				ArrayList<String> queue = waitlist.get(itemID);
				if (queue.contains(userID)) {
					logmessage = df.format(new Date()) + " Add to Wait List " + userID + " " + itemID
							+ " Failed: Already in the waitlist";
					writeFile(logpath, logmessage);

					return "Atw1";

				} else {
					queue.add(userID);
					waitlist.put(itemID, queue);
					logmessage = df.format(new Date()) + " Add to Wait List " + userID + " " + itemID + " Successful";
					writeFile(logpath, logmessage);
					return "Atw0";
				}
			} else {
				ArrayList<String> queue = new ArrayList<String>();
				queue.add(userID);
				waitlist.put(itemID, queue);
				logmessage = df.format(new Date()) + " Add to Wait List " + userID + " " + itemID + " Successful";
				writeFile(logpath, logmessage);
				return "Atw0";
			}
		} else {
			String x = "";
			String y = "";
			int tmp;
			byte[] message_addtowaitlist = ("addtowaitlist" + "," + userID + "," + itemID).getBytes();
			tmp = portlist.get(itemID.substring(0, 3));
			x = sendMessage(message_addtowaitlist, tmp - 1);
			if (x.contains("success")) {
				return "Atw0";
			} else {
				return "Atw1";
			}

		}
	}

	public static HashMap<String, Record> copyrecord(HashMap<String, Record> original) {
		HashMap<String, Record> copy = new HashMap<String, Record>();
		for (Map.Entry<String, Record> entry : original.entrySet()) {
			copy.put(entry.getKey(),
					// Or whatever List implementation you'd like here.
					entry.getValue());
		}
		return copy;
	}

	public String exchange(String userID, String newItemID, String oldItemID) {
		synchronized (this) {
			userID = userID.trim();
			newItemID = newItemID.trim();
			oldItemID = oldItemID.trim();
			String x = "";
			String x1 = "";
			String x2 = "";
			HashMap<String, Record> person = records.get(userID);
			personClone = copyrecord(person);
			String check = checkReturn(userID, newItemID, oldItemID);
			if (check.equals("yes")) {
				if (userID.substring(0, 3).equals(newItemID.substring(0, 3))) {
					x = checkBookAvailable(userID, newItemID, oldItemID, "check");
				} else {
					int tmp;
					byte[] message = ("checkBookAvailable" + "," + userID + "," + newItemID + ","
							+ oldItemID.substring(0, 7) + "," + "check").getBytes();
					tmp = portlist.get(newItemID.substring(0, 3));
					x = sendMessage(message, tmp - 1);
				}
				MyException myException = new MyException();
				switch (x) {
				case "available":
					try {
						if (newItemID.substring(0, 3).equals(libraryID)) {
							x1 = borrowforExchange(userID, newItemID, oldItemID);
						} else {
							int tmp1;
							byte[] message1 = ("borrowforexchange" + "," + userID + "," + newItemID.substring(0, 7)
									+ "," + oldItemID.substring(0, 7)).getBytes();
							tmp1 = portlist.get(newItemID.substring(0, 3));
							x1 = sendMessage(message1, tmp1 - 1);
							String[] m = x1.split(",");
							if (x1.contains("success")) {
								borrowRecord(userID, newItemID, m[1]);
							}
						}
						if (oldItemID.substring(0, 3).equals(libraryID)) {
							x2 = returnforexchange(userID, newItemID, oldItemID);
						} else {
							int tmp2;
							byte[] message2 = ("returnforexchange" + "," + userID + "," + newItemID + ","
									+ oldItemID.substring(0, 7)).getBytes();
							tmp2 = portlist.get(oldItemID.substring(0, 3));
							x2 = sendMessage(message2, tmp2 - 1);
							if (x2.contains("success")) {
								HashMap<String, Record> personrecord = records.get(userID);
								Record p = personrecord.get(oldItemID);
								p.setStatus();
							}
						}
						if ((!(x1.contains("success")) || (!(x2.contains("success"))))) {
							myException.testMyException();
						} else {
							return "Ex0";
						}
					} catch (Exception e) {
						x = "";
						if (!x2.equals("don't exist")) {
							Record p = person.get(oldItemID);
							p.rollbackStatus();
						}
						person.remove(newItemID);
						if (!x1.equals("failed")) {
							if (newItemID.substring(0, 3).equals(libraryID)) {
								rollbackBorrow(userID, newItemID);
							} else {
								int tmp1;
								byte[] message1 = ("rollbackborrow" + "," + userID + "," + newItemID.substring(0, 7))
										.getBytes();
								tmp1 = portlist.get(newItemID.substring(0, 3));
								x = sendMessage(message1, tmp1 - 1);

							}
						}
						if (oldItemID.substring(0, 3).equals(libraryID)) {
							rollbackReturn(userID, oldItemID);
						} else {
							int tmp2;
							byte[] message2 = ("rollbackreturn" + "," + userID + "," + oldItemID.substring(0, 7))
									.getBytes();
							tmp2 = portlist.get(oldItemID.substring(0, 3));
							x = sendMessage(message2, tmp2 - 1);
						}
						if (x2.equals("don't exist")) {
							return "Ex2";
						}
						if (x1.equals("failed")) {
							return "Ex3";
						}
						return "Ex1";
					}
				case "notAvailable":
					return "Ex4";
				default:
					return x;
				}

			} else if (check.equals("don't exist")) {
				return "Ex2";
			} else {
				return "Ex9";
			}
		}
	}

	public String checkBookAvailable(String userID, String newItemID, String oldItemID, String operation) {
		if (books.containsKey(newItemID)) {
			if (libRecords.containsKey(newItemID)) {
				HashMap<String, Record> item = libRecords.get(newItemID);
				if (item.containsKey(userID)) {
					Record i = item.get(userID);
					if (i.getStatus().equals("Borrowed")) {
						logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID
								+ " Old: " + oldItemID + " Failed: Already borrow the new book";
						writeFile(logpath, logmessage);
						return "Ex5";
					}
				}
			}
			if (waitlist.containsKey(newItemID)) {
				ArrayList<String> waitlistfornewitem = waitlist.get(newItemID);
				if (waitlistfornewitem.contains(userID)) {
					logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID + " Old: "
							+ oldItemID + " Failed: Already in the waitlist of the new book";
					writeFile(logpath, logmessage);
					return "Ex7";
				}
			}

			if (!userID.substring(0, 3).equals(libraryID)) {
				if (checkIfUserInWaitList(userID)) {
					logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID + " Old: "
							+ oldItemID + " Failed: Already in a waitlist";
					writeFile(logpath, logmessage);
					return "Ex8";
				}
				for (String key : libRecords.keySet()) {
					HashMap<String, Record> item = libRecords.get(key);
					if (item.containsKey(userID) && item.get(userID).getStatus().equals("Borrowed")) {
						if (!key.substring(0, 3).equals(newItemID.substring(0, 3))) {
							logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID
									+ " Old: " + oldItemID + " Failed: Already borrow another book in that library";
							writeFile(logpath, logmessage);
							return "Ex6";
						}
					}
				}
			}
			Book bb = books.get(newItemID);
			Quantity temp1 = bb.getQuantity();
			if (temp1.getQuantity() != 0) {
				if (operation.equals("check")) {
					newBookClone = (Book) bb.clone();
					temp1.setQuantity((temp1.getQuantity()) - 1);
					bb.setQuantity(temp1);
				}
				return "available";
			} else {
				return "notAvailable";
			}
		} else {
			logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID + " Old: "
					+ oldItemID + " Failed: The new book does not exist";
			writeFile(logpath, logmessage);
			return "Ex3";
		}
	}

	public String borrowforExchange(String userID, String newItemID, String oldItemID) {
		String x = checkBookAvailable(userID, newItemID, oldItemID, "borrow");
		if (x.contains("does not exist")) {
			return "failed";
		}
		if (libRecords.containsKey(newItemID)) {
			HashMap<String, Record> itemRecords = libRecords.get(newItemID);
			newItemRecordsClone = copyrecord(itemRecords);

		}
		Book bb = books.get(newItemID);
		libraryRecord(userID, newItemID);
		if (userID.substring(0, 3).equals(libraryID)) {
			borrowRecord(userID, newItemID, bb.getBookName());
		}
		logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID + " Old: " + oldItemID
				+ " Borrow successfully";
		writeFile(logpath, logmessage);
		return "success" + "," + bb.getBookName();
	}

	public String returnforexchange(String userID, String newItemID, String oldItemID) {
		String x = checkReturn(userID, newItemID, oldItemID);
		if (x.equals("yes")) {
			HashMap<String, Record> itemRecords = libRecords.get(oldItemID);
			oldItemRecordsClone = copyrecord(itemRecords);
			Record i = itemRecords.get(userID);
			i.setStatus();
			if (userID.substring(0, 3).equals(libraryID)) {
				HashMap<String, Record> person = records.get(userID);
				Record p = person.get(oldItemID);
				p.setStatus();
			}
			Book bb = books.get(oldItemID);
			oldBookClone = (Book) bb.clone();
			if (waitlist.containsKey(oldItemID)) {
				ArrayList<String> queue = waitlist.get(oldItemID);
				queueClone = CloneUtils.clone(queue);
			}
			Quantity temp1 = bb.getQuantity();
			int temp = temp1.getQuantity();
			temp = temp + 1;
			temp1.setQuantity(temp);
			bb.setQuantity(temp1);
			searchWaitList(oldItemID);
			logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID + " Old: "
					+ oldItemID + " Return successfully";
			writeFile(logpath, logmessage);
			return "success";
		} else if (x.equals("don't exist")) {
			return "don't exist";
		} else {
			return "failed";
		}
	}

	public String checkReturn(String userID, String newItemID, String oldItemID) {
		if (oldItemID.substring(0, 3).equals(libraryID)) {
				if (books.containsKey(oldItemID)) {
					if (libRecords.containsKey(oldItemID)) {
						HashMap<String, Record> itemRecords = libRecords.get(oldItemID);
						if (itemRecords.containsKey(userID) && itemRecords.get(userID).getStatus().equals("Borrowed")) {
							return "yes";
						} else {
							logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID
									+ " Old: " + oldItemID + " Failed: Haven't borrowed the old book.";
							writeFile(logpath, logmessage);
							return "no";
						}
					} else {
						logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID
								+ " Old: " + oldItemID + " Failed: Haven't borrowed the old book.";
						writeFile(logpath, logmessage);
						return "no";
					}
				} else {
					logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID + " Old: "
							+ oldItemID + " Failed: The book you wants to return does not exist.";
					writeFile(logpath, logmessage);
					return "don't exist";
				}
			
		} else {
			String x = "";
			int tmp1;
			byte[] message1 = ("checkReturn" + "," + userID + "," + newItemID + "," + oldItemID.substring(0, 7))
					.getBytes();
			tmp1 = portlist.get(oldItemID.substring(0, 3));
			x = sendMessage(message1, tmp1 - 1);
			if (x.equals("yes")) {
				return "yes";
			} else if (x.equals("don't exist")) {
				return "don't exist";
			} else {
				return "no";
			}
		}
	}

	public String rollbackBorrow(String userID, String newItemID) {
		Book bb = books.get(newItemID);
		bb = newBookClone;
		Quantity temp1 = bb.getQuantity();
		int temp = temp1.getQuantity();
		temp = temp + 1;
		temp1.setQuantity(temp);
		bb.setQuantity(temp1);
		logmessage = df.format(new Date()) + " Rollback the quantity of " + newItemID + " to the quantity before "
				+ userID + " exchanged to it";
		writeFile(logpath, logmessage);
		if (!(newItemRecordsClone.isEmpty())) {
			HashMap<String, Record> itemRecords = libRecords.get(newItemID);
			itemRecords.putAll(newItemRecordsClone);
		} else {
			libRecords.remove(newItemID);
		}
		logmessage = df.format(new Date()) + " Rollback the borrow record of " + newItemID + " to the version before "
				+ userID + " exchanged to it";
		writeFile(logpath, logmessage);
		return "rollback success";
	}

	public String rollbackReturn(String userID, String oldItemID) {
		HashMap<String, Record> itemRecords = libRecords.get(oldItemID);
		if (!(newItemRecordsClone.isEmpty()))
			itemRecords.putAll(oldItemRecordsClone);
		Book bb = books.get(oldItemID);
		bb = oldBookClone;
		if (!(queueClone.isEmpty())) {
			ArrayList<String> queue = waitlist.get(oldItemID);
			queue.clear();
			queue = CloneUtils.clone(queueClone);
			waitlist.put(oldItemID, queue);
			logmessage = df.format(new Date()) + " Rollback waitlist of " + oldItemID + " to the version before "
					+ userID + " returned it";
			writeFile(logpath, logmessage);
			String nextuser = queue.get(0);
			if (nextuser.subSequence(0, 3).equals(libraryID)) {
				HashMap<String, Record> nextuserrecord = records.get(nextuser);
				nextuserrecord.remove(oldItemID);
				logmessage = df.format(new Date()) + " Rollback borrow record of " + nextuser
						+ " to the version before " + userID + " returned it";
				writeFile(logpath, logmessage);
			} else {
				String x = "";
				int tmp1;
				byte[] message1 = ("deleteborrowrecord" + "," + nextuser + "," + oldItemID.substring(0, 7)).getBytes();
				tmp1 = portlist.get(nextuser.substring(0, 3));
				x = sendMessage(message1, tmp1 - 1);
			}
		}
		return "rollback success";
	}

	public String deleteborrowrecord(String nextuser, String oldItemID) {
		HashMap<String, Record> nextuserrecord = records.get(nextuser);
		nextuserrecord.remove(oldItemID);
		return "success";
	}

	public String addToWaitlistforExchange(String userID, String newItemID, String oldItemID) {
		String check = "";
		String x1 = "";
		String x2 = "";
		String x3 = "";
		String x4 = "";
		if (userID.substring(0, 3).equals(newItemID.substring(0, 3))) {
			check = checkBookAvailable(userID, newItemID, oldItemID,"check");
		} else {
			int tmp;
			byte[] message = ("checkBookAvailable" + "," + userID + "," + newItemID + ","
					+ oldItemID.substring(0, 7)+","+"check").getBytes();
			tmp = portlist.get(newItemID.substring(0, 3));
			check = sendMessage(message, tmp - 1);
		}
		MyException myException = new MyException();
		switch (check) {
		case "available":
			try {
				if (newItemID.substring(0, 3).equals(libraryID)) {
					x1 = borrowforExchange(userID, newItemID, oldItemID);
				} else {
					int tmp1;
					byte[] message1 = ("borrowforexchange" + "," + userID + "," + newItemID.substring(0, 7) + ","
							+ oldItemID.substring(0, 7)).getBytes();
					tmp1 = portlist.get(newItemID.substring(0, 3));
					x1 = sendMessage(message1, tmp1 - 1);
					String[] m = x1.split(",");
					if (x1.contains("success")) {
						borrowRecord(userID, newItemID, m[1]);
					}
				}
				if (oldItemID.substring(0, 3).equals(libraryID)) {
					x2 = returnforexchange(userID, newItemID, oldItemID);
				} else {
					int tmp2;
					byte[] message2 = ("returnforexchange" + "," + userID + "," +newItemID+","+ oldItemID.substring(0, 7)).getBytes();
					tmp2 = portlist.get(oldItemID.substring(0, 3));
					x2 = sendMessage(message2, tmp2 - 1);
					if (x2.contains("success")) {
						HashMap<String, Record> personrecord = records.get(userID);
						Record p = personrecord.get(oldItemID);
						p.setStatus();
					}
				}
				if ((!(x1.contains("success")) || (!(x2.contains("success"))))) {
					logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID + " Old: "
							+ oldItemID + " Failed";
					writeFile(logpath, logmessage);
					myException.testMyException();
				} else {
					logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID + " Old: "
							+ oldItemID + " Successful";
					writeFile(logpath, logmessage);
					return "Ex0";
				}
			} catch (Exception e) {
				HashMap<String, Record> person = records.get(userID);
				String x = "";
				person.remove(newItemID);
				Record p = person.get(oldItemID);
				p.rollbackStatus();
				if (newItemID.substring(0, 3).equals(libraryID)) {
					rollbackBorrow(userID, newItemID);
				} else {
					int tmp1;
					byte[] message1 = ("rollbackborrow" + "," + userID + "," + newItemID.substring(0, 7)).getBytes();
					tmp1 = portlist.get(newItemID.substring(0, 3));
					x = sendMessage(message1, tmp1 - 1);

				}
				if (oldItemID.substring(0, 3).equals(libraryID)) {
					rollbackReturn(userID, oldItemID);
				} else {
					int tmp2;
					byte[] message2 = ("rollbackreturn" + "," + userID + "," + oldItemID.substring(0, 7)).getBytes();
					tmp2 = portlist.get(oldItemID.substring(0, 3));
					x = sendMessage(message2, tmp2 - 1);
				}
				return "Ex1";
			}
		case "notAvailable":
			try {

				if (newItemID.substring(0, 3).equals(libraryID)) {
					x3 = addToWaitlist(userID, newItemID);
				} else {
					int tmp1;
					byte[] message1 = ("addtowaitlist" + "," + userID + "," + newItemID.substring(0, 7)).getBytes();
					tmp1 = portlist.get(newItemID.substring(0, 3));
					x3 = sendMessage(message1, tmp1 - 1);
				}
				if (oldItemID.substring(0, 3).equals(libraryID)) {
					x4 = returnforexchange(userID, newItemID, oldItemID);
				} else {
					int tmp2;
					byte[] message2 = ("returnforexchange" + "," + userID + "," +newItemID+","+ oldItemID.substring(0, 7)).getBytes();
					tmp2 = portlist.get(oldItemID.substring(0, 3));
					x4 = sendMessage(message2, tmp2 - 1);
					if (x4.contains("success")) {
						HashMap<String, Record> personrecord = records.get(userID);
						Record p = personrecord.get(oldItemID);
						p.setStatus();
					}
				}
				if ((!(x3.contains("success")) || (!(x4.contains("success"))))) {

					logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID + " Old: "
							+ oldItemID + " Failed";
					writeFile(logpath, logmessage);
					myException.testMyException();
				} else {
					logmessage = df.format(new Date()) + " Exchange Items " + userID + " New: " + newItemID + " Old: "
							+ oldItemID + " Successful";
					writeFile(logpath, logmessage);
					return "AtwEx0";
				}
			} catch (Exception e) {
				String x = "";
				HashMap<String, Record> person = records.get(userID);
				if (!x4.equals("don't exist")) {
					Record p = person.get(oldItemID);
					p.rollbackStatus();
				}
				if (!x3.equals("failed")) {
					if (newItemID.substring(0, 3).equals(libraryID)) {
						ArrayList<String> queue = waitlist.get(newItemID);
						queue.remove(queue.size() - 1);
					} else {
						int tmp1;
						byte[] message1 = ("rollbackwaitlist" + "," + userID + "," + newItemID.substring(0, 7))
								.getBytes();
						tmp1 = portlist.get(newItemID.substring(0, 3));
						x = sendMessage(message1, tmp1 - 1);

					}
				}
				if (oldItemID.substring(0, 3).equals(libraryID)) {
					rollbackReturn(userID, oldItemID);
				} else {
					int tmp2;
					byte[] message2 = ("rollbackreturn" + "," + userID + "," + oldItemID.substring(0, 7)).getBytes();
					tmp2 = portlist.get(oldItemID.substring(0, 3));
					x = sendMessage(message2, tmp2 - 1);
				}
				if (x4.equals("don't exist")) {
					return "Ex2";
				}
				if(x3.equals("failed")) {
					return "Ex3";
				}
				return "Ex1";
			}
		default:
			return check;
		}
	}


	public String rollbackwaitlist(String userID, String newItemID) {
		ArrayList<String> queue = waitlist.get(newItemID);
		queue.remove(queue.size() - 1);
		logmessage = df.format(new Date()) + " Rollback waitlist: Delete " + userID + " from the waitlist of "
				+ newItemID + " successfully";
		writeFile(logpath, logmessage);
		return "success";
	}

}
