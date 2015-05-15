// PDU.java

package Support;				// protocol support package

import MSG.Link;

public abstract class PDU implements Cloneable{

    public static final int NIL = 0;
    public static final int IP = 1;
    public static final int TCP = 2;
    public static final int ECMP = 3;
    public static final int DARD = 4;
    public static final int DARDCTRL = 5;
    public static final int LBDAR = 6;
    public static final int FCTRL = 7;

    /** Protocol Data Unit type */
    public int type;

    /** Protocol Data Unit user data length */
    public int size = 0;
    private int linkcount = 0;
    /** Protocol Data payload type */
    public int subtype;

    /** Protocol Data Unit payload as Service Data Unit */
    public PDU sdu = null;

    /**
    Constructor for a PDU object.

    @param type		PDU type
    @param sdu		PDU payload as SDU
     */
    public int routingSpace = -1;
    public boolean first_hop = true;
    public boolean second_hop = true;
    public Link last_transmited = null;
    public PDU(int type, int size, int subtype, PDU sdu) {
        this.type = type;
        this.size = size;
        this.subtype = subtype;
        this.sdu = sdu;
    }
    public PDU(PDU t) {
    	 this.type = t.type;
    	 this.size = t.size;
    	 this.linkcount = t.linkcount;
    	 this.subtype = t.subtype;
    	 this.sdu = t.sdu.duplicate();
    }

    /**
    Convert a PDU to a string representation.

    @return		string representation of a PDU
     */
    public String toString() {
        return ("PDU <Type " + type + ", Size " + size + ", Subtype " + subtype + ", Data " + sdu.toString() + ">");
    }

    /**
    Convert a PDU to a hash code representation.

    @return		hash code of a PDU
     */
    public int hash(int k) {
        return (((Integer) size).hashCode() + ((sdu == null) ? 0 : sdu.hash(k))) % k;
    }
    protected int flowid = 0;
    public abstract int getdestid();
    public abstract int getflowid();

    public abstract int getsrcid();
    public String routehistory = "";

    public void trasmit() {
        this.linkcount += 1;
    }
    public int getlinkcount() {
        return this.linkcount;
    }

    public int pathtoken = -1;
    public void setpathtoken(int k) {
        pathtoken =k;
    }

	public abstract PDU duplicate();

}

