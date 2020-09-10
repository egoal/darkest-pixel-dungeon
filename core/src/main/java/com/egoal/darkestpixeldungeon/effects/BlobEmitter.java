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
package com.egoal.darkestpixeldungeon.effects;

import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Random;

public class BlobEmitter extends Emitter {

  private Blob blob;

  public BlobEmitter(Blob blob) {

    super();

    this.blob = blob;
    blob.use(this);
  }

  @Override
  protected void emit(int index) {

    if (blob.getVolume() <= 0) {
      return;
    }

    if (blob.getArea().isEmpty())
      blob.setupArea();

    int[] map = blob.getCur();
    float size = DungeonTilemap.SIZE;

    int cell;
    for (int i = blob.getArea().left; i < blob.getArea().right; i++) {
      for (int j = blob.getArea().top; j < blob.getArea().bottom; j++) {
        cell = i + j * Dungeon.level.width();
        if (map[cell] > 0 && Dungeon.visible[cell]) {
          float x = (i + Random.Float()) * size;
          float y = (j + Random.Float()) * size;
          factory.emit(this, index, x, y);
        }
      }
    }
  }
}
