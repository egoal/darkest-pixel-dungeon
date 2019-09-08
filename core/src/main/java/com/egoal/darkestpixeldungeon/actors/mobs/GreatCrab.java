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
package com.egoal.darkestpixeldungeon.actors.mobs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.PropertyConfiger;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Ghost;
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat;
import com.egoal.darkestpixeldungeon.items.wands.Wand;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.GreatCrabSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;

public class GreatCrab extends Crab {

  {
    PropertyConfiger.INSTANCE.set(this, GreatCrab.class.getSimpleName());

    spriteClass = GreatCrabSprite.class;

    baseSpeed = 1f;
    state = WANDERING;
  }

  private int moving = 0;

  @Override
  protected boolean getCloser(int target) {
    //this is used so that the crab remains slower, but still detects the 
    // player at the expected rate.
    moving++;
    if (moving < 3) {
      return super.getCloser(target);
    } else {
      moving = 0;
      return true;
    }

  }

  @Override
  public int takeDamage(Damage dmg) {
    //crab blocks all attacks originating from the hero or enemy characters 
    // or traps if it is alerted.
    //All direct damage from these sources is negated, no exceptions. 
    // blob/debuff effects go through as normal.
    if ((enemySeen && state != SLEEPING && paralysed == 0)
            && (dmg.from instanceof Wand || dmg.from instanceof Char)) {
      GLog.n(Messages.get(this, "noticed"));
      sprite.showStatus(CharSprite.NEUTRAL, Messages.get(this, "blocked"));

      return 0;
    } else {
      return super.takeDamage(dmg);
    }
  }

  @Override
  public void die(Object cause) {
    super.die(cause);

    Ghost.Quest.process();

    Dungeon.level.drop(new MysteryMeat(), pos);
    Dungeon.level.drop(new MysteryMeat(), pos).sprite.drop();
  }
}
