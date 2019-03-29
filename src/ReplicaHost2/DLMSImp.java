package ReplicaHost2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Logger;

import Model.Item;

import javax.swing.*;

public class DLMSImp {
    public HashMap<String, Item> map;
    public HashMap<String, List<String>> UserBorrow;
    public Map<String, Queue<String>> WaitList;
    public HashMap<String, Item> temp;
    public Logger log;

    DatagramSocket aSocket = null;
    int LocalPort;
    String library;
    public boolean bugFree = false;

    public DLMSImp(String local, int localPort,Logger log) {
        this.log = log;
        this.LocalPort = localPort;
        this.library = local;
        map = new HashMap<>();
        temp = new HashMap<>();
        UserBorrow = new HashMap<>();
        WaitList = new HashMap<>();

        /*
         * Runnable r1 = () -> { receive(1112); }; Runnable r2 = () -> { receive(2223);
         * }; Runnable r3 = () -> { receive(3334); };
         *
         * Thread thread1 = new Thread(r1); Thread thread2 = new Thread(r2); Thread
         * thread3 = new Thread(r3); thread1.start(); thread2.start(); thread3.start();
         */

        if (library.equals("CON")) {
            map.put("CON1111", new Item("CON1111", "Test for Concordia", 3));
            map.put("CON2222", new Item("CON2222", "Math", 5));
            map.put("CON3333", new Item("CON3333", "French", 1));

        } else if (library.equals("MCG")) {
            map.put("MCG1111", new Item("MCG1111", "Test for Mcgill", 8));
            map.put("MCG2222", new Item("MCG2222", "Math", 1));
            map.put("MCG3333", new Item("MCG3333", "French", 1));

        } else if (library.equals("MON")) {
            map.put("MON1111", new Item("MON1111", "Test for Mcgill", 5));
            map.put("MON2222", new Item("MON2222", "Math", 2));
            map.put("MON3333", new Item("MON3333", "French", 1));
        }

    }

