package com.egoal.darkestpixeldungeon.levels.diggers.specials;

import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Questioner;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult;
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger;
import com.egoal.darkestpixeldungeon.levels.diggers.XRect;
import com.egoal.darkestpixeldungeon.levels.diggers.XWall;

/**
 * Created by 93942 on 2018/12/17.
 */

public class QuestionerDigger extends RectDigger {
  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY);

    // no door
    Questioner q = new Questioner().random().hold(rect);
    q.pos = level.pointToCell(overlapedWall(wall, rect).random());
    Set(level, q.pos, Terrain.WALL_SPECIAL);
    level.mobs.add(q);

    return new DigResult(DigResult.Type.LOCKED);
  }
}
