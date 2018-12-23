package com.egoal.darkestpixeldungeon.levels.diggers.unordinary;

import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult;
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger;
import com.egoal.darkestpixeldungeon.levels.diggers.XRect;
import com.egoal.darkestpixeldungeon.levels.diggers.XWall;
import com.egoal.darkestpixeldungeon.levels.traps.FireTrap;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 12/4/2018.
 */

public class BlackSmithDigger extends RectDigger {
  @Override
  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(4, 7), Random.IntRange(4, 7));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.TRAP);
    Fill(level, rect.inner(1), Terrain.EMPTY_SP);

    Point din = overlapedWall(wall, rect).random(0);
    Set(level, din, Terrain.DOOR);

    // 2 weapons
    for (int i = 0; i < 2; ++i) {
      int pos;
      do {
        pos = level.pointToCell(rect.random(1));
      } while (level.map[pos] != Terrain.EMPTY_SP);
      level.drop(Generator.random(Random.oneOf(Generator.Category.ARMOR,
              Generator.Category.WEAPON)), pos);
    }

    // smith
    Blacksmith npc = new Blacksmith();
    do {
      npc.pos = level.pointToCell(rect.random(1));
    } while (level.heaps.get(npc.pos) != null);
    level.mobs.add(npc);

    // traps
    for (Point p : rect.getAllPoints()) {
      int cell = level.pointToCell(p);
      if (level.map[cell] == Terrain.TRAP)
        level.setTrap(new FireTrap().reveal(), cell);
    }

    DigResult dr = new DigResult();
    dr.type = DigResult.Type.SPECIAL;
    dr.walls = walls(rect, wall.direction);
    
    return dr;
  }
}