    public String addItem(String managerID, String itemID, String itemName, int quantity) {
        String userwl;
        String answer = "";
        synchronized (this) {
            if (!map.containsKey(itemID)) {
                map.put(itemID, new Item(itemID, itemName, quantity));
                answer = "Ad0";              //successfully add
            } else {
                int qty = map.get(itemID).getQuantity();
                qty = qty + quantity;
                map.put(itemID, new Item(itemID, itemName, qty));
                answer = "Ad0";            //successfully add
                for (int i = 1; i <= quantity; i++) {
                    int num = map.get(itemID).getQuantity();
                    if (!WaitList.isEmpty() && WaitList.containsKey(itemID)) {
                        while (WaitList.get(itemID).size() != 0) {
                            userwl = WaitList.get(itemID).peek();
                            if (userwl.substring(0, 3).equalsIgnoreCase(itemID.substring(0, 3))) {
                                map.put(itemID, new Item(itemID, itemName, num - 1));
                                if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userwl)) {
                                    List list = UserBorrow.get(userwl);
                                    list.add(itemID);
                                    UserBorrow.put(userwl, list);

                                } else {
                                    List list = new LinkedList<String>();
                                    list.add(itemID);
                                    UserBorrow.put(userwl, list);
                                }
                                WaitList.get(itemID).poll();
                                break;
                            } else {
                                if (UserBorrow.containsKey(WaitList.get(itemID).peek())) {
                                    WaitList.get(itemID).poll();
                                } else {
                                    map.put(itemID, new Item(itemID, itemName, num - 1));
                                    List list = new LinkedList<String>();
                                    list.add(itemID);
                                    UserBorrow.put(userwl, list);
                                    WaitList.get(itemID).poll();
                                    break;
                                }
                            }
                        }

                    }
                }
                removeNullWaitlist(WaitList);
            }
            log.info("Add item: "+managerID+", "+itemID+", "+quantity+": "+answer);
            return answer;
        }
    }

    public void removeNullWaitlist(Map<String, Queue<String>> WaitList) {
        Iterator<HashMap.Entry<String, Queue<String>>> iter = WaitList.entrySet().iterator();
        while (iter.hasNext()) {
            HashMap.Entry<String, Queue<String>> entry1 = iter.next();
            String key = entry1.getKey();
            if (WaitList.get(key).size() == 0) {
                iter.remove();
            }
        }
    }

    public String removeItem(String managerID, String itemID, int quantity) {
        String answer = "";
        synchronized (this) {
            if (!map.isEmpty() && map.containsKey(itemID)) {
                String name = map.get(itemID).getItemName();
                int qty = map.get(itemID).getQuantity();
                if (quantity < 0) {
                    Iterator<HashMap.Entry<String, Item>> iter = map.entrySet().iterator();
                    while (iter.hasNext()) {
                        HashMap.Entry<String, Item> entry1 = iter.next();
                        String key = entry1.getKey();
                        if (key.equals(itemID)) {
                            iter.remove();
                        }
                    }
                    Iterator<HashMap.Entry<String, List<String>>> it = UserBorrow.entrySet().iterator();
                    while (it.hasNext()) {
                        HashMap.Entry<String, List<String>> entry = it.next();
                        String key = entry.getKey();
                        if (UserBorrow.get(key).contains(itemID)) {
                            List list = UserBorrow.get(key);
                            for (int i = 0; i < list.size(); i++) {
                                if (itemID.equals(list.get(i))) {
                                    list.remove(itemID);
                                }
                            }
                        }
                        if (UserBorrow.get(key).size() == 0) {
                            it.remove();
                        }
                    }
                    Iterator<HashMap.Entry<String, Queue<String>>> iter1 = WaitList.entrySet().iterator();
                    while (iter.hasNext()) {
                        HashMap.Entry<String, Queue<String>> entry1 = iter1.next();
                        String key3 = entry1.getKey();
                        if (key3.equals(itemID)) {
                            iter1.remove();
                        }
                    }

                    answer = "Re0";    //successfully remove
                } else if (quantity <= qty) {
                    map.put(itemID, new Item(itemID, name, qty - quantity));
                    answer = "Re1";    //successfully decrease
                } else if (quantity > qty) {
                    answer = "Re2";     //unsuccessfully remove,
                }
            } else {
                answer = "Re3";   ///item not exist
            }
            log.info("Remove item: "+managerID+", "+itemID+", "+quantity+": "+answer);
            return answer;
        }
    }

    public String listItemAvailability(String managerID) {
        String answer = "";
        synchronized (this) {
            for (String key : map.keySet()) {
                answer += key + "," + map.get(key).getItemName() + "=" + map.get(key).getQuantity() + "\n";
            }
            log.info("List item: "+managerID+": "+answer);
            return answer.trim();
        }

    }

    public String borrowItem(String userID, String itemID) {
        String answer = "";
        synchronized (this) {
            if (userID.substring(0, 3).equalsIgnoreCase(itemID.substring(0, 3))) {
                answer = borrowItemUdp(userID, itemID);
            } else {
                answer = sendToUdpServer(itemID, "Borrow item:" + "&" + userID + "&" + itemID);
            }
        }
        log.info("Borrow item: "+userID+", "+itemID+": "+answer);
        return answer;
    }

    public String borrowItemUdp(String userID, String itemID) {
        String answer = "";
        int qty;
        String name;
        if (!map.isEmpty() && map.containsKey(itemID)) {
            qty = map.get(itemID).getQuantity();
            name = map.get(itemID).getItemName();
            if (!WaitList.isEmpty() && WaitList.containsKey(itemID) && WaitList.get(itemID).contains(userID)) {
                answer = "Br5";      //already in the waitlist
            } else {
                if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID) && UserBorrow.get(userID).equals(itemID)) {
                    answer = "Br3";     //you have borrowed this item
                } else if (!userID.substring(0, 3).equals(itemID.substring(0, 3)) && !UserBorrow.isEmpty()
                        && UserBorrow.containsKey(userID)) {
                    answer = "Br4";     //you have borrowed from this library
                } else {
                    if (qty > 0) {
                        map.put(itemID, new Item(itemID, name, qty - 1));
                        if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)) {
                            List list = UserBorrow.get(userID);
                            list.add(itemID);
                            UserBorrow.put(userID, list);
                        } else {
                            List list = new LinkedList<String>();
                            list.add(itemID);
                            UserBorrow.put(userID, list);
                        }
                        answer = "Br0";    ///successfully borrow
                    } else {
                        answer = "Br2";   // item not available, add to wait list?
                    }
                }
            }
        } else {
            answer = "Br1";  //item not exist
        }
        return answer;
    }

    public String findItem(String userID, String itemName) {
        String answer = "";
        answer = findItemUdp(itemName);
        if (userID.substring(0, 3).equalsIgnoreCase("con")) {
            answer += sendMessage(2223, "Find:" + "&" + userID + "&" + itemName)
                    + sendMessage(3334, "Find:" + "&" + userID + "&" + itemName);
        } else if (userID.substring(0, 3).equalsIgnoreCase("mcg")) {
            answer += sendMessage(1112, "Find:" + "&" + userID + "&" + itemName)
                    + sendMessage(3334, "Find:" + "&" + userID + "&" + itemName);
        } else if (userID.substring(0, 3).equalsIgnoreCase("mon")) {
            answer += sendMessage(2223, "Find:" + "&" + userID + "&" + itemName)
                    + sendMessage(1112, "Find:" + "&" + userID + "&" + itemName);
        }
        log.info("Find item: "+userID+": "+answer);
        return answer.trim();
    }

    public String findItemUdp(String name) {
        String answer = "";
        for (Object key : map.keySet()) {
            Item value = (Item) map.get(key);
            if (value.getItemName().equals(name)) {
                answer += value.getItemId()+","+value.getQuantity() + "\n";
            }
        }

        return answer;
    }

    public String returnItem(String userID, String itemID) {
        String answer = "";
        synchronized (this) {
            if (userID.substring(0, 3).equalsIgnoreCase(itemID.substring(0, 3))) {
                answer = returnItemUdp(userID, itemID);
            } else {

                answer = sendToUdpServer(itemID, "Return item:" + "&" + userID + "&" + itemID);
            }
            log.info("Return item: "+userID+", "+itemID+": "+answer);
            return answer;
        }
    }

    public String returnItemUdp(String userID, String itemID) {
        String answer = "";
        int qty;
        String name;
        if (!map.isEmpty() && map.containsKey(itemID)) {
            qty = map.get(itemID).getQuantity();
            name = map.get(itemID).getItemName();
            Iterator<HashMap.Entry<String, List<String>>> it = UserBorrow.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry<String, List<String>> entry = it.next();
                String key = entry.getKey();
                if (UserBorrow.get(key).contains(itemID) && key.equals(userID)) {
                    List list = UserBorrow.get(key);
                    for (int i = 0; i < list.size(); i++) {
                        if (itemID.equals(list.get(i))) {
                            list.remove(i);
                            if (list.size() == 0) {
                                it.remove();
                            }
                            if (!WaitList.isEmpty() && WaitList.containsKey(itemID)) {
                                while (WaitList.get(itemID).size() != 0) {
                                    String userwl = WaitList.get(itemID).peek();
                                    if (userwl.substring(0, 3).equalsIgnoreCase(itemID.substring(0, 3))) {
                                        if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userwl)) {
                                            List list1 = UserBorrow.get(userwl);
                                            list1.add(itemID);
                                            UserBorrow.put(userwl, list1);
                                        } else {
                                            List list2 = new LinkedList<String>();
                                            list2.add(itemID);
                                            UserBorrow.put(userwl, list2);
                                        }
                                        WaitList.get(itemID).poll();
                                        break;
                                    } else {
                                        if (UserBorrow.containsKey(WaitList.get(itemID).peek())) {
                                            WaitList.get(itemID).poll();
                                            if (WaitList.get(itemID).size() == 0) {
                                                map.put(itemID, new Item(itemID, name, qty + 1));
                                                break;
                                            }
                                        } else {
                                            List list3 = new LinkedList<String>();
                                            list3.add(itemID);
                                            UserBorrow.put(userwl, list3);
                                            break;
                                        }
                                    }
                                }
                                removeNullWaitlist(WaitList);
                                break;
                            } else {
                                map.put(itemID, new Item(itemID, name, qty + 1));
                                break;
                            }

                        }
                    }

                    answer = "Rtn0";    //successfully return

                } else {
                    answer = "Rtn2";    //you didn't borrow it
                }
            }
        } else {
            answer = "Rtn1";    //item not exist
        }
        return answer;
    }

    public String addWaitlistUdp(String userID, String itemID) {
        String answer = "";
        if (WaitList.containsKey(itemID)) {
            if (WaitList.get(itemID).contains(userID)) {
                answer = "Atw1";   //already in the waitlist
            } else {
                Queue queue = WaitList.get(itemID);
                queue.offer(userID);
                WaitList.put(itemID, queue);
                answer = "Atw0";   //successfully add to waitlist
            }
        } else {
            Queue<String> queue = new LinkedList<String>();
            queue.offer(userID);
            WaitList.put(itemID, queue);
            answer = "Atw0";   //successfully add to waitlist
        }
        return answer;
    }

    public String addWaitList(String userID, String itemID) {
        String answer = "";
        synchronized (this) {
            if (itemID.substring(0, 3).equals(userID.substring(0, 3))) {
                answer = addWaitlistUdp(userID, itemID);
            } else {
                answer = sendToUdpServer(itemID, "Addlist:" + "&" + userID + "&" + itemID);
            }
            log.info("Add waitlist: "+userID+", "+itemID+": "+answer);
            return answer;
        }
    }

    public String checkOldItemUdp(String userID, String oldItemID) {
        String answer = "";
        if (!map.isEmpty() & map.containsKey(oldItemID)) {
            if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID) && UserBorrow.get(userID).contains(oldItemID)) {
                answer = "available";
            } else {
                answer = "Ex9";   //you didn't borrow it
            }
        } else {
            answer = "Ex2";   //old item not exist
        }
        return answer;
    }

    public String checkNewItemUdp(String userID, String newItemID, String oldItemID) {
        String answer = "";
        int qty;
        if (!map.isEmpty() & map.containsKey(newItemID)) {
            qty = map.get(newItemID).getQuantity();
            if (!WaitList.isEmpty() && WaitList.containsKey(newItemID) && WaitList.get(newItemID).contains(userID)) {
                answer = "Ex7";   //already in the waitlist
            } else if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)
                    && UserBorrow.get(userID).contains(newItemID)) {
                answer = "Ex5";     //"You have borrowed this item.";
            } else if (!userID.substring(0, 3).equals(newItemID.substring(0, 3))
                    && newItemID.substring(0, 3).equals(oldItemID.substring(0, 3)) && UserBorrow.containsKey(userID)
                    && !UserBorrow.get(userID).contains(oldItemID)) {

                //System.out.println("U------ userborrow: " + UserBorrow.get(userID));
                answer = "Ex6"; //"You have borrowed from this library.";
            } else {
                if (qty == 0) {
                    answer = "Ex4"; //"This item is unavailable, do you want to add to waiting list?";
                } else {
                    String itemname = map.get(newItemID).getItemName();
                    map.put(newItemID, new Item(newItemID, itemname, qty - 1));
                    temp.put(newItemID, new Item(newItemID, itemname, 1));
                    answer = "available";
                }
            }
        } else {
            answer = "Ex3"; //"New item does not exist.";
        }
        return answer;
    }

    public String returnNewItem(String userID, String newItemID) {
        if (!map.isEmpty() && map.containsKey(newItemID)) {
            String name = map.get(newItemID).getItemName();
            int qty = map.get(newItemID).getQuantity();
            Iterator<HashMap.Entry<String, List<String>>> it = UserBorrow.entrySet().iterator();
            while (it.hasNext()) {
                HashMap.Entry<String, List<String>> entry = it.next();
                String key = entry.getKey();
                if (UserBorrow.get(key).contains(newItemID) && key.equals(userID)) {
                    List list = UserBorrow.get(key);
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).equals(newItemID)) {
                            list.remove(i);
                            map.put(newItemID, new Item(newItemID, name, qty + 1));
                        }
                    }
                    if (list.size() == 0) {
                        it.remove();
                    }
                }
            }
        }
        return "";
    }

    public String exchangeItem(String userID, String newItemID, String oldItemID) {
        String answer = "";
        String replynew = "";
        String replyold = "";
        String replyborrow = "";
        String replyreturn = "";
        synchronized (this) {
            if (userID.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
                replyold = checkOldItemUdp(userID, oldItemID);
            } else {

                replyold = sendToUdpServer(oldItemID, "Check old item:" + "&" + userID + "&" + oldItemID);
            }
            if (replyold.equals("available")) {
                if (userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))) {
                    replynew = checkNewItemUdp(userID, newItemID, oldItemID);
                } else {
                    replynew = sendToUdpServer(newItemID,
                            "Check new item:" + "&" + userID + "&" + newItemID + "&" + oldItemID);
                }
                if (replynew.equals("available")) {
                    if (userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))) {
                        replyborrow = borrowNewItemUdp(userID, newItemID);
                    } else {
                        replyborrow = sendToUdpServer(newItemID, "Borrow new item:" + "&" + userID + "&" + newItemID);
                    }
                    if (replyborrow.equals("Successfully borrow.")) {
                        if (userID.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
                            replyreturn = returnOldItemUdp(userID, oldItemID);
                        } else {
                            replyreturn = sendToUdpServer(oldItemID,
                                    "Return old item:" + "&" + userID + "&" + oldItemID);
                        }
                        if (replyreturn.equals("Successfully return.")) {
                            answer ="Ex0"; // "Successfully exchange.";
                        } else {
                            String str;
                            System.out.println("replyreturn not equals====Successfully return."+replyborrow);
                            if (userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))) {
                                str = returnNewItem(userID, newItemID);
                            } else {
                                str = sendToUdpServer(newItemID, "Return new item:" + "&" + userID + "&" + newItemID);
                            }
                            answer = "Ex1"; //"Unsuccessfully exchange.";
                        }
                    } else {
                        return replyborrow;
                    }
                } else {
                    return replynew;
                }
            } else {
                return replyold;
            }
            log.info("Exchange item: "+userID+", newItem-"+newItemID+", oldItem-"+oldItemID+": "+answer);
            return answer;
        }
    }

    public String newExchange(String userID, String newItemID, String oldItemID) {
        String answer = "";
        String replynew = "";
        String replyold = "";
        String replyborrow = "";
        String replyreturn = "";
        synchronized (this) {
            if (userID.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
                replyold = checkOldItemUdp(userID, oldItemID);
            } else {
                replyold = sendToUdpServer(oldItemID, "Check old item:" + "&" + userID + "&" + oldItemID);
            }
            if (replyold.equals("available")) {
                if (userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))) {
                    replynew = checkNewItemUdp(userID, newItemID, oldItemID);
                } else {
                    replynew = sendToUdpServer(newItemID,
                            "Check new item:" + "&" + userID + "&" + newItemID + "&" + oldItemID);
                }
                if (replynew.equals("available")) {
                    if (userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))) {
                        replyborrow = borrowNewItemUdp(userID, newItemID);
                    } else {
                        replyborrow = sendToUdpServer(newItemID, "Borrow new item:" + "&" + userID + "&" + newItemID);
                    }
                    if (replyborrow.equals("Successfully borrow.")) {
                        if (userID.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
                            replyreturn = returnOldItemUdp(userID, oldItemID);
                        } else {
                            replyreturn = sendToUdpServer(oldItemID,
                                    "Return old item:" + "&" + userID + "&" + oldItemID);
                        }
                        if (replyreturn.equals("Successfully return.")) {
                            answer = "Ex0"; //"Successfully exchange.";
                        } else {
                            String ss;
                            System.out.println("reply return not equals====Successfully return."+replyborrow);
                            if (userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))) {
                                ss = returnNewItem(userID, newItemID);
                            } else {
                                ss = sendToUdpServer(newItemID, "Return new item:" + "&" + userID + "&" + newItemID);
                            }
                            answer = "Ex1"; //"Unsuccessfully exchange.";
                        }
                    } else {
                        return replyborrow;
                    }
                } else if (replynew.equals("Ex4")) {
                    if (userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))) {
                        replyborrow = addWaitlistUdp(userID, newItemID);
                    } else {
                        replyborrow = sendToUdpServer(newItemID, "Addlist:" + "&" + userID + "&" + newItemID);
                    }
                    if (replyborrow.equals("Successfully borrow.")
                            || replyborrow.equals("Atw0")) {
                        if (userID.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
                            replyreturn = returnOldItemUdp(userID, oldItemID);
                        } else {
                            replyreturn = sendToUdpServer(oldItemID,
                                    "Return old item:" + "&" + userID + "&" + oldItemID);
                        }
                        if (replyreturn.equals("Successfully return.")) {
                            answer = "AtwEx0";
                        }
                    } else {
                        return replyborrow;
                    }
                } else {
                    return replynew;
                }
            } else {
                return replyold;
            }
            log.info("Add to waitlist for Exchange item: "+userID+", newItem-"+newItemID+", oldItem-"+oldItemID+": "+answer);
            return answer;
        }
    }

    public String borrowNewItemUdp(String userID, String newItemID) {
        String answer = "";
        int qty;
        if (!map.isEmpty() && map.containsKey(newItemID)) {
            qty = map.get(newItemID).getQuantity();
            String itemName = map.get(newItemID).getItemName();
            if (!WaitList.isEmpty() && WaitList.containsKey(newItemID) && WaitList.get(newItemID).contains(userID)) {
                answer = "You are already in the waitlist.";
            } else {
                if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)
                        && UserBorrow.get(userID).contains(newItemID)) {
                    answer = "You have borrowed this item.";
                } else {
                    map.put(newItemID, new Item(newItemID, itemName, qty));
                    if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)) {
                        List list = UserBorrow.get(userID);
                        list.add(newItemID);
                        UserBorrow.put(userID, list);
                    } else {
                        List list = new LinkedList<String>();
                        list.add(newItemID);
                        UserBorrow.put(userID, list);
                    }
                    answer = "Successfully borrow.";
                }
            }
        } else {
            answer = "Such item does not exist.";
        }
        return answer;
    }

    public String returnOldItemUdp(String userID, String oldItemID) {
        String answer = "";
        int qty;
        String name;
        if (!map.isEmpty() && map.containsKey(oldItemID)) {
            name = map.get(oldItemID).getItemName();
            qty = map.get(oldItemID).getQuantity();
            if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)) {
                Iterator<HashMap.Entry<String, List<String>>> it = UserBorrow.entrySet().iterator();
                while (it.hasNext()) {
                    HashMap.Entry<String, List<String>> entry = it.next();
                    String key = entry.getKey();
                    if (UserBorrow.get(key).contains(oldItemID) && key.equals(userID)) {
                        List list = UserBorrow.get(key);
                        for (int i = 0; i < list.size(); i++) {
                            if (list.get(i).equals(oldItemID)) {
                                list.remove(i);
                                if (!WaitList.isEmpty() && WaitList.containsKey(oldItemID)) {
                                    while (WaitList.get(oldItemID).size() != 0) {
                                        String userwl = WaitList.get(oldItemID).peek();
                                        if (userwl.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
                                            if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userwl)) {
                                                List list1 = UserBorrow.get(userwl);
                                                list1.add(oldItemID);
                                                UserBorrow.put(userwl, list1);
                                            } else {
                                                List list2 = new LinkedList<String>();
                                                list2.add(oldItemID);
                                                UserBorrow.put(userwl, list2);
                                            }
                                            WaitList.get(oldItemID).poll();
                                            break;
                                        } else {
                                            if (UserBorrow.containsKey(WaitList.get(oldItemID).peek())) {
                                                WaitList.get(oldItemID).poll();
                                                if (WaitList.get(oldItemID).size() == 0) {
                                                    map.put(oldItemID, new Item(oldItemID, name, qty + 1));
                                                    break;
                                                }
                                            } else {
                                                List list3 = new LinkedList<String>();
                                                list3.add(oldItemID);
                                                UserBorrow.put(userwl, list3);
                                                WaitList.get(oldItemID).poll();
                                                break;
                                            }
                                        }
                                    }
                                    removeNullWaitlist(WaitList);
                                } else {
                                    map.put(oldItemID, new Item(oldItemID, name, qty + 1));
                                    break;
                                }

                            }
                        }
                        if (list.size() == 0) {
                            it.remove();
                        }
                        answer = "Successfully return.";
                    }
                }
            } else {
                answer = "You didn't borrow this item.";
            }
        } else {
            answer = "Such item does not exist.";
        }
        return answer;
    }

    public String sendToUdpServer(String itemID, String msg) {
        String reply = null;
        if (itemID.substring(0, 3).equalsIgnoreCase("con")) {
            reply = sendMessage(1112, msg);
        } else if (itemID.substring(0, 3).equalsIgnoreCase("mcg")) {
            reply = sendMessage(2223, msg);
        } else if (itemID.substring(0, 3).equalsIgnoreCase("mon")) {
            reply = sendMessage(3334, msg);
        }
        return reply;
    }

    public void receive(int portNumber) {

        try {
            aSocket = new DatagramSocket(portNumber);
            byte[] buffer = new byte[1000];
            if (portNumber == 1112) {
                System.out.println("Concordia UdpServer Started............");
            } else if (portNumber == 2223) {
                System.out.println("Mcgill UdpServer Started............");
            } else if (portNumber == 3334) {
                System.out.println("Montreal UdpServer Started............");
            }
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String str = new String(request.getData(), 0, request.getLength());
                String param[] = str.split("&");
                String answer = "";

                if (param[0].equals("Find:")) {
                    answer = findItemUdp(param[2]);
                } else if (param[0].equals("Borrow item:")) {
                    answer = borrowItemUdp(param[1], param[2]);
                } else if (param[0].equals("Addlist:")) {
                    answer = addWaitlistUdp(param[1], param[2]);
                } else if (param[0].equals("Return item:")) {
                    answer = returnItemUdp(param[1], param[2]);
                } else if (param[0].equals("Check new item:")) {
                    answer = checkNewItemUdp(param[1], param[2], param[3]);
                } else if (param[0].equals("Check old item:")) {
                    answer = checkOldItemUdp(param[1], param[2]);
                } else if (param[0].equals("Borrow new item:")) {
                    answer = borrowNewItemUdp(param[1], param[2]);
                } else if (param[0].equals("Return old item:")) {
                    answer = returnOldItemUdp(param[1], param[2]);
                } else if (param[0].equals("Return new item:")) {
                    answer = returnNewItem(param[1], param[2]);
                }

                DatagramPacket reply = new DatagramPacket(answer.getBytes(), answer.length(), request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) {
                aSocket.close();
            }
        }
    }

    public String sendMessage(int portNumber, String str) {
        DatagramSocket aSocket = null;
        try {
            System.out.println("Client Started........");
            aSocket = new DatagramSocket();
            byte[] message = str.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");

            DatagramPacket request = new DatagramPacket(message, str.length(), aHost, portNumber);
            aSocket.send(request);

            byte[] receiveMsg = new byte[1024];
            DatagramPacket reply = new DatagramPacket(receiveMsg, receiveMsg.length);
            aSocket.receive(reply);
            String info = new String(reply.getData(), 0, reply.getLength());

            System.out.println("Reply message sent from the client is : " + info);
            return info;

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) {
                // aSocket.close();
            }
        }
        return "";
    }

}
