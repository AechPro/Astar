package openGLTests.main.pathing;

import java.awt.*;
import java.util.ArrayList;

import org.newdawn.slick.opengl.Texture;

public abstract class Node
{
	protected boolean saved;
	protected boolean removed;
	protected Texture texture;
	protected boolean overlayable;
	protected int backgroundTileID;
	protected int maxX;
	protected int maxY;
	protected boolean collidable;
	protected boolean fan;
	protected int velocity;
	protected int fanHeight;
	protected int x,y,h,g;
	protected boolean walkable,open,closed,home,dest,path,playerWalkable;
	protected boolean displaying,currentTile,validJumpPoint;
	protected ArrayList<Node> walkNodes, jumpNodes, fallNodes;
	protected Node parent;
	protected ArrayList<Node> reachableNodes;
	protected int ID;
	public Node(int _x, int _y, int id)
	{
		ID=id;
		x=_x;
		y=_y;
		h=g=0;
		walkable=true;
		playerWalkable=false;
		open=false;
		closed=false;
		home=false;
		dest=false;
		path=false;
		reachableNodes = new ArrayList<Node>();
	}
	public abstract void update(int x, int y);
	public abstract void render();
	public void render(Graphics2D g)
	{
		if(walkable){g.setColor(Color.GRAY);}
		if(!walkable){g.setColor(Color.BLACK);}
		if(path){g.setColor(Color.YELLOW);}
		if(home){g.setColor(Color.GREEN);}
		if(dest){g.setColor(Color.RED);}
		if(currentTile){g.setColor(Color.BLUE);}
		if(displaying){g.setColor(Color.PINK);}
		if(validJumpPoint){g.setColor(Color.magenta);}
		g.fillRect(x, y,32,32);
	}
	
	public ArrayList<Node> getReachableNodes(){return reachableNodes;}
	public boolean isOpen() {return open;}
	public int getH() {return h;}
	public int getG() {return g;}
	public boolean isPath(){return path;}
	public boolean isWalkable() {return walkable;}
	public boolean isClosed() {return closed;}
	public boolean isHome() {return home;}
	public boolean isDest() {return dest;}
	public boolean isPlayerWalkable(){return playerWalkable;}
	public int getX(){return x;}
	public int getY(){return y;}
	public int getF(){return g+h;}
	public Node getParent(){return parent;}
	public int getID() {return ID;}
	public boolean isSaved(){return saved;}
	public boolean isRemoved(){return removed;}
	public int getBackgroundTileID(){return backgroundTileID;}
	public boolean isOverlayable(){return overlayable;}
	public boolean isFan(){return fan;}
	public int getVelocity(){return velocity;}
	public int getFanHeight(){return fanHeight;}
	public ArrayList<Node> getJumpNodes(){return jumpNodes;}
	public ArrayList<Node> getFallNodes(){return fallNodes;}
	public ArrayList<Node> getWalkNodes(){return walkNodes;}
	
	public void setFan(boolean i){fan=i;}
	public void setID(int iD) {ID = iD;}
	public void setSaved(boolean i){saved=i;}
	public void setBackgroundTileID(int i){backgroundTileID=i;}
	public void setOverlayable(boolean i){overlayable=i;}
	public void setFanHeight(int i){fanHeight=i;}
	public void setVelocity(int i){velocity=i;}
	public void setValidJumpPoint(boolean i){validJumpPoint=i;}
	public void setCurrentTile(boolean i){currentTile=i;}
	public void setDisplaying(boolean i){displaying=i;}
	public void addReachableNode(Node i, int j)
	{
		reachableNodes.add(i);
	}
	public void setPlayerWalkable(boolean i){playerWalkable=i;}
	public void setH(int h) {this.h = h;}
	public void setG(int g) {this.g = g;}
	public void setWalkable(boolean walkable) {this.walkable = walkable;}
	public void setOpen(boolean open) {this.open = open;}
	public void setClosed(boolean closed) {this.closed = closed;}
	public void setHome(boolean home) 
	{
		this.home = home;
		setWalkable(home);
	}
	public void setDest(boolean dest) 
	{
		this.dest = dest;
		setWalkable(dest);
	}
	public void setDestination(Node d)
	{
		h = Math.abs(y - d.getY()) + Math.abs(x - d.getX());
	}
	public void addHeuristic(int i){h+=i;}
	public void setParent(Node p)
	{
		parent=p;
		g = parent.getG()+10;
	}
	public void setPath(boolean i){path=i;}
}
