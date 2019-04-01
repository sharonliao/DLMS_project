package FrontEndAPP;

/**
* FrontEndAPP/FrontEndHolder.java .
* 由IDL-to-Java 编译器 (可移植), 版本 "3.2"生成
* 从E:/dlms/DLMS_project/FrontEnd.idl
* 2019年4月1日 星期一 下午04时30分43秒 EDT
*/

public final class FrontEndHolder implements org.omg.CORBA.portable.Streamable
{
  public FrontEndAPP.FrontEnd value = null;

  public FrontEndHolder ()
  {
  }

  public FrontEndHolder (FrontEndAPP.FrontEnd initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = FrontEndAPP.FrontEndHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    FrontEndAPP.FrontEndHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return FrontEndAPP.FrontEndHelper.type ();
  }

}
