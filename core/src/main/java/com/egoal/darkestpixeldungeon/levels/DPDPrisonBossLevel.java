package com.egoal.darkestpixeldungeon.levels;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Bones;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Ignorant;
import com.egoal.darkestpixeldungeon.actors.mobs.DPDTengu;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.actors.mobs.Rat;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.Torch;
import com.egoal.darkestpixeldungeon.items.keys.IronKey;
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.PrisonLevel;
import com.egoal.darkestpixeldungeon.levels.Room;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.painters.Painter;
import com.egoal.darkestpixeldungeon.levels.traps.AlarmTrap;
import com.egoal.darkestpixeldungeon.levels.traps.SpearTrap;
import com.egoal.darkestpixeldungeon.levels.traps.Trap;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by 93942 on 9/13/2018.
 */

public class DPDPrisonBossLevel extends Level {
  {
    color1 = 0x6a723d;
    color2 = 0x88924c;

    viewDistance = 3;
  }

  public Room rmStart, rmHall, rmExit;
  private ArrayList<Room> rmPrisonCells;
  private int[] hallLights;
  private boolean enteredMainHall = false;
  public boolean isLighted = true;

  @Override
  public String tilesTex() {
    return Assets.TILES_PRISON;
  }

  @Override
  public String waterTex() {
    return Assets.WATER_PRISON;
  }

  public void turnLights(boolean on) {
    if (on == isLighted) return;

    isLighted = on;
    for (int i : hallLights) {
      map[i] = on ? Terrain.WALL_LIGHT_ON : Terrain.WALL_LIGHT_OFF;

      GameScene.updateMap(i);
    }

    if (!on) {
      for (int i : hallLights)
        removeLightVisualAt(i);
    }

    buildFlagMaps();
    Dungeon.observe();
  }

  public int hallCenter(){
    return pointToCell(rmHall.centerFixed());
  }
  
  @Override
  protected boolean build() {
    Arrays.fill(map, Terrain.WALL);

    buildHall();

    // place the entrance
    {
      int w = Random.Int(4, 6);
      int h = Random.Int(4, 6);
      int x = Random.Int(0, rmHall.left - w);
      int y = Random.Int(0, height / 3);

      rmStart = (Room) new Room().set(x, y, x + w, y + h);

      Painter.fill(this, rmStart, 1, Terrain.EMPTY);
      map[pos(rmStart.centerFixed().x, rmStart.bottom)] = Terrain.DOOR;

      int sp;
      do {
        sp = pointToCell(rmStart.random());
      } while (map[sp] != Terrain.EMPTY);
      map[sp] = Terrain.SIGN;
    }

    // link start-> hall
    Painter.linkV(this, rmStart.centerFixed().x, rmStart.bottom + 1, rmHall
            .bottom - 1, Terrain.EMPTY);
    Painter.linkH(this, rmHall.bottom - 1, rmStart.centerFixed().x, rmHall
                    .left - 1,
            Terrain.EMPTY);

    // place the exit
    {
      int w = Random.Int(4, 6);
      int h = Random.Int(4, 6);
      int x = Random.Int(rmHall.right, width - w);
      int y = Random.Int(height * 2 / 3, height) - h;

      rmExit = (Room) new Room().set(x, y, x + w, y + h);

      Painter.fill(this, rmExit, 1, Terrain.EMPTY);
      map[pos(rmExit.centerFixed().x, rmExit.top)] = Terrain.DOOR;
    }

    // link hall-> exit
    Painter.linkH(this, rmHall.top + 1, rmHall.right + 1, rmExit.centerFixed
                    ().x,
            Terrain.EMPTY);
    Painter.linkV(this, rmExit.centerFixed().x, rmHall.top + 1, rmExit.top -
            1, Terrain.EMPTY);

    // now add some prison cells on the lane side
    rmPrisonCells = new ArrayList<>();

    // prison cell is 3x3 inner space
    if (rmStart.centerFixed().x >= 5)
      placePrisonCellsBesideLaneV(rmStart.centerFixed().x, rmStart.bottom + 1,
              rmHall.bottom - 2, true);
    if (rmHall.left - rmStart.centerFixed().x >= 5)
      placePrisonCellsBesideLaneV(rmStart.centerFixed().x, rmStart.bottom + 1,
              rmHall.bottom - 2, false);

    if (rmPrisonCells.isEmpty())
      return false;

    if (width - rmExit.centerFixed().x >= 5)
      placePrisonCellsBesideLaneV(rmExit.centerFixed().x, rmHall.top + 2,
              rmExit.top - 1, false);
    if (rmExit.centerFixed().x - rmHall.right >= 5)
      placePrisonCellsBesideLaneV(rmExit.centerFixed().x, rmHall.top + 2,
              rmExit.top - 1, true);

    if (height - rmHall.bottom >= 5)
      placePrisonCellsBesideLaneH(rmHall.bottom - 1, rmStart.centerFixed().x,
              rmHall.right, false);
    if (rmHall.top >= 5)
      placePrisonCellsBesideLaneH(rmHall.top + 1, rmHall.left, rmExit
              .centerFixed().x, true);

    // at last, check the door pos
    for (int i : PathFinder.NEIGHBOURS4)
      if (map[hallEntrance() + i] == Terrain.DOOR || map[hallExit() + i] ==
              Terrain.DOOR)
        return false;

    // entrance && exit
    entrance = pointToCell(rmStart.centerFixed());
    exit = pointToCell(rmExit.centerFixed());

//    do {
//      entrance = pointToCell(rmHall.random(1));
//    } while (map[entrance] != Terrain.EMPTY);

    map[entrance] = Terrain.ENTRANCE;
    map[exit] = Terrain.EXIT;

    return true;
  }

