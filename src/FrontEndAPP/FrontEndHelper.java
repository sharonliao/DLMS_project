package FrontEndAPP;


/**
* FrontEndAPP/FrontEndHelper.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从E:/dlms/DLMS_project/FrontEnd.idl
* 2019年3月22日 星期五 上午11时53分28秒 EDT
*/

abstract public class FrontEndHelper
{
  private static String  _id = "IDL:FrontEndAPP/FrontEnd:1.0";

  public static void insert (org.omg.CORBA.Any a, FrontEndAPP.FrontEnd that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static FrontEndAPP.FrontEnd extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (FrontEndAPP.FrontEndHelper.id (), "FrontEnd");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static FrontEndAPP.FrontEnd read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_FrontEndStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, FrontEndAPP.FrontEnd value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static FrontEndAPP.FrontEnd narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof FrontEndAPP.FrontEnd)
      return (FrontEndAPP.FrontEnd)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      FrontEndAPP._FrontEndStub stub = new FrontEndAPP._FrontEndStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static FrontEndAPP.FrontEnd unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof FrontEndAPP.FrontEnd)
      return (FrontEndAPP.FrontEnd)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      FrontEndAPP._FrontEndStub stub = new FrontEndAPP._FrontEndStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
