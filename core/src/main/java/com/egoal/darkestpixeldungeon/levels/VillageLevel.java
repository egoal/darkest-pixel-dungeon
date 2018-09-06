package com.egoal.darkestpixeldungeon.levels;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.Fog;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.actors.mobs.DevilGhost;
import com.egoal.darkestpixeldungeon.actors.mobs.MadMan;
import com.egoal.darkestpixeldungeon.actors.mobs.Rat;
import com.egoal.darkestpixeldungeon.actors.mobs.SkeletonKnight;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Alchemist;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.CatLix;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.DPDShopKeeper;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.DisheartenedBuddy;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Jessica;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.PotionSeller;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Scholar;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.ScrollSeller;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Statuary;
import com.egoal.darkestpixeldungeon.items.Gold;
import com.egoal.darkestpixeldungeon.items.Torch;
import com.egoal.darkestpixeldungeon.items.artifacts.RiemannianManifoldShield;
import com.egoal.darkestpixeldungeon.items.artifacts.UrnOfShadow;
import com.egoal.darkestpixeldungeon.items.bags.PotionBandolier;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.egoal.darkestpixeldungeon.items.weapon.melee.AssassinsBlade;
import com.egoal.darkestpixeldungeon.items.weapon.melee.BattleGloves;
import com.egoal.darkestpixeldungeon.items.weapon.melee.Knuckles;
import com.egoal.darkestpixeldungeon.levels.painters.Painter;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.watabou.utils.*;
import com.watabou.utils.Random;

import java.util.*;

public class VillageLevel extends RegularLevel {
  {
    color1 = 0x48763c;
    color2 = 0x59994a;
    viewDistance = 8;
    seeDistance = 8;
  }

  @Override
  public String tilesTex() {
    return Assets.DPD_TILES_VILLAGE;
  }

  @Override
  public String waterTex() {
    return Assets.DPD_WATER_VILLAGE;
  }

  protected boolean build() {
    if (!initRooms() || rooms.size() < 2)
      return false;

    feeling = Feeling.NONE;

    paint();

    // lanes between the rooms is painted
    paintLanes();

    // exit is on the room Exit
    exit = roomExit.top * width() + (roomExit.left + roomExit.right) / 2;
    // map[exit] = Terrain.UNLOCKED_EXIT;
    map[exit] = Terrain.LOCKED_EXIT;
    {
      for (int i = -1; i <= 1; ++i) {
        if (map[exit + width + i] == Terrain.WALL)
          return false;
      }

    }

    paintWater();
    paintGrass();

    // no traps

    return true;
  }

  @Override
  protected boolean initRooms() {
    // rewrite the room placement, 
    // actually, no rooms anymore
    rooms = new HashSet<>();
    //0. exit room, 1->4
    {
      int w = Random.Int(10, 16);
      int h = Random.Int(6, 12);
      int x = Random.Int(10, width - w - 10);
      int y = Random.Int(1, 4);
      roomExit = (Room) new Room().set(new Rect(x, y, x + w, y + h));
    }
    roomExit.type = Room.Type.BOSS_EXIT;
    rooms.add(roomExit);

    //1. entrance, -3->-1
    {
      int x = Random.Int(10, width - 13);
      int y = height - 6;
      roomEntrance = (Room) new Room().set(new Rect(x, y, x + 4, y + 4));
    }
    roomEntrance.type = Room.Type.ENTRANCE;
    rooms.add(roomEntrance);

    //2. place more rooms
    {
      int numRooms = Random.Int(8, 12) + 2;
      for (int i = 0; i < 1000 && rooms.size() < numRooms; ++i) {
        int w = Random.Int(4, 9);
        int h = Random.Int(4, 9);
        int x = Random.Int(0, width - w);
        int y = Random.Int(4, height - 3 - h);
        Room newRoom = (Room) new Room().set(new Rect(x, y, x + w, y + h));

        for (Room rm : rooms) {
          if (!newRoom.intersect(rm).isEmpty()) {
            // intersects, failed
            newRoom = null;
            break;
          }
        }

        if (newRoom == null)
          continue;
        // set as standard type
        newRoom.type = Room.Type.STANDARD;
        rooms.add(newRoom);
      }
    }

    return true;
  }

  @Override
  protected boolean[] water() {
    boolean[] arWater = new boolean[length];
    Arrays.fill(arWater, false);

    return arWater;
  }

  @Override
  protected boolean[] grass() {
    return Patch.generate(this, 0.5f, 8);
  }