  @Override
  protected void decorate() {
    // some blood on floor
    for (Room r : rmPrisonCells)
      if (Random.Int(2) == 0)
        map[pointToCell(r.random())] = Terrain.EMPTY_DECO;

    int cntblood = Random.Int(6, 15);
    while (cntblood > 0) {
      int pos = pointToCell(rmHall.random());
      if (map[pos] == Terrain.EMPTY) {
        map[pos] = Terrain.EMPTY_DECO;
        --cntblood;
      }
    }

    // some book shelves on the side
  }

  @Override
  public void press(int cell, Char ch) {
    super.press(cell, ch);

    // when hero enter the main hall, lock the level, let tengu in
    if (ch == Dungeon.hero && !enteredMainHall && rmHall.inside(cellToPoint
            (cell))) {
      enteredMainHall = true;
      onHeroEnteredHall(ch);
    }
  }

  private void onHeroEnteredHall(Char hero) {
    seal();

    // lock the hall exit
    set(hallEntrance(), Terrain.LOCKED_DOOR);
    GameScene.updateMap(hallEntrance());
    Dungeon.observe();

    // add dpd tengu
    Mob tengu = new DPDTengu();
    tengu.pos = pointToCell(rmHall.centerFixed());
    GameScene.add(tengu);
    tengu.state = tengu.HUNTING;

    tengu.notice();

    // give buff
    Buff.prolong(hero, Ignorant.class, 1000);
    
  }

  @Override
  public Heap drop(Item item, int cell) {
    if (item instanceof SkeletonKey) {
      // tengu is dead...
      unseal();

      set(hallEntrance(), Terrain.DOOR);
      GameScene.updateMap(hallEntrance());
      Dungeon.observe();
    }

    return super.drop(item, cell);
  }

  // place rooms beside a vertical lane
  private void placePrisonCellsBesideLaneV(int x, int y1, int y2, boolean
          atLeft) {
    int s = y1 < y2 ? y1 : y2;
    int e = y1 < y2 ? y2 : y1;

    int left = atLeft ? x - 5 : x + 1;
    int currentTop = s;

    while (currentTop + 4 <= e) {
      if (Random.Int(2) == 0) {
        // skip to next row
        ++currentTop;
        continue;
      }

      Room r = placePrisonCell(left, currentTop, atLeft ? 1 : 0);
      rmPrisonCells.add(r);
      currentTop += 4;
    }
  }

  private void placePrisonCellsBesideLaneH(int y, int x1, int x2, boolean up) {
    int s = x1 < x2 ? x1 : x2;
    int e = x1 < x2 ? x2 : x1;

    int top = up ? y - 5 : y + 1;
    int currentLeft = s;
    while (currentLeft + 4 < e) {
      if (Random.Int(2) == 0) {
        ++currentLeft;
        continue;
      }

      Room r = placePrisonCell(currentLeft, top, up ? 3 : 2);
      rmPrisonCells.add(r);
      currentLeft += 4;
    }
  }

  // door: 0, 1, 2, 3: left, right, top, bottom
  private Room placePrisonCell(int left, int top, int doordir) {
    // size is fixed 3x3
    int w = 4;
    int h = 4;

    Room r = (Room) new Room().set(left, top, left + w, top + h);

    Painter.fill(this, r, Terrain.WALL);
    Painter.fill(this, r.shrink(1), Terrain.EMPTY);

    int door = -1;
    int light = -1;
    switch (doordir) {
      case 0:
        door = pos(left, top + 2);
        light = pos(left + w, top + 2);
        break;
      case 1:
        door = pos(left + w, top + 2);
        light = pos(left, top + 2);
        break;
      case 2:
        door = pos(left + 2, top);
        light = pos(left + 2, top + h);
        break;
      case 3:
        door = pos(left + 2, top + h);
        light = pos(left + 2, top);
        break;
    }

    if (door > 0)
      map[door] = Terrain.DOOR;
    if (light > 0)
      map[light] = Terrain.WALL_LIGHT_ON;

    return r;
  }

