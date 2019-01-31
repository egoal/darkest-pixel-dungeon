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
package com.egoal.darkestpixeldungeon.items.unclassified;

import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class Dewdrop extends Item {

  {
    image = ItemSpriteSheet.DEWDROP;

    stackable = true;
  }

  @Override
  public boolean doPickUp(Hero hero) {

    DewVial vial = hero.belongings.getItem(DewVial.class);

    if (vial == null || vial.isFull()) {

      int value = 1 + (Dungeon.depth - 1) / 5;
      if (hero.subClass == HeroSubClass.WARDEN) {
        value += 2;
      }

      int effect = Math.min(hero.HT - hero.HP, value * quantity);
      if (effect > 0) {
        hero.HP += effect;
        hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
        hero.sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, 
                "value", effect));
      } else {
        GLog.i(Messages.get(this, "already_full"));
        return false;
      }

    } else {

      vial.collectDew(this);

    }

    Sample.INSTANCE.play(Assets.SND_DEWDROP);
    hero.spendAndNext(TIME_TO_PICK_UP);

    return true;
  }
}
