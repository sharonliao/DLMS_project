package FrontEndAPP;


/**
* FrontEndAPP/FrontEndOperations.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��E:/dlms/DLMS_project/FrontEnd.idl
* 2019��3��22�� ������ ����11ʱ53��28�� EDT
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
  void shutdown ();
} // interface FrontEndOperations
