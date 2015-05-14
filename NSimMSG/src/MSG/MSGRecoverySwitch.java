package MSG;

import Support.Coordinate;
import Support.NetworkEvent;
import Support.PDU;

public class MSGRecoverySwitch extends Switch {

	public MSGRecoverySwitch(Coordinate coor, int port, int id,
			int routing_hop, boolean flex_space) {
		super(coor, port, id, routing_hop, flex_space);
	}
	@Override
    protected PairLinkEvent[] getLink(NetworkEvent e) {
		return null;
	}
}
