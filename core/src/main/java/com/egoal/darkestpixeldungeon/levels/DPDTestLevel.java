package com.egoal.darkestpixeldungeon.levels;

import android.util.Log;
import android.util.Pair;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Bones;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.mobs.Bestiary;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll;
import com.egoal.darkestpixeldungeon.levels.diggers.*;
import com.egoal.darkestpixeldungeon.levels.traps.FireTrap;
import com.egoal.darkestpixeldungeon.levels.traps.Trap;
import com.egoal.darkestpixeldungeon.levels.traps.WornTrap;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by 93942 on 11/11/2018.
 */

public class DPDTestLevel extends Level {
  {
    color1 = 0x48763c;
    color2 = 0x59994a;
    viewDistance = 8;
    seeDistance = 8;
  }

  @Override
  public String tilesTex() {
    return Assets.TILES_SEWERS;
  }

  @Override
  public String waterTex() {
    return Assets.WATER_SEWERS;
  }

  @Override
  protected void setupSize() {
    if (width == 0 && height == 0)
      width = height = 36;

    length = width * height;
  }

  @Override
  protected boolean build() {
    // dig rooms
    if (!digLevel())
      return false;

    // place entrance and exit
    entrance = xy2cell(width / 2, height / 2);
    exit = entrance + 1;
    map[entrance] = Terrain.ENTRANCE;
    map[exit] = Terrain.EXIT;

    // do some painting
    paintWater();
    paintGrass();
    placeTraps();

    return true;
  }

  @Override
  protected void decorate() {
  }

  @Override
  public int nMobs() {
    switch (Dungeon.depth) {
      case 0:
      case 1:
        return 0;
      default:
        return (3 + Dungeon.depth % 5 + Random.Int(6));
    }
  }

  protected void createSellers() {
  }

  @Override
  protected void createMobs() {
    createSellers();

    int mobsToSpawn = Dungeon.depth == 1 ? 10 : nMobs();

    // well distributed in each rooms
    Iterator<XRect> iter = normalSpaces.iterator();
    while (mobsToSpawn > 0) {
      if (!iter.hasNext())
        iter = normalSpaces.iterator();
      XRect rect = iter.next();
      Mob mob = Bestiary.mob(Dungeon.depth);
      mob.pos = pointToCell(rect.random());

      if (findMob(mob.pos) == null && passable[mob.pos]) {
        --mobsToSpawn;
        mobs.add(mob);
        if (mobsToSpawn > 0 && Random.Int(4) == 0) {
          mob = Bestiary.mob(Dungeon.depth);
          mob.pos = pointToCell(rect.random());
          if (findMob(mob.pos) == null && passable[mob.pos]) {
            --mobsToSpawn;
            mobs.add(mob);
          }
        }
      }

    }
  }

  @Override
  public int randomRespawnCell() {
    for (int i = 0; i < 30; ++i) {
      int pos = pointToCell(Random.element(normalSpaces).random());

      if (!Dungeon.visible[pos] && Actor.findChar(pos) == null && passable[pos])
        return pos;
    }

    return -1;
  }

  @Override
  public int randomDestination() {
    while (true) {
      int pos = Random.Int(length());
      if (passable[pos])
        return pos;
    }
  }

  @Override
  protected void createItems() {
    // drop the items
    for (Item item : itemsToSpawn) {
      int cell = randomDropCell();
      // don't drop scroll on fire trap
      if (item instanceof Scroll) {
        while (map[cell] == Terrain.TRAP || map[cell] == Terrain.SECRET_TRAP &&
                traps.get(cell) instanceof FireTrap)
          cell = randomDropCell();
      }

      drop(item, cell).type = Heap.Type.HEAP;
    }

    // drop the hero bones
    Item item = Bones.get();
    if (item != null) {
      drop(item, randomDropCell()).type = Heap.Type.REMAINS;
    }
  }

  protected int randomDropCell() {
    while (true) {
      int cell = pointToCell(Random.element(normalSpaces).random());
      if (passable[cell])
        return cell;
    }
  }

  protected void paintWater() {
    boolean[] waters = Patch.generate(this, 0.45f, 5);
    for (int i = 0; i < length(); ++i)
      if (map[i] == Terrain.EMPTY && waters[i])
        map[i] = Terrain.WATER;
  }

  protected void paintGrass() {
    boolean[] grass = Patch.generate(this, 0.4f, 4);

    for (int i = width() + 1; i < length() - width() - 1; i++) {
      if (map[i] == Terrain.EMPTY && grass[i]) {
        int count = 1;
        for (int n : PathFinder.NEIGHBOURS8) {
          if (grass[i + n]) {
            count++;
          }
        }
        map[i] = (Random.Float() < count / 12f) ? Terrain.HIGH_GRASS :
                Terrain.GRASS;
      }
    }
  }

  // traps
  protected int nTraps() {
    return Random.NormalIntRange(1, 3 + Dungeon.depth / 2);
  }

  protected Class<?>[] trapClasses() {
    return new Class<?>[]{WornTrap.class};
  }

  protected float[] trapChances() {
    return new float[]{1};
  }

