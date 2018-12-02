package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;

import java.util.ArrayList;

/**
 * Created by 93942 on 12/2/2018.
 */

public class InitRoomDigger extends RectRoomDigger {
  @Override
  protected void createPattern() {
    // the wall only tells the rect position  
    pattern = new DigPattern(wall.x1, wall.x2, wall.y1, wall.y2);
    pattern.fill(Terrain.EMPTY);
  }

  @Override
  protected void digWall(Level level) {
    // nothing to do with walls
  }
}
