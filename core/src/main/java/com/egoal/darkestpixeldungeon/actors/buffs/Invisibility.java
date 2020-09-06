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
package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.items.artifacts.CloakOfShadows;
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;

public class Invisibility extends FlavourBuff {

  public static final float DURATION = 20f;

  {
    type = buffType.POSITIVE;
  }

  @Override
  public boolean attachTo(Char target) {
    if (super.attachTo(target)) {
      target.setInvisible(target.getInvisible() + 1);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void detach() {
    if (target.getInvisible() > 0)
      target.setInvisible(target.getInvisible() - 1);
    super.detach();
  }

  @Override
  public int icon() {
    return BuffIndicator.INVISIBLE;
  }

  @Override
  public void fx(boolean on) {
    if (on) target.getSprite().add(CharSprite.State.INVISIBLE);
    else if (target.getInvisible() == 0)
      target.getSprite().remove(CharSprite.State.INVISIBLE);
  }

  @Override
  public String toString() {
    return Messages.get(this, "name");
  }

  @Override
  public String desc() {
    return Messages.get(this, "desc", dispTurns());
  }

  public static void dispel() {
    Invisibility buff = Dungeon.hero.buff(Invisibility.class);
    if (buff != null) {
      buff.detach();
    }
    CloakOfShadows.cloakStealth cloakBuff = Dungeon.hero.buff(CloakOfShadows
            .cloakStealth.class);
    if (cloakBuff != null) {
      cloakBuff.dispel();
    }
    
    //this isn't a form of invisibilty, but it is meant to dispel at the same
    // time as it.
//    TimekeepersHourglass.TimeFreeze timeFreeze = Dungeon.hero.buff
//            (TimekeepersHourglass.TimeFreeze.class);
//    if (timeFreeze != null) 
//      timeFreeze.detach();
    
  }
}
