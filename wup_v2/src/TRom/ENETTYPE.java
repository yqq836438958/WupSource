// **********************************************************************
// This file was generated by a TAF parser!
// TAF version 3.1.1.2 by WSRD Tencent.
// Generated from `LoginQ.jce'
// **********************************************************************

package TRom;

public final class ENETTYPE implements java.io.Serializable
{
    private static ENETTYPE[] __values = new ENETTYPE[5];
    private int __value;
    private String __T = new String();

    public static final int _NET_UNKNOWN = 0;
    public static final ENETTYPE NET_UNKNOWN = new ENETTYPE(0,_NET_UNKNOWN,"NET_UNKNOWN");
    public static final int _NET_WIFI = 1;
    public static final ENETTYPE NET_WIFI = new ENETTYPE(1,_NET_WIFI,"NET_WIFI");
    public static final int _NET_2G = 2;
    public static final ENETTYPE NET_2G = new ENETTYPE(2,_NET_2G,"NET_2G");
    public static final int _NET_3G = 3;
    public static final ENETTYPE NET_3G = new ENETTYPE(3,_NET_3G,"NET_3G");
    public static final int _NET_4G = 4;
    public static final ENETTYPE NET_4G = new ENETTYPE(4,_NET_4G,"NET_4G");

    public static ENETTYPE convert(int val)
    {
        for(int __i = 0; __i < __values.length; ++__i)
        {
            if(__values[__i].value() == val)
            {
                return __values[__i];
            }
        }
        return null;
    }

    public static ENETTYPE convert(String val)
    {
        for(int __i = 0; __i < __values.length; ++__i)
        {
            if(__values[__i].toString().equals(val))
            {
                return __values[__i];
            }
        }
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

    private ENETTYPE(int index, int val, String s)
    {
        __T = s;
        __value = val;
        __values[index] = this;
    }

}
