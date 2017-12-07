package com.egoal.darkestpixeldungeon.levels;

import android.util.Log;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.CatLix;
import com.watabou.utils.Graph;
import com.watabou.utils.Random;

import java.util.*;

public class VillageLevel extends RegularLevel{
	{
		color1  =   0x48763c;
		color2  =   0x59994a;
	}

	@Override
	public String tilesTex(){
		return Assets.DPD_TILES_VILLAGE;
	}

	@Override
	public String waterTex(){
		return Assets.DPD_WATER_VILLAGE;
	}

	protected boolean build(){
		if(!initRooms() || rooms.size()<2)
			return false;

		// select entrance & exit
		// entrance is below while exit(entrance to the dungeon is on the top)

		// room at bottom is entrance, while exit on the top
		Iterator iter   =   rooms.iterator();
		roomEntrance    =   roomExit    =   (Room)iter.next();
		while(iter.hasNext()){
			Room rm =   (Room)(iter.next());
			if(rm.bottom>roomEntrance.bottom)
				roomEntrance    =   rm;
			if(rm.top<roomExit.top)
				roomExit    =   rm;
			else if(rm.top==roomExit.top && rm.square()>roomExit.square())
				roomExit    =   rm;
		}
		// check size
		if(roomExit.square()<50)
			return false;

		roomEntrance.type   =   Room.Type.ENTRANCE;
		roomExit.type   =   Room.Type.EXIT;

		// now build path
		Graph.buildDistanceMap(rooms, roomExit);
		List<Room> lstPath  =   Graph.buildPath(rooms, roomEntrance, roomExit);

		HashSet<Room> rmConnected   =   new HashSet<>();
		{
			Room room=roomEntrance;
			rmConnected.add(room);
			for(Room next : lstPath){
				room.connect(next);
				room    =   next;
				rmConnected.add(room);
			}
		}

		// build again
		Graph.setPrice(lstPath, roomEntrance.distance);
		Graph.buildDistanceMap(rooms, roomExit);
		lstPath =   Graph.buildPath(rooms, roomEntrance, roomExit);
		{
			Room room   =   roomEntrance;
			for(Room next: lstPath){
				room.connect(next);
				room    =   next;
				rmConnected.add(room);
			}
		}

		// ensure connections
		int nConnected  =   (int)(rooms.size()*Random.Float(0.5f, 0.7f));
		while(rmConnected.size()<nConnected){
			Room cr =   Random.element(rmConnected);
			Room or =   Random.element(cr.neigbours);
			if(!rmConnected.contains(or)){
				cr.connect(or);
				rmConnected.add(or);
			}
		}

		// no need to give special rooms
		specials    =   new ArrayList<>();
		if(!assignRoomType())
			return false;

		paint();
		paintWater();
		paintGrass();

		// no traps
		// no sign

		return true;
	}

	@Override
	protected boolean[] water(){
		boolean[] arWater   =   new boolean[length];
		Arrays.fill(arWater, false);

		return arWater;
	}

	@Override
	protected boolean[] grass(){
		return Patch.generate(this, 0.4f, 8);
	}

	@Override
	protected void decorate(){

	}

	// create

	// don't generate any mobs
	@Override
	public int nMobs(){ return 0; }

	@Override
	protected void createMobs(){
		// add lix the cat in the entrance room
		CatLix cl   =   new CatLix();
		do{
			cl.pos  =   pointToCell(roomEntrance.random());
		}while(map[cl.pos]==Terrain.ENTRANCE || map[cl.pos]==Terrain.SIGN);
		mobs.add(cl);

		super.createMobs();
	}

	// will not auto generate monsters
	@Override
	public Actor respawner(){ return null; }

	@Override
	protected void createItems(){
		// does not generate anything
	}
}
