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
package com.egoal.darkestpixeldungeon.items.potions;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;

public class PotionOfInvisibility extends Potion {

  private static final float ALPHA = 0.4f;

  {
    initials = 3;
  }

  @Override
  public void apply(Hero hero) {
    setKnown();
    Buff.affect(hero, Invisibility.class, Invisibility.DURATION);
    GLog.i(Messages.get(this, "invisible"));
    Sample.INSTANCE.play(Assets.SND_MELD);
  }

  @Override
  public int price() {
    return isKnown() ? (int) (40 * quantity * (reinforced ? 1.5 : 1)) : super
            .price();
  }

  public static void melt(Char ch) {
    if (ch.sprite.parent != null) {
      ch.sprite.parent.add(new AlphaTweener(ch.sprite, ALPHA, 0.4f));
    } else {
      ch.sprite.alpha(ALPHA);
    }
  }
}
