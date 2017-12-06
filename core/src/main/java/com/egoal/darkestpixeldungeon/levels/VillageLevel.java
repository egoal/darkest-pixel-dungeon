package com.egoal.darkestpixeldungeon.levels;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Challenges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.CatLix;
import com.egoal.darkestpixeldungeon.levels.painters.ShopPainter;
import com.watabou.utils.*;
import com.watabou.utils.Random;

import java.util.*;

public class VillageLevel extends RegularLevel{

	// properties
	@Override
	public String tilesTex(){
		return Assets.DPD_TILES_VILLAGE;
	}

	@Override
	public String waterTex(){
		return Assets.DPD_WATER_VILLAGE;
	}

	// create actions
	@Override
	public void create() {
		// small
		width   =   16;
		height  =   16;

		setupSize();
		PathFinder.setMapSize(width(), height());
		passable	= new boolean[length()];
		losBlocking	= new boolean[length()];
		flamable	= new boolean[length()];
		secret		= new boolean[length()];
		solid		= new boolean[length()];
		avoid		= new boolean[length()];
		water		= new boolean[length()];
		pit			= new boolean[length()];

		map = new int[length()];
		visited = new boolean[length()];
		Arrays.fill( visited, false );
		mapped = new boolean[length()];
		Arrays.fill( mapped, false );

		do {
			Arrays.fill( map, feeling == Feeling.CHASM ? Terrain.CHASM : Terrain.WALL );

			pitRoomNeeded   =   false;
			weakFloorCreated    =   false;

			mobs = new HashSet<>();
			heaps = new SparseArray<>();
			blobs = new HashMap<>();
			plants = new SparseArray<>();
			traps = new SparseArray<>();
			customTiles = new HashSet<>();

		} while (!build());
		decorate();

		buildFlagMaps();
		cleanWalls();

		createMobs();
		createItems();
	}

	@Override
	protected boolean build(){
		// rewrite build method
		if (!initRooms()) {
			return false;
		}

		int distance;
		int retry = 0;
		int minDistance = (int)Math.sqrt( rooms.size() );

		Iterator<Room> iter =   rooms.iterator();
		roomEntrance    =   iter.next();
		roomExit    =   iter.next();

		Graph.buildDistanceMap(rooms, roomExit);
//		do {
//			do {
//				roomEntrance = Random.element( rooms );
//			} while (roomEntrance.width() < 4 || roomEntrance.height() < 4);
//
//			do {
//				roomExit = Random.element( rooms );
//			} while (roomExit == roomEntrance || roomExit.width() < 4 || roomExit.height() < 4);
//
//			Graph.buildDistanceMap( rooms, roomExit );
//			distance = roomEntrance.distance();
//
//			if (retry++ > 10) {
//				return false;
//			}
//
//		} while (distance < minDistance);

		roomEntrance.type = Room.Type.ENTRANCE;
		roomExit.type = Room.Type.EXIT;

		HashSet<Room> connected = new HashSet<Room>();
		connected.add( roomEntrance );

		Graph.buildDistanceMap( rooms, roomExit );
		List<Room> path = Graph.buildPath( rooms, roomEntrance, roomExit );

		Room room = roomEntrance;
		for (Room next : path) {
			room.connect( next );
			room = next;
			connected.add( room );
		}

		Graph.setPrice( path, roomEntrance.distance );

		Graph.buildDistanceMap( rooms, roomExit );
		path = Graph.buildPath( rooms, roomEntrance, roomExit );

		room = roomEntrance;
		for (Room next : path) {
			room.connect( next );
			room = next;
			connected.add( room );
		}

		int nConnected = (int)(rooms.size() * Random.Float( 0.5f, 0.7f ));
		while (connected.size() < nConnected) {

			Room cr = Random.element( connected );
			Room or = Random.element( cr.neigbours );
			if (!connected.contains( or )) {

				cr.connect( or );
				connected.add( or );
			}
		}

		specials = new ArrayList<Room.Type>( Room.SPECIALS );
		if (Dungeon.bossLevel( Dungeon.depth + 1 )) {
			specials.remove( Room.Type.WEAK_FLOOR );
		}

		// left room type has effects
		if (!assignRoomType())
			return false;

		paint();
		paintWater();
		paintGrass();

		return true;
	}

	// init room
	@Override
	protected boolean initRooms(){
		rooms   =   new HashSet<>();
		rooms.add((Room)new Room().set(new Rect(6, 0, 6, 6)));
		rooms.add((Room)new Room().set(new Rect(4, 12, 4, 6)));

		Room[] ra = rooms.toArray( new Room[0] );
		for (int i=0; i < ra.length-1; i++) {
			for (int j=i+1; j < ra.length; j++) {
				ra[i].addNeigbour( ra[j] );
			}
		}

		return true;
	}

	@Override
	protected boolean[] water(){
		boolean[] nowater   =   new boolean[length];

		Arrays.fill(nowater, false);
		return nowater;
	}

	@Override
	protected boolean[] grass(){
		boolean[] noGrass   =   new boolean[length];
		Arrays.fill(noGrass, false);

		return noGrass;
	}

	@Override
	protected void decorate(){
		// do nothing
	}

	@Override
	protected void createMobs(){
		CatLix cl=new CatLix();
		do{
			cl.pos=pointToCell(roomEntrance.random());
		}while(map[cl.pos]==Terrain.ENTRANCE||map[cl.pos]==Terrain.SIGN);
		mobs.add(cl);

		super.createMobs();
	}

	@Override
	protected void createItems(){
		super.createItems();
	}
}
