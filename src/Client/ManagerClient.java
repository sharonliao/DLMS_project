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

public class ManagerClient {

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
		String itemID, managerID, itemName;
		int quantity;
		String choice;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			System.out.println("Welcome to DLMS System. Please Input Your Operation:");
			System.out.println("1. Add Item\n2. Remove Item\n3. List Item Availability\n4. Exit");
			choice = br.readLine().trim();

			switch (choice) {
			case "1":
				System.out.println("Please enter your managerID: ");
				managerID = br.readLine().trim().toUpperCase();
				if (isManager(managerID)) {
					System.out.println("Please enter the itemID: ");
					itemID = br.readLine().trim().toUpperCase();
					if (isItemID(itemID) && itemID.substring(0, 3).equalsIgnoreCase(managerID.substring(0, 3))) {
						System.out.println("Please enter the itemName: ");
						itemName = br.readLine().trim();
						System.out.println("Please enter the quantity: ");
						quantity = Integer.parseInt(br.readLine().trim());
						if (quantity >= 0)
							System.out.println(frontend.addItem(managerID, itemID, itemName, quantity));
						else
							System.out.println("\nInvalid quantity.");
					} else {
						System.out.println("\nInvalid itemID.");
					}
				} else {
					System.out.println("\nInvalid managerID.");
				}
				break;
			case "2":
				System.out.println("Please enter your managerID: ");
				managerID = br.readLine().trim().toUpperCase();
				if (isManager(managerID)) {
					System.out.println("Please enter the itemID: ");
					itemID = br.readLine().trim().toUpperCase();
					if (isItemID(itemID) && itemID.substring(0, 3).equalsIgnoreCase(managerID.substring(0, 3))) {
						System.out.println("Please enter the quantity: ");
						quantity = Integer.parseInt(br.readLine().trim());
						System.out.println(frontend.removeItem(managerID, itemID, quantity));
					} else {
						System.out.println("\nInvalid itemID.");
					}
				} else {
					System.out.println("\nInvalid managerID.");
				}
				break;
			case "3":
				System.out.println("Please enter your managerID: ");
				managerID = br.readLine().trim().toUpperCase();
				if (isManager(managerID)) {
					System.out.println(frontend.listItemAvailability(managerID));
				} else {
					System.out.println("\nInvalid managerID.");
				}
				break;
			case "4":
				System.out.println("Exited");
				System.exit(1);
				break;
			default:
				System.out.println("\nERROR: Invalid input please try again.");
				break;
			}
		}
	}

	public static boolean isManager(String ID) {
		boolean answer = false;
		if (ID.length() == 8) {
			Matcher isNum = pattern.matcher(ID.substring(4, 8));
			if (ID.substring(0, 4).equalsIgnoreCase("conm") && isNum.matches()) {
				answer = true;
			} else if (ID.substring(0, 4).equalsIgnoreCase("mcgm") && isNum.matches()) {
				answer = true;
			} else if (ID.substring(0, 4).equalsIgnoreCase("monm") && isNum.matches()) {
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