  private void buildHall() {
    int width = Random.Int(16, 20);
    int height = Random.Int(16, 20);
    int left = Random.Int(8, this.width - 8 - width);
    int top = Random.Int(8, this.height - 8 - height);

    rmHall = (Room) new Room().set(left, top, left + width, top + height);

    // paint
    Painter.fill(this, rmHall.shrink(), Terrain.EMPTY);

    // center
    int cx = rmHall.centerFixed().x;
    int cy = rmHall.centerFixed().y;
    Painter.fill(this, cx - 1, cy - 1, 3, 3, Terrain.EMPTY_SP);

    // centroid is water, to avoid fire damage
    map[pos(cx, cy)] = Terrain.WATER;

    // 8 lights
    hallLights = new int[12];
    {
      hallLights[0] = randomPos(rmHall.left, cx - 3, rmHall.top, cy - 3, 1);
      hallLights[1] = randomPos(cx - 3, cx + 4, rmHall.top, cy - 3, 1);
      hallLights[2] = randomPos(cx + 4, rmHall.right, rmHall.top, cy - 3, 1);

      hallLights[3] = randomPos(rmHall.left, cx - 3, cy - 3, cy + 4, 1);
      hallLights[4] = randomPos(cx + 4, rmHall.right, cy - 3, cy + 4, 1);

      hallLights[5] = randomPos(rmHall.left, cx - 3, cy + 4, rmHall.bottom, 1);
      hallLights[6] = randomPos(cx - 3, cx + 4, cy + 4, rmHall.bottom, 1);
      hallLights[7] = randomPos(cx + 4, rmHall.right, cy + 4, rmHall.bottom, 1);

      // put in the corners
      hallLights[8] = pos(cx - 3, cy - 3);
      hallLights[9] = pos(cx + 3, cy - 3);
      hallLights[10] = pos(cx - 3, cy + 3);
      hallLights[11] = pos(cx + 3, cy + 3);
    }
    for (int i : hallLights)
      map[i] = Terrain.WALL_LIGHT_ON;

    // surround by traps
    for (int x = cx - 3; x <= cx + 3; ++x) {
      for (int y = cy - 3; y <= cy + 3; ++y) {
        int pos = pos(x, y);
        if (map[pos] == Terrain.EMPTY) {
          Trap t = new SpearTrap().reveal();
          setTrap(t, pos);
          map[pos] = Terrain.TRAP;
        }
      }
    }

    // an alarm trap at left bottom,
    {
      Trap t = new AlarmTrap().hide();
      int pos = pos(rmHall.left + 1, rmHall.bottom - 1);
      setTrap(t, pos);

      map[pos] = Terrain.SECRET_TRAP;
      map[pos(rmHall.left + 1, rmHall.bottom - 2)] = Terrain.WALL;
    }

    // door
    // enter
    map[pos(rmHall.left, rmHall.bottom - 1)] = Terrain.LOCKED_DOOR;
    // exit
    map[pos(rmHall.right, rmHall.top + 1)] = Terrain.LOCKED_EXIT;
  }

  private int hallEntrance() {
    return pos(rmHall.left, rmHall.bottom - 1);
  }

  private int hallExit() {
    return pos(rmHall.right, rmHall.top + 1);
  }

  private int pos(int x, int y) {
    return x + y * width;
  }

  private int randomPos(int xl, int xr, int yt, int yb, int inner) {
    return pos(Random.Int(xl + inner, xr - inner), Random.Int(yt + inner, yb
            - inner));
  }

  @Override
  protected void createMobs() {
  }

  @Override
  public Actor respawner() {
    return null;
  }

  @Override
  protected void createItems() {
    // dead body's bone
    Item item = Bones.get();
    if (item != null)
      drop(item, randomRespawnCell()).type = Heap.Type.REMAINS;

    // drop the key to the entrance
    ArrayList<Room> rmOnTheLeft = new ArrayList<>();
    for (Room rm : rmPrisonCells) {
      if (rm.right <= rmHall.left)
        rmOnTheLeft.add(rm);
    }
    int keypos = pointToCell(Random.element(rmOnTheLeft).random());
    IronKey ik = new IronKey(Dungeon.depth);
    drop(ik, keypos);
    
    // give a torch, torches is necessary to fight tengu
    int torchpos  = pointToCell(Random.element(rmOnTheLeft).random());
    drop(new Torch(), torchpos);
  }

  @Override
  public String tileName(int tile) {
    switch (tile) {
      case Terrain.WATER:
        return Messages.get(PrisonLevel.class, "water_name");
      default:
        return super.tileName(tile);
    }
  }

  @Override
  public String tileDesc(int tile) {
    switch (tile) {
      case Terrain.EMPTY_DECO:
        return Messages.get(PrisonLevel.class, "empty_deco_desc");
      case Terrain.BOOKSHELF:
        return Messages.get(PrisonLevel.class, "book_self_desc");
      default:
        return super.tileDesc(tile);
    }
  }

  private static final String ENTERED = "entered";
  private static final String HALL = "hall";
  private static final String LIGHTED  = "lighted";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);

    bundle.put(ENTERED, enteredMainHall);
    bundle.put(HALL, rmHall);
    bundle.put(LIGHTED, isLighted);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);

    enteredMainHall = bundle.getBoolean(ENTERED);
    isLighted = bundle.getBoolean(LIGHTED);
    rmHall = new Room();
    rmHall.restoreFromBundle(bundle.getBundle(HALL));
  }
}
