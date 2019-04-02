package Client;

import org.omg.CosNaming.*;
import FrontEndAPP.FrontEnd;
import FrontEndAPP.FrontEndHelper;

import org.omg.CORBA.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
public class StartServer {
	/**
     * @param args
     * the command line arguments
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
        boolean userType = (i2.equals("M") || i2.equals("U"));
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
                System.out.println("1.Add Book");
                System.out.println("2.Remove Book");
                System.out.println("3.List all books");
                System.out.println("4.Quit");
                String option = c.nextLine();
                Scanner in2 = new Scanner(System.in);
                String result;
                String book = "";
                String name = "";
                String quantity = "";
                int n = 0;
                boolean b = true;
                String result2;
                if (option.equals("1")) {
                	String id;
                	while (true) {
                        System.out.println("Please input your User:");
                        id = c.nextLine();
                        id = id.toUpperCase();
                        if (isIDCorret(id)) {
                            break;
                        } else {
                            System.out.println("Your UseID is invalid!");
                        }
                    }
                	String logpath = "E:/project/CORBA 40093667/library_corba/user/" + id + ".log";
                    System.out.println("Please input the ID of the book:");
                    while (true) {
                        book = in2.nextLine();
                        book = book.toUpperCase();
                        if (isItemCorrect(id, book)) {
                            break;
                        } else {
                            System.out.println("Your input is invalid. Please input again!:");
                        }

                    }
                    System.out.println("Please input the name of the book:");
                    while (true) {
                        name = in2.nextLine();
                        if (name.trim().isEmpty()) {
                            System.out.println("Your input is empty. Please input again!:");
                        } else {
                            break;
                        }
                    }
                    System.out.println("Please input the number of the book:");
                    while (true) {
                        quantity = in2.nextLine();
                        if (quantity.trim().isEmpty()) {
                            System.out.println("Your input is empty. Please input again!:");
                        } else {
                            try {
                                n = Integer.parseInt(quantity);

                                break;

                            } catch (Exception e) {
                                System.out.println("It's not a number. Please input again!:");
                            }
                        }
                    }
                    result = libobj.addItem(id, book, name.trim(), n);
                    System.out.println(result);
                    writeFile(logpath, df.format(new Date()) + " " + result);
                    continue;
                } else if (option.equals("2")) {
                	String id;
                	while (true) {
                        System.out.println("Please input your User:");
                        id = c.nextLine();
                        id = id.toUpperCase();
                        if (isIDCorret(id)) {
                            break;
                        } else {
                            System.out.println("Your UseID is invalid!");
                        }
                    }
                	String logpath = "E:/project/CORBA 40093667/library_corba/user/" + id + ".log";
                    System.out.println("Please input the ID of the book you want to remove");
                    while (true) {
                        book = in2.nextLine();
                        book = book.toUpperCase();
                        if (isItemCorrect(id, book)) {
                            break;
                        } else {
                            System.out.println("Your input is invalid. Please input again!:");
                        }
                    }
                    book = book.toUpperCase();
                    System.out.println("Please input the number of the book you want to remove:");
                    while (true) {
                        quantity = in2.nextLine();
                        if (quantity.trim().isEmpty()) {
                            System.out.println("Your input is empty. Please input again!:");
                        } else {
                            try {
                                n = Integer.parseInt(quantity);
                                break;
                            } catch (Exception e) {
                                System.out.println("It's not a number. Please input again!:");
                            }
                        }
                    }
                    result = libobj.removeItem(id, book, n);
                    System.out.println(result);
                    writeFile(logpath, df.format(new Date()) + " " + result);
                    continue;
                } else if (option.equals("3")) {
                	String id;
                	while (true) {
                        System.out.println("Please input your User:");
                        id = c.nextLine();
                        id = id.toUpperCase();
                        if (isIDCorret(id)) {
                            break;
                        } else {
                            System.out.println("Your UseID is invalid!");
                        }
                    }
                	String logpath = "E:/project/CORBA 40093667/library_corba/user/" + id + ".log";
                    result2 = libobj.listItemAvailability(id);
                    System.out.println(result2);
                    if (result2.contains("no book")) {
                        writeFile(logpath, df.format(new Date()) + " " + "List Available Items"
                                + " Failed: No books in the library");
                    } else {
                        writeFile(logpath, df.format(new Date()) + " " + "List Available Items" + " Successful");
                    }

                    continue;
                } else if (option.equals("4")) {
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