  protected void placeTraps() {
    float[] trapChances = trapChances();
    Class<?>[] trapClasses = trapClasses();

    LinkedList<Integer> validCells = new LinkedList<Integer>();

    for (int i = 0; i < length(); ++i) {
      if (map[i] == Terrain.EMPTY)
        validCells.add(i);
    }

    int ntraps = Math.min(nTraps(), (int) (validCells.size() * 0.15));
    Collections.shuffle(validCells);

    for (int i = 0; i < ntraps; ++i) {
      int tp = validCells.removeFirst();
      try {
        Trap trap = ((Trap) trapClasses[Random.chances(trapChances)]
                .newInstance()).hide();
        setTrap(trap, tp);
        // some traps would not be hidden
        map[tp] = trap.visible ? Terrain.TRAP : Terrain.SECRET_TRAP;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    Log.d("dpd", String.format("%d traps added.", ntraps));
  }

  //////////////////////////////////////////////////////////////////////////////
  // my digging algorithm
  private ArrayList<XWall> digableWalls; // = new ArrayList<>();

  // keep in mind that the normal spaces is always rectangle.
  //todo: may track the space type.
  private ArrayList<XRect> normalSpaces; // = new ArrayList<>();

  private boolean digLevel() {
    digableWalls = new ArrayList<>();
    normalSpaces = new ArrayList<>();

    digFirstRoom();

    ArrayList<Digger> diggers = chooseDiaggers();
    Log.d("dpd", String.format("%d rooms to dig.", diggers.size()));

    while (!diggers.isEmpty() && !digableWalls.isEmpty()) {
      // choose a digger
      Digger digger = Random.element(diggers);

      // dig a room
      boolean digged = false;
      for (int i = 0; i < 100; ++i) {
        XWall wall = Random.element(digableWalls);
        XRect rect = digger.chooseDigArea(wall);
        if (canDigAt(rect)) {
          // free to dig, dig!
          digged = true;

          Digger.DigResult dr = digger.dig(this, wall, rect);

          digableWalls.remove(wall);
          digableWalls.addAll(dr.walls);
          if (dr.type == Digger.DigResult.Type.NORMAL)
            normalSpaces.add(rect);
          break;
        }
      }

      if (!digged) return false;


      diggers.remove(digger);
    }
    Log.d("dpd", String.format("%d diggers left...", diggers.size()));

    int lc = makeLoopClosure(6);
    Log.d("dpd", String.format("%d loop linked.", lc));

    if (lc <= 2)
      return false;

    return true;
  }

  private void digFirstRoom() {
    int w = Random.IntRange(3, 6);
    int h = Random.IntRange(3, 6);
    int x1 = Random.IntRange(width / 4, width / 4 * 3 - w);
    int y1 = Random.IntRange(height / 4, height / 4 * 3 - h);
    int x2 = x1 + w - 1;
    int y2 = y1 + h - 1;

    Digger.Fill(this, x1, y1, w, h, Terrain.EMPTY);
    digableWalls.add(new XWall(x1 - 1, x1 - 1, y1, y2, Digger.LEFT));
    digableWalls.add(new XWall(x2 + 1, x2 + 1, y1, y2, Digger.RIGHT));
    digableWalls.add(new XWall(x1, x2, y1 - 1, y1 - 1, Digger.UP));
    digableWalls.add(new XWall(x1, x2, y2 + 1, y2 + 1, Digger.DOWN));
  }

  protected ArrayList<Digger> chooseDiaggers() {
    ArrayList<Digger> diggers = new ArrayList<>();
    diggers.add(new ArmoryDigger());
    diggers.add(new GardenDigger());
    diggers.add(new LaboratoryDigger());
    diggers.add(new LibraryDigger());
    diggers.add(new MagicWellDigger());
    if(pitRoomNeeded)
      diggers.add(new PitDigger());
    diggers.add(new PoolDigger());
    diggers.add(new ShopDigger());
    diggers.add(new StatuaryDigger());
    diggers.add(new StatueDigger());
    diggers.add(new StorageDigger());
    diggers.add(new TrapsDigger());
    diggers.add(new TreasuryDigger());
    diggers.add(new VaultDigger());
    diggers.add(new WeakFloorDigger());
    int n = 20 - diggers.size();
    for (int i = 0; i < n; ++i) {
      diggers.add(new NormalRoomDigger());
    }

    // no need to shuffle.
    return diggers;
  }

  private boolean canDigAt(XRect rect) {
    if (rect.x1 > 0 && rect.x2 < width - 1 && rect.y1 > 0 && rect.y2 < height
            - 1) {
      for (int x = rect.x1 - 1; x <= rect.x2 + 1; ++x)
        for (int y = rect.y1 - 1; y <= rect.y2 + 1; ++y)
          if (map[xy2cell(x, y)] != Terrain.WALL)
            return false;

      return true;
    }

    return false;
  }

  private int makeLoopClosure(int maxLoops) {
    int loops = 0;

    // simply dig a door when there's overlapped wall
    ArrayList<Pair<XWall, XWall>> overlappedWalls = new ArrayList<>();

    int cntWalls = digableWalls.size();
    for (int i = 0; i < cntWalls; ++i) {
      XWall wi = digableWalls.get(i);
      for (int j = i + 1; j < cntWalls; ++j) {
        XWall wj = digableWalls.get(j);

        if (wi.direction == -wj.direction && wi.overlap(wj).isValid())
          overlappedWalls.add(new Pair<>(wi, wj));
      }
    }

    Collections.shuffle(overlappedWalls);
    for (Pair<XWall, XWall> pr : overlappedWalls) {
      int dp = pointToCell(pr.first.overlap(pr.second).random(0));

      boolean canBeDoor = map[dp] == Terrain.WALL;
      if (canBeDoor) {
        for (int i : PathFinder.NEIGHBOURS8)
          if (map[dp + i] == Terrain.DOOR) {
            canBeDoor = false;
            break;
          }
      }

      if (canBeDoor) {
        Digger.Set(this, dp, Terrain.DOOR);
        digableWalls.remove(pr.first);
        digableWalls.remove(pr.second);

        if (++loops >= maxLoops)
          break;
      }
    }

    // not enough, random select two wall and dig between them
    if (loops < maxLoops && digableWalls.size() >= 2) {
    }

    return loops;
  }
}
