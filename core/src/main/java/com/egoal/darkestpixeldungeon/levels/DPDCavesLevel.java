package com.egoal.darkestpixeldungeon.levels;

import android.view.ViewDebug;

import com.egoal.darkestpixeldungeon.Assets;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 5/7/2018.
 */

public class DPDCavesLevel extends Level{
	
	{
		color1 = 0x534f3e;
		color2 = 0xb9d661;

		viewDistance = 8;
	}
	
	@Override
	protected boolean build(){
		//0. generate the floor
		if(!generateFloor()) return false;
		
		//1. entrance & exit
		entrance	=	randomEmptyCell();
		map[entrance]	=	Terrain.ENTRANCE;
		exit	=	randomEmptyCell();
		map[exit]	=	Terrain.EXIT;
		
		// 2. paint
		// paint();
		// paintLuminary();
		paintWater();
		paintGrass();

		// placeTraps();
		
		return true;
	}
	
	// is bigger
	@Override
	protected void setupSize(){
		if(width*height==0){
			width	=	height	=	48;
		}
		
		length	=	width*height;
	}
	
	private boolean generateFloor(){
		//0. simple patch generate
		boolean[] floors	=	new boolean[length];
		{
			boolean[] floors2	=	Patch.generate(width*2,height*2,0.5f,5);
			for(int i=0; i<length; ++i)
				floors[i]	=	floors2[2*i];
			
			for(int i=0; i<width; ++i){
				floors[i]	=	false;
				floors[(height-1)*width+i]	=	false;
			}
			for(int i=0; i<height; ++i){
				floors[i*width+0]	=	false;
				floors[i*width+width-1]	=	false;
			}
		}
		
		//1. fill holes, a simple flood fill
		int[] regions	=	FloodFill.floodFillAll(this, floors);
		
		//2. check size
		
		//3. assign data
		for(int i=0; i<length; ++i){
			if(floors[i])
				map[i]	=	Terrain.EMPTY;
		}
		
		return true;
	}
	
	// private: only check EMPTY, 
	private int randomEmptyCell(){
		int pos;
		do{
			pos	=	Random.Int(length);
		}while(map[pos]!=Terrain.EMPTY);
		return pos;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_CAVES;
	}
	@Override
	public String waterTex() {
		return Assets.WATER_CAVES;
	}
	
	// water & grass
	protected boolean[] water() {
		return Patch.generate( this, feeling == Feeling.WATER ? 0.60f : 0.45f, 6 );
	}
	protected boolean[] grass() {
		return Patch.generate( this, feeling == Feeling.GRASS ? 0.55f : 0.35f, 3 );
	}

	protected void paintWater() {
		boolean[] lake = water();
		for (int i=0; i < length(); i++) {
			if (map[i] == Terrain.EMPTY && lake[i]) {
				map[i] = Terrain.WATER;
			}
		}
	}

	protected void paintGrass() {
		boolean[] grass = grass();
		
		for (int i=width()+1; i < length()-width()-1; i++) {
			if (map[i] == Terrain.EMPTY && grass[i]) {
				int count = 1;
				for (int n : PathFinder.NEIGHBOURS8) {
					if (grass[i + n]) {
						count++;
					}
				}
				map[i] = (Random.Float() < count / 12f) ? Terrain.HIGH_GRASS : Terrain.GRASS;
			}
		}
	}
	
	@Override
	protected void decorate(){

	}

	@Override
	protected void createMobs(){
		
	}

	@Override
	protected void createItems(){

	}

	private static class FloodFill{
		// a very simple algorithm
		public static int[] floodFillAll(Level level, boolean[] data){
			int[] arData	=	new int[data.length];
			
			ArrayList<Interval > alIntervals;
			for(int r=0; r<level.height(); ++r){
				// for each rows
				ArrayList<Interval > alCurIntervals;
				for(int c=0; c<level.width(); ++c){
					int i	=	r*level.width()+c;
					if(!data[i]){
						arData[i]	=	0;
					}else{
						
					}
				}
			}
			
			return arData;
		}
		
		private class Interval{
			public Interval(int tag){
				this.tag	=	tag;
			}
			Interval left(int v){ left=v; return this; }
			Interval right(int v){ right=v; return this; }
			
			int left, right;
			int tag;
		}
	}
}
