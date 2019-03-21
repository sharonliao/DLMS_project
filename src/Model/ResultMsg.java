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
	 * Remove
	 */
	public static String Re0 = "Successfully remove.";
	public static String Re1 = "Successfully decrease.";
	public static String Re2 = "Unsuccessfully remove.";
	public static String Re3 = "Such item does not exist.";
	public static String Re4 = "Invalid operation. Please try again.";

	/**
	 * borrow result
	 */

	public static String Br0 = "Successfully borrow.";
	public static String Br1 = "Unsuccessfully borrow.";
	public static String Br2 = "Such item does not exist.";
	public static String Br3 = "This item is unavailable, do you want to add to waiting list?";
	public static String Br4 = "You have borrowed this item.";
	public static String Br5 = "You have borrowed from this library.";
	public static String Br6 = "You are already in the waitlist.";

	/**
	 * return
	 */
	public static String Rtn0 = "Successfully return.";
	public static String Rtn1 = "Unsuccessfully return.";
	public static String Rtn2 = "Such item does not exist.";
	public static String Rtn3 = "Automatically lend the item to the user in waitlist.";
	public static String Rtn4 = "You didn't borrow this item.";
	/**
	 * exchange
	 */
	public static String Ex0 = "Successfully exchange.";
	public static String Ex1 = "Successfully exchange.";
	public static String Ex2 = "Such item does not exist.";
	public static String Ex3 = "This item is unavailable, do you want to add to waiting list?";
	public static String Ex4 = "You have borrowed this item.";
	public static String Ex5 = "You have borrowed from this library.";
	public static String Ex6 = "You are already in the waitlist.";
	public static String Ex7 = "You didn't borrow this item.";
}
