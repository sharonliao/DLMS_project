package FrontEndAPP;


/**
* FrontEndAPP/FrontEndOperations.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从E:/dlms/DLMS_project/FrontEnd.idl
* 2019年4月1日 星期一 下午04时30分43秒 EDT
*/

public interface FrontEndOperations 
{
  String addItem (String managerID, String itemID, String itemName, int quantity);
  String removeItem (String managerID, String itemID, int quantity);
  String listItemAvailability (String managerID);
  String borrowItem (String userID, String itemID);
  String findItem (String userID, String itemName);
  String returnItem (String userID, String itemID);
  String checkBorrowList (String userID);
  String checkWaitList (String itemID);
  String addToWaitlist (String userID, String itemID);
  String exchange (String studentID, String newItemID, String oldItemID);
  String addToWaitlistforExchange (String studentID, String newItemID, String oldItemID);
  String setUpFailureType (int option);
  void shutdown ();
} // interface FrontEndOperations
