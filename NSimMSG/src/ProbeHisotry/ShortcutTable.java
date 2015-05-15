package ProbeHisotry;

import java.util.Vector;
import MSG.Link;
import Support.Coordinate;

public class ShortcutTable {
	public Vector<ShortcutTableEntry> v;
	public ShortcutTable() {
		v  = new Vector<ShortcutTableEntry>();
	}
	public Link findLinkByCoor(Coordinate x) {
		for (ShortcutTableEntry t:v)
			if (t.coor.dist_to_switch(x) < x.eps()) {
				return t.link;
			}
		return null;
	}
	public void addShortcut(Coordinate x, Link l) {
		v.add(new ShortcutTableEntry(x,l));
	}
}
