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

import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Amok;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Imp;
import com.egoal.darkestpixeldungeon.items.KindOfWeapon;
import com.egoal.darkestpixeldungeon.items.food.Food;
import com.egoal.darkestpixeldungeon.items.weapon.melee.Knuckles;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.MonkSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.HashSet;

public class Monk extends Mob {

  {
    spriteClass = MonkSprite.class;

    HP = HT = 70;
    defenseSkill = 30;

    EXP = 11;
    maxLvl = 21;

    loot = new Food();
    lootChance = 0.072f;

    addResistances(Damage.Element.SHADOW, 1.25f);
    addResistances(Damage.Element.HOLY, .667f);
  }

  @Override
  public Damage giveDamage(Char target) {
    int value = Random.NormalIntRange(12, 25);
    Damage dmg = new Damage(value, this, target).addElement(Damage.Element
            .ACID);
    if (value > 21)
      dmg.addFeature(Damage.Feature.CRITICAL);
    return dmg;
  }

  @Override
  public int attackSkill(Char target) {
    return 30;
  }

  @Override
  protected float attackDelay() {
    return 0.5f;
  }

  @Override
  public Damage defendDamage(Damage dmg) {
    dmg.value -= Random.NormalIntRange(0, 2);
    return dmg;
  }

  @Override
  public void die(Object cause) {
    Imp.Quest.process(this);

    super.die(cause);
  }

  private int hitsToDisarm = 0;

  @Override
  public Damage attackProc(Damage damage) {

    if (damage.to == Dungeon.hero) {

      Hero hero = Dungeon.hero;
      KindOfWeapon weapon = hero.getBelongings().weapon;

      if (weapon != null && !(weapon instanceof Knuckles) && !weapon.cursed) {
        if (hitsToDisarm == 0) hitsToDisarm = Random.NormalIntRange(5, 8);

        if (--hitsToDisarm == 0) {
          hero.getBelongings().weapon = null;
          Dungeon.quickslot.clearItem(weapon);
          weapon.updateQuickslot();
          Dungeon.level.drop(weapon, hero.pos).sprite.drop();
          GLog.w(Messages.get(this, "disarm", weapon.name()));
        }
      }
    }

    return damage;
  }

  private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

  static {
    IMMUNITIES.add(Amok.class);
    IMMUNITIES.add(Terror.class);
  }

  @Override
  public HashSet<Class<?>> immunizedBuffs() {
    return IMMUNITIES;
  }

  private static String DISARMHITS = "hitsToDisarm";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(DISARMHITS, hitsToDisarm);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    hitsToDisarm = bundle.getInt(DISARMHITS);
  }
}
