// **********************************************************************
// This file was generated by a TAF parser!
// TAF version 3.1.1.2 by WSRD Tencent.
// Generated from `LoginQ.jce'
// **********************************************************************

package TRom;

public final class ELOGINRET implements java.io.Serializable
{
    private static ELOGINRET[] __values = new ELOGINRET[2];
    private int __value;
    private String __T = new String();

    public static final int _E_QUA_ERROR = -1;
    public static final ELOGINRET E_QUA_ERROR = new ELOGINRET(0,_E_QUA_ERROR,"E_QUA_ERROR");
    public static final int _E_QUA_SN_UNCONF = -2;
    public static final ELOGINRET E_QUA_SN_UNCONF = new ELOGINRET(1,_E_QUA_SN_UNCONF,"E_QUA_SN_UNCONF");

    public static ELOGINRET convert(int val)
    {
        for(int __i = 0; __i < __values.length; ++__i)
        {
            if(__values[__i].value() == val)
            {
                return __values[__i];
            }
        }
        assert false;
        return null;
    }

    public static ELOGINRET convert(String val)
    {
        for(int __i = 0; __i < __values.length; ++__i)
        {
            if(__values[__i].toString().equals(val))
            {
                return __values[__i];
            }
        }
        assert false;
        return null;
    }

    public int value()
    {
        return __value;
    }

    public String toString()
    {
        return __T;
    }

    private ELOGINRET(int index, int val, String s)
    {
        __T = s;
        __value = val;
        __values[index] = this;
    }

}