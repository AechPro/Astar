package openGLTests.main.pathing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.*;
public class Main extends JPanel implements Runnable
{
	private static final long serialVersionUID = -2193874754023463406L;
	private static final int width=1280;
	private static final int height=720;
	private static boolean running;
	private int curTile=0,nodeSize=32;
	private Thread thread;
	private BufferedImage image;
	private Graphics2D g;
	private int searchLeft, searchRight;
	private ArrayList<Node> path;
	private Map m;
	private ArrayList<Node> nodes;
	private Astar pathFinder;
	private int xSign = 1;
	private int ySign = 1;
	private boolean foundReachableNode=false;
	private int jumpHeight, fallHeight;
	private int mapWidth;
	private double jumpSpd, xSpd, gravity;

	public Main()
	{
		super();
		this.setFocusable(true);
		requestFocus();
	}
	public void addNotify()
	{
		super.addNotify();

		if(thread==null)
		{
			thread = new Thread(this);
			thread.start();
		}
	}
	public void init()
	{
		image = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		g = (Graphics2D) image.getGraphics();
		m = new Map(32);
		mapWidth = m.getWidth();
		path = new ArrayList<Node>();
		nodes = m.getNodes();
		for(int i=0;i<nodes.size();i++)
		{
			if(nodes.get(i).isHome()){curTile=i; break;}
		}
		jumpHeight=2;
		fallHeight=10;
		searchLeft=2;
		searchRight=3;

		jumpSpd=-8;
		gravity=0.5;
		xSpd=2;
		//ArrayList<Node> n, int jumpH, int fallH, int nodeS, int jumpS, int xS, int grav
		pathFinder = new Astar(nodes,fallHeight,nodeSize,jumpSpd,xSpd,gravity);
		//long t1=System.nanoTime();
		
		//initNodes();
		//System.out.println("initialized in: "+(System.nanoTime()-t1)/1000000+"ms");

		path = pathFinder.calculatePath();

		running=true;
	}
	public void run() 
	{
		init();
		while(running)
		{
			update();
			render();
			draw();
			delay(17);
		}
	}
	public void update()
	{

	}
	public void render()
	{
		clearCanvas();
		m.render(g);
		for(int i=0;i<path.size();i++)
		{
			path.get(i).render(g);
		}
	}
	public void draw()
	{
		Graphics g2 = getGraphics();
		g2.drawImage(image,0,0,null);
		g2.dispose();
	}

	public void initNodes()
	{
		jumpHeight = (int)((jumpSpd*(jumpSpd/gravity)-(gravity*(jumpSpd/gravity)*(jumpSpd/gravity)/2.0))/nodeSize);
		System.out.println(jumpHeight);
		double t[] = solveQuadratic(gravity/2, jumpSpd, jumpHeight);

		searchLeft=searchRight = (int)(xSpd*t[0]);
		if(t[0]<0){searchLeft=searchRight = (int)(xSpd*t[1]);}

		t = solveQuadratic(gravity/2, 0, fallHeight);

		if(t[0]<0 && (int)xSpd*t[1]>searchLeft){searchLeft=searchRight = (int)(xSpd*t[1]);}
		else if((int)xSpd*t[0]>searchLeft){searchLeft=searchRight = (int)(xSpd*t[0]);}
		searchLeft+=nodeSize-searchLeft%nodeSize;
		searchRight+=nodeSize-searchRight%nodeSize;
		System.out.println(searchLeft+" "+searchRight);

		int destNode=0;
		for(int i=0;i<nodes.size();i++)
		{
			if(nodes.get(i).isDest()){destNode=i;break;}
		}
		for(int i=0;i<nodes.size();i++)
		{
			nodes.get(i).setDestination(nodes.get(destNode));
			for(int x=nodes.get(i).getX();x<Math.abs(nodes.get(destNode).getX()-nodes.get(i).getX());x+=nodeSize*(nodes.get(destNode).getX()-nodes.get(i).getX())/Math.abs(nodes.get(destNode).getX()-nodes.get(i).getX()))
			{
				Node cur = getNode(x,nodes.get(i).getY());
				if(cur!=null && !cur.isWalkable()){nodes.get(i).addHeuristic(10);}
			}
			if(!nodes.get(i).isWalkable())
			{
				for(int j=0;j<nodes.size();j++)
				{
					if(nodes.get(j).getY()==nodes.get(i).getY()-32 && nodes.get(j).getX()==nodes.get(i).getX() && nodes.get(j).isWalkable())
					{
						nodes.get(j).setPlayerWalkable(true);
					}
				}
			}
		}
		for(int i=0;i<nodes.size();i++)
		{
			if(nodes.get(i).isPlayerWalkable())
			{
				findReachableNodes(nodes.get(i), i);

			}
		}
		System.out.println("exiting init");
	}

