package FrontEndAPP;


/**
* FrontEndAPP/FrontEndOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from /Users/chy/eclipse-workspace/DLMS_project/FrontEnd.idl
* Tuesday, March 19, 2019 5:35:36 o'clock PM EDT
*/

public interface FrontEndOperations 
{
  String addItem (String managerID, String itemID, String itemName, int quantity);
  String removeItem (String managerID, String itemID, int quantity);
  String listItemAvailability (String managerID);
  String borrowItem (String userID, String itemID);
  String findItem (String userID, String itemName);
  String returnItem (String userID, String itemID);
  String exchangeItem (String userID, String newItemID, String oldItemID);
} // interface FrontEndOperations
