// **********************************************************************
// This file was generated by a TAF parser!
// TAF version 3.2.1.4 by WSRD Tencent.
// Generated from `LogReport.jce'
// **********************************************************************

package TRom.log;

public final class GetTicketReq extends com.qq.taf.jce.JceStruct implements java.lang.Cloneable
{
    public String className()
    {
        return "TRom.GetTicketReq";
    }

    public String fullClassName()
    {
        return "TRom.GetTicketReq";
    }

    public TRom.RomBaseInfo stRomBaseInfo = null;

    public TRom.RomBaseInfo getStRomBaseInfo()
    {
        return stRomBaseInfo;
    }

    public void  setStRomBaseInfo(TRom.RomBaseInfo stRomBaseInfo)
    {
        this.stRomBaseInfo = stRomBaseInfo;
    }

    public GetTicketReq()
    {
    }

    public GetTicketReq(TRom.RomBaseInfo stRomBaseInfo)
    {
        this.stRomBaseInfo = stRomBaseInfo;
    }

    public boolean equals(Object o)
    {
        if(o == null)
        {
            return false;
        }

        GetTicketReq t = (GetTicketReq) o;
        return (
            com.qq.taf.jce.JceUtil.equals(stRomBaseInfo, t.stRomBaseInfo) );
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
        if (null != stRomBaseInfo)
        {
            _os.write(stRomBaseInfo, 0);
        }
    }

    static TRom.RomBaseInfo cache_stRomBaseInfo;

    public void readFrom(com.qq.taf.jce.JceInputStream _is)
    {
        if(null == cache_stRomBaseInfo)
        {
            cache_stRomBaseInfo = new TRom.RomBaseInfo();
        }
        this.stRomBaseInfo = (TRom.RomBaseInfo) _is.read(cache_stRomBaseInfo, 0, false);
    }

    public void display(java.lang.StringBuilder _os, int _level)
    {
        com.qq.taf.jce.JceDisplayer _ds = new com.qq.taf.jce.JceDisplayer(_os, _level);
        _ds.display(stRomBaseInfo, "stRomBaseInfo");
    }

    public void displaySimple(java.lang.StringBuilder _os, int _level)
    {
        com.qq.taf.jce.JceDisplayer _ds = new com.qq.taf.jce.JceDisplayer(_os, _level);
        _ds.displaySimple(stRomBaseInfo, false);
    }

}

