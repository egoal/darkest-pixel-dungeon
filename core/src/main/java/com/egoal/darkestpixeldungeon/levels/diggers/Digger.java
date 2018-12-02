package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.watabou.utils.Point;

import java.util.ArrayList;
import java.util.Collections;

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

  public static void Set(Level level, int cell, int tile) {
    level.map[cell] = tile;
  }

  public static void Set(Level level, int x, int y, int tile) {
    Set(level, level.xy2cell(x, y), tile);
  }

  public static void Set(Level level, Point pos, int tile) {
    Set(level, pos.x, pos.y, tile);
  }

  public static void Fill(Level level, int x, int y, int w, int h, int tile) {
    for (int r = 0; r < h; ++r)
      for (int c = 0; c < w; ++c)
        Set(level, x + c, y + r, tile);
  }

  public static void Fill(Level level, XRect rect, int tile) {
    Fill(level, rect.x1, rect.y1, rect.w(), rect.h(), tile);
  }

  // lane links, closed range[y1, y2]
  public static void LinkV(Level level, int x, int y1, int y2, int value) {
    int s = y1 < y2 ? y1 : y2;
    int e = y1 < y2 ? y2 : y1;

    for (int y = s; y <= e; ++y)
      Set(level, y * level.width() + x, value);
  }

  public static void LinkH(Level level, int y, int x1, int x2, int value) {
    int s = x1 < x2 ? x1 : x2;
    int e = x1 < x2 ? x2 : x1;

    for (int x = s; x <= e; ++x)
      Set(level, y * level.width() + x, value);
  }

  public static void RandomLink(Level level, int x1, int y1, int x2, int y2,
                                int value) {
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

      level.map[y * level.width() + x] = value;
    }
  }

  public static void RandomLink(Level level, Point s, Point e, int value) {
    RandomLink(level, s.x, s.y, e.x, e.y, value);
  }

  static public boolean IsAll(Level level, XRect rect, int tile) {
    for (int x = rect.x1; x <= rect.x2; ++x)
      for (int y = rect.y1; y <= rect.y2; ++y)
        if (level.map[level.xy2cell(x, y)] != tile)
          return false;

    return true;
  }

  // class digger
  protected DigPattern pattern;
  protected XWall wall;

  // this must be called before dig!
  public Digger wall(XWall wall) {
    this.wall = wall;
    return this;
  }

  public static class DigResult {
    public DigPattern pattern;
    public ArrayList<XWall> walls;
  }

  public XRect desireDigSpace() {
    if (pattern == null)
      createPattern();
    return pattern;
  }

  public DigResult dig(Level level) {
    if (pattern == null)
      createPattern();
    
    // copy dig pattern
    digPattern(level);
    digWall(level);

    DigResult dr = new DigResult();
    dr.pattern = pattern;
    dr.walls = newDigableWalls();
    return dr;
  }

  // interface.
  protected abstract void createPattern();
  
  protected void digPattern(Level level){
    for (int x = pattern.x1; x <= pattern.x2; ++x)
      for (int y = pattern.y1; y <= pattern.y2; ++y)
        level.map[level.xy2cell(x, y)] = pattern.data[pattern.xy2cell(x, y)];
  }
  protected void digWall(Level level){
    
  }
  
  
  protected ArrayList<XWall> newDigableWalls() {
    return new ArrayList<>();
  }

}
