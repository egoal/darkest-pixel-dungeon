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

import android.util.Pair;

import com.egoal.darkestpixeldungeon.actors.buffs.Pressure;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.artifacts.UnstableSpellbook;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.ItemStatusHandler;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.HeroSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public abstract class Scroll extends Item {

  public static final String AC_READ = "READ";

  protected static final float TIME_TO_READ = 1f;

  protected int initials;

  private static final Class<?>[] scrolls = {
          ScrollOfIdentify.class,
          ScrollOfMagicMapping.class,
          ScrollOfRecharging.class,
          ScrollOfRemoveCurse.class,
          ScrollOfTeleportation.class,
          ScrollOfUpgrade.class,
          ScrollOfRage.class,
          ScrollOfTerror.class,
          ScrollOfLullaby.class,
          ScrollOfEnchanting.class,
          ScrollOfPsionicBlast.class,
          ScrollOfMirrorImage.class,
          ScrollOfCurse.class,
          ScrollOfLight.class,
  };

  private static final HashMap<String, Integer> runes = new HashMap<String,
          Integer>() {
    {
      put("KAUNAN", ItemSpriteSheet.SCROLL_KAUNAN);
      put("SOWILO", ItemSpriteSheet.SCROLL_SOWILO);
      put("LAGUZ", ItemSpriteSheet.SCROLL_LAGUZ);
      put("YNGVI", ItemSpriteSheet.SCROLL_YNGVI);
      put("GYFU", ItemSpriteSheet.SCROLL_GYFU);
      put("RAIDO", ItemSpriteSheet.SCROLL_RAIDO);
      put("ISAZ", ItemSpriteSheet.SCROLL_ISAZ);
      put("MANNAZ", ItemSpriteSheet.SCROLL_MANNAZ);
      put("NAUDIZ", ItemSpriteSheet.SCROLL_NAUDIZ);
      put("BERKANAN", ItemSpriteSheet.SCROLL_BERKANAN);
      put("ODAL", ItemSpriteSheet.SCROLL_ODAL);
      put("TIWAZ", ItemSpriteSheet.SCROLL_TIWAZ);
      put("QI", ItemSpriteSheet.SCROLL_QI);
      put("LINGEL", ItemSpriteSheet.SCROLL_LINGEL);
    }
  };

  private static ItemStatusHandler<Scroll> handler;

  private String rune;

  public boolean ownedByBook = false;

  {
    stackable = true;
    defaultAction = AC_READ;

    cursed = false;
    cursedKnown = true;
  }

  @SuppressWarnings("unchecked")
  public static void initLabels() {
    handler = new ItemStatusHandler<>((Class<? extends Scroll>[]) scrolls,
            runes);
  }

  public static void save(Bundle bundle) {
    handler.save(bundle);
  }

  public static void saveSelectively(Bundle bundle, ArrayList<Item> items) {
    handler.saveSelectively(bundle, items);
  }

  @SuppressWarnings("unchecked")
  public static void restore(Bundle bundle) {
    handler = new ItemStatusHandler<>((Class<? extends Scroll>[]) scrolls,
            runes, bundle);
  }

  public Scroll() {
    super();
    reset();
  }

  @Override
  public void reset() {
    super.reset();
    image = handler.image(this);
    rune = handler.label(this);
  }

  ;

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    actions.add(AC_READ);
    return actions;
  }

  @Override
  public void execute(Hero hero, String action) {

    super.execute(hero, action);

    if (action.equals(AC_READ)) {

      if (hero.buff(Blindness.class) != null) {
        GLog.w(Messages.get(this, "blinded"));
      } else if (hero.buff(UnstableSpellbook.bookRecharge.class) != null
              && hero.buff(UnstableSpellbook.bookRecharge.class).isCursed()
              && !(this instanceof ScrollOfRemoveCurse)) {
        GLog.n(Messages.get(this, "cursed"));
      } else {
        // want read
        kotlin.Pair<Boolean, String> pr = hero.canRead();
        if (!pr.getFirst()) GLog.n(pr.getSecond());
        else {
          curUser = hero;
          curItem = detach(hero.getBelongings().backpack);
          doRead();
        }
      }

    }
  }

  abstract protected void doRead();

  protected void readAnimation() {
    curUser.spend(TIME_TO_READ);
    curUser.busy();
    ((HeroSprite) curUser.sprite).read();
  }

  public boolean isKnown() {
    return handler.isKnown(this);
  }

  public void setKnown() {
    if (!isKnown() && !ownedByBook) {
      handler.know(this);
    }

    Badges.INSTANCE.validateAllScrollsIdentified();
  }

  @Override
  public Item identify() {
    setKnown();
    return super.identify();
  }

  @Override
  public String name() {
    return isKnown() ? name : Messages.get(Scroll.class, rune);
  }

  @Override
  public String info() {
    return isKnown() ?
            desc() :
            Messages.get(this, "unknown_desc");
  }

  public Integer initials() {
    return isKnown() ? initials : null;
  }

  @Override
  public boolean isUpgradable() {
    return false;
  }

  @Override
  public boolean isIdentified() {
    return isKnown();
  }

  public static HashSet<Class<? extends Scroll>> getKnown() {
    return handler.known();
  }

  public static HashSet<Class<? extends Scroll>> getUnknown() {
    return handler.unknown();
  }

  public static boolean allKnown() {
    return handler.known().size() == scrolls.length;
  }

  @Override
  public int price() {
    return 30 * quantity;
  }
}
