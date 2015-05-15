package ProbeHisotry;

import java.util.Vector;

import Support.Coordinate;

public class ProbeHistoryTable {
	private Vector<ProbeHistoryTableEntry> v;
	public ProbeHistoryTable() {
		 v= new Vector<ProbeHistoryTableEntry>();
	}
	public void addEntry(ProbeHistoryTableEntry e) {
		int j = getFirstEntryIDbyCoordiante(e.coor);
		if (j>=0) {
			v.get(j).mergeEntry(e);
			return;
		}
		v.add(e);
		return;
	}
	private int getFirstEntryIDbyCoordinate(Coordinate c,int st) {
		for (int i = st ; i < v.size(); i++)
			if (v.get(i).coor.dist_to_switch(c) < c.eps())
				return i;
		return -1;
	}
	private int getFirstEntryIDbyCoordiante(Coordinate c){
		return getFirstEntryIDbyCoordinate(c,0);
	}
	public ProbeHistoryTableEntry getEntrybyCoordiante(Coordinate c) {
		for (ProbeHistoryTableEntry w: v) 
			if (w.coor.dist_to_switch(c)  < c.eps()) 
				return w;
		return null;
	}
}
