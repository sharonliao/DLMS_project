package FrontEndAPP;

/**
* FrontEndAPP/FrontEndHolder.java .
* ��IDL-to-Java ������ (����ֲ), �汾 "3.2"����
* ��E:/dlms/DLMS_project/FrontEnd.idl
* 2019��4��1�� ����һ ����04ʱ30��43�� EDT
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
