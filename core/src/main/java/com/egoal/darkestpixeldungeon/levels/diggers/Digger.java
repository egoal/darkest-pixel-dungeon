package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.watabou.utils.Point;

import java.util.ArrayList;

/**
 * Created by 93942 on 11/11/2018.
 */

public abstract class Digger {
  public static final int LEFT = -1;
  public static final int RIGHT = 1;
  public static final int NONE = 0;
  public static final int UP = -2;
  public static final int DOWN = 2;

  public static int[] directions = new int[4];

  {
    directions[0] = LEFT;
    directions[1] = RIGHT;
    directions[2] = UP;
    directions[3] = DOWN;
  }

  public static void set(Level level, int cell, int tile) {
    level.map[cell] = tile;
  }

  public static void set(Level level, int x, int y, int tile) {
    set(level, level.xy2cell(x, y), tile);
  }
  
  public static void set(Level level, Point pos, int tile){
    set(level, pos.x, pos.y, tile);
  }

  public static void fill(Level level, int x, int y, int w, int h, int tile) {
    for (int r = 0; r < h; ++r)
      for (int c = 0; c < w; ++c)
        set(level, x + c, y + r, tile);
  }

  public static void fill(Level level, XRect rect, int tile) {
    fill(level, rect.x1, rect.y1, rect.w(), rect.h(), tile);
  }

  static public boolean isAll(Level level, XRect rect, int tile){
    for (int x = rect.x1; x <= rect.x2; ++x)
      for (int y = rect.y1; y <= rect.y2; ++y)
        if(level.map[level.xy2cell(x, y)]!= tile)
          return false;

    return true;
  }
  
  // class digger
  protected XRoom room;

  public Digger(XRoom room) {
    this.room = room;
  }

  public abstract ArrayList<XWall> dig(Level level);

}
