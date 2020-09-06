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

import com.egoal.darkestpixeldungeon.PropertyConfiger;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.actors.buffs.Chill;
import com.egoal.darkestpixeldungeon.actors.buffs.Frost;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.egoal.darkestpixeldungeon.items.wands.WandOfFireblast;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Blazing;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.sprites.ElementalSprite;
import com.watabou.utils.Random;

import java.util.HashSet;

public class Elemental extends Mob {

  {
    PropertyConfiger.INSTANCE.set(this, "Elemental");
    spriteClass = ElementalSprite.class;

    loot = new PotionOfLiquidFlame();

    setFlying(true);
  }

  @Override
  public Damage giveDamage(Char target) {
    return super.giveDamage(target).addElement(Damage.Element.FIRE);
  }

  @Override
  public Damage attackProc(Damage damage) {
    Char enemy = (Char) damage.to;
    if (Random.Int(2) == 0) {
      Buff.affect(enemy, Burning.class).reignite(enemy);
    }

    return damage;
  }

  @Override
  public void add(Buff buff) {
    if (buff instanceof Burning) {
      if (getHP() < getHT()) {
        setHP(getHP() + 1);
        getSprite().emitter().burst(Speck.factory(Speck.HEALING), 1);
      }
    } else if (buff instanceof Frost || buff instanceof Chill) {
      if (Level.Companion.getWater()[this.getPos()])
        takeDamage(new Damage(Random.NormalIntRange(getHT() / 2, getHT()), buff, this)
                .addElement(Damage.Element.ICE));
      else
        takeDamage(new Damage(Random.NormalIntRange(1, getHT() * 2 / 3), buff,
                this).addElement(Damage.Element.ICE));
    } else {
      super.add(buff);
    }
  }

  private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

  static {
    IMMUNITIES.add(Burning.class);
    IMMUNITIES.add(Blazing.class);
    IMMUNITIES.add(WandOfFireblast.class);
  }

  @Override
  public HashSet<Class<?>> immunizedBuffs() {
    return IMMUNITIES;
  }
}
