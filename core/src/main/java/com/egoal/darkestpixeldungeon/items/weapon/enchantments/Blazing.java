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
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Blazing extends Weapon.Enchantment {

  private static ItemSprite.Glowing ORANGE = new ItemSprite.Glowing(0xFF4400);

  @Override
  public Damage proc(Weapon weapon, Damage damage) {
    Char defender = (Char) damage.to;
    // lvl 0 - 33%
    // lvl 1 - 50%
    // lvl 2 - 60%
    int level = Math.max(0, weapon.level());

    if (Random.Int(level + 3) >= 2) {
      if (Random.Int(2) == 0) {
        Buff.affect(defender, Burning.class).reignite(defender);
      }
      // defender.damage( Random.Int( 1, level + 2 ), this );
      defender.takeDamage(new Damage(Random.Int(1, level + 2),
              this, defender).type(Damage.Type.MAGICAL).addElement(Damage
              .Element.FIRE));

      defender.getSprite().emitter().burst(FlameParticle.FACTORY, level + 1);
    }

    return damage.addElement(Damage.Element.FIRE);
  }

  @Override
  public ItemSprite.Glowing glowing() {
    return ORANGE;
  }
}
