package Client;

import org.omg.CosNaming.*;
import FrontEndAPP.FrontEnd;
import FrontEndAPP.FrontEndHelper;

import org.omg.CORBA.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class StartClient {

	/**
	 * @param args
	 *            the command line arguments
	 */
	static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	static ArrayList<String> libid = new ArrayList<String>() {
		{
			add("CON");
			add("MCG");
			add("MON");
		}

		;

	};

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

	public static boolean isIDCorret(String id) {
		if (id.length() != 8) {
			return false;
		}
		String i1 = id.substring(0, 3);
		String i2 = id.substring(3, 4);
		boolean userType = (i2.equals("U"));
		if (libid.contains(i1) && userType && isInteger(id.substring(4, 7))) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isItemCorrect(String userID, String id) {
		if (id.length() != 7) {
			return false;
		}
		String i1 = id.substring(0, 3);
		String i2 = id.substring(4, 7);
		boolean empty = (!id.trim().isEmpty());
		if (userID.substring(3, 4).equals("M")) {
			boolean samelib = (userID.substring(0, 3).equals(i1));
			if (empty && libid.contains(i1) && samelib && isInteger(i2)) {
				return true;
			} else {
				return false;
			}
		} else {
			if (empty && libid.contains(i1) && isInteger(i2)) {
				return true;
			} else {
				return false;
			}
		}
	}

	public static boolean isInteger(String str) {
		Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
		return pattern.matcher(str).matches();
	}

	public static void main(String[] args) {
		try {
			ORB orb = ORB.init(args, null);
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			FrontEnd libobj;
			Scanner c = new Scanner(System.in);
			libobj = (FrontEnd) FrontEndHelper.narrow(ncRef.resolve_str("FE"));
			System.out.println("Welcome to the Online Library System:");
			while (true) {
				System.out.println("Please choose the service:");
				System.out.println("1.Software Failure");
				System.out.println("2.Crash Failure");
				String s = "";
				int option = Integer.parseInt(c.nextLine());
				if (option == 1 || option == 2) {
					s = libobj.setUpFailureType(option);
					break;
				} else {
					System.out.println("Your input is invalid!");
				}
			}

			while (true) {
				System.out.println("Please Choose the Service:");
				System.out.println("1.Borrow Book");
				System.out.println("2.Return Book");
				System.out.println("3.Find Book");
				System.out.println("4.Exchange Book");
				System.out.println("5.Quit");
				String option = c.nextLine();
				Scanner in1 = new Scanner(System.in);
				String result;
				String result1;
				if (option.equals("1")) {
					String id;
					while (true) {
						System.out.println("Please input your User:");
						id = c.nextLine();
						id.trim();
						id = id.toUpperCase();
						if (isIDCorret(id)) {
							break;
						} else {
							System.out.println("Your UseID is invalid!");
						}
					}
					String logpath = "./" + id + ".log";
					System.out.println("Please input the ID of book you want to borrow");
					String book;
					while (true) {
						book = in1.nextLine();
						book.trim();
						book = book.toUpperCase();
						if (isItemCorrect(id, book)) {
							break;
						} else {
							System.out.println("Your input is invalid. Please input again!:");
						}
					}
					result = libobj.borrowItem(id, book);
					System.out.println(result);
					writeFile(logpath, df.format(new Date()) + " " + result);
					if (result.contains("not available")) {
						while (true) {
							Scanner r = new Scanner(System.in);
							String demand = r.nextLine();
							demand.trim();
							if (demand.equals("Y") || demand.equals("y")) {
								result1 = libobj.addToWaitlist(id, book);
								System.out.println(result1);
								writeFile(logpath, df.format(new Date()) + " " + result1);
								break;
							} else if (demand.equals("N") || demand.equals("n")) {
								System.out.println(df.format(new Date()) + " " + book + " :Borrow failed!");
								break;
							} else {
								System.out.println("The answer you input is invaild. Please input again!");
							}
						}
					}
					continue;
				} else if (option.equals("2")) {
					String id;
					while (true) {
						System.out.println("Please input your User:");
						id = c.nextLine();
						id.trim();
						id = id.toUpperCase();
						if (isIDCorret(id)) {
							break;
						} else {
							System.out.println("Your UseID is invalid!");
						}
					}
					String logpath = "./" + id + ".log";
					System.out.println("Please input the ID of book you want to return");
					String bookid;
					while (true) {
						bookid = in1.nextLine();
						bookid.trim();
						bookid = bookid.toUpperCase();
						if (isItemCorrect(id, bookid)) {
							break;
						} else {
							System.out.println("Your input is invalid. Please input again!:");
						}
					}
					result = libobj.returnItem(id, bookid);
					System.out.println(result);
					writeFile(logpath, df.format(new Date()) + " " + result);
					continue;
				} else if (option.equals("3")) {
					String id;
					while (true) {
						System.out.println("Please input your User:");
						id = c.nextLine();
						id.trim();
						id = id.toUpperCase();
						if (isIDCorret(id)) {
							break;
						} else {
							System.out.println("Your UseID is invalid!");
						}
					}
					String logpath = "./" + id + ".log";
					System.out.println("Please input the name of book you want to find");
					String bookname;
					while (true) {
						bookname = in1.nextLine();
						bookname.trim();
						if (bookname.trim().isEmpty()) {
							System.out.println("Your input is empty. Please input again!:");
						} else {
							break;
						}
					}
					result = libobj.findItem(id, bookname);
					System.out.println(result);
					if (!result.contains("exist")) {
						writeFile(logpath, df.format(new Date()) + " Find Item " + bookname + " Successfully!");
					} else {
						writeFile(logpath, df.format(new Date()) + " Find Item " + bookname + " Failed: Don't exist");
					}

					continue;
				} else if (option.equals("4")) {
					String id;
					while (true) {
						System.out.println("Please input your User:");
						id = c.nextLine();
						id.trim();
						id = id.toUpperCase();
						if (isIDCorret(id)) {
							break;
						} else {
							System.out.println("Your UseID is invalid!");
						}
					}
					String logpath = "./" + id + ".log";
					System.out.println("Please input the ID of book you want to return");
					String oldItem;
					while (true) {
						oldItem = in1.nextLine();
						oldItem.trim();
						oldItem = oldItem.toUpperCase();
						if (isItemCorrect(id, oldItem)) {
							break;
						} else {
							System.out.println("Your input is invalid. Please input again!:");
						}
					}
					System.out.println("Please input the ID of book you want to borrow");
					String newItem;
					while (true) {
						newItem = in1.nextLine();
						newItem.trim();
						newItem = newItem.toUpperCase();
						if (isItemCorrect(id, newItem)) {
							break;
						} else {
							System.out.println("Your input is invalid. Please input again!:");
						}
					}
					if (oldItem.equals(newItem)) {
						writeFile(logpath, df.format(new Date()) + " " + "You can not exchange the same item");
						System.out.println("You can not exchange the same item");
					} else {
						result = libobj.exchange(id, newItem, oldItem);
						System.out.println(result);
						if (result.contains("not available")) {
							while (true) {
								Scanner r = new Scanner(System.in);
								String demand = r.nextLine();
								demand.trim();
								if (demand.equals("Y") || demand.equals("y")) {
									result1 = libobj.addToWaitlistforExchange(id, newItem, oldItem);
									System.out.println(result1);
									if (result1.contains("success")) {
										writeFile(logpath, df.format(new Date()) + " " + "New: " + newItem + " Old: "
												+ oldItem + ": Exchange successfully.");
									} else {
										writeFile(logpath, df.format(new Date()) + " " + "New: " + newItem + " Old: "
												+ oldItem + ": Exchange Failed.");
									}
									break;
								} else if (demand.equals("N") || demand.equals("n")) {
									result1 = "New: " + newItem + " Old: " + oldItem
											+ ": Exchange failed. The new book is not available.";
									writeFile(logpath, df.format(new Date()) + " " + result1);
									System.out.println(result1);
									break;
								} else {
									System.out.println("The answer you input is invaild. Please input again!");
								}
							}
						} else if (result.contains("successful")) {
							writeFile(logpath, df.format(new Date()) + " " + result);
						} else {
							writeFile(logpath, df.format(new Date()) + " " + result);
						}

						continue;
					}
				} else if (option.equals("5")) {
					System.out.println("Thank you for using Library System. Bye!");
					break;
				} else {
					System.out.println("The number you input is not available. Please try again");
					continue;
				}
			}

		} catch (Exception e) {
			System.out.println("Hello Client exception: " + e);
			e.printStackTrace();
		}

	}

}
