package com.egoal.darkestpixeldungeon.levels.diggers;

import java.util.ArrayList;

/**
 * Created by 93942 on 2018/12/23.
 */ 

public class DigResult {
  // space type
  public enum Type {
    NORMAL,
    SPECIAL,
    LOCKED,

    PIT, // pit room only, fall from weak floor
    WEAK_FLOOR,

    EXIT, ENTRANCE,
  }

  public Type type;
  public ArrayList<XWall> walls;

  public DigResult() {
    this(Type.NORMAL);
  }

  public DigResult(Type type) {
    this.type = type;
    walls = new ArrayList<>();
  }

  public DigResult type(Type type) {
    this.type = type;
    return this;
  }

  public DigResult walls(ArrayList<XWall> walls) {
    this.walls = walls;
    return this;
  }
}
