package com.egoal.darkestpixeldungeon.levels;

import android.util.Log;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Alchemist;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.CatLix;
import com.egoal.darkestpixeldungeon.levels.painters.Painter;
import com.watabou.utils.*;
import com.watabou.utils.Random;

import java.util.*;
import java.util.concurrent.Callable;

public class VillageLevel extends RegularLevel{
	{
		color1  =   0x48763c;
		color2  =   0x59994a;
		viewDistance	=	8;
		seeDistance	=	8;
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
			if(rm.top<roomExit.top){
				roomExit    =   rm;
			}
			else if(rm.top==roomExit.top && rm.square()>roomExit.square()){
				// choose bigger size, make sure only one room is on the top
//				if(roomExit!=roomEntrance)
//					rooms.remove(roomExit);
				roomExit    =   rm;
			}
		}
		// check size
		if(roomExit.square()<50 || roomExit.top>5 || roomEntrance.bottom<width-5)
			return false;

		roomEntrance.type   =   Room.Type.ENTRANCE;
		roomExit.type   =   Room.Type.BOSS_EXIT;

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
		for(Room rm: rooms)
			rm.type =   rm.type==Room.Type.NULL?Room.Type.STANDARD:rm.type;

//		specials    =   new ArrayList<>();
//		if(!assignRoomType())
//			return false;

		// no feeling
		feeling =   Feeling.NONE;

		paint();

		// exit is on the room Exit
		exit    =   roomExit.top * width() + (roomExit.left + roomExit.right) / 2;
		// map[exit] = Terrain.UNLOCKED_EXIT;
		map[exit]   =   Terrain.LOCKED_EXIT;
		{
			for(int i=-1;i<=1;++i){
				if(map[exit+width+i]==Terrain.WALL)
					return false;
			}

		}

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
		return Patch.generate(this, 0.5f, 8);
	}

	@Override
	protected void paint(){
		// simple fill all rooms as normal
		// super.paint();
		for(Room r: rooms){
			if(r.type!=Room.Type.NULL){
				super.placeDoors(r);

				// paint wall
				Painter.fill(this,r,Terrain.WALL);
				Painter.fill(this,r,1,Terrain.EMPTY);

				// paint doors
				for(Room.Door d : r.connected.values())
					d.set(Room.Door.Type.TUNNEL);

				paintDoors(r);
			}

			// place entrance
			if(r.type==Room.Type.ENTRANCE){
				do{
					entrance    =   pointToCell(r.random(1));
				}while(findMob(entrance)!=null);
			}
		}
	}

	@Override
	protected void paintGrass(){
		boolean[] grass =   grass();

		for(int i=width()+1; i<length()-width()-1; ++i){
			if(map[i]==Terrain.EMPTY && grass[i]){
				// no high grass, the grass is the grassland
				int count   =   1;
				for(int n: PathFinder.NEIGHBOURS8){
					if(grass[i+n])
						count++;
				}
				map[i]	=	(Random.Int(12)<count)? Terrain.HIGH_GRASS: Terrain.GRASS;
			}
		}
	}

	@Override
	protected void decorate(){
		// the village main stage should be stone tile
		for(int r=roomExit.top+1; r<roomExit.bottom; ++r){
			for(int c=roomExit.left+1; c<roomExit.right; ++c){
				int pos =   pointToCell(new Point(c, r));
				map[pos]    =   Terrain.EMPTY_SP;
			}
		}

		for(int c=roomExit.left+1; c<roomExit.right; ++c){
			int i   =   roomEntrance.top*width()+c;
			if(map[i]==exit || map[i]!=Terrain.WALL)
				map[i+width()]  =   Terrain.EMPTY;
		}

		// hide the entrance stairs
		map[entrance]   =   Terrain.EMPTY;

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
		}while(findMob(cl.pos)!=null);
		mobs.add(cl);

		// add villagers
		// old alchemist
		Alchemist a =   new Alchemist();
		Alchemist.Quest.reset();
		do{
			a.pos   =   pointToCell(roomExit.random());
		}while(findMob(a.pos)!=null || !passable[a.pos]);
		mobs.add(a);

		super.createMobs();
	}

	// will not auto generate monsters
	@Override
	public Actor respawner(){ return null; }

	@Override
	protected void createItems(){
		// does not generate anything
	}

	@Override
	public void storeInBundle(Bundle bundle){
		super.storeInBundle(bundle);

	}

	@Override
	public void restoreFromBundle(Bundle bundle){
		super.restoreFromBundle(bundle);


	}
}
