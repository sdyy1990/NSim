package TCP;

import Support.Coordinate;

public class TCPRecoveryCoorMessage extends TCPCoorMessage {
	public int type;
	public TCPRecoveryCoorMessage(int size, Coordinate src, Coordinate dest,
			short sport, short dport, int ttl, int srcc, int dstc, int flowid) {
		super(size, src, dest, sport, dport, ttl, srcc, dstc, flowid);
		type = 1;
	}
	public final static int REGULAR_TYPE = 1;
	public final static int BFS_PROBE_TYPE = 2;
	public final static int BFS_REPLY_TYPE = 3;
	public void setBFSProbe() {
		this.type = BFS_PROBE_TYPE;
	}
	public void setBFSReply() {
		this.type = BFS_REPLY_TYPE;
	}
	
}
