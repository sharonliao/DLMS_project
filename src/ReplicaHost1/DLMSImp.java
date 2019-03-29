package ReplicaHost1;


import Model.Item;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DLMSImp {
    public boolean bugFree = false;
    private HashMap<String, Integer> portMap = new HashMap<>();
    DatagramSocket aSocket = null;
    Logger log;

    //when start the server, initial data
    int LocalPort;
    String localFlag;

    private ConcurrentHashMap<String, Item> bookList= new ConcurrentHashMap<String, Item>();//itemId ItemName quantity
    private ConcurrentHashMap<String, Integer> borrowList = new ConcurrentHashMap<String, Integer>();//userId itemId
    private ConcurrentHashMap<String, Queue<String>> waitingList= new ConcurrentHashMap<String, Queue<String>>();

    public DLMSImp(String local, int localPort, Logger log) {
        this.log = log;
        this.LocalPort = localPort;
        this.localFlag = local;
        portMap.put("CON", 7777);
        portMap.put("MCG", 8888);
        portMap.put("MON", 9999);
        initContent();
    }


    private void initContent() {

        bookList = new ConcurrentHashMap<String, Item>();
        borrowList = new ConcurrentHashMap<String, Integer>();
        waitingList = new ConcurrentHashMap<String, Queue<String>>();

        bookList.put(localFlag + "1111", new Item(localFlag + "1111", "AA", 2));
        bookList.put(localFlag + "2222", new Item(localFlag + "2222", "BB", 1));
        bookList.put(localFlag + "3333", new Item(localFlag + "3333", "CC", 5));
        bookList.put(localFlag + "4444", new Item(localFlag + "4444", "DD", 2));
    }


    public String addItem(String managerId, String itemId, String itemName, int quantity) {
        String returnMsg = "";
        String waitingInfos = "";
        if (bookList.containsKey(itemId)) { //simply increase quantity
            Item item = bookList.get(itemId);
            if (!item.getItemName().equals(itemId)) {
                return "Ad2";
            }
            int newQuantity = item.getQuantity() + quantity;
            item.setQuantity(newQuantity);
            bookList.put(itemId, item);
            returnMsg = "Ad0";

            while (item.getQuantity() > 0 && waitingList.containsKey(itemId)) {
                System.out.println("quantity: " + item.getQuantity() + "  waitingList: " + waitingList.get(itemId));
                String waitingInfo = autoLendWaitingItem(itemId);
                waitingInfos = waitingInfos + waitingInfo + "\n";
            }

        } else {
            Item newItem = new Item(itemId, itemName, quantity);
            bookList.put(itemId, newItem);
            returnMsg = "Ad0";
        }
        System.out.println("add book - book list闁挎冻鎷�" + bookList);

        return returnMsg;
    }

    public String removeItem(String managerId, String itemId, int quantity) {
        System.out.println("removeItem - booklist:" + bookList);
        String rtn = "";
        String logInfo = "";
        if (bookList.containsKey(itemId)) {
            Item item = bookList.get(itemId);

            if (quantity < 0) { //delete the item
                bookList.remove(itemId);
                rtn = "Re0";

                //remove record from borrow list
                String borrowInfo = "";
                Collection<String> allKey = borrowList.keySet();
                for (String s : allKey) {
                    if (itemId.equals(s.substring(9))) {
                        borrowList.remove(s);
                        borrowInfo = "remove item from borrow list.\n";
                    }
                }
                // remove record grom waiting list
                String waitingInfo = "";
                if (waitingList.containsKey(itemId)) {
                    waitingList.remove(itemId);
                    waitingInfo = "remove item from waiting list.\n";
                }
                logInfo = borrowInfo + waitingInfo + rtn;

            } else if (quantity <= bookList.get(itemId).getQuantity()) { //simply decrease quantity
                int newQuantity = (item.getQuantity() - quantity);
                item.setQuantity(newQuantity);
                rtn = "Re1";
                logInfo = rtn;
            } else if (quantity > bookList.get(itemId).getQuantity()) {  //wrong number
                rtn = "Re2";
            }
        } else {
            rtn = "Re3";
        }

        logInfo = "Remove item" + " \n"
                + "managerId-" + managerId + ", itemId-" + itemId + " quantity-" + quantity + "\n"
                + logInfo + "\n";

        return rtn;
    }


    public String listItemAvailability(String managerID) {
        //list books of  local library first
        String listOfBook = "";
        Collection<Item> allValue = bookList.values();
        for (Item item : allValue) {
            listOfBook = listOfBook + item.toString() + "\n";
        }

        String msg = fomatString("availableInLocal," + managerID);

//        for(int port : portMap.values()){
//            if(port != LocalPort){
//                String info = udpClient(msg,port);
//                listOfBook = listOfBook + info +"\n";
//            }
//        }
        System.out.println("3--" + listOfBook);
        return listOfBook.trim();
    }

    String availableInLocal(String managerID) {
        String listOfBook = "";
        Collection<Item> allValue = bookList.values();
        for (Item item : allValue) {
            listOfBook = listOfBook + item.toString() + "\n";
        }
        return listOfBook;
    }


    public String borrowItem(String userId, String itemId) {
        String rtn = "";
        String userAndItemID = userId + "-" + itemId;
        String message = "borrow," + userId + "," + itemId;
        System.out.println(message);

        //check if the item is in local library
        if (isLocalBook(itemId)) {
            rtn = borrowItemInLocal(userId, itemId);
            System.out.print("borrowItemInLocal rtn---" + rtn);

        } else {
            rtn = udpCommunication(itemId, message);
        }
        System.out.print("rtn " + rtn + "\n");
        String info = "Borrow an item \n"
                + "userId-" + userId + ", itemId-" + itemId + "\n"
                + rtn + "\n";
        return rtn;
    }


    public synchronized String returnItem(String userId, String itemId) {
        String returnInfo = "";
        String waitingInfo = "";
        // check the book is belonged to local library
        if (itemId.substring(0, 3).equals(localFlag)) {
            returnInfo = returnInLocal(userId, itemId);

        } else { // if the book is not belong to local library
            String message = "return," + userId + "," + itemId;

            returnInfo = udpCommunication(itemId, message);

        }
        String info = "Return an item \n"
                + "userId-" + userId + ", itemId-" + itemId + "\n"
                + returnInfo + "\n"
                + waitingInfo + "\n";

        return returnInfo;  //濠碘�冲�归悘澶嬬▔瀹ュ懐娼ｅù婊冨濠�浼村炊閸欍儱濮涘Λ锝呮濞堟垶绋婇敂鑲╃闁告瑯浜為鍝ユ媼閺夎法绉块柡鍕靛灠閹浇銇愰幒鏇犵闁挎稑鐭侀崵锔界瀹告竵itinglist闁煎浜滄慨鈺冪磼椤撶儑鎷烽悢鍛娦﹀鑸电墵椤╊偊鎳涢鍕畳閻犱焦婢樼紞宥夋晬鐏炵偓鎷遍柛锔芥緲濞存ɑ绋婇敂璺ㄧ憹闂傚浄鎷烽悷鏇氳兌閻擄繝鏌嗛敓锟�
    }

    public String returnInLocal(String userId, String itemId) {
        String returnInfo = "";
        String returnItem = userId + "-" + itemId;
        String waitingInfo = "";
        //check user if borrowed this book
        if (borrowList.containsKey(returnItem)) {
            borrowList.remove(returnItem);
            bookList.get(itemId).returnOneItem();//quantity ++
            returnInfo = "Rtn0";
            //check waiting list
            waitingInfo = autoLendWaitingItem(itemId);

        } else {
            returnInfo = "Rtn2";
        }
        return returnInfo;
    }


    public String findItem(String userId, String itemName) {
        String listOfBook = "";
        Collection<Item> allValue = bookList.values();
        for (Item item : allValue) {
            if (item.getItemName().equals(itemName)) {
                listOfBook = listOfBook + item.getItemId()+","+item.getQuantity() + "\n";
            }
        }
        String msg = fomatString("findInLocal," + userId + "," + itemName);

        for (int port : portMap.values()) {
            if(port != LocalPort){
                String info = udpClient(msg, port);
                listOfBook = listOfBook + info +"\n";
            }
        }

        String info = "Find an item \n"
                + "userId-" + userId + ", itemName-" + itemName + "\n"
                + listOfBook + "\n";

        return listOfBook;
    }


    String findInLocal(String userId, String itemName) {
        String listOfBook = "";
        Collection<Item> allValue = bookList.values();
        for (Item item : allValue) {
            if (item.getItemName().equals(itemName)) {
                listOfBook = listOfBook + item.getItemId()+","+item.getQuantity() + "\n";
            }
        }
        return listOfBook;
    }


    public void udpServer() {
        try {
            aSocket = new DatagramSocket(LocalPort);
            System.out.println("Server Started............");
            while (true) {
                byte[] buffer = new byte[1000];
                String rtnMsg = "";
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);// request received
                String requestMsg = new String(request.getData()).trim();
                System.out.println("Request received from client: " + requestMsg);
                //闁哄秶鍘х槐锟� 1闁靛棔鍏瑄nction 2闁靛棔妞掔粻锝夊触鎼淬劌鍘撮柡鍕靛灠瀵剟寮敓锟�
                String[] params = requestMsg.split(",");
                switch (params[0].trim()) {
                    case "borrow":
                        rtnMsg = borrowItemInLocal(params[1], params[2]);
                        break;
                    case "return":
                        rtnMsg = returnInLocal(params[1], params[2]);
                        break;
                    case "putInWaiting":
                        rtnMsg = putInWaiting(params[1], params[2]);
                        break;
                    case "findInLocal":
                        rtnMsg = findInLocal(params[1], params[2]);
                        break;
                    case "checkIfRtnAvailLocal":
                        rtnMsg = checkIfRtnAvailLocal(params[1], params[2]);
                        break;
                    case "checkAndBorrowInlocal":
                        rtnMsg = checkAndBorrowInlocal(params[1], params[2], params[3]);
                        break;
                    case "addToWaitlistforExchagne":
                        rtnMsg = ex_putInWaiting(params[1], params[2], params[3]);
                        break;
                    case "availableInLocal":
                        rtnMsg = availableInLocal(params[1]);
                        break;
                    default:
                        rtnMsg = "";
                }
                System.out.print("UDP sever send : " + fomatString(rtnMsg));
                DatagramPacket reply = new DatagramPacket(fomatString(rtnMsg).getBytes(), fomatString(rtnMsg).length(), request.getAddress(),
                        request.getPort());// reply packet ready
                aSocket.send(reply);// reply sent
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


    public String borrowItemInLocal(String userId, String itemId) {
        //check if in local library
        String rtn = "";
        String userAndItemID = userId + "-" + itemId;
        if (borrowList.containsKey(userAndItemID)) { // check the user if has already borrowed this book
            rtn = "Br3";
        } else if (!userId.substring(0, 3).equals(localFlag) && ifBorrow(userAndItemID)) { //check whether borrowed a book from this library
            rtn = "Br4";
        } else {
            if (ifAvailable(itemId).equals("Available")) { //check  the item is available or not
                borrowList.put(userAndItemID, 0);
                bookList.get(itemId).lendOneItem();
                rtn = "Br0";
            } else if (ifAvailable(itemId).equals("Waiting List")) {//put in waiting list or not
                if (waitingList.get(itemId) != null) {
                    if (waitingList.get(itemId).contains(userId)) {
                        return "Br5"; // already in the waitlist.
                    }
                }
                rtn = "Br2";
            } else { //no this item
                rtn = "Br1";
            }
        }
        return rtn;
    }

    public String udpClient(String msg, int sPort) {
        DatagramSocket aSocket = null;
        String returnMsg = "";
        try {
            System.out.println("Client Started........");
            aSocket = new DatagramSocket();
            byte[] message = msg.getBytes();

            InetAddress aHost = InetAddress.getByName("localhost");
            int serverPort = sPort;
            DatagramPacket request = new DatagramPacket(message, message.length, aHost, serverPort);
            aSocket.send(request);
            System.out.println("Request message sent from the client is : " + new String(request.getData()));
            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);
            returnMsg = new String(reply.getData()).trim();
            System.out.println("Reply received from the server is: " + returnMsg);

        } catch (Exception e) {
            System.out.println("udpClient error: " + e);
        }
        return returnMsg;
    }


    public boolean isLocalBook(String itemId) {
        if (itemId.substring(0, 3).equals(localFlag))
            return true;
        else
            return false;
    }


    String ifAvailable(String itemId) {
        if (bookList.containsKey(itemId)) {
            if (bookList.get(itemId).getQuantity() > 0) {
                return "Available";
            } else if (bookList.get(itemId).getQuantity() == 0) {
                return "Waiting List";
            } else {
                return "NO this book";
            }
        } else {
            return "NO this book";
        }
    }

    boolean ifBorrow(String userAndItemID) {
        boolean ifBorrowOthL = false;
        Collection<String> allKey = borrowList.keySet();
        for (String s : allKey)
            if (userAndItemID.substring(0, 12).equals(s.substring(0, 12))) {
                ifBorrowOthL = true;
                break;
            }
        return ifBorrowOthL;
    }

    /**
     * check whether it is available for return the old item
     * check bookList first
     * check borrowList
     * if the item is not in this library , send UDP message to the properly library
     */
    public String checkIfReturnAvailable(String userId, String itemId) {
        String returnInfo = "";

        if (itemId.substring(0, 3).equals(localFlag)) {
            returnInfo = checkIfRtnAvailLocal(userId, itemId);
        } else {
            // if the book is not belong to local library
            String message = "checkIfRtnAvailLocal," + userId + "," + itemId;
            returnInfo = udpCommunication(itemId, message);
        }
        System.out.print("checkIfReturnAvailable1 returnInfo --- " + returnInfo);
        String info = "Check if user(" + userId + ") borrowed the Book(" + itemId + ") 闁炽儲鏌￠敓鑺ユ煛閿熻姤鏌￠敓鏂ゆ嫹 "
                + returnInfo + "\n";
        //writeLog(info);
        return returnInfo;
    }


    public String checkIfRtnAvailLocal(String userId, String itemId) {
        String returnInfo = "";
        String borrowId = userId + "-" + itemId;
        if (bookList.containsKey(itemId)) {
            if (borrowList.containsKey(borrowId)) {
                returnInfo = "YES";
            } else {
                returnInfo = "Ex9";//The user didn't borrow this book;
            }
        } else {
            returnInfo = "Ex2";//There is no this book;
        }
        return returnInfo;
    }


    public synchronized String checkAndBorrowNewItem(String odlItemId, String newItemId, String userId) {
        String rtn = "";
        String info = "";
        String userAndItemID = userId + "-" + newItemId;
        String message = "checkAndBorrowInlocal," + userId + "," + odlItemId + "," + newItemId;
        // check if the book is in local library first

        if (isLocalBook(newItemId)) {
            if (borrowList.containsKey(userAndItemID)) { // check the user if has already borrowed this book
                rtn = "Ex5";
            } else {
                if (ifAvailable(newItemId).equals("Available")) { //check  the item is available or not
                    borrowList.put(userAndItemID, 0);
                    bookList.get(newItemId).lendOneItem();
                    rtn = "Ex0";
                } else if (ifAvailable(newItemId).equals("Waiting List")) {//put in waiting list or not
                    if (waitingList.get(newItemId) != null) {
                        if (waitingList.get(newItemId).contains(userId)) {
                            return "Ex7"; // already in the waitlist.
                        }
                    }
                    rtn = "Ex4"; // if put in waiting list
                } else { //no this item
                    rtn = "Ex3";
                }
            }
        } else {
            rtn = udpCommunication(newItemId, message);
        }
        return rtn;
    }

    public String checkAndBorrowInlocal(String userId, String odlItemId, String newItemId) {
        System.out.print("1");
        String rtn = "";
        String userAndItemID = userId + "-" + newItemId;
        if (borrowList.containsKey(userAndItemID)) { // check the user if has already borrowed this book
            return "Ex5";
        }
        // check if user is from other libraries
        if (!userId.substring(0, 3).equals(localFlag)) {
            //check if the user has borrowed a item from this library
            if (ifBorrow(userAndItemID)) {
                //if old item and new item from same library, then borrow is availabe
                if (!odlItemId.substring(0, 3).equals(newItemId.substring(0, 3))) {
                    // exchange end
                    rtn = "Ex6"; //You have borrowed a book from this Library.
                    return rtn;
                }
            }
        }
        // continue to check if the new item is availabe
        if (ifAvailable(newItemId).equals("Available")) {
            //check  the item is available or not
            System.out.print("borrowList -- " + borrowList);
            borrowList.put(userAndItemID, 100);
            bookList.get(newItemId).lendOneItem();
            rtn = "Br0"; //User borrow book successfully
            //put in waiting list or not
        } else if (ifAvailable(newItemId).equals("Waiting List")) {
            if (waitingList.get(newItemId) != null) {
                if (waitingList.get(newItemId).contains(userId)) {
                    return "Ex7"; // already in the waitlist.
                }
            }
            rtn = "Ex4";//The book is not available now, if put you in the waiting list, YES or NOT
        } else {
            rtn = "Ex3";//no this item
        }
        System.out.print("checkAndBorrowInlocal rtn :" + rtn + "\n");
        return rtn;
    }


    public String putInWaiting(String userId, String itemId) {

        String rtnMsg = "";
        if (itemId.substring(0, 3).equals(localFlag)) {
            if (waitingList.containsKey(itemId)) {
                Queue<String> queue = waitingList.get(itemId);
                queue.add(userId);
            } else {
                Queue<String> queue = new LinkedList<String>();
                queue.add(userId);
                waitingList.put(itemId, queue);
            }
            rtnMsg = "Atw0";

        } else {
            String message = "putInWaiting," + userId + "," + itemId;
            rtnMsg = udpCommunication(itemId, message);
        }
        return rtnMsg;
    }


    public String ex_putInWaiting(String userId, String newItemId, String oldItem) {
        String rtnMsg = "";
        if (newItemId.substring(0, 3).equals(localFlag)) {
            if (waitingList.containsKey(newItemId)) {
                Queue<String> queue = waitingList.get(newItemId);
                queue.add(userId);
            } else {
                Queue<String> queue = new LinkedList<String>();
                queue.add(userId);
                waitingList.put(newItemId, queue);
            }
            returnItem(userId, oldItem);//after putting waiting list, return then old book
            rtnMsg = "AtwEx0";

        } else {
            String message = "addToWaitlistforExchagne," + userId + "," + newItemId + "," + oldItem;
            rtnMsg = udpCommunication(newItemId, message);
        }
        return rtnMsg;
    }


    public String autoLendWaitingItem(String itemId) {
        String returnInfo = "";
        if (waitingList.containsKey(itemId)) {
            boolean getNextUser = true;
            String waitingUId;
            do {
                waitingUId = waitingList.get(itemId).poll();
                System.out.println("waiting User:" + waitingUId);
                if (!waitingUId.isEmpty()) {
                    if (!waitingUId.substring(0, 3).equals(localFlag) && ifBorrow(waitingUId + "-" + itemId)) { //if waiting user from other library did not borrow a book in this library.
                        String info = "waiting User (" + waitingUId + ") has borrow a book form this library  " + localFlag;
                        System.out.println(info);
                        returnInfo = returnInfo + info + "\n";
                        continue;
                    } else {
                        getNextUser = false;
                    }
                }
            } while (getNextUser);
            System.out.println("waitingUId 闁煎浜滄慨鈺呭磹閻旇泛濮涢柨娑虫嫹" + waitingUId);

            String userAndItemID = waitingUId + "-" + itemId;

            borrowList.put(userAndItemID, 60);//automatic lend the item to the waiting user
            bookList.get(itemId).lendOneItem();

            returnInfo = "Automatically lend the item(" + itemId + ") to waiting user(" + waitingUId + ") ";

            if (waitingList.get(itemId).isEmpty()) { //if no one is waiting for this item, remove the queue
                waitingList.remove(itemId);
            }
        }
        return returnInfo;
    }


    public String test() {
        return " server connected!";
    }

    public String getAllItems() {
        return bookList.toString();
    }

    public String getAllBorrow() {
        return borrowList.toString();
    }


    public String getWaitingList() {
        return waitingList.toString();
    }


    public boolean isCorrectId(String id) {
        boolean isCorrectId = false;
        //CONU*** or CONM ***
        if ((id.startsWith(localFlag + "U") || id.startsWith(localFlag + "M")) && id.length() == 8) {
            isCorrectId = true;

        } else if (id.startsWith(localFlag) && id.length() == 7) {
            isCorrectId = true;
        }
        return isCorrectId;
    }


    public synchronized String exchangeItem(String userId, String newItemID, String oldItemID) {
        //check user whether borrow the oldItem
        String info = "";
        String returnResult = "";
        String borrowResult = "";
        try {
            //ckeck if return is availabe
            String checkResult = checkIfReturnAvailable(userId, oldItemID);
            // return is not availabe
            if (!checkResult.equals("YES")) {
                System.out.println("checkResult : ----- " + checkResult);
                return checkResult;
            } else {
                // return is availabe, then check borrow is available or not , if available then borrow directly
                borrowResult = checkAndBorrowNewItem(oldItemID, newItemID, userId);
                System.out.println("borrowResult : ----- " + borrowResult);
                String successInfo = "Br0";
                //if borrow successfully, then return the old item
                if (borrowResult.equals(successInfo)) {
                    returnResult = returnItem(userId, oldItemID);
                    System.out.print("returnResult:----" + returnResult);
                    info = "Ex0";
                } else {
                    // return fail information
                    return borrowResult;
                }
            }
        } catch (Exception e) {
        }
        return info;
    }


    public String udpCommunication(String itemId, String msg) {
        String rtnMsg = "";
        String message = fomatString(msg);
        int port = portMap.get(itemId.substring(0, 3));
        rtnMsg = udpClient(message, port);
        return rtnMsg;
    }

    public static String fomatString(String msg) {
        String message = String.format("%-200s", msg);
        return message;
    }


    enum DLMS_Port {
        DLMS_PORT;
        final int CON_PORT = 7777;
        final int MCG_PORT = 8888;
        final int MON_PORT = 9999;
    }

    public static void main(String[] args) {
        try {

        } catch (Exception e) {

        }
    }


}

