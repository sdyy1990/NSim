package MSG;

import java.util.Vector;

import ProbeHisotry.ProbeHistoryTable;
import ProbeHisotry.ProbeHistoryTableEntry;
import SimpleBed.SimpleHost;
import Support.Coordinate;
import Support.NetworkEvent;
import Support.PDU;
import TCP.TCPCoorMessage;
import TCP.TCPRecoveryCoorMessage;

public class MSGRecoverySwitch extends Switch {
	protected Vector<Boolean> portActive;
	public MSGRecoverySwitch(Coordinate coor, int port, int id,
			int routing_hop, boolean flex_space) {
		super(coor, port, id, routing_hop, flex_space);
		portActive = new Vector<Boolean>(port);
	}
	@Override
    public void addNeighbour(Link l, Node neigh) {
        super.addNeighbour(l, neigh);
        portActive.add(true);
    }
	@Override
    public void addHost(Link l, SimpleHost h0) {
		super.addHost(l, h0);
		portActive.addElement(true);
    }
    
	@Override
    protected PairLinkEvent[] getLink(NetworkEvent e) {
		PDU u = e.getPDU();
		switch (((TCPRecoveryCoorMessage) u).type) {
			case TCPRecoveryCoorMessage.REGULAR_TYPE: 
				return getLinkRegular(e);
			case TCPRecoveryCoorMessage.BFS_PROBE_TYPE:
				return getLinkBFSProbe(e);
			case TCPRecoveryCoorMessage.BFS_REPLY_TYPE:
				return getLinkReply(e);
		}
		return null;
	}
	private PairLinkEvent[] getLinkReply(NetworkEvent e) {
		// TODO Auto-generated method stub
		return null;
	}
	private PairLinkEvent[] getLinkBFSProbe(NetworkEvent e) {
		PDU pdu = e.getPDU();
		//TODO
		return null;
	}
	private Link getsourceLinkByPDU(PDU pdu) {
		Coordinate coor2 = ((TCPCoorMessage) pdu).getdestCoor();
		
        return null;
	}

	private PairLinkEvent[] getLinkRegular(NetworkEvent e) {
		PDU pdu = e.getPDU();
        Integer srcDest = Integer.valueOf(pdu.getsrcid()*1024+pdu.getdestid());

        int subid;
        if (!flowSrcDestLst.containsKey(srcDest)) {
            flowSrcDestLst.put(srcDest, new Vector<Integer>());
            flowSrcDestLst.get(srcDest).add(Integer.valueOf(pdu.getflowid()));
        }
        else if (flowSrcDestLst.get(srcDest).indexOf(Integer.valueOf(pdu.getflowid()))<0)
            flowSrcDestLst.get(srcDest).add(Integer.valueOf(pdu.getflowid()));
        subid = flowSrcDestLst.get(srcDest).indexOf(Integer.valueOf(pdu.getflowid()));

        Coordinate coor2 = ((TCPCoorMessage) pdu).getdestCoor();
        double dist = calculation_dist(this.coor,coor2,pdu);
        int who = -1;
        for (int i = 0 ; i < this.neighbours.size(); i++) {
            double d = coor2.dist_to_switch(((Switch)neighbours.get(i)).getCoor());
            if (d<dist) {
                who = i;
                dist= d;
            }
        }
        if (who < 0)  {
        	if (coor2.dist_to_switch(this.coor)<this.coor.eps())
        	{ 
        		PairLinkEvent ans[] = new PairLinkEvent[1];
        		ans[0] = new PairLinkEvent(e, getHostLink(coor2));
        		return ans;
        	}
        	return startBFS(e);
        	
        }
        
		return null;
	}
	private PairLinkEvent[] startBFS(NetworkEvent e) {
    	//For each neighbor send a probe message
    	//add this to probeHistory Table, this is BFSstarter.
		Link srclink = e.lastTransmittedLink();
		ProbeHistoryTableEntry p = probeHistory.getEntrybyCoordiante(((TCPCoorMessage) e.getPDU()).getdestCoor());
		if (p!=null) {
			
			//for each neigbhor that has not sent a BFS packet to, send a BFS packet to it.
			PairLinkEvent[] ans = new PairLinkEvent[this.neighbours.size()];
			 for (int i = 0 ; i < this.neighbours.size(); i++) 
			 	if (this.portActive.get(i+hosts.size())){
	               Link link = ports.get(i+hosts.size());
	               if (link == srclink) continue;
	               if (p.matchActLink(link)) continue;
	               p.addLinkHistory(srclink, link);
	               NetworkEvent enew = (NetworkEvent) e.duplicate();
	               ((TCPRecoveryCoorMessage) enew.getPDU()).type = TCPRecoveryCoorMessage.BFS_PROBE_TYPE;
	               ans[i] = new PairLinkEvent(enew, link);
	           }
			 	
			return null;
		}
		 probeHistory.addEntry(new ProbeHistoryTableEntry(((TCPCoorMessage) e.getPDU()).getdestCoor(), null,null, ProbeHistoryTableEntry.SENT_BFS));
		 PairLinkEvent[] ans = new PairLinkEvent[this.neighbours.size()];
		 for (int i = 0 ; i < this.neighbours.size(); i++) 
		 	if (this.portActive.get(i+hosts.size())){
               Link link = ports.get(i+hosts.size());
               p.addLinkHistory(srclink, link);
               NetworkEvent enew = (NetworkEvent) e.duplicate();
               ((TCPRecoveryCoorMessage) enew.getPDU()).type = TCPRecoveryCoorMessage.BFS_PROBE_TYPE;
               ans[i] = new PairLinkEvent(enew, link);
           }
		 return ans;
	}
	private ProbeHistoryTable probeHistory;
	
	
	
}
