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

import Model.Item;
import Model.ResultMsg;

public class DLMSImpl  {

	HashMap<String, Item> map;
	HashMap<String, List<String>> UserBorrow;
	Map<String, Queue<String>> WaitList;
	HashMap<String, Item> temp;

	int portNum;
	String answer;

	public DLMSImpl() {
		
		super();
	}




	public String addItem(String managerID, String itemID, String itemName, int quantity) {
		String userwl;
		synchronized (this) {
			if (!map.containsKey(itemID)) {
				map.put(itemID, new Item(itemID, itemName, quantity));
				answer = ResultMsg.Ad0;
			} else {
				int qty = map.get(itemID).getQuantity();
				qty = qty + quantity;
				map.put(itemID, new Item(itemID, itemName, qty));
				answer = ResultMsg.Ad0;

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
								answer += ResultMsg.Ad2;
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
									// System.out.println("UserBorrow(after poll):" + UserBorrow);
									answer += ResultMsg.Ad2;
									break;
								}
							}
						}
						Iterator<HashMap.Entry<String, Queue<String>>> iter = WaitList.entrySet().iterator();
						while (iter.hasNext()) {
							HashMap.Entry<String, Queue<String>> entry1 = iter.next();
							String key = entry1.getKey();
							if (WaitList.get(key).size() == 0) {
								iter.remove();
							}
						}
					}
				}
			}
			// log.info(managerID+ "Add Item:" + itemID + ": " + answer);
			return answer;
		}

	}

	public String removeItem(String managerID, String itemID, int quantity) {
		synchronized (this) {
			if (map.get(itemID) != null) {
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
					answer = ResultMsg.Re0;
				} else if (quantity <= qty) {
					map.put(itemID, new Item(itemID, name, qty - quantity));
					answer = ResultMsg.Re1;
				} else if (quantity > qty) {
					answer = ResultMsg.Re4;
				}
			} else {
				answer = ResultMsg.Re3;
			}
			// log.info(managerID+ "Remove Item:" + itemID + ": " + answer);
			return answer;
		}
	}

	public String listItemAvailability(String managerID) {
		synchronized (this) {
			if (managerID.substring(0, 3).equalsIgnoreCase(managerID.substring(0, 3))) {
				if (!map.isEmpty()) {

					for (String key : map.keySet())
						answer = map.get(key).getItemId() + " " + map.get(key).getItemName() + " "
								+ map.get(key).getQuantity() + ";";

				}
			} else {
				if (managerID.substring(0, 3).equalsIgnoreCase("con")) {
					answer = sendMessage(3334, "List item:" + "&" + managerID)
							+ sendMessage(2223, "List item:" + "&" + managerID);
				} else if (managerID.substring(0, 3).equalsIgnoreCase("mcg")) {
					answer = sendMessage(1112, "List item:" + "&" + managerID)
							+ sendMessage(3334, "List item:" + "&" + managerID);
				} else if (managerID.substring(0, 3).equalsIgnoreCase("mon")) {
					answer = sendMessage(1112, "List item:" + "&" + managerID)
							+ sendMessage(2223, "List item:" + "&" + managerID);
				}
			}
			// log.info(managerID+ " List Item: " + itemID + " : " + answer);
			return answer;
		}
	}

	public String borrowItem(String userID, String itemID) {
		synchronized (this) {
			if (userID.substring(0, 3).equalsIgnoreCase(itemID.substring(0, 3))) {
				if (!map.isEmpty() && map.containsKey(itemID)) {
					int qty = map.get(itemID).getQuantity();
					if (!WaitList.isEmpty() && WaitList.containsKey(itemID) && WaitList.get(itemID).contains(userID)) {
						answer = ResultMsg.Br6;
					} else {
						if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)
								&& UserBorrow.get(userID).contains(itemID)) {
							answer = ResultMsg.Br4;
						} else {
							if (qty > 0) {
								String itemName = map.get(itemID).getItemName();
								map.put(itemID, new Item(itemID, itemName, qty - 1));
								if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)) {
									List list = UserBorrow.get(userID);
									list.add(itemID);
									UserBorrow.put(userID, list);
								} else {
									List list = new LinkedList<String>();
									list.add(itemID);
									UserBorrow.put(userID, list);
								}
								answer = ResultMsg.Br0;
							} else {
								answer = ResultMsg.Br3;
							}
						}
					}
				} else {
					answer = ResultMsg.Br2;
				}
			} else {
				if (itemID.substring(0, 3).equalsIgnoreCase("con")) {
					answer = sendMessage(1112, "Borrow item:" + "&" + userID + "&" + itemID);
				} else if (itemID.substring(0, 3).equalsIgnoreCase("mcg")) {
					answer = sendMessage(2223, "Borrow item:" + "&" + userID + "&" + itemID);
				} else if (itemID.substring(0, 3).equalsIgnoreCase("mon")) {
					answer = sendMessage(3334, "Borrow item:" + "&" + userID + "&" + itemID);
				}
			}
		}

		// log.info(userID+" Borrow Item: "+itemID +" : " + answer);
		return answer;
	}

	public String findItem(String userID, String itemName) {
		if (!map.isEmpty()) {
			for (Object key : map.keySet()) {
				Item value = (Item) map.get(key);
				if (value.getItemName().equals(itemName)) {
					answer += value + ";";
				}
			}
		}
		if (userID.substring(0, 3).equalsIgnoreCase("con")) {
			answer = sendMessage(2223, "Find:" + "&" + userID + "&" + itemName)
					+ sendMessage(3334, "Find:" + "&" + userID + "&" + itemName);
		} else if (userID.substring(0, 3).equalsIgnoreCase("mcg")) {
			answer = sendMessage(1112, "Find:" + "&" + userID + "&" + itemName)
					+ sendMessage(3334, "Find:" + "&" + userID + "&" + itemName);
		} else if (userID.substring(0, 3).equalsIgnoreCase("mon")) {
			answer = sendMessage(2223, "Find:" + "&" + userID + "&" + itemName)
					+ sendMessage(1112, "Find:" + "&" + userID + "&" + itemName);
		}
		// log.info(userID+" Find Item: "+itemName +" : " + answer);
		return answer;
	}

	public String returnItem(String userID, String itemID) {
		synchronized (this) {
			if (userID.substring(0, 3).equalsIgnoreCase(itemID.substring(0, 3))) {
				System.out.println(UserBorrow);
				if (!map.isEmpty() & map.containsKey(itemID)) {
					String itemName = map.get(itemID).getItemName();
					int qty = map.get(itemID).getQuantity();
					Iterator<HashMap.Entry<String, List<String>>> it = UserBorrow.entrySet().iterator();
					while (it.hasNext()) {
						HashMap.Entry<String, List<String>> entry = it.next();
						String key = entry.getKey();
						if (UserBorrow.get(key).contains(itemID) && key.equals(userID)) {
							List list = UserBorrow.get(key);
							for (int i = 0; i < list.size(); i++) {
								if (itemID.equals(list.get(i))) {
									list.remove(itemID);
								}
							}
							if (UserBorrow.get(key).size() == 0) {
								it.remove();
							}
							answer = ResultMsg.Rtn0;
							if (!WaitList.isEmpty() && WaitList.containsKey(itemID)) {
								while (WaitList.get(itemID).size() != 0) {
									String userwl = WaitList.get(itemID).peek();
									if (userwl.substring(0, 3).equalsIgnoreCase(itemID.substring(0, 3))) {
										if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userwl)) {
											List list1 = UserBorrow.get(userID);
											list1.add(itemID);
											UserBorrow.put(userwl, list1);

										} else {
											List list2 = new LinkedList<String>();
											list2.add(itemID);
											UserBorrow.put(userwl, list2);
										}
										WaitList.get(itemID).poll();
										answer += ResultMsg.Rtn3;
										// System.out.println(answer);
										break;
									} else {
										if (UserBorrow.containsKey(WaitList.get(itemID).peek())) {
											WaitList.get(itemID).poll();
											if (WaitList.get(itemID).size() == 0) {
												map.put(itemID, new Item(itemID, itemName, qty + 1));
												break;
											}
										} else {
											List list4 = new LinkedList<String>();
											list4.add(itemID);
											UserBorrow.put(userwl, list4);
											WaitList.get(itemID).poll();
											answer += ResultMsg.Rtn3;
											// System.out.println(answer);
											break;
										}
									}
								}
								Iterator<HashMap.Entry<String, Queue<String>>> iter = WaitList.entrySet().iterator();
								while (iter.hasNext()) {
									HashMap.Entry<String, Queue<String>> entry1 = iter.next();
									String key1 = entry1.getKey();
									if (WaitList.get(key1).size() == 0) {
										iter.remove();
									}
								}
								break;
							} else {
								map.put(itemID, new Item(itemID, itemName, qty + 1));
								break;
							}
						} else {
							answer = ResultMsg.Rtn4;
						}
					}

				} else {
					answer = ResultMsg.Rtn2;
				}
			} else {
				if (itemID.substring(0, 3).equalsIgnoreCase("con")) {
					answer = sendMessage(1112, "Return item:" + "&" + userID + "&" + itemID);
				} else if (itemID.substring(0, 3).equalsIgnoreCase("mcg")) {
					answer = sendMessage(2223, "Return item:" + "&" + userID + "&" + itemID);
				} else if (itemID.substring(0, 3).equalsIgnoreCase("mon")) {
					answer = sendMessage(3334, "Return item:" + "&" + userID + "&" + itemID);
				}
			}
            //log.info(userID+" Return Item: "+ itemID+" : "+answer);
			return answer;
		}
	}

	public String addWaitList(String itemID, String userID) {
		synchronized (this) {
			if (itemID.substring(0, 3).equals(userID.substring(0, 3))) {
				if (!WaitList.isEmpty() && WaitList.containsKey(itemID)) {
					Queue queue = WaitList.get(itemID);
					queue.offer(userID);
					WaitList.put(itemID, queue);
					//System.out.println("WaitList:" + WaitList);
				} else {
					Queue<String> queue = new LinkedList<String>();
					queue.offer(userID);
					WaitList.put(itemID, queue);
					//System.out.println("WaitList : " + WaitList);
				}
				answer = "Add to the waiting list.";
			} else {
				if (itemID.substring(0, 3).equalsIgnoreCase("con")) {
					answer = sendMessage(1112, "Addlist:" + "&" + userID + "&" + itemID);
				} else if (itemID.substring(0, 3).equalsIgnoreCase("mcg")) {
					answer = sendMessage(2223, "Addlist:" + "&" + userID + "&" + itemID);
				} else if (itemID.substring(0, 3).equalsIgnoreCase("mon")) {
					answer = sendMessage(3334, "Addlist:" + "&" + userID + "&" + itemID);
				}
			}
			//log.info(userID+" Add to waitlist: "+ itemID+" : "+answer);
			return answer;
		}
	}

	public String exchangeItem(String userID, String newItemID, String oldItemID) {
		String replynew = "";
		String replyold = "";
		String replyborrow = "";
		String replyreturn = "";
		String itemName;
		int qty;
		synchronized (this) {
			if (!newItemID.equals(oldItemID)) {
				if (userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))) {
					if (!map.isEmpty() & map.containsKey(newItemID)) {

						qty = map.get(newItemID).getQuantity();
						if (!WaitList.isEmpty() && WaitList.containsKey(newItemID)
								&& WaitList.get(newItemID).contains(userID)) {
							replynew = "You are already in the waitlist.";
						} else if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)
								&& UserBorrow.get(userID).contains(newItemID)) {
							replynew = "You have borrowed this item.";
						} else {
							if (qty == 0) {
								replynew = "Add to the waiting queue?";
							} else {
								itemName = map.get(newItemID).getItemName();
								map.put(newItemID, new Item(newItemID, itemName, qty - 1));
								replynew = "available";
								temp.put(newItemID, new Item(newItemID, itemName, 1));
							}
						}

					} else {
						replynew = "Such item does not exist.";
					}
				} else {
					if (newItemID.substring(0, 3).equalsIgnoreCase("con")) {
						replynew = sendMessage(1112,
								"Check new item:" + "&" + userID + "&" + newItemID + "&" + oldItemID);
					} else if (newItemID.substring(0, 3).equalsIgnoreCase("mcg")) {
						replynew = sendMessage(2223,
								"Check new item:" + "&" + userID + "&" + newItemID + "&" + oldItemID);
					} else if (newItemID.substring(0, 3).equalsIgnoreCase("mon")) {
						replynew = sendMessage(3334,
								"Check new item:" + "&" + userID + "&" + newItemID + "&" + oldItemID);
					}
				}
				if (userID.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
					if (!map.isEmpty() & map.containsKey(oldItemID)) {
						if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)
								&& UserBorrow.get(userID).contains(oldItemID)) {
							replyold = "available";
						} else {
							replyold = "You don't borrow this item.";
						}
					} else {
						replyold = "Such item does not exist.";
					}
				} else {
					if (oldItemID.substring(0, 3).equalsIgnoreCase("con")) {
						replyold = sendMessage(1112, "Check old item:" + "&" + userID + "&" + oldItemID);
					} else if (oldItemID.substring(0, 3).equalsIgnoreCase("mcg")) {
						replyold = sendMessage(2223, "Check old item:" + "&" + userID + "&" + oldItemID);
					} else if (oldItemID.substring(0, 3).equalsIgnoreCase("mon")) {
						replyold = sendMessage(3334, "Check old item:" + "&" + userID + "&" + oldItemID);
					}
				}
				if (replynew.equals("available") && replyold.equals("available")) {
					if (userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))) {
						if (!map.isEmpty() && map.containsKey(newItemID)) {
							qty = map.get(newItemID).getQuantity();
							if (!WaitList.isEmpty() && WaitList.containsKey(newItemID)
									&& WaitList.get(newItemID).contains(userID)) {
								replyborrow = "You are already in the waitlist.";
							} else {
								if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)
										&& UserBorrow.get(userID).contains(newItemID)) {
									replyborrow = "You have borrowed this item. ";
								} else {
									itemName = map.get(newItemID).getItemName();
									if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)) {
										List list = UserBorrow.get(userID);
										list.add(newItemID);
										UserBorrow.put(userID, list);

									} else {
										List list = new LinkedList<String>();
										list.add(newItemID);
										UserBorrow.put(userID, list);
									}
									replyborrow = " Successfully borrow";
								}
							}
						} else {
							replyborrow = "Such item does not exist or library is empty.";
						}

					} else {
						if (newItemID.substring(0, 3).equalsIgnoreCase("con")) {
							replyborrow = sendMessage(1112, "Borrow new item:" + "&" + userID + "&" + newItemID);
						} else if (newItemID.substring(0, 3).equalsIgnoreCase("mcg")) {
							replyborrow = sendMessage(2223, "Borrow new item:" + "&" + userID + "&" + newItemID);
						} else if (newItemID.substring(0, 3).equalsIgnoreCase("mon")) {
							replyborrow = sendMessage(3334, "Borrow new item:" + "&" + userID + "&" + newItemID);
						}
					}
					if (userID.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
						if (!map.isEmpty() & map.containsKey(oldItemID)) {
							itemName = map.get(oldItemID).getItemName();
							qty = map.get(oldItemID).getQuantity();
							Iterator<HashMap.Entry<String, List<String>>> it = UserBorrow.entrySet().iterator();
							while (it.hasNext()) {
								HashMap.Entry<String, List<String>> entry = it.next();
								String key = entry.getKey();
								if (UserBorrow.get(key).contains(oldItemID) && key.equals(userID)) {
									List list = UserBorrow.get(key);
									for (int i = 0; i < list.size(); i++) {
										if (oldItemID.equals(list.get(i))) {
											list.remove(oldItemID);
										}
									}
									if (UserBorrow.get(key).size() == 0) {
										it.remove();
									}
									replyreturn = "Successfully Return!";
									if (!WaitList.isEmpty() && WaitList.containsKey(oldItemID)) {
										while (WaitList.get(oldItemID).size() != 0) {
											String userwl = WaitList.get(oldItemID).peek();
											if (userwl.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
												if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userwl)) {
													List list1 = UserBorrow.get(userID);
													list1.add(oldItemID);
													UserBorrow.put(userwl, list1);
												} else {
													List list2 = new LinkedList<String>();
													list2.add(oldItemID);
													UserBorrow.put(userwl, list2);
												}
												WaitList.get(oldItemID).poll();
												replyreturn += " Automatically lend item:" + oldItemID
														+ "to the user of waitlist:" + userwl + " ";
												System.out.println(replyreturn);
												break;
											} else {
												if (UserBorrow.containsKey(WaitList.get(oldItemID).peek())) {
													WaitList.get(oldItemID).poll();
													if (WaitList.get(oldItemID).size() == 0) {
														map.put(oldItemID, new Item(oldItemID, itemName, qty + 1));
														break;
													}
												} else {
													List list4 = new LinkedList<String>();
													list4.add(oldItemID);
													UserBorrow.put(userwl, list4);
													WaitList.get(oldItemID).poll();
													replyreturn += " Automatically lend item:" + oldItemID
															+ "to the user of waitlist:" + userwl + " ";
													System.out.println(replyreturn);
													break;
												}
											}
										}
										Iterator<HashMap.Entry<String, Queue<String>>> iter = WaitList.entrySet()
												.iterator();
										while (iter.hasNext()) {
											HashMap.Entry<String, Queue<String>> entry1 = iter.next();
											String key1 = entry1.getKey();
											if (WaitList.get(key1).size() == 0) {
												iter.remove();
											}
										}
										break;
									} else {
										map.put(oldItemID, new Item(oldItemID, itemName, qty + 1));
										break;
									}
								} else {
									replyreturn = " You don't borrow this item. ";
								}
							}
						} else {
							replyreturn = " Such item doesn't exist or library is empty. ";
						}
					} else {
						if (oldItemID.substring(0, 3).equalsIgnoreCase("con")) {
							replyreturn = sendMessage(1112, "Return old item:" + "&" + userID + "&" + oldItemID);
						} else if (oldItemID.substring(0, 3).equalsIgnoreCase("mcg")) {
							replyreturn = sendMessage(2223, "Return old item:" + "&" + userID + "&" + oldItemID);
						} else if (oldItemID.substring(0, 3).equalsIgnoreCase("mon")) {
							replyreturn = sendMessage(3334, "Return old item:" + "&" + userID + "&" + oldItemID);
						}
					}
					if (replyreturn.contains("Successfully") && replyborrow.contains("Successfully")) {
						answer = "Exchange successfully.";
					} else if (replyreturn.contains("Successfully") && !replyborrow.contains("Successfully")) {
						if (!temp.isEmpty() & temp.containsKey(newItemID)) {
							itemName = temp.get(newItemID).getItemName();
							if (!map.isEmpty() & map.containsKey(newItemID)) {
								qty = map.get(newItemID).getQuantity();
								map.put(newItemID, new Item(newItemID, itemName, qty + 1));
							}
						}
						answer = "Exchange unsuccessfully.";
					} else if (!replyreturn.contains("Successfully") && replyborrow.contains("Successfully")) {
						if (!temp.isEmpty() & temp.containsKey(newItemID)) {
							itemName = temp.get(newItemID).getItemName();
							if (!map.isEmpty() & map.containsKey(newItemID)) {
								qty = map.get(newItemID).getQuantity();
								map.put(newItemID, new Item(newItemID, itemName, qty + 1));
							}
						}
						answer = "Exchange unsuccessfully.";
					}
				} else if (replynew.equals("Add to the waiting queue?") && replyold.equals("available")) {
					answer = "Add to the waiting queue?";
				} else if (replynew.equals("available") && !replyold.equals("available")
						&& !replynew.equals("Add to the waiting queue?")) {
					if (!temp.isEmpty() & temp.containsKey(newItemID)) {
						itemName = temp.get(newItemID).getItemName();
						if (!map.isEmpty() & map.containsKey(newItemID)) {
							qty = map.get(newItemID).getQuantity();
							map.put(newItemID, new Item(newItemID, itemName, qty + 1));
						}
					}
					answer = replyold;
				} else if (replyold.equals("available") && !replynew.equals("available")
						&& !replynew.equals("Add to the waiting queue?")) {
					answer = replynew;
				}
			} else {
				answer = "You can't exchange the same item.";
			}
			return answer;
		}
	}

	public String newExchange(String userID, String newItemID, String oldItemID) {
		String replynew = "";
		String replyold = "";
		String replyborrow = "";
		String replyreturn = "";
		String itemName;
		int qty;
		synchronized (this) {

			if (userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))) {
				if (!map.isEmpty() & map.containsKey(newItemID)) {
					qty = map.get(newItemID).getQuantity();
					if (!WaitList.isEmpty() && WaitList.containsKey(newItemID)
							&& WaitList.get(newItemID).contains(userID)) {
						replynew = "You are already in the waitlist.";
					} else if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)
							&& UserBorrow.get(userID).contains(newItemID)) {
						replynew = "You have borrowed this item.";
					} else {
						if (qty == 0) {
							replynew = "Add to the waiting queue.";
						} else {
							replynew = "available";
						}
					}
				} else {
					replynew = "Such item does not exist.";
				}
			} else {
				if (newItemID.substring(0, 3).equalsIgnoreCase("con")) {
					replynew = sendMessage(1112, "Check new item:" + "&" + userID + "&" + newItemID + "&" + oldItemID);
				} else if (newItemID.substring(0, 3).equalsIgnoreCase("mcg")) {
					replynew = sendMessage(2223, "Check new item:" + "&" + userID + "&" + newItemID + "&" + oldItemID);
				} else if (newItemID.substring(0, 3).equalsIgnoreCase("mon")) {
					replynew = sendMessage(3334, "Check new item:" + "&" + userID + "&" + newItemID + "&" + oldItemID);
				}
			}
			if (userID.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
				if (!map.isEmpty() & map.containsKey(oldItemID)) {
					if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)
							&& UserBorrow.get(userID).contains(oldItemID)) {
						replyold = "available";
					} else {
						replyold = "You don't borrow this item.";
					}
				} else {
					replyold = "Such item does not exist.";
				}
			} else {
				if (oldItemID.substring(0, 3).equalsIgnoreCase("con")) {
					replyold = sendMessage(1112, "Check old item:" + "&" + userID + "&" + oldItemID);
				} else if (oldItemID.substring(0, 3).equalsIgnoreCase("mcg")) {
					replyold = sendMessage(2223, "Check old item:" + "&" + userID + "&" + oldItemID);
				} else if (oldItemID.substring(0, 3).equalsIgnoreCase("mon")) {
					replyold = sendMessage(3334, "Check old item:" + "&" + userID + "&" + oldItemID);
				}
			}
			if (replynew.equals("available") && replyold.equals("available")) {
				if (userID.substring(0, 3).equalsIgnoreCase(newItemID.substring(0, 3))) {
					qty = map.get(newItemID).getQuantity();
					itemName = map.get(newItemID).getItemName();
					map.put(newItemID, new Item(newItemID, itemName, qty - 1));
					if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userID)) {
						List list = UserBorrow.get(userID);
						list.add(newItemID);
						UserBorrow.put(userID, list);

					} else {
						List list = new LinkedList<String>();
						list.add(newItemID);
						UserBorrow.put(userID, list);
					}
					replyborrow = " Successfully borrow. ";
				} else {
					if (newItemID.substring(0, 3).equalsIgnoreCase("con")) {
						replyborrow = sendMessage(1112, "Borrow new item:" + "&" + userID + "&" + newItemID);
					} else if (newItemID.substring(0, 3).equalsIgnoreCase("mcg")) {
						replyborrow = sendMessage(2223, "Borrow new item:" + "&" + userID + "&" + newItemID);
					} else if (newItemID.substring(0, 3).equalsIgnoreCase("mon")) {
						replyborrow = sendMessage(3334, "Borrow new item:" + "&" + userID + "&" + newItemID);
					}
				}
				if (userID.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
					itemName = map.get(oldItemID).getItemName();
					qty = map.get(oldItemID).getQuantity();
					Iterator<HashMap.Entry<String, List<String>>> it = UserBorrow.entrySet().iterator();
					while (it.hasNext()) {
						HashMap.Entry<String, List<String>> entry = it.next();
						String key = entry.getKey();
						if (UserBorrow.get(key).contains(oldItemID) && key.equals(userID)) {
							List list = UserBorrow.get(key);
							for (int i = 0; i < list.size(); i++) {
								if (oldItemID.equals(list.get(i))) {
									list.remove(oldItemID);
								}
							}
							if (UserBorrow.get(key).size() == 0) {
								it.remove();
							}
							replyreturn = "Successfully Return. ";
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
										replyreturn += " Automatically lend item:" + oldItemID
												+ "to the user of waitlist:" + userwl + " ";
										System.out.println(replyreturn);
										break;
									} else {
										if (UserBorrow.containsKey(WaitList.get(oldItemID).peek())) {
											WaitList.get(oldItemID).poll();
											if (WaitList.get(oldItemID).size() == 0) {
												map.put(oldItemID, new Item(oldItemID, itemName, qty + 1));
												break;
											}
										} else {
											List list4 = new LinkedList<String>();
											list4.add(oldItemID);
											UserBorrow.put(userwl, list4);
											WaitList.get(oldItemID).poll();
											replyreturn += " Automatically lend item:" + oldItemID
													+ "to the user of waitlist:" + userwl + " ";
											System.out.println(replyreturn);
											break;
										}
									}
								}
								Iterator<HashMap.Entry<String, Queue<String>>> iter = WaitList.entrySet().iterator();
								while (iter.hasNext()) {
									HashMap.Entry<String, Queue<String>> entry1 = iter.next();
									String key1 = entry1.getKey();
									if (WaitList.get(key1).size() == 0) {
										iter.remove();
									}
								}
							} else {
								map.put(oldItemID, new Item(oldItemID, itemName, qty + 1));
							}

						}
					}

				} else {
					if (oldItemID.substring(0, 3).equalsIgnoreCase("con")) {
						replyreturn = sendMessage(1112, "Return old item:" + "&" + userID + "&" + oldItemID);
					} else if (oldItemID.substring(0, 3).equalsIgnoreCase("mcg")) {
						replyreturn = sendMessage(2223, "Return old item:" + "&" + userID + "&" + oldItemID);
					} else if (oldItemID.substring(0, 3).equalsIgnoreCase("mon")) {
						replyreturn = sendMessage(3334, "Return old item:" + "&" + userID + "&" + oldItemID);
					}
				}
				if (replyreturn.contains("Successfully") && replyborrow.contains("Successfully")) {
					answer = "Exchange successfully.";
				} else {
					answer = "Exchange unsuccessfully.";
				}
			} else if (replynew.contains("Add to the waiting queue") && replyold.equals("available")) {
				if (newItemID.substring(0, 3).equals(userID.substring(0, 3))) {
					if (!WaitList.isEmpty() && WaitList.containsKey(newItemID)) {
						Queue queue = WaitList.get(newItemID);
						queue.offer(userID);
						WaitList.put(newItemID, queue);
					} else {
						Queue<String> queue = new LinkedList<String>();
						queue.offer(userID);
						WaitList.put(newItemID, queue);
					}
					replyborrow = "Add to the wait list.";
				} else {
					if (newItemID.substring(0, 3).equalsIgnoreCase("con")) {
						replyborrow = sendMessage(1112, "Addlist:" + "&" + userID + "&" + newItemID);
					} else if (newItemID.substring(0, 3).equalsIgnoreCase("mcg")) {
						replyborrow = sendMessage(2223, "Addlist:" + "&" + userID + "&" + newItemID);
					} else if (newItemID.substring(0, 3).equalsIgnoreCase("mon")) {
						replyborrow = sendMessage(3334, "Addlist:" + "&" + userID + "&" + newItemID);
					}
				}
				if (userID.substring(0, 3).equalsIgnoreCase(oldItemID.substring(0, 3))) {
					itemName = map.get(oldItemID).getItemName();
					qty = map.get(oldItemID).getQuantity();
					Iterator<HashMap.Entry<String, List<String>>> it = UserBorrow.entrySet().iterator();
					while (it.hasNext()) {
						HashMap.Entry<String, List<String>> entry = it.next();
						String key = entry.getKey();
						if (UserBorrow.get(key).contains(oldItemID) && key.equals(userID)) {
							List list = UserBorrow.get(key);
							for (int i = 0; i < list.size(); i++) {
								if (oldItemID.equals(list.get(i))) {
									list.remove(oldItemID);
								}
							}
							if (UserBorrow.get(key).size() == 0) {
								it.remove();
							}
							replyreturn = "Successfully Return. ";
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
										replyreturn += " Automatically lend item:" + oldItemID
												+ "to the user of waitlist:" + userwl + " ";
										break;
									} else {
										if (UserBorrow.containsKey(WaitList.get(oldItemID).peek())) {
											WaitList.get(oldItemID).poll();
											if (WaitList.get(oldItemID).size() == 0) {
												map.put(oldItemID, new Item(oldItemID, itemName, qty + 1));
												break;
											}
										} else {
											List list4 = new LinkedList<String>();
											list4.add(oldItemID);
											UserBorrow.put(userwl, list4);
											WaitList.get(oldItemID).poll();
											replyreturn += " Automatically lend item:" + oldItemID
													+ "to the user of waitlist:" + userwl + " ";
											break;
										}
									}
								}
								Iterator<HashMap.Entry<String, Queue<String>>> iter = WaitList.entrySet().iterator();
								while (iter.hasNext()) {
									HashMap.Entry<String, Queue<String>> entry1 = iter.next();
									String key1 = entry1.getKey();
									if (WaitList.get(key1).size() == 0) {
										iter.remove();
									}
								}
								break;
							} else {
								map.put(oldItemID, new Item(oldItemID, itemName, qty + 1));
								break;
							}
						}
					}
				} else {
					if (oldItemID.substring(0, 3).equalsIgnoreCase("con")) {
						replyreturn = sendMessage(1112, "Return old item:" + "&" + userID + "&" + oldItemID);
					} else if (oldItemID.substring(0, 3).equalsIgnoreCase("mcg")) {
						replyreturn = sendMessage(2223, "Return old item:" + "&" + userID + "&" + oldItemID);
					} else if (oldItemID.substring(0, 3).equalsIgnoreCase("mon")) {
						replyreturn = sendMessage(3334, "Return old item:" + "&" + userID + "&" + oldItemID);
					}
				}
				if (replyreturn.contains("Successfully") && replyborrow.contains("Add")) {
					answer = "Exchange successfully.";
				} else {
					answer = "Exchange unsuccessfully.";
				}
			} else if (replynew.equals("available") && !replyold.equals("available")
					&& !replynew.equals("Add to the waiting queue?")) {
				answer = replyold;
			} else if (replyold.equals("available") && !replynew.equals("available")
					&& !replynew.equals("Add to the waiting queue?")) {
				answer = replynew;
			}

			return answer;
		}
	}

	public String listItemUdp(String str) {
		// String[] values = str.split("&");
		String answer = null;
		if (!map.isEmpty()) {
			for (String key : map.keySet())
				answer = map.get(key).getItemId() + " " + map.get(key).getItemName() + " " + map.get(key).getQuantity()
						+ ";";
		}
		return answer;
	}

	public String findItemUdp(String str) {
		String[] values = str.split("&");
		String answer = null;
		String name = values[1];
		if (!map.isEmpty()) {
			for (Object key : map.keySet()) {
				Item value = (Item) map.get(key);
				if (value.getItemName().equals(name)) {
					answer += value + ";";
				}
			}
		} 
		return answer;
	}

	public String borrowItemUdp(String str) {
		String[] values = str.split("&");
		String answer = null;
		String userid = values[1];
		String itemid = values[2];
		int qty;
		String name;
		if (!map.isEmpty() && map.containsKey(itemid)) {
			qty = map.get(itemid).getQuantity();
			name = map.get(itemid).getItemName();
			if (!WaitList.isEmpty() && WaitList.containsKey(itemid) && WaitList.get(itemid).contains(userid)) {
				answer = ResultMsg.Br6;
			} else {
				if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userid) && UserBorrow.get(userid).equals(itemid)) {
					answer = ResultMsg.Br4;
				} else if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userid)) {
					answer = ResultMsg.Br5;
				} else {
					if (qty > 0) {
						map.put(itemid, new Item(itemid, name, qty - 1));
						if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userid)) {
							List list = UserBorrow.get(userid);
							list.add(itemid);
							UserBorrow.put(userid, list);
						} else {
							List list = new LinkedList<String>();
							list.add(itemid);
							UserBorrow.put(userid, list);
						}
						answer = ResultMsg.Br0;
					} else {
						answer = ResultMsg.Br3;
					}
				}
			}
		} else {
			answer = ResultMsg.Br2;
		}
		return answer;
	}

	public String returnItemUdp(String str) {
		String[] values = str.split("&");
		String answer = null;
		String userid = values[1];
		String itemid = values[2];
		int qty;
		String name;
		if (!map.isEmpty() && map.containsKey(itemid)) {
			qty = map.get(itemid).getQuantity();
			name = map.get(itemid).getItemName();
			//if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userid)) {
				Iterator<HashMap.Entry<String, List<String>>> it = UserBorrow.entrySet().iterator();
				while (it.hasNext()) {
					HashMap.Entry<String, List<String>> entry = it.next();
					String key = entry.getKey();
					if (UserBorrow.get(key).contains(itemid) && key.equals(userid)) {
						List list = UserBorrow.get(key);
						for (int i = 0; i < list.size(); i++) {
							if (itemid.equals(list.get(i))) {
								list.remove(itemid);
							}
						}
						if (list.size() == 0) {
							it.remove();
						}
						answer = ResultMsg.Rtn0;
						if (!WaitList.isEmpty() && WaitList.containsKey(itemid)) {
							while (WaitList.get(itemid).size() != 0) {
								String userwl = WaitList.get(itemid).peek();
								if (userwl.substring(0, 3).equalsIgnoreCase(itemid.substring(0, 3))) {
									if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userwl)) {
										List list1 = UserBorrow.get(userwl);
										list1.add(itemid);
										UserBorrow.put(userwl, list1);

									} else {
										List list2 = new LinkedList<String>();
										list2.add(itemid);
										UserBorrow.put(userwl, list2);
									}
									WaitList.get(itemid).poll();
									answer += ResultMsg.Rtn3;
									//System.out.println(answer);
									break;
								} else {
									if (UserBorrow.containsKey(WaitList.get(itemid).peek())) {
										WaitList.get(itemid).poll();
										if (WaitList.get(itemid).size() == 0) {
											map.put(itemid, new Item(itemid, name, qty + 1));
											break;
										}
									} else {
										List list3 = new LinkedList<String>();
										list3.add(itemid);
										UserBorrow.put(userwl, list3);
										answer += ResultMsg.Rtn3;
										//System.out.println(answer);
										break;
									}
								}
							}
							Iterator<HashMap.Entry<String, Queue<String>>> iter = WaitList.entrySet().iterator();
							while (iter.hasNext()) {
								HashMap.Entry<String, Queue<String>> entry1 = iter.next();
								String key1 = entry1.getKey();
								if (WaitList.get(key1).size() == 0) {
									iter.remove();
								}
							}
							break;
						} else {
							map.put(itemid, new Item(itemid, name, qty + 1));
							break;
						}
					} else {
						answer = ResultMsg.Rtn4;
					}
				}
			//} else {
			//	answer = "You don't borrow this item or nobody borrows item. ";
			//}
		} else {
			answer = ResultMsg.Rtn2;
		}
		return answer;
	}

	public String addWaitlistUdp(String str) {
		String[] values = str.split("&");
		String answer = null;
		String userid = values[1];
		String itemid = values[2];
		if (!WaitList.isEmpty()) {
			if (WaitList.containsKey(itemid)) {
				Queue queue = WaitList.get(itemid);
				queue.offer(userid);
				WaitList.put(itemid, queue);
			} else {
				Queue<String> queue = new LinkedList<String>();
				queue.offer(userid);
				WaitList.put(itemid, queue);
			}
		} else {
			Queue<String> queue = new LinkedList<String>();
			queue.offer(userid);
			WaitList.put(itemid, queue);
		}
		answer = "Add to the wait list.";
		return answer;
	}

	public String checkNewItemUdp(String str) {
		String[] values = str.split("&");
		String answer = null;
		String userid = values[1];
		String itemid = values[2];
		String itemidold = values[3];
		int qty;
		if (!map.isEmpty() & map.containsKey(itemid)) {
			qty = map.get(itemid).getQuantity();
			if (!WaitList.isEmpty() && WaitList.containsKey(itemid) && WaitList.get(itemid).contains(userid)) {
				answer = "You are already in the waitlist.";
			} else if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userid)
					&& UserBorrow.get(userid).contains(itemid)) {
				answer = "You have borrowed this item.";
			} else if (!itemid.substring(0, 3).equals(itemidold.substring(0, 3)) && !UserBorrow.isEmpty()
					&& UserBorrow.containsKey(userid)) {
				answer = "You have borrowed from this library.";
			} else {
				if (qty == 0) {
					answer = "Add to the waiting queue?";
				} else {
					String itemname = map.get(itemid).getItemName();
					map.put(itemid, new Item(itemid, itemname, qty - 1));
					temp.put(itemid, new Item(itemid, itemname, 1));
					answer = "available";
				}
			}
		} else {
			answer = "Such item does not exist.";
		}
		return answer;
	}

	public String checkOldItemUdp(String str) {
		String[] values = str.split("&");
		String answer = null;
		String userid = values[1];
		String itemid = values[2];
		if (!map.isEmpty() & map.containsKey(itemid)) {
			if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userid) && UserBorrow.get(userid).contains(itemid)) {
				answer = "available";
			} else {
				answer = "You don't borrow this item.";
			}
		} else {
			answer = "Such item does not exist.";
		}
		return answer;
	}

	public String borrowNewItemUdp(String str) {
		String[] values = str.split("&");
		String answer = null;
		String userid = values[1];
		String itemid = values[2];
		int qty;
		if (!map.isEmpty() && map.containsKey(itemid)) {
			qty = map.get(itemid).getQuantity();
			String itemName = map.get(itemid).getItemName();
			if (!WaitList.isEmpty() && WaitList.containsKey(itemid) && WaitList.get(itemid).contains(userid)) {
				answer = "You are already in the waitlist.";
			} else {
				if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userid)
						&& UserBorrow.get(userid).contains(itemid)) {
					answer = "You have borrowed this item.";
				} else {
					map.put(itemid, new Item(itemid, itemName, qty));
					if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userid)) {
						List list = UserBorrow.get(userid);
						list.add(itemid);
						UserBorrow.put(userid, list);
					}

					else {
						List list = new LinkedList<String>();
						list.add(itemid);
						UserBorrow.put(userid, list);
					}
					answer = " Successfully borrow. ";
				}
			}
		} else {
			answer = "Such item does not exist.";
		}
		return answer;
	}

	public String returnOldItemUdp(String str) {
		String[] values = str.split("&");
		String answer = null;
		String userid = values[1];
		String itemid = values[2];
		int qty;
		String name;
		if (!map.isEmpty() && map.containsKey(itemid)) {
			name = map.get(itemid).getItemName();
			qty = map.get(itemid).getQuantity();
			if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userid)) {
				Iterator<HashMap.Entry<String, List<String>>> it = UserBorrow.entrySet().iterator();
				while (it.hasNext()) {
					HashMap.Entry<String, List<String>> entry = it.next();
					String key = entry.getKey();
					if (UserBorrow.get(key).contains(itemid) && key.equals(userid)) {
						List list = UserBorrow.get(key);
						for (int i = 0; i < list.size(); i++) {
							if (list.get(i).equals(itemid)) {
								list.remove(itemid);
							}
						}
						if (list.size() == 0) {
							it.remove();
						}
						answer = "Successfully Return. ";
						if (!WaitList.isEmpty() && WaitList.containsKey(itemid)) {
							while (WaitList.get(itemid).size() != 0) {
								String userwl = WaitList.get(itemid).peek();
								if (userwl.substring(0, 3).equalsIgnoreCase(itemid.substring(0, 3))) {
									if (!UserBorrow.isEmpty() && UserBorrow.containsKey(userwl)) {
										List list1 = UserBorrow.get(userwl);
										list1.add(itemid);
										UserBorrow.put(userwl, list1);
									} else {
										List list2 = new LinkedList<String>();
										list2.add(itemid);
										UserBorrow.put(userwl, list2);
									}
									WaitList.get(itemid).poll();
									answer += " Automatically lend item:" + itemid + "to the user of waitlist:" + userwl
											+ " ";
									break;
								} else {
									if (UserBorrow.containsKey(WaitList.get(itemid).peek())) {
										WaitList.get(itemid).poll();
										if (WaitList.get(itemid).size() == 0) {
											map.put(itemid, new Item(itemid, name, qty + 1));
											break;
										}
									} else {
										List list3 = new LinkedList<String>();
										list3.add(itemid);
										UserBorrow.put(userwl, list3);
										WaitList.get(itemid).poll();
										answer += " Automatically lend item:" + itemid + "to the user of waitlist:"
												+ userwl + " ";
										break;
									}
								}
							}
							Iterator<HashMap.Entry<String, Queue<String>>> iter = WaitList.entrySet().iterator();
							while (iter.hasNext()) {
								HashMap.Entry<String, Queue<String>> entry1 = iter.next();
								String key1 = entry1.getKey();
								if (WaitList.get(key1).size() == 0) {
									iter.remove();
								}
							}
						} else {
							map.put(itemid, new Item(itemid, name, qty + 1));
							break;
						}
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

	public void receive(int portNumber) {
		DatagramSocket aSocket = null;
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
				String answer = "";
				if (str.startsWith("List item:")) {
					answer = listItemUdp(str);
				} else if (str.startsWith("Find:")) {
					answer = findItemUdp(str);
				} else if (str.startsWith("Borrow item:")) {
					answer = borrowItemUdp(str);
				} else if (str.startsWith("Addlist:")) {
					answer = addWaitlistUdp(str);
				} else if (str.startsWith("Return item:")) {
					answer = returnItemUdp(str);
				} else if (str.startsWith("Check new item:")) {
					answer = checkNewItemUdp(str);
				} else if (str.startsWith("Check old item:")) {
					answer = checkOldItemUdp(str);
				} else if (str.startsWith("Borrow new item:")) {
					answer = borrowNewItemUdp(str);
				} else if (str.startsWith("Return old item:")) {
					answer = returnOldItemUdp(str);
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
				aSocket.close();
			}
		}
		return "";
	}

}
