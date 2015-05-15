package TCP;

import MSG.Host;
import SimpleBed.SimpleHost;

public class TCPRecoveryCoorBony extends TCPCoorBony {

	public TCPRecoveryCoorBony(Host here, short s, int flowsizeKB) {
		super(here, s, flowsizeKB);
	}

	public TCPRecoveryCoorBony(Host host, short s) {
		super(host,s);
	}
	@Override
	public TCPMessage newMessage(int i, short port2, short port3, int ttl2,
			SimpleHost h1,SimpleHost h2, int flowid2) {
		return new TCPRecoveryCoorMessage(i,((Host) h1).getCoor(),((Host) h2).getCoor(),port2,port3,ttl2,h1.id,h2.id,flowid2);
	}
}
