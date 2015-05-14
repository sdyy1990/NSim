package MSG;

import Support.NetworkEvent;
import Support.PDU;

public class PairLinkEvent {
	public NetworkEvent event;
	public Link link;
	PairLinkEvent(NetworkEvent _event, Link _link) {
		link = _link; event = _event;
	}
}
