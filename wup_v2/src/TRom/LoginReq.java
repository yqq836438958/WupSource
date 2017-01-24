// **********************************************************************
// This file was generated by a TAF parser!
// TAF version 3.1.1.2 by WSRD Tencent.
// Generated from `LoginQ.jce'
// **********************************************************************

package TRom;

public final class LoginReq extends com.qq.taf.jce.JceStruct implements java.lang.Cloneable
{
    public String className()
    {
        return "TRom.LoginReq";
    }

    public String fullClassName()
    {
        return "TRom.LoginReq";
    }

    public TRom.RomBaseInfo stBaseInfo = null;

    public String sMac = "";

    public TRom.RomBaseInfo getStBaseInfo()
    {
        return stBaseInfo;
    }

    public void  setStBaseInfo(TRom.RomBaseInfo stBaseInfo)
    {
        this.stBaseInfo = stBaseInfo;
    }

    public String getSMac()
    {
        return sMac;
    }

    public void  setSMac(String sMac)
    {
        this.sMac = sMac;
    }

    public LoginReq()
    {
    }

    public LoginReq(TRom.RomBaseInfo stBaseInfo, String sMac)
    {
        this.stBaseInfo = stBaseInfo;
        this.sMac = sMac;
    }

    public boolean equals(Object o)
    {
        if(o == null)
        {
            return false;
        }

        LoginReq t = (LoginReq) o;
        return (
            com.qq.taf.jce.JceUtil.equals(stBaseInfo, t.stBaseInfo) && 
            com.qq.taf.jce.JceUtil.equals(sMac, t.sMac) );
    }

    public int hashCode()
    {
        try
        {
            throw new Exception("Need define key first!");
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return 0;
    }
    public java.lang.Object clone()
    {
        java.lang.Object o = null;
        try
        {
            o = super.clone();
        }
        catch(CloneNotSupportedException ex)
        {
            assert false; // impossible
        }
        return o;
    }

    public void writeTo(com.qq.taf.jce.JceOutputStream _os)
    {
        if (null != stBaseInfo)
        {
            _os.write(stBaseInfo, 0);
        }
        if (null != sMac)
        {
            _os.write(sMac, 1);
        }
    }

    static TRom.RomBaseInfo cache_stBaseInfo;

    public void readFrom(com.qq.taf.jce.JceInputStream _is)
    {
        if(null == cache_stBaseInfo)
        {
            cache_stBaseInfo = new TRom.RomBaseInfo();
        }
        this.stBaseInfo = (TRom.RomBaseInfo) _is.read(cache_stBaseInfo, 0, false);
        this.sMac =  _is.readString(1, false);
    }

    public void display(java.lang.StringBuilder _os, int _level)
    {
        com.qq.taf.jce.JceDisplayer _ds = new com.qq.taf.jce.JceDisplayer(_os, _level);
        _ds.display(stBaseInfo, "stBaseInfo");
        _ds.display(sMac, "sMac");
    }

    public void displaySimple(java.lang.StringBuilder _os, int _level)
    {
        com.qq.taf.jce.JceDisplayer _ds = new com.qq.taf.jce.JceDisplayer(_os, _level);
        _ds.displaySimple(stBaseInfo, true);
        _ds.displaySimple(sMac, false);
    }

}
