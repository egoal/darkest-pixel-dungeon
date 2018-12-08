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

package com.watabou.utils;

public class Point {

  public int x;
  public int y;

  public Point() {
  }

  public Point(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Point(Point p) {
    this.x = p.x;
    this.y = p.y;
  }

  public Point set(int x, int y) {
    this.x = x;
    this.y = y;
    return this;
  }

  public Point set(Point p) {
    x = p.x;
    y = p.y;
    return this;
  }

  public Point clone() {
    return new Point(this);
  }

  public Point scale(float f) {
    this.x *= f;
    this.y *= f;
    return this;
  }

  public Point offset(int dx, int dy) {
    x += dx;
    y += dy;
    return this;
  }

  public Point offset(Point d) {
    x += d.x;
    y += d.y;
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Point) {
      Point p = (Point) obj;
      return p.x == x && p.y == y;
    } else {
      return false;
    }
  }

  public static int DistanceL1(Point p1, Point p2) {
    return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
  }

  public static int DistanceL22(Point p1, Point p2) {
    return (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y);
  }

  public static int DistanceInf(Point p1, Point p2) {
    return Math.max(Math.abs(p1.x - p2.x), Math.abs(p1.y - p2.y));
  }
}
