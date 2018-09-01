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

import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Berserk;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite.Glowing;
import com.watabou.utils.Random;

public class Lucky extends Weapon.Enchantment {

  private static ItemSprite.Glowing GREEN = new ItemSprite.Glowing(0x00FF00);

  @Override
  public Damage proc(Weapon weapon, Damage damage) {
    Char defender = (Char) damage.to;
    Char attacker = (Char) damage.from;

    int level = Math.max(0, weapon.level());

    if (Random.Int(100) < (55 + level)) {
      int exStr = 0;
      if (attacker == Dungeon.hero)
        exStr = Math.max(0, Dungeon.hero.STR() - weapon.STRReq());
      damage.value = weapon.imbue.damageFactor(weapon.max()) + exStr;
      damage = defender.defendDamage(damage);
    } else {
      damage.value = weapon.imbue.damageFactor(weapon.min());
      damage = defender.defendDamage(damage);
    }

    // berserker perk
    if (attacker == Dungeon.hero && Dungeon.hero.subClass == HeroSubClass
            .BERSERKER) {
      damage.value = Buff.affect(Dungeon.hero, Berserk.class).damageFactor
              (damage.value);
    }

    damage.value = Math.max(0, damage.value);
    return damage;
  }

  @Override
  public Glowing glowing() {
    return GREEN;
  }
}
