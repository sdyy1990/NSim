package ProbeHisotry;

import java.util.Vector;

import MSG.Link;
import MSG.PairLinkLink;
import Support.Coordinate;

public class ProbeHistoryTableEntry {
	public Coordinate coor;
	public Vector<PairLinkLink> links;
	public int status;
	public ProbeHistoryTableEntry(Coordinate coor, Link srclink, Link actlink, int status){
		this.coor = coor;
		this.links = new Vector<PairLinkLink>();
		if (srclink!=null)
			this.links.add(new PairLinkLink(srclink,actlink));
		
		this.status = status;
	}
	public static int SENT_BFS = 1;
	public static int ESTABLISHED = 2;
	public double distanceToCoor(Coordinate co) {
		return coor.dist_to_switch(co);
	}
	public void mergeEntry(ProbeHistoryTableEntry e) {
		this.status = e.status;
		this.links.addAll(e.links);
		
	}
	public void addLinkHistory(Link srclink, Link actlink) {
		this.links.add(new PairLinkLink(srclink,actlink));
	}
	public boolean matchActLink(Link link) {
		for (PairLinkLink pll:links)
			if (pll.act == link) return true;
		return false;
	}
}
