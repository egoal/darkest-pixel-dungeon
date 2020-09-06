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
package com.egoal.darkestpixeldungeon.levels.traps;

import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.KindOfWeapon;
import com.egoal.darkestpixeldungeon.items.weapon.melee.Knuckles;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.TrapSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class DisarmingTrap extends Trap {

  {
    color = TrapSprite.RED;
    shape = TrapSprite.LARGE_DOT;
  }

  @Override
  public void activate() {
    Heap heap = Dungeon.level.getHeaps().get(pos);

    if (heap != null) {
      int cell = Dungeon.level.randomRespawnCell();

      if (cell != -1) {
        Item item = heap.pickUp();
        Dungeon.level.drop(item, cell).setSeen(true);
        for (int i : PathFinder.NEIGHBOURS9)
          Dungeon.level.getVisited()[cell + i] = true;
        GameScene.updateFog();

        Sample.INSTANCE.play(Assets.SND_TELEPORT);
        CellEmitter.get(pos).burst(Speck.factory(Speck.LIGHT), 4);
      }
    }

    if (Dungeon.hero.getPos() == pos) {
      Hero hero = Dungeon.hero;
      KindOfWeapon weapon = hero.getBelongings().getWeapon();

      if (weapon != null && !(weapon instanceof Knuckles) && !weapon.cursed) {

        int cell = Dungeon.level.randomRespawnCell();
        if (cell != -1) {
          hero.getBelongings().setWeapon(null);
          Dungeon.quickslot.clearItem(weapon);
          weapon.updateQuickslot();

          Dungeon.level.drop(weapon, cell).setSeen(true);
          for (int i : PathFinder.NEIGHBOURS9)
            Dungeon.level.getVisited()[cell + i] = true;
          GameScene.updateFog();

          GLog.w(Messages.get(this, "disarm"));

          Sample.INSTANCE.play(Assets.SND_TELEPORT);
          CellEmitter.get(pos).burst(Speck.factory(Speck.LIGHT), 4);

        }

      }
    }
  }
}
