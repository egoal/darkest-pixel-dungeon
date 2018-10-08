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
package com.egoal.darkestpixeldungeon.items.weapon.enchantments;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

import java.util.EventListener;

public class Vampiric extends Weapon.Enchantment {

  private static ItemSprite.Glowing RED = new ItemSprite.Glowing(0x660022);

  @Override
  public Damage proc(Weapon weapon, Damage damage) {
    Char defender = (Char) damage.to;
    Char attacker = (Char) damage.from;

    int level = Math.max(0, weapon.level());

    // lvl 0 - 20% -> .25
    // lvl 1 - 21.5% -> .268
    // lvl 2 - 23% -> .286
    int maxValue = Math.round(damage.value * ((level + 10) / (float) (level +
            40)));
    int effValue = Math.min(Random.IntRange(0, maxValue), attacker.HT - 
            attacker.HP);

    if (effValue > 0) {

      attacker.HP += effValue;
      attacker.sprite.emitter().start(Speck.factory(Speck.HEALING), 0.4f, 1);
      attacker.sprite.showStatus(CharSprite.POSITIVE, Integer.toString
              (effValue));

    }

    return damage.addElement(Damage.Element.SHADOW);
  }

  @Override
  public ItemSprite.Glowing glowing() {
    return RED;
  }
}
