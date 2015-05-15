package Support;

import MSG.Link;

public class PairLinkEvent {
	public NetworkEvent event;
	public Link link;
	public PairLinkEvent(NetworkEvent _event, Link _link) {
		link = _link; event = _event;
	}
}
