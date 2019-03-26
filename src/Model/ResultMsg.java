package Model;

/**
 * Created by liaoxiaoyun on 2019-03-12.
 */
public class ResultMsg {
    /**
     * Add
     */
    public static String Ad0 = "Successfully add.";
    public static String Ad1 = "Unsuccessfully add.";
    public static String Ad2 = "Automatically lend the item to the user in waitlist.";


    /**
     *Remove
     */
    public static String Re0 = "Successfully remove.";
    public static String Re1 = "Successfully decrease.";
    public static String Re2 = "Unsuccessfully remove.";
    public static String Re3 = "Such item does not exist.";
    public static String Re4 = "Invalid operation. Please try again.";


    /**
     * borrow result
     */

    static String	Br0 = "Successfully borrow.";
    static String	Br1 = "Unsuccessfully borrow.";
    static String	Br2 = "Such item does not exist.";
    static String	Br3 = "This item is unavailable, do you want to add to waiting list?";
    static String	Br4 = "You have borrowed this item.";
    static String	Br5 = "You have borrowed from this library.";
    static String	Br6 = "You are already in the waitlist.";

    /**
     * return
     */
    static String	Rtn0 = "Successfully return.";
    static String	Rtn1 = "Unsuccessfully return.";
    static String	Rtn2 = "Such item does not exist.";
    static String	Rtn3 = "Automatically lend the item to the user in waitlist.";


    /**
     * exchange
     */
    static String	Ex0 = "Successfully exchange.";
    static String	Ex1 = "Successfully exchange.";
    static String	Ex2 = "Such item does not exist.";
    static String	Ex3 = "This item is unavailable, do you want to add to waiting list?";
    static String	Ex4 = "You have borrowed this item.";
    static String	Ex5 = "You have borrowed from this library.";
    static String	Ex6 = "You are already in the waitlist.";
    static String	Ex7 = "You didn't borrow this item.";
}
