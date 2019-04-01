package FrontEndAPP;


/**
* FrontEndAPP/FrontEndPOA.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从E:/dlms/DLMS_project/FrontEnd.idl
* 2019年4月1日 星期一 下午04时30分43秒 EDT
*/

public abstract class FrontEndPOA extends org.omg.PortableServer.Servant
 implements FrontEndAPP.FrontEndOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("addItem", new java.lang.Integer (0));
    _methods.put ("removeItem", new java.lang.Integer (1));
    _methods.put ("listItemAvailability", new java.lang.Integer (2));
    _methods.put ("borrowItem", new java.lang.Integer (3));
    _methods.put ("findItem", new java.lang.Integer (4));
    _methods.put ("returnItem", new java.lang.Integer (5));
    _methods.put ("checkBorrowList", new java.lang.Integer (6));
    _methods.put ("checkWaitList", new java.lang.Integer (7));
    _methods.put ("addToWaitlist", new java.lang.Integer (8));
    _methods.put ("exchange", new java.lang.Integer (9));
    _methods.put ("addToWaitlistforExchange", new java.lang.Integer (10));
    _methods.put ("setUpFailureType", new java.lang.Integer (11));
    _methods.put ("shutdown", new java.lang.Integer (12));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // FrontEndAPP/FrontEnd/addItem
       {
         String managerID = in.read_string ();
         String itemID = in.read_string ();
         String itemName = in.read_string ();
         int quantity = in.read_long ();
         String $result = null;
         $result = this.addItem (managerID, itemID, itemName, quantity);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 1:  // FrontEndAPP/FrontEnd/removeItem
       {
         String managerID = in.read_string ();
         String itemID = in.read_string ();
         int quantity = in.read_long ();
         String $result = null;
         $result = this.removeItem (managerID, itemID, quantity);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 2:  // FrontEndAPP/FrontEnd/listItemAvailability
       {
         String managerID = in.read_string ();
         String $result = null;
         $result = this.listItemAvailability (managerID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 3:  // FrontEndAPP/FrontEnd/borrowItem
       {
         String userID = in.read_string ();
         String itemID = in.read_string ();
         String $result = null;
         $result = this.borrowItem (userID, itemID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 4:  // FrontEndAPP/FrontEnd/findItem
       {
         String userID = in.read_string ();
         String itemName = in.read_string ();
         String $result = null;
         $result = this.findItem (userID, itemName);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 5:  // FrontEndAPP/FrontEnd/returnItem
       {
         String userID = in.read_string ();
         String itemID = in.read_string ();
         String $result = null;
         $result = this.returnItem (userID, itemID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 6:  // FrontEndAPP/FrontEnd/checkBorrowList
       {
         String userID = in.read_string ();
         String $result = null;
         $result = this.checkBorrowList (userID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 7:  // FrontEndAPP/FrontEnd/checkWaitList
       {
         String itemID = in.read_string ();
         String $result = null;
         $result = this.checkWaitList (itemID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 8:  // FrontEndAPP/FrontEnd/addToWaitlist
       {
         String userID = in.read_string ();
         String itemID = in.read_string ();
         String $result = null;
         $result = this.addToWaitlist (userID, itemID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 9:  // FrontEndAPP/FrontEnd/exchange
       {
         String studentID = in.read_string ();
         String newItemID = in.read_string ();
         String oldItemID = in.read_string ();
         String $result = null;
         $result = this.exchange (studentID, newItemID, oldItemID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 10:  // FrontEndAPP/FrontEnd/addToWaitlistforExchange
       {
         String studentID = in.read_string ();
         String newItemID = in.read_string ();
         String oldItemID = in.read_string ();
         String $result = null;
         $result = this.addToWaitlistforExchange (studentID, newItemID, oldItemID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 11:  // FrontEndAPP/FrontEnd/setUpFailureType
       {
         int option = in.read_long ();
         String $result = null;
         $result = this.setUpFailureType (option);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 12:  // FrontEndAPP/FrontEnd/shutdown
       {
         this.shutdown ();
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:FrontEndAPP/FrontEnd:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public FrontEnd _this() 
  {
    return FrontEndHelper.narrow(
    super._this_object());
  }

  public FrontEnd _this(org.omg.CORBA.ORB orb) 
  {
    return FrontEndHelper.narrow(
    super._this_object(orb));
  }


} // class FrontEndPOA
