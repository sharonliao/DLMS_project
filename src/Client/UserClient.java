package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import FrontEndAPP.FrontEnd;
import FrontEndAPP.FrontEndHelper;

public class UserClient {

	static Pattern pattern = Pattern.compile("[0-9]*");

	public static void main(String[] args) throws IOException {

		try {
			// create and initialize the ORB
			ORB orb = ORB.init(new String[] { "-ORBInitialPort", "1050", "-ORBInitialHost", "localhost" }, null);

			// get the root naming context
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			// Use NamingContextExt instead of NamingContext. This is
			// part of the Interoperable naming Service.
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

			// resolve the Object Reference in Naming
			FrontEnd frontend = FrontEndHelper.narrow(ncRef.resolve_str("FrontEnd"));
			Menu(frontend);

		} catch (Exception e) {
			System.out.println("ERROR : " + e);
			e.printStackTrace(System.out);
		}

	}

	private static void Menu(FrontEnd frontend) throws IOException {
		String itemID,userID,newitemID,olditemID,itemName;
		int quantity;
		String choice;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("Welcome to DLMS System. Please Input Your Operation:");
			System.out.println("1. Borrow Item\n2. Find Item\n3. Return Item\n4. Exchange Item\n5. Exit");
			choice = br.readLine().trim();

			switch (choice) {
			case "1":
				System.out.println("Please enter your userID: ");
				userID = br.readLine().trim().toUpperCase();
				if (isUser(userID)) {
					System.out.println("Please enter the itemID: ");
					itemID = br.readLine().trim().toUpperCase();
					if (isItemID(itemID)) {
						System.out.println(frontend.borrowItem(userID, itemID));						
					} else {
						System.out.println("Invalid itemID.");
					}
				} else {
					System.out.println("Invalid userID.");
				}
				break;
				
			case "2":
				System.out.println("Please enter your userID: ");
				userID = br.readLine().trim().toUpperCase();
				if (isUser(userID) && userID.substring(0, 3).equalsIgnoreCase("mon")) {
					System.out.println("Please enter the itemName: ");
					itemName = br.readLine().trim();
					if (!itemName.isEmpty()) {
						System.out.println(frontend.findItem(userID, itemName));
					} else {
						System.out.println("Please enter the itemName.");
					}
				} else {
					System.out.println("Invalid userID.");
				}
				break;
				
			case "3":
				System.out.println("Please enter your userID: ");
				userID = br.readLine().trim().toUpperCase();
				if (isUser(userID) && userID.substring(0, 3).equalsIgnoreCase("mon")) {
					System.out.println("Please enter the itemID: ");
					itemID = br.readLine().trim().toUpperCase();
					if (isItemID(itemID)) {
						System.out.println(frontend.returnItem(userID, itemID));
					} else {
						System.out.println("Invalid itemID.");
					}
				} else {
					System.out.println("Invalid userID.");
				}
				break;
				
			case "4":
				System.out.println("Please enter your userID: ");
				userID = br.readLine().trim().toUpperCase();
				if (isUser(userID) & userID.substring(0, 3).equalsIgnoreCase("mon")) {
					System.out.println("Please enter the new itemID: ");
					newitemID = br.readLine().trim().toUpperCase();
					if (isItemID(newitemID)) {
						System.out.println("Please enter the old itemID: ");
						olditemID = br.readLine().trim().toUpperCase();
						if (isItemID(olditemID)) {
							System.out.println(frontend.exchangeItem(userID, newitemID, olditemID));
						} else {
							System.out.println("Invalid itemID.");
						}
					} else {
						System.out.println("Invalid itemID.");
					}
				} else {
					System.out.println("Invalid userID.");
				}
				break;
				
			case "5":
				System.out.println("Exited");
				System.exit(1);
				break;
			default:
				System.out.println("\nERROR: Invalid input please try again.");
				break;
			}
		}
	}

	public static boolean isUser(String ID) {
		boolean answer = false;
		if (ID.length() == 8) {
			Matcher isNum = pattern.matcher(ID.substring(4, 8));
			if (ID.substring(0, 4).equalsIgnoreCase("conu") && isNum.matches()) {
				answer = true;
			} else if (ID.substring(0, 4).equalsIgnoreCase("mcgu") && isNum.matches()) {
				answer = true;
			} else if (ID.substring(0, 4).equalsIgnoreCase("monu") && isNum.matches()) {
				answer = true;
			}
		} else {
			answer = false;
		}
		return answer;
	}

	public static boolean isItemID(String ID) {
		boolean answer = false;
		if (ID.length() == 7) {
			Matcher isNum = pattern.matcher(ID.substring(3, 7));
			if (ID.substring(0, 3).equalsIgnoreCase("con") && isNum.matches()) {
				answer = true;
			} else if (ID.substring(0, 3).equalsIgnoreCase("mcg") && isNum.matches()) {
				answer = true;
			} else if (ID.substring(0, 3).equalsIgnoreCase("mon") && isNum.matches()) {
				answer = true;
			}
		} else {
			answer = false;
		}
		return answer;
	}
}

