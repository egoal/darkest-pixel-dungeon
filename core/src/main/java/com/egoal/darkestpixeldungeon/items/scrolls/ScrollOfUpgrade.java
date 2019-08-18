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
package com.egoal.darkestpixeldungeon.items.scrolls;

import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle;
import com.egoal.darkestpixeldungeon.items.armor.Armor;
import com.egoal.darkestpixeldungeon.items.rings.Ring;
import com.egoal.darkestpixeldungeon.items.wands.Wand;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndBag;
import com.egoal.darkestpixeldungeon.items.Item;
import com.watabou.utils.Random;

public class ScrollOfUpgrade extends InventoryScroll {

  {
    initials = 11;
    mode = WndBag.Mode.UPGRADEABLE;

    bones = true;
  }

  @Override
  protected void onItemSelected(Item item) {
    upgrade(curUser);
    curUser.recoverSanity(Random.Float(0.5f, 3.5f));

    //logic for telling the user when item properties change from upgrades
    //...yes this is rather messy
    if (item instanceof Weapon) {
      Weapon w = (Weapon) item;
      boolean wasCursed = w.cursed;
      boolean hadCursedEnchant = w.hasCurseEnchant();
      boolean hadGoodEnchant = w.hasGoodEnchant();

      w.upgrade();

      if (hadCursedEnchant && !w.hasCurseEnchant()) {
        removeCurse(Dungeon.hero);
      } else if (wasCursed && !w.cursed) {
        weakenCurse(Dungeon.hero);
      }
      if (hadGoodEnchant && !w.hasGoodEnchant()) {
        GLog.w(Messages.get(Weapon.class, "incompatible"));
      }

    } else if (item instanceof Armor) {
      Armor a = (Armor) item;
      boolean wasCursed = a.cursed;
      boolean hadCursedGlyph = a.hasCurseGlyph();
      boolean hadGoodGlyph = a.hasGoodGlyph();

      a.upgrade();

      if (hadCursedGlyph && !a.hasCurseGlyph()) {
        removeCurse(Dungeon.hero);
      } else if (wasCursed && !a.cursed) {
        weakenCurse(Dungeon.hero);
      }
      if (hadGoodGlyph && !a.hasGoodGlyph()) {
        GLog.w(Messages.get(Armor.class, "incompatible"));
      }

    } else if (item instanceof Wand) {
      boolean wasCursed = item.cursed;

      item.upgrade();

      if (wasCursed && !item.cursed) {
        removeCurse(Dungeon.hero);
      }

    } else if (item instanceof Ring) {
      boolean wasCursed = item.cursed;

      item.upgrade();

      if (wasCursed && !item.cursed) {
        if (item.level() < 1) {
          weakenCurse(Dungeon.hero);
        } else {
          removeCurse(Dungeon.hero);
        }
      }

    } else {
      item.upgrade();
    }

    Badges.validateItemLevelAquired(item);
  }

  public static void upgrade(Hero hero) {
    hero.sprite.emitter().start(Speck.factory(Speck.UP), 0.2f, 3);
  }

  public static void weakenCurse(Hero hero) {
    GLog.p(Messages.get(ScrollOfUpgrade.class, "weaken_curse"));
    hero.sprite.emitter().start(ShadowParticle.UP, 0.05f, 5);
  }

  public static void removeCurse(Hero hero) {
    GLog.p(Messages.get(ScrollOfUpgrade.class, "remove_curse"));
    hero.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10);
  }

  @Override
  public int price() {
    return isKnown() ? 50 * quantity : super.price();
  }
}