	public ArrayList<Node> calculatePath()
	{
		System.out.println("calculating path");
		ArrayList<Node> optPath = new ArrayList<Node>();
		Node cur = null;
		int otherG=0;
		int min=0;
		boolean found=false;
		boolean timedOut=false;
		long timePassed=System.nanoTime();
		while(!found && !timedOut)
		{
			min = 100000;
			for(int i=0,stop=nodes.size();i<stop;i++)
			{
				if(nodes.get(i).isClosed() && nodes.get(i).isDest())
				{
					cur = nodes.get(i);
					found=true;
					break;
				}
				else if(nodes.get(i).isOpen() && nodes.get(i).getF()<min)
				{
					min = nodes.get(i).getF();
					curTile = i;
				}
			}
			nodes.get(curTile).setClosed(true);
			nodes.get(curTile).setOpen(false);
			otherG = nodes.get(curTile).getG()+10;
			for(int i=0;i<nodes.get(curTile).getReachableNodes().size();i++)
			{
				nodes.get(curTile).getReachableNodes().get(i).setDisplaying(true);
			}
			for(int i=0,stop=nodes.size();i<stop;i++)
			{
				//if((System.nanoTime()-timePassed)/1000000000==4){timedOut=true;}
				if(i!=curTile && nodes.get(i).isPlayerWalkable() && nodes.get(i).isWalkable() && nodes.get(curTile).getReachableNodes().contains(nodes.get(i)))
				{
					//System.out.println(nodes.get(curTile).getReachableNodes().size());
					if(!nodes.get(i).isClosed())
					{
						if(nodes.get(i).isOpen())
						{
							if(otherG<nodes.get(i).getG())
							{
								nodes.get(i).setParent(nodes.get(curTile));
								nodes.get(i).setClosed(true);
							}
						}
						else
						{
							nodes.get(i).setOpen(true);
							nodes.get(i).setParent(nodes.get(curTile));
						}
					}
				}
			}
			render();
			draw();
			delay(100);
			for(int i=0;i<nodes.get(curTile).getReachableNodes().size();i++)
			{
				nodes.get(curTile).getReachableNodes().get(i).setDisplaying(false);
			}
		}

		if(timedOut){System.out.println("timed out"); return null;}
		while(cur != null && !cur.isHome())
		{
			optPath.add(cur);
			cur.setPath(true);
			cur = cur.getParent();
		}
		return optPath;
	}
	public void findReachableNodes(Node n, int index)
	{
		Node cur = null;
		//n.setPath(true);
		for(int x=n.getX();x>=n.getX()-searchLeft;x-=nodeSize)
		{

			cur = getNode(x,n.getY());
			if(cur != null)
			{
				if(!cur.isWalkable()){break;}
				Node temp = getNode(x,n.getY()+nodeSize);
				if(temp!= null && temp.isWalkable()){break;}
				else if(temp==null){break;}
				if(cur.isPlayerWalkable()){n.addReachableNode(cur);}
			}
		}
		for(int x=n.getX();x<=n.getX()+searchRight;x+=nodeSize)
		{
			cur = getNode(x,n.getY());
			if(cur != null)
			{
				if(!cur.isWalkable()){break;}
				Node temp = getNode(x,n.getY()+nodeSize);
				if(temp!= null && temp.isWalkable()){break;}
				else if(temp==null){break;}
				if(cur.isPlayerWalkable()){n.addReachableNode(cur);}
			}
		}
		for(int x=n.getX()-searchLeft;x<=n.getX()+searchRight;x+=nodeSize)
		{
			for(int y=n.getY()-jumpHeight*nodeSize;y<=n.getY()+fallHeight*nodeSize;y+=nodeSize)
			{
				cur = getNode(x,y);
				if(cur!=null && cur!=n && cur.isPlayerWalkable())
				{
					if(cur.getX()!=n.getX()){xSign = (cur.getX()-n.getX())/Math.abs(cur.getX()-n.getX());}
					if(cur.getY()!=n.getY()){ySign = (n.getY()-cur.getY())/Math.abs(cur.getY()-n.getY());}
					foundReachableNode=false;
					isReachable(cur.getX(),cur.getY(),n.getX(),n.getY());
					if(foundReachableNode)
					{
						//System.out.println(x+"   "+y);
						boolean flag=false;
						if(cur!= null && !n.getReachableNodes().contains(cur))
						{
							//int sign = (cur.getX()-n.getX())/Math.abs(cur.getX()-n.getX());
							//if(getNode(n.getX()+nodeSize*sign,n.getY()-nodeSize)!=null && !getNode(n.getX()+nodeSize*sign,n.getY()-nodeSize).isWalkable()){flag=true;}
							int w = 16;
							
							if(findTrajectory(n,cur,jumpSpd,w,32,xSpd,false)){n.addReachableNode(cur); flag=true;} // jump

							if(!flag)
							{
								if(findTrajectory(n,cur,0,w,32,xSpd,true)){n.addReachableNode(cur);}//fall
							}

							/*cur.setDisplaying(true);

							render();
							draw();
							delay(50);
							if(cur!=null)
							{
								cur.setDisplaying(false);
							}*/
						}
					}
				}
			}
		}
		//n.setPath(false);
	}
	public boolean findTrajectory(Node a, Node b, double jumpS, int width, int height, double xSpd, boolean falling)
	{
		//System.out.println("finding trajectory");
		//System.out.println(width+"   "+height);
		int startX=0;
		double t=0;
		double jT=0;
		double xS = xSpd;
		double y=0;
		double x=0;
		double grav = gravity;
		double jS=jumpS;
		double xOffset=0;
		double yOffset=0;
		boolean flag=false;
		boolean debugging=false;
		boolean topCollision=false;
		int peakY=0;
		if(debugging)
		{
			a.setPath(true);
			b.setCurrentTile(true);
			render();
		}
		boolean flag1 = false;
		int sign = (b.getX()-a.getX())/Math.abs(a.getX()-b.getX());
		if(falling)
		{
			if(b.getX()<a.getX()){startX=-width-1;}
			else if(b.getX()>a.getX()){startX=nodeSize+1;}
		}
		if(!falling)
		{
			for(int p=0;p<nodeSize - width;p++)
			{
				jS=jumpS;
				grav=gravity;
				jT=0;
				t=0;
				topCollision=false;
				xS=xSpd;
				flag1=false;
				for(double i=0;i<Math.abs(a.getX()-b.getX())+nodeSize*2;i+=xSpd,t++)
				{
					//System.out.println(i);
					if(xS!=0){x = (xS*t+startX)*sign+p;}
					if(jS!=0 || grav!=0)
					{
						//System.out.println(jT+"   "+t+"    "+topCollision+"    "+(grav/2.0)*Math.pow(jT,2)+"    "+jS);
						y = (jS*jT) + (grav/2.0)*t*t;
					}
					if((int)y>peakY){peakY=(int)y;}
					Rectangle r1 = new Rectangle((int)(a.getX()+x),(int)(a.getY()+y),width,height);
					Rectangle r2 = new Rectangle(b.getX(),b.getY(),nodeSize,nodeSize);
					if(debugging)
					{
						render();
						g.setColor(Color.MAGENTA);
						g.fillRect((int)(a.getX()+x),(int)(a.getY()+y),width,height);
						draw();
					}
					xOffset=0;
					yOffset=0;
					for(int j=0;j<nodes.size();j++)
					{
						if(!nodes.get(j).isWalkable())
						{
							Rectangle r3 = new Rectangle(nodes.get(j).getX(),nodes.get(j).getY(),nodeSize,nodeSize);
							if(r1.intersects(r3))
							{
								double angle=0;
								angle = Math.atan2(Math.toDegrees((double)(nodes.get(j).getY()+nodeSize/2.0)-(a.getY()+y+height/2.0)),Math.toDegrees((double)(nodes.get(j).getX()+nodeSize/2.0)-(a.getX()+x+width/2.0)));
								//System.out.println(angle);
								angle*=180.0;
								angle/=-Math.PI;
								if(angle<0){angle=360-Math.abs(angle);}
								//System.out.println(angle);

								/*g.setColor(Color.YELLOW);
								g.drawLine((int)(nodes.get(j).getX()+nodeSize/2.0),(int)(nodes.get(j).getY()+nodeSize/2.0),(int)(a.getX()+x+width/2.0),(int)(a.getY()+y+height/2.0));
								draw();*/
								//System.out.println(angle);
								yOffset=Math.abs((a.getY() + y + height/2) -(nodes.get(j).getY()+nodeSize/2)) - (nodeSize/2 + nodeSize/2);
								xOffset = Math.abs((a.getX() + x + width/2)-(nodes.get(j).getX()+nodeSize/2)) - (nodeSize/2 + nodeSize/2);
								if((angle<44 || angle>316) || (angle>136 && angle<224))
								{
									xS=0;
									x+=xOffset*sign;
								}
								else if((angle>46 && angle<134))
								{
									//jT=t;
									//System.out.println(yOffset);
									topCollision=true;
									y-=yOffset;

									//System.out.println("top collision");
								}
								else if((angle>226 && angle<314))
								{
									//System.out.println("bottom collision");
									grav=jS=xS=0;
									y+=yOffset;
								}
							}
						}
					}
					if(!topCollision){jT++;}
					if(r1.intersects(r2)&& !flag1)
					{
						double d = Math.sqrt(Math.pow((a.getX()+x+width/2) - (b.getX()+nodeSize/2),2)+Math.pow((a.getY()+y+height/2) - (b.getY()+nodeSize/2),2));
						//System.out.println(d);
						if(d<=nodeSize/1.5)
						{
							//System.out.println("pathable node found "+(a.getY()+y)+"   "+(b.getY()));
							flag=true;
							if(Math.abs(b.getY()-a.getY()+peakY)/32>fallHeight){flag=false;}
							break;
						}

					}
					//System.out.println(t);
					//System.out.println("("+x+","+y+")");
				}
				if(flag){break;}
			}
		}
		else if(falling || nodeSize<=width)
		{
			for(double i=0;i<Math.abs(a.getX()-b.getX())+nodeSize*2;i+=xSpd,t++)
			{
				//System.out.println(i);
				if(xS!=0){x = (xS*sign*t+startX);}
				if(jS!=0 || grav!=0)
				{
					//System.out.println(jT+"   "+t+"    "+topCollision+"    "+(grav/2.0)*Math.pow(t,2)+"    "+jS);
					y = (jS*jT) + (grav/2.0)*t*t;
				}
				if((int)y>peakY){peakY=(int)y;}
				Rectangle r1 = new Rectangle((int)(a.getX()+x),(int)(a.getY()+y),width,height);
				Rectangle r2 = new Rectangle(b.getX(),b.getY(),nodeSize,nodeSize);
				if(debugging)
				{
					render();
					g.setColor(Color.MAGENTA);
					g.fillRect((int)(a.getX()+x),(int)(a.getY()+y),width,height);
					draw();
					delay(1);
				}
				xOffset=0;
				yOffset=0;
				for(int j=0;j<nodes.size();j++)
				{
					if(!nodes.get(j).isWalkable())
					{
						Rectangle r3 = new Rectangle(nodes.get(j).getX(),nodes.get(j).getY(),nodeSize,nodeSize);
						if(r1.intersects(r3))
						{
							double angle=0;
							angle = Math.atan2(Math.toDegrees((double)(nodes.get(j).getY()+nodeSize/2.0)-(a.getY()+y+height/2.0)),Math.toDegrees((double)(nodes.get(j).getX()+nodeSize/2.0)-(a.getX()+x+width/2.0)));
							//System.out.println(angle);
							angle*=180.0;
							angle/=-Math.PI;
							if(angle<0){angle=360-Math.abs(angle);}
							//System.out.println(angle);

							/*g.setColor(Color.YELLOW);
							g.drawLine((int)(nodes.get(j).getX()+nodeSize/2.0),(int)(nodes.get(j).getY()+nodeSize/2.0),(int)(a.getX()+x+width/2.0),(int)(a.getY()+y+height/2.0));
							draw();*/
							//System.out.println(angle);
							yOffset=Math.abs((a.getY() + y + height/2) -(nodes.get(j).getY()+nodeSize/2)) - (nodeSize/2 + nodeSize/2);
							xOffset = Math.abs((a.getX() + x + width/2)-(nodes.get(j).getX()+nodeSize/2)) - (nodeSize/2 + nodeSize/2);
							if((angle<44 || angle>316) || (angle>136 && angle<224))
							{
								xS=0;
								x+=xOffset*sign;
							}
							else if((angle>46 && angle<134))
							{
								jT=t;
								//System.out.println(yOffset);
								topCollision=true;
								y-=yOffset;

								//System.out.println("top collision");
							}
							else if((angle>226 && angle<314))
							{
								//System.out.println("bottom collision");
								grav=jS=xS=0;
								y+=yOffset;
							}
						}
					}
				}
				if(!topCollision){jT++;}
				if(r1.intersects(r2)&& !flag1)
				{
					double d = Math.sqrt(Math.pow((a.getX()+x+width/2) - (b.getX()+nodeSize/2),2)+Math.pow((a.getY()+y+height/2) - (b.getY()+nodeSize/2),2));
					//System.out.println(d);
					if(d<=nodeSize/1.5)
					{
						//System.out.println("pathable node found "+(a.getY()+y)+"   "+(b.getY()));
						flag=true;
						if(Math.abs(b.getY()-a.getY()+peakY)/32>fallHeight){flag=false;}
						break;
					}

				}
				//System.out.println(t);
				//System.out.println("("+x+","+y+")");
			}
		}
		//System.out.println(yC.size());
		if(debugging)
		{
			if(flag)
			{
				b.setDisplaying(true);
				render();
				draw();
				delay(1000);
				b.setDisplaying(false);
			}
			b.setCurrentTile(false);
			a.setPath(false);
		}
		return flag;
	}
	public void isReachable(int nx, int ny, int gx, int gy)
	{
		/*getNode(nx,ny).setCurrentTile(true);
		getNode(gx,gy).setPath(true);
		render();
		draw();
		//delay(50);
		getNode(nx,ny).setCurrentTile(false);
		getNode(gx,gy).setPath(false);*/
		if(nx==gx && ny==gy && !foundReachableNode)
		{

			foundReachableNode=true;
			return;
		}
		if(getNode(nx,ny) != null && !getNode(nx,ny).isWalkable() && !foundReachableNode)
		{
			foundReachableNode=false;
			return;
		}
		if(nx!=gx){isReachable(nx-nodeSize*xSign,ny,gx,gy);}
		if(ny!=gy){isReachable(nx,ny+(nodeSize*ySign),gx,gy);}
		return;
	}
	public double[] solveQuadratic(double a, double b, double c)
	{
		return new double[] {(-b + Math.sqrt(Math.pow(b,2) - 4*a*c))/(2*a),(-b - Math.sqrt(Math.pow(b,2) - 4*a*c))/(2*a)};
	}
	public Node getNode(int x, int y)
	{
		for(int i=0;i<nodes.size();i++)
		{
			if(nodes.get(i).getX()==x && nodes.get(i).getY()==y){return nodes.get(i);}
		}
		return null;
	}
	public void clearCanvas()
	{
		g.clearRect(0, 0, width, height);
		g.setColor(Color.WHITE);
		g.fillRect(0,0,width,height);
	}
	public void delay(int millis)
	{
		try{Thread.sleep(millis);}
		catch(Exception e){e.printStackTrace();}
	}
	public static void main(String[] args)
	{
		JFrame frame = new JFrame("Main");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new Main());
		frame.setSize(width+16,height+38);
		frame.setVisible(true);
	}
}
