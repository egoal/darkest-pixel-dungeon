package com.egoal.darkestpixeldungeon.levels.diggers;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by 93942 on 12/2/2018.
 */

public class DigPattern extends XRect {
  public static enum Type {
    NORMAL,
  }

  public int[] data;
  public Type type = Type.NORMAL;

  public DigPattern(int _x1, int _x2, int _y1, int _y2) {
    super(_x1, _x2, _y1, _y2);

    data = new int[w() * h()];
  }

  // dig helper
  public int xy2cell(int x, int y) {
    return (y - y1) * w() + (x - x1);
  }

  public int length() {
    return w() * h();
  }

  public void moveTo(int x1, int y1) {
    int w = w();
    int h = h();
    this.x1 = x1;
    this.x2 = x1 + w - 1;
    this.y1 = y1;
    this.y2 = y1 + h - 1;
  }

  public void set(int x, int y, int tile) {
    data[xy2cell(x, y)] = tile;
  }

  public void fill(int xl, int xr, int yt, int yb, int tile) {
    for (int x = xl; x <= xr; ++x)
      for (int y = yt; y <= yb; ++y)
        set(x, y, tile);
  }

  public void fill(int tile, int inner) {
    fill(x1 + inner, x2 - inner, y1 + inner, y2 - inner, tile);
  }

  public void fill(int tile) {
    fill(tile, 0);
  }
}
