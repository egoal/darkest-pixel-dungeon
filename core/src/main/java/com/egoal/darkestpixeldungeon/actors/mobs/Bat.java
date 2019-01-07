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

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.sprites.BatSprite;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing;
import com.watabou.utils.Random;

public class Bat extends Mob {

  {
    spriteClass = BatSprite.class;

    HP = HT = 30;
    defenseSkill = 15;
    baseSpeed = 2f;

    EXP = 7;
    maxLvl = 15;

    flying = true;

    loot = new PotionOfHealing();
    lootChance = 0.1667f; //by default, see die()

    addResistances(Damage.Element.SHADOW, 1.25f);
  }

  @Override
  public Damage giveDamage(Char target) {
    if (Random.Int(4) == 0)
      return new Damage(Random.NormalIntRange(1, 5), this, target).type
              (Damage.Type.MENTAL);
    else
      return new Damage(Random.NormalIntRange(5, 15), this, target).addElement
              (Damage.Element.SHADOW);
  }

  @Override
  public int attackSkill(Char target) {
    return 16;
  }

  @Override
  public Damage defendDamage(Damage dmg) {
    dmg.value -= Random.NormalIntRange(0, 4);
    return dmg;
  }

  @Override
  public Damage attackProc(Damage damage) {
    if (damage.type != Damage.Type.MENTAL) {
      int reg = (int) (Math.min(damage.value, HT - HP) * .3f);

      if (reg > 0) {
        HP += reg;
        sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
      }
    }

    return damage;
  }

  @Override
  public void die(Object cause) {
    //sets drop chance
    lootChance = 1f / (8 + Dungeon.limitedDrops.batHP.count);
    super.die(cause);
  }

  @Override
  protected Item createLoot() {
    Dungeon.limitedDrops.batHP.count++;
    return super.createLoot();
  }

}
