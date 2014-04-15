package MSG;

import Support.Coordinate;
import Support.Entity;
import Support.PDU;

public abstract class Node implements Entity {
	@Override
	//abstract public String getName();
	abstract public Coordinate getCoor();
	abstract public void setCoordinate(Coordinate c);
	abstract protected Link getLink(PDU pdu);
}
