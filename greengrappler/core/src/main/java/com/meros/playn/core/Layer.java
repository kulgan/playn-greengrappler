package com.meros.playn.core;

public class Layer {
	
	int myWidth;
	int myHeight;
	
	Tile[][] myTiles;

	public Layer(int aWidth, int aHeight) {
		myWidth = aWidth;
		myHeight = aHeight;
		
		myTiles = new Tile[aWidth][aHeight];
	}
	
	public void setTile(int aX,	int aY,	Tile aTile)
	{
		myTiles[aX][aY] = aTile;
	}

	public Tile getTile(int aX, int aY)
	{
		return myTiles[aX][aY];
	}
	
	public float getHeight() {
		// TODO Auto-generated method stub
		return myHeight;
	}

	public float getWidth() {
		// TODO Auto-generated method stub
		return myWidth;
	}

}