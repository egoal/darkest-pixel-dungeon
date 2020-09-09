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

import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.TrapSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class TeleportationTrap extends Trap {

  {
    color = TrapSprite.TEAL;
    shape = TrapSprite.DOTS;
  }

  @Override
  public void activate() {

    CellEmitter.get(pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3);
    Sample.INSTANCE.play(Assets.SND_TELEPORT);

    Char ch = Actor.Companion.findChar(pos);
    if (ch instanceof Hero) {
      ScrollOfTeleportation.Companion.teleportHero((Hero) ch);
    } else if (ch != null) {
      int count = 10;
      int pos;
      do {
        pos = Dungeon.level.randomRespawnCell();
        if (count-- <= 0) {
          break;
        }
      } while (pos == -1);

      if (pos == -1 || Dungeon.bossLevel()) {

        GLog.w(Messages.get(ScrollOfTeleportation.class, "no_tele"));

      } else {
        ch.setPos(pos);
        if(ch instanceof Mob && ((Mob) ch).getState() == ((Mob) ch).getHUNTING())
          ((Mob) ch).setState(((Mob) ch).getWANDERING());
        
        ch.getSprite().place(ch.getPos());
        ch.getSprite().visible = Dungeon.visible[pos];

      }
    }

    Heap heap = Dungeon.level.getHeaps().get(pos);

    if (heap != null) {
      int cell = Dungeon.level.randomRespawnCell();

      Item item = heap.pickUp();

      if (cell != -1) {
        Dungeon.level.drop(item, cell);
      }
    }
  }
}
