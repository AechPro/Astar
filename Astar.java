package openGLTests.main.pathing;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Astar 
{
	private int curTile=0;
	private ArrayList<Node> nodes;
	private Semaphore mutex;
	private Initializer initializer;
	public Astar(ArrayList<Node> n, int fallH, int nodeS, double jumpS, double xS, double grav, Semaphore mut)
	{
		mutex=mut;
		initializer = new Initializer(n,fallH,nodeS,jumpS,xS,grav);
		long t1=System.nanoTime();
		nodes = initializer.initNodes();
		//System.out.println("initialization completed in: "+(System.nanoTime()-t1)/1000000+"ms");
	}
	public ArrayList<Node> calculatePath()
	{
		try
		{
			mutex.acquire();
		
		if(nodes == null){return null;}
		
		for(int i=0;i<nodes.size();i++)
		{
			if(nodes.get(i).isHome()){curTile=i;break;}
		}
		int destNode=0;
		boolean hasHome=false, hasDest=false;
		for(int i=0;i<nodes.size();i++)
		{
			if(nodes.get(i).isDest()){destNode=i; hasDest=true;}
			if(nodes.get(i).isHome()){hasHome=true;}
			nodes.get(i).setPath(false);
		}
		if(!hasHome || !hasDest){return null;}
		System.out.println("beginning calculation");
		for(int i=0;i<nodes.size();i++)
		{
			nodes.get(i).setDestination(nodes.get(destNode));
		}
		ArrayList<Node> optPath = new ArrayList<Node>();
		Node cur = null;
		int otherG=0;
		int min=0;
		boolean found=false;
		boolean timedOut=false;
		long timePassed=System.nanoTime();
		while(!found && !timedOut)
		{
			min = 1000000000;
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
					//System.out.println(curTile);
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
				if((System.nanoTime()-timePassed)/1000000000==1){timedOut=true;}
				if(i!=curTile && nodes.get(i).isPlayerWalkable() && nodes.get(curTile).getReachableNodes().contains(nodes.get(i)))
				{
					//System.out.println(nodes.get(curTile).getReachableNodes().size());
					if(!nodes.get(i).isClosed())
					{
						if(nodes.get(i).isOpen())
						{
							//System.out.println("checking open tile "+otherG+" "+nodes.get(i).getG());
							if(otherG<nodes.get(i).getG())
							{
								//System.out.println("found path tile");
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
		}

		if(timedOut){System.out.println("timed out"); return null;}
		while(cur != null && !cur.isHome())
		{
			//System.out.println("creating path");
			optPath.add(cur);
			cur.setPath(true);
			cur = cur.getParent();
		}
		mutex.release();
		System.out.println("ending calculation");
		return optPath;
		}catch(Exception e){}
		
		return null;
	}
}
