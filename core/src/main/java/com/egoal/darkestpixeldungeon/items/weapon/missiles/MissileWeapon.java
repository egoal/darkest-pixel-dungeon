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
package com.egoal.darkestpixeldungeon.items.weapon.missiles;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.PinCushion;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass;
import com.egoal.darkestpixeldungeon.actors.hero.HeroPerk;
import com.egoal.darkestpixeldungeon.items.rings.RingOfSharpshooting;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Projecting;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.items.Item;
import com.watabou.utils.Random;

import java.util.ArrayList;

abstract public class MissileWeapon extends Weapon {

  {
    stackable = true;
    levelKnown = true;

    defaultAction = AC_THROW;
    usesTargeting = true;
  }

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    actions.remove(AC_EQUIP);
    return actions;
  }

  @Override
  public int throwPos(Hero user, int dst) {
    if (hasEnchant(Projecting.class)
            && !Level.solid[dst] && Dungeon.level.distance(user.pos, dst) <= 
            4) {
      return dst;
    } else {
      return super.throwPos(user, dst);
    }
  }

  @Override
  protected void onThrow(int cell) {
    Char enemy = Actor.findChar(cell);
    if (enemy == null || enemy == curUser) {
      if (this instanceof Boomerang)
        super.onThrow(cell);
      else
        miss(cell);
    } else {
      if (!curUser.shoot(enemy, this)) {
        miss(cell);
      } else if (!(this instanceof Boomerang)) {

        int bonus = RingOfSharpshooting.getBonus(curUser, RingOfSharpshooting
                .Aim.class);

        //* huntress bonus
        if (curUser.getHeroClass() == HeroClass.HUNTRESS && enemy.buff(PinCushion
                .class) == null)
          bonus += 3;

        if (Random.Float() > Math.pow(0.7, bonus)) {
          if (enemy.isAlive())
            Buff.affect(enemy, PinCushion.class).stick(this);
          else
            Dungeon.level.drop(this, enemy.pos).sprite.drop();
        }

      }
    }
  }

  protected void miss(int cell) {
    int bonus = RingOfSharpshooting.getBonus(curUser, RingOfSharpshooting.Aim
            .class);

    //degraded ring of sharpshooting will even make missed shots break.
    if (Random.Float() < Math.pow(0.6, -bonus))
      super.onThrow(cell);
  }

  @Override
  public Damage proc(Damage dmg) {
    Hero hero = (Hero) (dmg.from);
    // remove self
    if (hero.getRangedWeapon() == null && stackable) {
      if (quantity == 1)
        doUnequip(hero, false, false);
      else
        detach(null);
    }

    return super.proc(dmg);
  }

  @Override
  public Damage giveDamage(Hero hero, Char target) {
    Damage dmg = super.giveDamage(hero, target).addFeature(Damage.Feature
            .RANGED);

    // huntress perk
    int exstr = hero.STR() - STRReq();
    if (hero.getHeroPerk().contain(HeroPerk.Perk.SHOOTER))
      dmg.value += Random.Int(1, exstr);

    return dmg;
  }

  @Override
  public Item random() {
    return this;
  }

  @Override
  public boolean isUpgradable() {
    return false;
  }

  @Override
  public boolean isIdentified() {
    return true;
  }

  @Override
  public String info() {

    String info = desc();

    info += "\n\n" + Messages.get(MissileWeapon.class, "stats", imbue
            .damageFactor(min()), imbue.damageFactor(max()), STRReq());

    if (STRReq() > Dungeon.hero.STR()) {
      info += " " + Messages.get(Weapon.class, "too_heavy");
    } else if (Dungeon.hero.getHeroClass() == HeroClass.HUNTRESS && Dungeon.hero
            .STR() > STRReq()) {
      info += " " + Messages.get(Weapon.class, "excess_str", Dungeon.hero.STR
              () - STRReq());
    }

    if (enchantment != null && (cursedKnown || !enchantment.curse())) {
      info += "\n\n" + Messages.get(Weapon.class, "enchanted", enchantment
              .name());
      info += " " + Messages.get(enchantment, "desc");
    }

    if (cursed && isEquipped(Dungeon.hero)) {
      info += "\n\n" + Messages.get(Weapon.class, "cursed_worn");
    } else if (cursedKnown && cursed) {
      info += "\n\n" + Messages.get(Weapon.class, "cursed");
    }

    info += "\n\n" + Messages.get(MissileWeapon.class, "distance");

    return info;
  }
}
