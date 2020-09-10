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
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.artifacts.CapeOfThorns;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Dazzling extends Weapon.Enchantment {

  private static ItemSprite.Glowing YELLOW = new ItemSprite.Glowing(0xFFFF00);

  @Override
  public Damage proc(Weapon weapon, Damage damage) {
    Char defender = (Char) damage.to;
    // lvl 0 - 20%
    // lvl 1 - 33%
    // lvl 2 - 43%
    int level = Math.max(0, weapon.level());

    if (Random.Int(level + 5) >= 4) {

      Buff.Companion.prolong(defender, Blindness.class, Random.Float(1f, 1f + level));
      Buff.Companion.prolong(defender, Cripple.class, Random.Float(1f, 1f + level / 2f));
      defender.getSprite().emitter().burst(Speck.factory(Speck.LIGHT), 6);

    }

    return damage;
  }

  @Override
  public ItemSprite.Glowing glowing() {
    return YELLOW;
  }

}