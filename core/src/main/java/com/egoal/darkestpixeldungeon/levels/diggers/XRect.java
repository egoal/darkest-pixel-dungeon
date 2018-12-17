package com.egoal.darkestpixeldungeon.levels.diggers;

import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Point;

import java.util.HashSet;

/**
 * Created by 93942 on 11/27/2018.
 */

// aabb box, 
public class XRect implements Bundlable {
  public int x1, x2, y1, y2;

  // default constructor for deserialization only 
  public XRect() {
  }

  public XRect(int _x1, int _x2, int _y1, int _y2) {
    x1 = _x1;
    x2 = _x2;
    y1 = _y1;
    y2 = _y2;
  }

  public static XRect create(int x, int y, int w, int h) {
    return new XRect(x, x + w - 1, y, y + h - 1);
  }

  public int w() {
    return x2 - x1 + 1;
  }

  public int h() {
    return y2 - y1 + 1;
  }

  public Point cen() {
    return new Point((x1 + x2) / 2, (y1 + y2) / 2);
  }

  public Point random(int inner) {
    return new Point(Random.IntRange(x1 + inner, x2 - inner),
            Random.IntRange(y1 + inner, y2 - inner));
  }

  public Point random() {
    return random(0);
  }

  public XRect inner(int i) {
    return new XRect(x1 + i, x2 - i, y1 + i, y2 - i);
  }

  public XRect overlap(XRect rect) {
    return new XRect(Math.max(x1, rect.x1), Math.min(x2, rect.x2),
            Math.max(y1, rect.y1), Math.min(y2, rect.y2));
  }

  public boolean isValid() {
    return x1 <= x2 && y1 <= y2;
  }

  public HashSet<Point> getAllPoints() {
    HashSet<Point> points = new HashSet<>(w() * h());
    for (int x = x1; x <= x2; ++x)
      for (int y = y1; y <= y2; ++y)
        points.add(new Point(x, y));

    return points;
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    x1 = bundle.getInt("x1");
    x2 = bundle.getInt("x2");
    y1 = bundle.getInt("y1");
    y2 = bundle.getInt("y2");
  }

  @Override
  public void storeInBundle(Bundle bundle) {
    bundle.put("x1", x1);
    bundle.put("x2", x2);
    bundle.put("y1", y1);
    bundle.put("y2", y2);
  }
}
