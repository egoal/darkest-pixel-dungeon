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
package com.egoal.darkestpixeldungeon.items.scrolls;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.effects.SpellSprite;
import com.watabou.noosa.audio.Sample;

public class ScrollOfMagicMapping extends Scroll {

  {
    initials = 3;
  }

  @Override
  protected void doRead() {

    int length = Dungeon.level.length();
    int[] map = Dungeon.level.map;
    boolean[] mapped = Dungeon.level.mapped;
    boolean[] discoverable = Level.discoverable;

    boolean noticed = false;

    for (int i = 0; i < length; i++) {

      int terr = map[i];

      if (discoverable[i]) {

        mapped[i] = true;
        if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {

          Dungeon.level.discover(i);

          if (Dungeon.visible[i]) {
            GameScene.discoverTile(i, terr);
            discover(i);

            noticed = true;
          }
        }
      }
    }
    GameScene.updateFog();

    GLog.i(Messages.get(this, "layout"));
    if (noticed) {
      Sample.INSTANCE.play(Assets.SND_SECRET);
    }

    SpellSprite.show(curUser, SpellSprite.MAP);
    Sample.INSTANCE.play(Assets.SND_READ);
    Invisibility.dispel();

    setKnown();

    readAnimation();
  }

  @Override
  public int price() {
    return isKnown() ? 40 * quantity : super.price();
  }

  public static void discover(int cell) {
    CellEmitter.get(cell).start(Speck.factory(Speck.DISCOVER), 0.1f, 4);
  }
}
