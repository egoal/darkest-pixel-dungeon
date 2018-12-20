package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Statuary;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.PathFinder;

/**
 * Created by 93942 on 2018/12/5.
 */

public class StatuaryDigger extends NormalDiamondDigger {
  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    DigResult dr = super.dig(level, wall, rect);

    int ccen = level.pointToCell(rect.cen());
    for (int i : PathFinder.NEIGHBOURS9)
      Set(level, ccen + i, Terrain.EMPTY_SP);

    Statuary s = new Statuary().random();
    s.pos = ccen;
    level.mobs.add(s);

    return dr;
  }
}
