package openGLTests.main.pathing;
import java.awt.Graphics2D;
import java.io.*;
import java.util.ArrayList;
import openGLTests.main.Tile;
public class Map
{
	private int tileSize;
	private ArrayList<Node> tiles;
	private int mapWidth;
	private int mapHeight;
	private int destTile=0;
	public Map (int tileSize)
	{
		tiles = new ArrayList<Node>();
		loadData("bin/openGLTests/main/pathing/testMap.txt");
		//loadData(this.getClass().getResource("testMap.txt").toString().replace("rsrc:",""));
		this.tileSize = tileSize;
	}

	public void loadData(String fileName)
	{
		System.out.println("loading");
		int x=0;
		int y=0;
		tiles = new ArrayList<Node>();
		int tileID=0;
		try 
		{
			BufferedReader s = new BufferedReader(new FileReader(fileName));
			mapWidth = Integer.parseInt(s.readLine());
			mapHeight = Integer.parseInt(s.readLine());
			String delimiters = " ";
			int counter=0;
			for(int i=0;i<mapHeight;i++)
			{
				for(int j=0;j<mapWidth;j++)
				{
					String raw = s.readLine();
					if(raw!=null)
					{
						String[] parsed = raw.split(delimiters);
						for(int k=0;k<parsed.length;k++)
						{
							tileID=Integer.parseInt(parsed[k]);
							tiles.add(new basicNode(x,y,tileID));
							if(tileID==0 || tileID==2 || tileID==3){tiles.get(counter).setWalkable(true);}
							else if(tileID==1){tiles.get(counter).setWalkable(false);}
							if(tileID==2){tiles.get(counter).setHome(true);}
							else if(tileID==3){tiles.get(counter).setDest(true); destTile=counter;}
							x+=32;
							counter++;
						}
						x=0;
						y+=32;
					}
				}
			}
			s.close();
		} 
		catch (Exception e){e.printStackTrace();}
		System.out.println("loading complete");
	}
	public int getDest(){return destTile;}
	public ArrayList<Node> getNodes(){return tiles;}
	public int getWidth(){return mapWidth;}
	public int getHeight(){return mapHeight;}
	public void render(Graphics2D g)
	{
		for(int i=0,stop=tiles.size();i<stop;i++)
		{
			tiles.get(i).render(g);
		}
	}
}