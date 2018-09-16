/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.levels.painters;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Room;
import com.watabou.utils.Point;
import com.watabou.utils.Rect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Painter {

  public static void set(Level level, int cell, int value) {
    level.map[cell] = value;
  }

  public static void set(Level level, int x, int y, int value) {
    set(level, x + y * level.width(), value);
  }

  public static void set(Level level, Point p, int value) {
    set(level, p.x, p.y, value);
  }

  public static void fill(Level level, int x, int y, int w, int h, int value) {

    int width = level.width();

    int pos = y * width + x;
    for (int i = y; i < y + h; i++, pos += width) {
      Arrays.fill(level.map, pos, pos + w, value);
    }
  }

  public static void fill(Level level, Rect rect, int value) {
    fill(level, rect.left, rect.top, rect.width() + 1, rect.height() + 1,
            value);
  }

  public static void fill(Level level, Rect rect, int m, int value) {
    fill(level, rect.left + m, rect.top + m, rect.width() + 1 - m * 2, rect
            .height() + 1 - m * 2, value);
  }

  public static void fill(Level level, Rect rect, int l, int t, int r, int b,
                          int value) {
    fill(level, rect.left + l, rect.top + t, rect.width() + 1 - (l + r), rect
            .height() + 1 - (t + b), value);
  }

  // lane links, closed range[y1, y2]
  public static void linkV(Level level, int x, int y1, int y2, int value) {
    int s = y1 < y2 ? y1 : y2;
    int e = y1 < y2 ? y2 : y1;
      
    for (int y = s; y <= e; ++y)
      set(level, y * level.width() + x, value);
  }

  public static void linkH(Level level, int y, int x1, int x2, int value) {
    int s = x1 < x2 ? x1 : x2;
    int e = x1 < x2 ? x2 : x1;

    for (int x = s; x <= e; ++x)
      set(level, y * level.width() + x, value);
  }

  public static void randomLink(Level level, int x1, int y1, int x2, int y2,
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

  public static Point drawInside(Level level, Room room, Point from, int n,
                                 int value) {

    Point step = new Point();
    if (from.x == room.left) {
      step.set(+1, 0);
    } else if (from.x == room.right) {
      step.set(-1, 0);
    } else if (from.y == room.top) {
      step.set(0, +1);
    } else if (from.y == room.bottom) {
      step.set(0, -1);
    }

    Point p = new Point(from).offset(step);
    for (int i = 0; i < n; i++) {
      if (value != -1) {
        set(level, p, value);
      }
      p.offset(step);
    }

    return p;
  }
}
