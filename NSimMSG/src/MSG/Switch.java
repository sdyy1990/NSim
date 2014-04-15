package MSG;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import Support.Coordinate;
import Support.EventManager;
import Support.NetworkEvent;
import Support.PDU;
import Support.Simusys;
public class Switch extends Node{
	private int port;
	public int id;
	protected Vector<Link> ports;
	private Vector< Vector<NetworkEvent>> send_events;
	private Vector< Vector<NetworkEvent>> recv_events;
	public Vector< Node > neighbours;
	public Vector<Host> hosts;
	private int buffer_size;
	public Coordinate coor;
	private int routing_neighbour_hop;
	private boolean flex_space_routing;
	protected Map< Integer, Vector<Integer> > flowSrcDestLst;
	private Map< Integer, Link > routeHistory;
	
	public Switch( Coordinate coor, int port , int id , int routing_hop, boolean flex_space){
		this.routing_neighbour_hop = routing_hop;
		this.flex_space_routing = flex_space;
		this.port = port;
		this.coor = coor;
		this.ports = new Vector<Link>();
		this.neighbours = new Vector<Node>();
		this.hosts = new Vector<Host>();
		buffer_size = port*1000*Link.PSIZE;
		send_events = new Vector<Vector<NetworkEvent>> ();
		recv_events = new Vector<Vector<NetworkEvent>> ();
		routeHistory = new HashMap<Integer,Link> ();
		flowSrcDestLst = new HashMap< Integer, Vector<Integer> >();
		for (int i = 0 ; i <=port; i++){
			send_events.add(new Vector<NetworkEvent>());
			recv_events.add(new Vector<NetworkEvent>());
		}
		EventManager.register(this);
		this.id = id;
	}
	public void addNeighbour(Link l, Node neigh){
		this.neighbours.add(neigh);
		this.ports.add(l);
	}
	public void addHost(Link l, Host host) {
		if (this.neighbours.size()>0) {
			System.err.println("err: add Neighbour before adding host");
		}
		if (this.ports.size()>=this.port) {
			System.err.println("err: no avialiable ports");
		}
		this.hosts.add(host);
		this.ports.add(l);
	}
	
	@Override
	public String getState() {
		return "["+buffer_size+"]";
	}

	@Override
	public boolean performEvent(NetworkEvent event) {
		
		if (event.getTarget()== this){
			if (event.getType()==NetworkEvent.SEND)
				return send(event);
			else if (event.getTime()==Simusys.time() && event.getType() == NetworkEvent.RECEIVE) {
				event.getRelatedLink().increaseQueueSize(this, event.getPDU().size);
				return recv(event);
			}
		}
		else 
			System.err.println("event"+event.hashCode()+"Scheduler fail: Unmatched event target=" + event.getTarget().getName() +" this="+getName() +"link = "+event.getRelatedLink().hashCode()+
					    " expected at " + event.getTime()
					  + ", at " + Simusys.time());

		return false;
	}

	private boolean recv(NetworkEvent event) {
		event.setType(NetworkEvent.SEND);
		event.setTarget(this);
		
		Link link = null;

		Integer myid = Integer.valueOf(event.getPDU().getsrcid()*1024+event.getPDU().getdestid()*1048576+event.getPDU().getflowid());
		if (routeHistory.containsKey(myid)) link = routeHistory.get(myid);
		else 
		{
	    link = getLink(event.getPDU());
	    if (link==null) {
	    	link = getLink(event.getPDU());
	    }
	    routeHistory.put(myid, link);
		}
	   // System.out.println("swich"+this.id+" recvpkgto"+event.getPDU().getdestid()+" link"+link.getName());
	    if (buffer_size >= event.getPDU().size) {
			this.getSendQueueByLink(link).add(event);
			event.setRelatedLink(link);
			buffer_size -= event.getPDU().size;
		}
		else 
			;//System.err.println(this.getName()+"has to drop packets due to full queue");
		
		return false;
	}

