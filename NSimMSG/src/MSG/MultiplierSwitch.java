package MSG;

import SimpleBed.SimpleSwitch;
import Support.NetworkEvent;
import Support.PDU;

public abstract class MultiplierSwitch extends SimpleSwitch {

	public MultiplierSwitch(int port, int id) {
		super(port, id);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected
    boolean recv(NetworkEvent event) {
        event.setType(NetworkEvent.SEND);
        event.setTarget(this);

        
        PairLinkEvent nxthps[] = getLinkEvents(event.getPDU());
        
        // System.out.println("swich"+this.id+" recvpkgto"+event.getPDU().getdestid()+" link"+link.getName());
        for (PairLinkEvent a: nxthps) {
        	if (buffer_size >= a.event.getPDU().size) {
        		this.getSendQueueByLink(a.link).add(a.event);
        		event.setRelatedLink(a.link);
        		buffer_size -= a.event.getPDU().size;
        	}
        else
            ;//System.err.println(this.getName()+"has to drop packets due to full queue");
        }
        return false;
    }
	protected abstract PairLinkEvent[] getLinkEvents(PDU pdu);

}
