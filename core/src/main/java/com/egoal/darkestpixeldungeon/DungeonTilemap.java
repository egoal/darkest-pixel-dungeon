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
package com.egoal.darkestpixeldungeon;

import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.noosa.Image;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.Tilemap;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

public class DungeonTilemap extends Tilemap {

  public static final int SIZE = 16;

  private static DungeonTilemap instance;

  public DungeonTilemap() {
    super(
            Dungeon.INSTANCE.getLevel().tilesTex(),
            new TextureFilm(Dungeon.INSTANCE.getLevel().tilesTex(), SIZE, SIZE));
    map(Dungeon.INSTANCE.getLevel().getMap(), Dungeon.INSTANCE.getLevel().width());

    instance = this;
  }

  public int screenToTile(int x, int y) {
    Point p = camera().screenToCamera(x, y).
            offset(this.point().negate()).
            invScale(SIZE).
            floor();
    return p.x >= 0
            && p.x < Dungeon.INSTANCE.getLevel().width()
            && p.y >= 0
            && p.y < Dungeon.INSTANCE.getLevel().height() ?
            p.x + p.y * Dungeon.INSTANCE.getLevel().width()
            : -1;
  }

  @Override
  public boolean overlapsPoint(float x, float y) {
    return true;
  }

  public void discover(int pos, int oldValue) {

    final Image tile = tile(oldValue);
    tile.point(tileToWorld(pos));

    // For bright mode
    tile.rm = tile.gm = tile.bm = rm;
    tile.ra = tile.ga = tile.ba = ra;
    parent.add(tile);

    parent.add(new AlphaTweener(tile, 0, 0.6f) {
      protected void onComplete() {
        tile.killAndErase();
        killAndErase();
      }

      ;
    });
  }

  public static PointF tileToWorld(int pos) {
    return new PointF(pos % Dungeon.INSTANCE.getLevel().width(), pos / Dungeon.INSTANCE.getLevel().width
            ()).scale(SIZE);
  }

  public static PointF tileCenterToWorld(int pos) {
    return new PointF(
            (pos % Dungeon.INSTANCE.getLevel().width() + 0.5f) * SIZE,
            (pos / Dungeon.INSTANCE.getLevel().width() + 0.5f) * SIZE);
  }

  public static Image tile(int index) {
    Image img = new Image(instance.texture);
    img.frame(instance.tileset.get(index));
    return img;
  }

  @Override
  public boolean overlapsScreenPoint(int x, int y) {
    return true;
  }

  @Override
  protected boolean needsRender(int pos) {
    return (Level.Companion.getDiscoverable()[pos] || Dungeon.INSTANCE.getLevel().getMap()[pos] == Terrain.CHASM) && Dungeon.INSTANCE.getLevel().getMap()[pos] != Terrain.WATER;
  }
}