	private Vector<NetworkEvent> getSendQueueByLink(Link link) {
		return send_events.get(ports.indexOf(link));
	}
	protected double calculation_dist(Coordinate a, Coordinate b, PDU pdu) {
		if (this.flex_space_routing)
			return a.dist_to_switch(b);
		else
		    return a.dist_to_switch_fixdim(b,pdu.getflowid());
	}
	protected Link getLink(PDU pdu) {
		
		Integer srcDest = Integer.valueOf(pdu.getsrcid()*1024+pdu.getdestid());
		
		int subid;
		if (!flowSrcDestLst.containsKey(srcDest)){
			flowSrcDestLst.put(srcDest, new Vector<Integer>());
			flowSrcDestLst.get(srcDest).add(Integer.valueOf(pdu.getflowid()));
		}
		else 
			if (flowSrcDestLst.get(srcDest).indexOf(Integer.valueOf(pdu.getflowid()))<0)
				flowSrcDestLst.get(srcDest).add(Integer.valueOf(pdu.getflowid()));
		subid = flowSrcDestLst.get(srcDest).indexOf(Integer.valueOf(pdu.getflowid()));
		
		Coordinate coor2 = pdu.getdestCoor();
		if (this.routing_neighbour_hop ==0) return null;
	    double dist = calculation_dist(this.coor,coor2,pdu);
	    int who = -1;
        for (int i = 0 ; i < this.neighbours.size(); i++) {
         	double d = coor2.dist_to_switch(neighbours.get(i).getCoor());
	       	if (d<dist) { who = i; dist= d;} 
	    }
        if (who < 0)  return getHostLink(coor2);
        if (pdu.first_hop) {
        	if (pdu.first_hop)
        	pdu.first_hop = false;
        	else pdu.second_hop = false;
        	
        	//if (dist < 1e-8) return ports.get(who+hosts.size()); //1hop neigh;
        	Vector<Link> possible_2hop = new Vector<Link>();
        	
        	possible_2hop.add(ports.get(who+hosts.size()));
        	
        	for (int i = 0 ; i < this.neighbours.size();i++) {
        		Switch neigh = (Switch) this.neighbours.get(i);
        		//if (dist >  coor2.dist_to_switch(neigh.getCoor()))
        		for (Node nn: neigh.neighbours) if (nn!=this){
        			double d = coor2.dist_to_switch(nn.getCoor());
        	       	if (d<dist) {
        	       		if (possible_2hop.indexOf(ports.get(i+hosts.size()))<0)
            				possible_2hop.add(ports.get(i+hosts.size()));
        	       		break;}
        		}
        	}
        	if (possible_2hop.size()>0) {
        	if (Simusys.loadAwareIsOn()) {
        		sortLinksByLoad(possible_2hop);
        		return possible_2hop.get(subid % possible_2hop.size());
        	}else 
        		return possible_2hop.get(randomIdWithUpperBound(pdu.getflowid(), possible_2hop.size()) );
        	}
        } 
        who = -1;
        dist = 1e10;
        return getLink_OCT30(pdu);
	}
	int randomIdWithUpperBound(int seed, int up) {
		return seed % up;
	}
	protected void sortLinksByLoad(Vector<Link> VL) {
		for (int i = 0 ; i < VL.size(); i++)
			for (int j = i+1 ; j < VL.size(); j++)
				if (VL.get(i).flowsOnMe.size() > VL.get(j).flowsOnMe.size())
					Collections.swap(VL,i,j);
		
	}
	protected Link getLink_Nov1(PDU pdu) {
		Coordinate coor2 = pdu.getdestCoor();
		if (this.routing_neighbour_hop ==0) return null;
	    double dist = this.coor.dist_to_switch(coor2);
	    double dist0 = dist;
	    Vector<Link> candidate = new Vector<Link>();
	    int [] spaces = new int [100];  
        if (dist < 1e-9) return getHostLink(coor2);
        for (int i = 0 ; i < this.neighbours.size(); i++) {
         	double d = coor2.dist_to_switch(neighbours.get(i).getCoor());
	       	if (d< 1e-9)  //candidate.add(ports.get(hosts.size()+i));
	       		return ports.get(hosts.size()+i);
	    }
        if (pdu.first_hop) {
        	pdu.first_hop= false;
        	for (int i = 0 ; i < this.neighbours.size();i++) {
        		Switch neigh = (Switch) this.neighbours.get(i);
        		//if (dist0 > calculation_dist(coor2,neigh.getCoor(),pdu))
        			for (Node nn: neigh.neighbours) if (nn!=this){
        				double d = coor2.dist_to_switch(nn.getCoor());
        				if (d<1e-9) //candidate.add(ports.get(hosts.size()+i));
        					return ports.get(hosts.size()+i);
        			}
        	}
        	for (int space = 0 ; space < coor2.getdim(); space ++) {
        		double mm = coor2.dist_to_switch_fixdim(this.coor, space);
        		double mm0 = mm;
        		int who = -1;
        		for (int i = 0 ; i < this.neighbours.size();i++) {
        			Switch neigh = (Switch) this.neighbours.get(i);
        	//		if (mm0 > coor2.dist_to_switch_fixdim(neigh.getCoor(),space))
        				for (Node nn: neigh.neighbours) if (nn!=this){
        					double d = coor2.dist_to_switch_fixdim(nn.getCoor(),space);
        					if (d<mm){ mm = d; who = i; } 
        				}
        		}
        		if (who<0) continue;
        		if (candidate.indexOf(ports.get(hosts.size()+who))<0){
        			candidate.add(ports.get(hosts.size()+who));
        			spaces[candidate.indexOf(ports.get(hosts.size()+who))] = space;
        		}
        	
        	}
        	int tod = Simusys.rand.nextInt(candidate.size());
        	pdu.routingSpace = spaces[tod];
        	return candidate.get(tod);
        }
        if (candidate.size() >0) return candidate.get(0);
        if (pdu.routingSpace < 0) System.out.println("Errrrrr");
        
        double dd = coor.dist_to_switch_fixdim(coor2,pdu.routingSpace);
        double dd0 = dd;
        int who = -1;
        for (int i = 0 ; i < this.neighbours.size(); i++) {
         	double d = coor2.dist_to_switch_fixdim(neighbours.get(i).getCoor(),pdu.routingSpace);
	       	if (d<dd) { who = i; dist= d;} 
	    }
        for (int i = 0 ; i < this.neighbours.size();i++) {
        	Switch neigh = (Switch) this.neighbours.get(i);
       // 	if (dd0 > calculation_dist(coor2,neigh.getCoor(),pdu))
        	for (Node nn: neigh.neighbours) if (nn!=this){
        		double d = coor2.dist_to_switch_fixdim(nn.getCoor(),pdu.routingSpace);
               	if (d<dd) { who = i; dd= d;}
        	}
        }
        
        
        if (who >=0) return ports.get(hosts.size()+who);
        return getHostLink(coor2);
        	
	}
	protected Link getLink_OCT30(PDU pdu) {
		
		Coordinate coor2 = pdu.getdestCoor();
		if (this.routing_neighbour_hop ==0) return null;
	    double dist = calculation_dist(this.coor,coor2,pdu);
	    double dist0 = dist;
        int who = -1;
        for (int i = 0 ; i < this.neighbours.size(); i++) {
         	double d = calculation_dist(coor2,neighbours.get(i).getCoor(),pdu);
	       	if (d<dist) { who = i; dist= d;} 
	    }
        if (this.routing_neighbour_hop ==1) {
	        if (who >= 0) return ports.get(who+this.hosts.size());
        } else {
        	for (int i = 0 ; i < this.neighbours.size();i++) {
        		Switch neigh = (Switch) this.neighbours.get(i);
        		if (dist0 > calculation_dist(coor2,neigh.getCoor(),pdu))
        		for (Node nn: neigh.neighbours) if (nn!=this){
        			double d = calculation_dist(coor2,nn.getCoor(),pdu);
        	       	if (d<dist) { who = i; dist= d;}
        		}
        	}
        	
        }
        if (dist < 1e-8) dist=dist0/2;
	    if (who >=0) {
	    	/*
	    	Vector<Link> possible = new Vector<Link>();
	    	for (int i = 0 ; i < this.neighbours.size(); i++) {
	         	double d = calculation_dist(coor2,neighbours.get(i).getCoor(),pdu);
		       	if (d<dist*1.00005 && d <dist0) possible.add(ports.get(hosts.size()+i)); 
		    }
	        if (this.routing_neighbour_hop ==2) {
		        for (int i = 0 ; i < this.neighbours.size();i++) {
	        		Switch neigh = (Switch) this.neighbours.get(i);
	        		//if (dist0 > calculation_dist(coor2,neigh.getCoor(),pdu))
	        		for (Node nn: neigh.neighbours) if (nn!=this){
	        			double d = calculation_dist(coor2,nn.getCoor(),pdu);
	        	       	if (d<dist*1.0000005 && d < dist0) {possible.add(ports.get(hosts.size()+i)); break;}
	        		}
	        	}
	        	
	        
	        }*/
	        return ports.get(hosts.size()+who);
	        //return possible.get(Simusys.rand.nextInt(possible.size()));
	        
	    }
        //if (who >= 0) return ports.get(who+this.hosts.size());
	    //route to this switch.
	    return getHostLink(coor2);
	
	}