  @Override
  protected void paint() {
    // simple fill all rooms as normal
    // super.paint();
    for (Room r : rooms) {
      if (r.type != Room.Type.NULL) {
        super.placeDoors(r);

        // paint wall
        Painter.fill(this, r, Terrain.WALL);
        Painter.fill(this, r, 1, Terrain.EMPTY);

        // paint doors
        for (Room.Door d : r.connected.values())
          d.set(Room.Door.Type.TUNNEL);

        paintDoors(r);
      }

      // place entrance
      if (r.type == Room.Type.ENTRANCE) {
//				int p	=	pointToCell(r.random(1));
//				map[p]	=	Terrain.ENCHANTING_STATION;
        do {
          // entrance    =   pointToCell(r.random(1));
          entrance = r.bottom * width() + Random.Int(r.left + 1, r.right);
        } while (findMob(entrance) != null);
        map[entrance] = Terrain.ENTRANCE;
      }
    }
  }

  protected void paintLanes() {
    ArrayList<Room> rmNotConnected = new ArrayList<>();
    rmNotConnected.addAll(rooms);

    Room curRoom = Random.element(rmNotConnected);
    rmNotConnected.remove(curRoom);
    while (!rmNotConnected.isEmpty()) {
      Room toRoom = Random.element(rmNotConnected);
      rmNotConnected.remove(toRoom);

      // link lanes
      Point pt0 = curRoom.random();
      Point pt1 = toRoom.random();
      linkLane(pt0.x, pt0.y, pt1.x, pt1.y);

      curRoom = toRoom;
    }

  }

  private void linkLaneV(int x, int y1, int y2) {
    int ds = y1 < y2 ? 1 : -1;
    for (int y = y1; y != y2; y += ds) {
      map[y * width + x] = Terrain.EMPTY;
    }
  }

  private void linkLaneH(int y, int x1, int x2) {
    int ds = x1 < x2 ? 1 : -1;
    for (int x = x1; x != x2; x += ds) {
      map[y * width + x] = Terrain.EMPTY;
    }
  }

  private void linkLane(int x1, int y1, int x2, int y2) {
    int dx = x1 > x2 ? -1 : 1;
    int nx = (x2 - x1) / dx;
    int dy = y1 > y2 ? -1 : 1;
    int ny = (y2 - y1) / dy;

    ArrayList<Point> adp = new ArrayList<>();
    for (int i = 0; i < nx; ++i)
      adp.add(new Point(dx, 0));
    for (int i = 0; i < ny; ++i)
      adp.add(new Point(0, dy));
    Collections.shuffle(adp);

    int x = x1;
    int y = y1;
    for (Point dp : adp) {
      x += dp.x;
      y += dp.y;

      map[y * width + x] = Terrain.EMPTY;
    }
  }

  @Override
  protected void paintGrass() {
    boolean[] grass = grass();

    for (int i = width() + 1; i < length() - width() - 1; ++i) {
      if (map[i] == Terrain.EMPTY && grass[i]) {
        // no high grass, the grass is the grassland
        int count = 1;
        for (int n : PathFinder.NEIGHBOURS8) {
          if (grass[i + n])
            count++;
        }
        map[i] = (Random.Int(12) < count) ? Terrain.HIGH_GRASS : Terrain.GRASS;
      }
    }
  }

  @Override
  protected void decorate() {
    // decorate like normal level do, 
    // just put some tiny stone on the floor, some grass on the wall
    for (int i = 0; i < length; ++i) {
      if (map[i] == Terrain.WALL) {
        int nearGrass = 0;
        for (int di : PathFinder.NEIGHBOURS4) {
          int pos = i + di;
          if (pos >= 0 && pos < length && map[pos] == Terrain.GRASS) {
            ++nearGrass;
          }
        }
        if (Random.Int(5) < nearGrass)
          map[i] = Terrain.WALL_DECO;
      }
    }

    for (int i = width() + 1; i < length() - width() - 1; i++) {
      if (map[i] == Terrain.EMPTY) {

        int count =
                (map[i + 1] == Terrain.WALL ? 1 : 0) +
                        (map[i - 1] == Terrain.WALL ? 1 : 0) +
                        (map[i + width()] == Terrain.WALL ? 1 : 0) +
                        (map[i - width()] == Terrain.WALL ? 1 : 0);

        if (Random.Int(16) < count * count) {
          map[i] = Terrain.EMPTY_DECO;
        }
      }
    }

    // the village main stage should be stone tile
    for (int r = roomExit.top + 1; r < roomExit.bottom; ++r) {
      for (int c = roomExit.left + 1; c < roomExit.right; ++c) {
        int pos = pointToCell(new Point(c, r));
        map[pos] = Terrain.EMPTY_SP;
      }
    }

    for (int c = roomExit.left + 1; c < roomExit.right; ++c) {
      int i = roomEntrance.top * width() + c;
      if (map[i] == exit || map[i] != Terrain.WALL)
        map[i + width()] = Terrain.EMPTY;
    }

    // place sign
    while (true) {
      int pos = pointToCell(roomExit.random());
      if (traps.get(pos) == null && findMob(pos) == null) {
        map[pos] = Terrain.SIGN;
        break;
      }
    }
  }

