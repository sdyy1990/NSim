package Example;

import MSG.Link;
import SimpleBed.SimpleSwitch;
import Support.NetworkEvent;
import Support.PDU;
import Support.PairLinkEvent;

public class ExampleSwitch extends SimpleSwitch {

	public ExampleSwitch(int port, int id) {
		super(port, id);
	}

	@Override
	public String getName() {
		return null;
	}
	//Example Switch: have 1 neighbor switch, 1 neighbor host
	@Override
	protected PairLinkEvent[] getLink(NetworkEvent e) {
		PairLinkEvent[] ans = new PairLinkEvent[1];
		ans[0].event = new NetworkEvent(e);
		int dst = e.getPDU().getdestid();
		if (dst == hosts.get(0).id)
			ans[0].link = ports.get(0);
		else 
			ans[0].link = ports.get(1);
		return ans;
	}

}