	private Link getHostLink(Coordinate coor2) {
		int who = -1;
		double dist = Coordinate.max_coordinate;
		for (int i = 0; i< this.hosts.size(); i++) {
    	double d = coor2.dist_to_Host(this.hosts.get(i).getCoor());
    	if (d<dist) {
    		who = i;
    		dist = d;
    	}
    }
    return ports.get(who);	
	}
	private boolean send(NetworkEvent event) {
		if (!event.getRelatedLink().hasMoreSpace(this, event.getPDU().size))
			return false;
		//event.setTarget(this);
		buffer_size += event.getPDU().size;
		event.getRelatedLink().transmit(event);
		return true;
	}

	@Override
	public boolean performEventsAt(long tick) {
		for (int i = 0; i < send_events.size(); i++) {
			Vector<NetworkEvent> sb = send_events.get(i);
			while (!sb.isEmpty()) {
				NetworkEvent re = sb.get(0);
				if (!this.performEvent(re))
					break;
				sb.remove(0);
			}
		}
		
		return true;
	}

	@Override
	public boolean performPendingEventsAt(long tick) {
		Vector<Vector<NetworkEvent>> rb = new Vector<Vector<NetworkEvent>>();
		int [] a = new int[recv_events.size()];
		for (int i = 0; i < recv_events.size(); i++) {
			rb.add(recv_events.get(i));
			a[i] = i;
		}
		while (!rb.isEmpty()) {
			int i = Simusys.rand.nextInt(rb.size());
			if (rb.get(i).isEmpty()) {
				rb.remove(i);
				continue;
			}

			NetworkEvent se = rb.get(i).get(0);
			if (!this.performEvent(se)) {
				rb.get(i).remove(0);
				//rb.remove(i);
				continue;
			}
		}
		
		return true;
		
	}

	@Override
	public void addEvent(NetworkEvent e) {
		//System.out.print("adding event time"+e.getTime()+" target=" + e.getTarget().getName() +" this="+getName()+  "  link="+e.getRelatedLink().toString());
		//System.out.println("  que="+getRecvQueueByLink(e.getRelatedLink()).hashCode() +"  linkhash="+e.getRelatedLink().hashCode() + "event"+e.hashCode());
		if (e.getType()==NetworkEvent.RECEIVE){
			this.getRecvQueueByLink(e.getRelatedLink()).add(e);
		}
		
	}

	private Vector<NetworkEvent> getRecvQueueByLink(Link relatedLink) {
		return recv_events.get(ports.indexOf(relatedLink));
	}
	public String getName(){
		return "S"+id;
	}

	public Coordinate getCoor() {		return coor;	}
	public void setCoordinate(Coordinate c) {this.coor = c;	}
	public int getNeighbourCount() {
		return neighbours.size();
	}
	public Vector<Node> getNeighbour(){
		return neighbours;
	}
}