  // create

  // don't generate any mobs
  @Override
  public int nMobs() {
    return 0;
  }

  @Override
  protected void createMobs() {
    // add lix the cat in the entrance room
    {
      CatLix cl = new CatLix();
      do {
        cl.pos = pointToCell(roomEntrance.random());
      } while (findMob(cl.pos) != null || cl.pos == entrance);
      mobs.add(cl);
    }

    // add villagers
    // old alchemist
    {
      Alchemist a = new Alchemist();
      Alchemist.Quest.reset();
      do {
        a.pos = pointToCell(roomExit.random(1));    // avoid to block the way
      }
      while (findMob(a.pos) != null || !passable[a.pos] || map[a.pos] ==
              Terrain.SIGN);
      mobs.add(a);
    }

    // jessica
    Jessica.Quest.spawnJessica(this, roomExit);

    // sodan
    {
      DisheartenedBuddy sodan = new DisheartenedBuddy();
      do {
        sodan.pos = pointToCell(roomExit.random(1));
      }
      while (findMob(sodan.pos) != null || !passable[sodan.pos] || map[sodan
              .pos] == Terrain.SIGN);
      mobs.add(sodan);
    }

    // scholar
    {
      // scholar is on the right side of the map
      Scholar s = new Scholar();
      Room theRoom = (Room) new Room().set(-1, -1, -1, -1);
      for (Room rm : rooms) {
        if (rm.right > theRoom.right)
          theRoom = rm;
      }
      do {
        s.pos = pointToCell(theRoom.random(1));
      } while (findMob(s.pos) != null || !passable[s.pos]);
      mobs.add(s);
    }

    // shopkeepers
    if (false) {
      DPDShopKeeper dsk = new DPDShopKeeper().initSellItems();
      dsk.addItemToSell(new PotionOfHealing());
      dsk.addItemToSell(new PotionOfHealing());
      dsk.addItemToSell(new AssassinsBlade());
      dsk.addItemToSell(new ScrollOfUpgrade());
      dsk.addItemToSell(new Torch().quantity(3));
      dsk.addItemToSell(new Knuckles());
      dsk.addItemToSell(new BattleGloves());
      do {
        dsk.pos = pointToCell(roomExit.random(1));
      }
      while (findMob(dsk.pos) != null || !passable[dsk.pos] || map[dsk.pos]
              == Terrain.SIGN);
      mobs.add(dsk);

      {
        DPDShopKeeper ps = new PotionSeller().initSellItems();
        do {
          ps.pos = pointToCell(roomExit.random(1));
        }
        while (findMob(ps.pos) != null || !passable[ps.pos] || map[ps.pos] ==
                Terrain.SIGN);
        mobs.add(ps);
      }
      {
        DPDShopKeeper ps = new ScrollSeller().initSellItems();
        do {
          ps.pos = pointToCell(roomExit.random(1));
        }
        while (findMob(ps.pos) != null || !passable[ps.pos] || map[ps.pos] ==
                Terrain.SIGN);
        mobs.add(ps);
      }
    }

    // test 
    if (false) {
       Rat dg	=	new Rat();
      // Statuary dg = new Statuary().type(Statuary.Type.MONSTER);
      // SkeletonKnight dg	=	new SkeletonKnight();
//			MadMan dg	=	new MadMan();
      do {
        dg.pos = pointToCell(roomEntrance.random());
      } while (findMob(dg.pos) != null || dg.pos == entrance);
      mobs.add(dg);
      
      drop(new Gold(1000), entrance);
      drop(new RiemannianManifoldShield(), entrance);
      drop(new UrnOfShadow().volume(10), entrance);
      drop(new PotionOfHealing().identify(), entrance);
    }

    super.createMobs();
  }

  // will not auto generate monsters
  @Override
  public Actor respawner() {
    return null;
  }

  @Override
  protected void createItems() {
    // does not generate anything
  }

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);

  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
  }
}
