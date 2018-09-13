package com.egoal.darkestpixeldungeon.levels.traps;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Bones;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.mobs.Bestiary;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.actors.mobs.Rat;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.PrisonLevel;
import com.egoal.darkestpixeldungeon.levels.Room;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.painters.Painter;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;

import java.util.Arrays;

/**
 * Created by 93942 on 9/13/2018.
 */

public class DPDPrisonBossLevel extends Level {
  {
    color1 = 0x6a723d;
    color2 = 0x88924c;
  }

  @Override
  public String tilesTex() {
    return Assets.TILES_PRISON;
  }

  @Override
  public String waterTex() {
    return Assets.WATER_PRISON;
  }

  @Override
  protected boolean build() {
    // placePrisonCell();
    buildMap();

    return true;
  }

  @Override
  protected void decorate() {
  }

  private void placePrisonCell() {
    int w = 5;
    int h = 5;
    int x = Random.Int(width - w);
    int y = Random.Int(0, height - h);

    Rect r = new Rect(x, y, x + w, y + h);

    Painter.fill(this, r, Terrain.WALL);
    Painter.fill(this, r.shrink(1), Terrain.EMPTY);
  }

  private void buildMap() {
    Arrays.fill(map, Terrain.WALL);

    // hall
    Painter.fill(this, 2, 2, width - 4, 1, Terrain.EMPTY);

    // corner
    Room corner = (Room) new Room().set(2, 2, 6, 7);
    Painter.fill(this, corner, Terrain.EMPTY);
    map[pointToCell(corner.random(1))] = Terrain.SIGN;
    map[corner.right + (corner.bottom+1) * width] = Terrain.DOOR;

    // big hall
    Painter.fill(this, 4, 9, 21, 21, Terrain.EMPTY);
    
    // prison cell
    // bottom


    // entrance && exit
    entrance = width*2 + (width - 3);
    exit = pos(Random.Int(4, 4+21), Random.Int(9, 9+21));

    map[exit] = Terrain.EXIT;
  }

  private int pos(int x, int y){ return x+y*width; }
  
  @Override
  protected void createMobs() {
    Mob mob = new Rat();// Bestiary.mob(Dungeon.depth);

    mob.pos = randomRespawnCell();
    mobs.add(mob);
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
}
