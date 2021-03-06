package FrontEnd;

import Model.RMAddressInfo;
import org.omg.CORBA.*;
import FrontEndAPP.*;
import Model.FEPort;
import Model.RMPort;
import Model.SequencerPort;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class FrontEndObj extends FrontEndPOA {


    private ORB orb;
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int MAXNUM = 5;
    private static final int TIMEOUT = 2000;
    String FEID;
    private static int portNum;
    static int portSeq = SequencerPort.SEQUENCER_PORT.sequencerPort;
    String logpath;
    private static boolean failureCase;
    private static boolean crashCase;
    private static boolean voteStatus;
    private static boolean listStatus = false;
    private static String sequenceID;

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

    public FrontEndObj(int portNum) {
        super();
        this.portNum = portNum;
        logpath = "E:/dlms/DLMS_project/server/" + FEID + "Server.log";
    }

    public void sendMessage(String message) throws Exception {
        // TODO:Sequener Ip;
        System.out.println("sendMessage");
        DatagramSocket aSocket = null;
        try {
            aSocket = new DatagramSocket();
            InetAddress aHost = InetAddress.getByName("localhost");
            byte[] data = message.getBytes();
            DatagramPacket request = new DatagramPacket(data, data.length, aHost, portSeq);
            aSocket.send(request);
            System.out.println("Request message sent from the client to server with port number " + portSeq + " is: "
                    + new String(request.getData()));
            // byte[] buffer = new byte[1000];
            // DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            //
            // aSocket.receive(reply);
            // System.out.println("Reply received from the server with port number " +
            // portSeq + " is: "
            // + new String(reply.getData()));

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }

    private static int registerListener(DatagramSocket socket, Map<String, String> resultSet) {
        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        try {
            socket.receive(packet);
            socket.send(packet);
            String result = new String(packet.getData(), 0, packet.getLength());

            System.out.println("receive " + result);
            if(!result.isEmpty()) {
                String[] res = result.split(":");
                resultSet.put(res[1], res[2]);
                sequenceID = res[0];
            }
        } catch (SocketException e) {

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
        String x = "";
        sequenceID = "";
        int count = 0;
        voteStatus = false;
        try {
            socket = new DatagramSocket(FEPort.FE_PORT.RegistorPort);
            String message = "addItem" + "," + managerID + "," + itemID + "," + itemName + "," + q;
            sendMessage(message);
            Timer timer = new Timer(socket, false, 10000);
            Thread thread = new Thread(timer);
            thread.start();
            while (count < 3 && !timer.timeout) {
                count = registerListener(socket, resultSet);
                if (count >= 2 && (!voteStatus)) {
                    x = majority(resultSet);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
        if (resultSet.size() < 3 && crashCase) {
            tellRMCrash(resultSet);
        }
        if (x.equals("Ad0")) {
            return itemID + " " + itemName + " " + q + ": Add Successful";
        } else {
            return itemID + " " + itemName + " " + q + ": The itemID and itemName don't match";
        }
    }

    @Override
    public String removeItem(String managerID, String itemID, int quantity) {
        Map<String, String> resultSet = new HashMap<>();
        DatagramSocket socket = null;
        String x = "";
        sequenceID = "";
        voteStatus = false;
        int count = 0;
        try {
            socket = new DatagramSocket(FEPort.FE_PORT.RegistorPort);
            String message = "removeItem" + "," + managerID + "," + itemID + "," + quantity;
            sendMessage(message);
            Timer timer = new Timer(socket, false, 10000);
            Thread thread = new Thread(timer);
            thread.start();
            while (count < 3 && !timer.timeout) {
                count = registerListener(socket, resultSet);
                if (count >= 2 && (!voteStatus)) {
                    x = majority(resultSet);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
        if (resultSet.size() < 3 && crashCase) {
            tellRMCrash(resultSet);
        }
        if (x.equals("Re0")) {
            return itemID + " " + quantity + ": Remove Successfully";
        } else if (x.equals("Re1")) {
            return itemID + " " + quantity + ": Decrease Successfully";
        } else if (x.equals("Re2")) {
            return itemID + " " + quantity + ": Remove Failed. The quantity is unvailable.";
        } else {
            return itemID + " " + quantity + ": Remove Failed: The itemID does not exist";
        }
    }

    @Override
    public String listItemAvailability(String managerID) {
        Map<String, String> resultSet = new HashMap<>();
        DatagramSocket socket = null;
        String x = "";
        sequenceID = "";
        voteStatus = false;
        listStatus = true;
        int count = 0;
        try {
            socket = new DatagramSocket(FEPort.FE_PORT.RegistorPort);
            String message = "listItem" + "," + managerID;
            sendMessage(message);
            Timer timer = new Timer(socket, false, 10000);
            Thread thread = new Thread(timer);
            thread.start();
            while (count < 3 && !timer.timeout) {
                count = registerListener(socket, resultSet);
                if (failureCase) {
                    if (count == 3) {
                        x = majorityList(resultSet);
                    }
                } else {
                    if (count >= 2 && (!voteStatus)) {
                        x = majorityList(resultSet);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
        if (resultSet.size() < 3 && crashCase) {
            tellRMCrash(resultSet);
        }
        if (x.length() > 0) {
            return x;
        } else {
            return "The library has no book now";
        }

    }

    @Override
    public String borrowItem(String userID, String itemID) {
        System.out.println("1 borrowItem");
        Map<String, String> resultSet = new HashMap<>();
        DatagramSocket socket = null;
        String x = "";
        sequenceID = "";
        voteStatus = false;
        int count = 0;
        try {
            socket = new DatagramSocket(FEPort.FE_PORT.RegistorPort);
            String message = "borrowItem" + "," + userID + "," + itemID;
            System.out.println("2 borrowItem");
            sendMessage(message);
            Timer timer = new Timer(socket, false, 10000);
            Thread thread = new Thread(timer);
            thread.start();
            while (count < 3 && !timer.timeout) {
                count = registerListener(socket, resultSet);
                if (count >= 2 && (!voteStatus)) {
                    x = majority(resultSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
        if (resultSet.size() < 3 && crashCase) {
            tellRMCrash(resultSet);
        }
        if (x.equals("Br0")) {
            return itemID + ": Borrow Successfully";
        } else if (x.equals("Br1")) {
            return itemID + ": Borrow Failed. The item does not exist";
        } else if (x.equals("Br2")) {
            return itemID + ": Borrow Failed. The book is not available now. Do you want to be added to waitlist? Y/N";
        } else if (x.equals("Br3")) {
            return itemID + ": Borrow Failed: You have borrowed this item and not returned yet";
        } else if (x.equals("Br4")) {
            return itemID + ": Borrow Failed: You already borrowed another book in that library and not returned yet";
        } else {
            return itemID + ": Borrow Failed: You are already in a waitlist of that library";
        }
    }

    @Override
    public String findItem(String userID, String itemName) {
        Map<String, String> resultSet = new HashMap<>();
        DatagramSocket socket = null;
        String x = "";
        sequenceID = "";
        voteStatus = false;
        int count = 0;
        try {
            socket = new DatagramSocket(FEPort.FE_PORT.RegistorPort);
            String message = "findItem" + "," + userID + "," + itemName;
            sendMessage(message);
            Timer timer = new Timer(socket, false, 10000);
            Thread thread = new Thread(timer);
            thread.start();
            while (count < 3 && !timer.timeout) {
                count = registerListener(socket, resultSet);
                if (count >= 2 && (!voteStatus)) {
                    x = majorityList(resultSet);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
        if (resultSet.size() < 3 && crashCase) {
            tellRMCrash(resultSet);
        }
        if (x.length() > 0) {
            return x;
        } else {
            return itemName + ": This book does not exist";
        }
    }

    @Override
    public String returnItem(String userID, String itemID) {
        Map<String, String> resultSet = new HashMap<>();
        DatagramSocket socket = null;
        String x = "";
        sequenceID = "";
        voteStatus = false;
        int count = 0;
        try {
            socket = new DatagramSocket(FEPort.FE_PORT.RegistorPort);
            String message = "returnItem" + "," + userID + "," + itemID;
            sendMessage(message);
            Timer timer = new Timer(socket, false, 10000);
            Thread thread = new Thread(timer);
            thread.start();
            while (count < 3 && !timer.timeout) {
                count = registerListener(socket, resultSet);
                if (count >= 2 && (!voteStatus)) {
                    x = majority(resultSet);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
        if (resultSet.size() < 3) {
            tellRMCrash(resultSet);
        }
        if (resultSet.size() < 3 && crashCase) {
            tellRMCrash(resultSet);
        }
        if (x.equals("Rtn0")) {
            return itemID + ": Return Successfully";
        } else if (x.equals("Rtn1")) {
            return itemID + ": Return Failed. The item does not exist";
        } else {
            return itemID + ": Return Failed. You haven't borrowed this item";
        }
    }

    @Override
    public String checkBorrowList(String userID) {
        return userID;
        // byte[] message = ("checkBorrow" + "," + userID).getBytes();
        // String x = sendMessage(message);
        // String[] m = x.split(",");
        // return m[1];
    }

    @Override
    public String checkWaitList(String itemID) {
        return itemID;
        // byte[] message = ("checkWaitList" + "," + itemID).getBytes();
        // String x = sendMessage(message);
        // String[] m = x.split(",");
        // return m[1];
    }

    @Override
    public String addToWaitlist(String userID, String itemID) {
        Map<String, String> resultSet = new HashMap<>();
        DatagramSocket socket = null;
        String x = "";
        sequenceID = "";
        voteStatus = false;
        int count = 0;
        try {
            socket = new DatagramSocket(FEPort.FE_PORT.RegistorPort);
            String message = "addToWaitlist" + "," + userID + "," + itemID;
            sendMessage(message);
            Timer timer = new Timer(socket, false, 10000);
            Thread thread = new Thread(timer);
            thread.start();
            while (count < 3 && !timer.timeout) {
                count = registerListener(socket, resultSet);
                if (count >= 2 && (!voteStatus)) {
                    x = majority(resultSet);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
        if (resultSet.size() < 3 && crashCase) {
            tellRMCrash(resultSet);
        }
        if (x.equals("Atw0")) {
            return itemID + ": Add to waitlist Successfully";
        } else {
            return itemID + ": Add to waitlist Failed. You're already in the waitlist.";
        }
    }

    @Override
    public String exchange(String studentID, String newItemID, String oldItemID) {
        Map<String, String> resultSet = new HashMap<>();
        DatagramSocket socket = null;
        String x = "";
        sequenceID = "";
        voteStatus = false;
        int count = 0;
        try {
            socket = new DatagramSocket(FEPort.FE_PORT.RegistorPort);
            String message = "exchangeItem" + "," + studentID + "," + newItemID + "," + oldItemID;
            sendMessage(message);
            Timer timer = new Timer(socket, false, 10000);
            Thread thread = new Thread(timer);
            thread.start();
            while (count < 3 && !timer.timeout) {
                count = registerListener(socket, resultSet);
                if (count >= 2 && (!voteStatus)) {
                    x = majority(resultSet);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
        if (resultSet.size() < 3 && crashCase) {
            tellRMCrash(resultSet);
        }
        if (x.equals("Ex0")) {
            return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Successfully";
        } else if (x.equals("Ex1")) {
            return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Failed.";
        } else if (x.equals("Ex2")) {
            return "New: " + newItemID + " Old: " + oldItemID
                    + ": Exchange Failed. The book you want to return does not exist";
        } else if (x.equals("Ex3")) {
            return "New: " + newItemID + " Old: " + oldItemID
                    + ": Exchange Failed. The book you want to borrow does not exist";
        } else if (x.equals("Ex4")) {
            return "New: " + newItemID + " Old: " + oldItemID
                    + ": Exchange Failed. The book is not available now. Do you want to be added to waitlist? Y/N";
        } else if (x.equals("Ex5")) {
            return "New: " + newItemID + " Old: " + oldItemID + ": Exchange failed. You already borrowed the new book";
        } else if (x.equals("Ex6")) {
            return "New: " + newItemID + " Old: " + oldItemID
                    + ": Exchange failed. You already borrowed another book in that library";
        } else if (x.equals("Ex7")) {
            return "New: " + newItemID + " Old: " + oldItemID
                    + ": Exchange failed. You're already in the waitlist of the new book";
        } else if (x.equals("Ex8")) {
            return "New: " + newItemID + " Old: " + oldItemID
                    + ": Exchange failed. You are already in a waitlist in that library";
        } else {
            return "New: " + newItemID + " Old: " + oldItemID + ": Exchange failed. You haven't borrowed the old item";
        }
    }

    @Override
    public String addToWaitlistforExchange(String studentID, String newItemID, String oldItemID) {
        Map<String, String> resultSet = new HashMap<>();
        DatagramSocket socket = null;
        String x = "";
        sequenceID = "";
        voteStatus = false;
        int count = 0;
        try {
            socket = new DatagramSocket(FEPort.FE_PORT.RegistorPort);
            String message = "addToWaitlistforExchagne" + "," + studentID + "," + newItemID + "," + oldItemID;
            sendMessage(message);
            Timer timer = new Timer(socket, false, 10000);
            Thread thread = new Thread(timer);
            thread.start();
            while (count < 3 && !timer.timeout) {
                count = registerListener(socket, resultSet);
                if (count >= 2 && (!voteStatus)) {
                    x = majority(resultSet);
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
        } finally {
            if (socket != null)
                socket.close();
        }
        if (resultSet.size() < 3 && crashCase) {
            tellRMCrash(resultSet);
        }
        if (x.equals("Ex0")) {
            return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Successfully";
        } else if (x.equals("AtwEx0")) {
            return "New: " + newItemID + " Old: " + oldItemID
                    + ": Exchange Successfully. Add to waitlist successfully.";
        } else if (x.equals("Ex1")) {
            return "New: " + newItemID + " Old: " + oldItemID + ": Exchange Failed.";
        } else if (x.equals("Ex2")) {
            return "New: " + newItemID + " Old: " + oldItemID
                    + ": Exchange Failed. The book you want to return does not exist";
        } else if (x.equals("Ex3")) {
            return "New: " + newItemID + " Old: " + oldItemID
                    + ": Exchange Failed. The book you want to borrow does not exist";
        } else if (x.equals("Ex5")) {
            return "New: " + newItemID + " Old: " + oldItemID + ": Exchange failed. You already borrowed the new book";
        } else if (x.equals("Ex6")) {
            return "New: " + newItemID + " Old: " + oldItemID
                    + ": Exchange failed. You already borrowed another book in that library";
        } else if (x.equals("Ex7")) {
            return "New: " + newItemID + " Old: " + oldItemID
                    + ": Exchange failed. You're already in the waitlist of the new book";
        } else if (x.equals("Ex8")) {
            return "New: " + newItemID + " Old: " + oldItemID
                    + ": Exchange failed. You are already in a waitlist in that library";
        } else {
            return "New: " + newItemID + " Old: " + oldItemID + ": Exchange failed. You haven't borrowed the old item";
        }
    }

    private static void tellRMCrash(Map<String, String> resultSet) {
        String msg = "";
        if (!resultSet.containsKey("1")) {
            msg = "Crash" + ":" + "1";
        } else if (!resultSet.containsKey("2")) {
            msg = "Crash" + ":" + "2";
        } else if (!resultSet.containsKey("3")) {
            msg = "Crash" + ":" + "3";
        }
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            multicastCrashMsg(msg, socket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    private static void tellRMFailure(String failServerNum) {
        String msg = "SoftWareFailure";
        if (failServerNum.equals("1")) {
            msg = msg + ":" + "1" + ":" + sequenceID;
        } else if (failServerNum.equals("2")) {
            msg = msg + ":" + "2" + ":" + sequenceID;
        } else if (failServerNum.equals("3")) {
            msg = msg + ":" + "3" + ":" + sequenceID;
        }
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            multicastCrashMsg(msg, socket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

    }

    private static DatagramPacket packet(String rmAddress, byte[] data, int replica) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(rmAddress);
        return new DatagramPacket(data, 0, data.length, address, replica);
    }

    private static void multicastCrashMsg(String msg, DatagramSocket socket) {
        System.out.println("Multicast to RMs:" + msg);
        DatagramSocket aSocket = null;
        DatagramPacket reply = null;
        List list = new LinkedList();
        try {
            System.out.println("Client Started........");


            InetAddress address1 = InetAddress.getByName(RMAddressInfo.RM_ADDRESS_INFO.RM1address);
            InetAddress address2 = InetAddress.getByName(RMAddressInfo.RM_ADDRESS_INFO.RM2address);
            InetAddress address3 = InetAddress.getByName(RMAddressInfo.RM_ADDRESS_INFO.RM3address);

            byte[] data = msg.getBytes();
            DatagramPacket[] packets = new DatagramPacket[3];
            packets[0] = new DatagramPacket(data, data.length, address1, RMPort.RM_PORT.rmPort1); // 6001
            packets[1] = new DatagramPacket(data, data.length, address2, RMPort.RM_PORT.rmPort2); // 6002
            packets[2] = new DatagramPacket(data, data.length, address3, RMPort.RM_PORT.rmPort3); // 6003


            for (int i = 0; i < 3; i++) {
                int send_count = 0;
                boolean tmp = false;
                while (!tmp && send_count < MAXNUM) {
                    try {
                        aSocket = new DatagramSocket();
                        aSocket.setSoTimeout(TIMEOUT);
                        aSocket.send(packets[i]);
                        byte[] buffer = new byte[1000];
                        reply = new DatagramPacket(buffer, buffer.length);
                        aSocket.receive(reply);
                        tmp = true;
                    } catch (InterruptedIOException e) {
                        send_count = 1;
                        System.out.println("Time out," + (MAXNUM - send_count) + "more times...");
                    } finally {
                        if(aSocket!=null){
                            aSocket.close();
                        }
                    }
                }


            }


        } catch (Exception e) {
            System.out.println("udpClient error: " + e);
        }
    }

    private static String majority(Map<String, String> resultSet) {
        Map<String, Integer> map = new HashMap<>();
        for (String s : resultSet.keySet()) {
            String tmp = resultSet.get(s);
            if (map.containsKey(tmp)) {
                map.put(tmp, map.get(tmp) + 1);
            } else {
                map.put(tmp, 1);
            }
        }

        Integer vote = 0;
        String candidate = "";
        for (String s : map.keySet()) {
            int tmp = map.get(s);
            if (tmp > vote) {
                candidate = s;
                vote = tmp;
            }
        }
        if (vote >= 2) {
            voteStatus = true;
        }
        return candidate;
    }

    private static String majorityList(Map<String, String> resultSet) {
        Map<HashMap<String, String>, Integer> map = new HashMap<>();
        Map<String, HashMap<String, String>> result = new HashMap<>();
        for (String s : resultSet.keySet()) {
            String tmp = resultSet.get(s);
            String s1[] = tmp.split("\\p{javaWhitespace}+");
            HashMap<String, String> tmp1 = new HashMap<>();
            for (int i = 0; i < s1.length; i++) {
                String s2[] = s1[i].split(",");
                tmp1.put(s2[0], s2[1]);
            }
            result.put(s, tmp1);
            if (map.containsKey(tmp1)) {
                map.put(tmp1, map.get(tmp1) + 1);
            } else {
                map.put(tmp1, 1);
            }
        }
        Integer vote = 0;
        HashMap<String, String> candidate = new HashMap<>();
        for (HashMap<String, String> s : map.keySet()) {
            int tmp = map.get(s);
            if (tmp > vote) {
                candidate = s;
                vote = tmp;
            }
        }
        StringBuilder stringbuilder = new StringBuilder();
        for (String s : candidate.keySet()) {
            stringbuilder.append(s + "," + candidate.get(s) + "\n");
        }
        String returnresult = stringbuilder.toString();
        if (failureCase && listStatus) {
            findSoftwareFailforHash(candidate, vote, result);
        }
        if (vote >= 2) {
            voteStatus = true;
        }
        return returnresult;
    }

    private static void findSoftwareFailforHash(HashMap<String, String> candidate, Integer vote,
                                                Map<String, HashMap<String, String>> resultSet) {
        if (vote == 3)
            return;
        String failServerNum = null;
        for (String s : resultSet.keySet()) {
            if (!candidate.equals(resultSet.get(s))) {
                failServerNum = s;
            }
        }
        tellRMFailure(failServerNum);
    }

    @Override
    public String setUpFailureType(int option) {
        String msg = "SetUpFailureType";
        msg = msg + ":" + option;
        if (option == 1) {
            this.failureCase = true;
        } else {
            this.crashCase = true;
        }

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            multicastCrashMsg(msg, socket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
        return "send";
    }
}
