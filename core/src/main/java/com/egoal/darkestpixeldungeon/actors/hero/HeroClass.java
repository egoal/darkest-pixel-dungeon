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
package com.egoal.darkestpixeldungeon.actors.hero;

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.items.ArmorKit;
import com.egoal.darkestpixeldungeon.items.CrownOfDwarf;
import com.egoal.darkestpixeldungeon.items.DemonicSkull;
import com.egoal.darkestpixeldungeon.items.DewVial;
import com.egoal.darkestpixeldungeon.items.Gold;
import com.egoal.darkestpixeldungeon.items.armor.MailArmor;
import com.egoal.darkestpixeldungeon.items.artifacts.Astrolabe;
import com.egoal.darkestpixeldungeon.items.TomeOfMastery;
import com.egoal.darkestpixeldungeon.items.UnholyBlood;
import com.egoal.darkestpixeldungeon.items.armor.PlateArmor;
import com.egoal.darkestpixeldungeon.items.ExtractionFlask;
import com.egoal.darkestpixeldungeon.items.artifacts.GoldPlatedStatue;
import com.egoal.darkestpixeldungeon.items.artifacts.HandOfTheElder;
import com.egoal.darkestpixeldungeon.items.artifacts.MasterThievesArmband;
import com.egoal.darkestpixeldungeon.items.artifacts.UrnOfShadow;
import com.egoal.darkestpixeldungeon.items.bags.SeedPouch;
import com.egoal.darkestpixeldungeon.items.books.Book;
import com.egoal.darkestpixeldungeon.items.food.Food;
import com.egoal.darkestpixeldungeon.items.Torch;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Challenges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.BrokenSeal;
import com.egoal.darkestpixeldungeon.items.armor.ClothArmor;
import com.egoal.darkestpixeldungeon.items.artifacts.CloakOfShadows;
import com.egoal.darkestpixeldungeon.items.food.Wine;
import com.egoal.darkestpixeldungeon.items.potions.*;
import com.egoal.darkestpixeldungeon.items.rings.RingOfAccuracy;
import com.egoal.darkestpixeldungeon.items.rings.RingOfEvasion;
import com.egoal.darkestpixeldungeon.items.rings.RingOfForce;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfCurse;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfLight;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfPsionicBlast;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTerror;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave;
import com.egoal.darkestpixeldungeon.items.wands.WandOfCorruption;
import com.egoal.darkestpixeldungeon.items.wands.WandOfDisintegration;
import com.egoal.darkestpixeldungeon.items.wands.WandOfMagicMissile;
import com.egoal.darkestpixeldungeon.items.wands.WandOfPrismaticLight;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Blazing;
import com.egoal.darkestpixeldungeon.items.weapon.melee.AssassinsBlade;
import com.egoal.darkestpixeldungeon.items.weapon.melee.BattleGloves;
import com.egoal.darkestpixeldungeon.items.weapon.melee.CrystalsSwords;
import com.egoal.darkestpixeldungeon.items.weapon.melee.Dagger;
import com.egoal.darkestpixeldungeon.items.weapon.melee.Knuckles;
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff;
import com.egoal.darkestpixeldungeon.items.weapon.melee.SorceressWand;
import com.egoal.darkestpixeldungeon.items.weapon.melee.Sword;
import com.egoal.darkestpixeldungeon.items.weapon.melee.WornShortsword;
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Boomerang;
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Dart;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.plants.Blindweed;
import com.egoal.darkestpixeldungeon.plants.Earthroot;
import com.egoal.darkestpixeldungeon.plants.Firebloom;
import com.egoal.darkestpixeldungeon.plants.Sorrowmoss;
import com.watabou.utils.Bundle;

public enum HeroClass {

  WARRIOR("warrior"),
  MAGE("mage"),
  ROGUE("rogue"),
  HUNTRESS("huntress"),

  SORCERESS("sorceress");

  private String title;

  HeroClass(String title) {
    this.title = title;
  }

  public void initHero(Hero hero) {

    hero.heroClass = this;

    initCommon(hero);

    switch (this) {
      case WARRIOR:
        initWarrior(hero);
        break;

      case MAGE:
        initMage(hero);
        break;

      case ROGUE:
        initRogue(hero);
        break;

      case HUNTRESS:
        initHuntress(hero);
        break;

      //
      case SORCERESS:
        initSorceress(hero);
        break;
    }

    initPerks(hero);
    hero.updateAwareness();
  }

  private static void initCommon(Hero hero) {
    if (!Dungeon.isChallenged(Challenges.NO_ARMOR))
      (hero.belongings.armor = new ClothArmor()).identify();

    if (!Dungeon.isChallenged(Challenges.NO_FOOD))
      new Food().identify().collect();

    if (!Dungeon.isChallenged(Challenges.DARKNESS))
      new Torch().identify().collect();

    if (DarkestPixelDungeon.debug()) {
      initDebug(hero);
    }
    
  }

  private static void initDebug(Hero hero) {
    hero.HP = 1000;
    hero.HT = 1000;
    hero.STR  = 18;
    hero.lvl  = 20;
    
    new ScrollOfMagicMapping().quantity(9).identify().collect();
    new ScrollOfLight().quantity(3).identify().collect();
    new ScrollOfCurse().quantity(3).identify().collect();
    
    new PotionOfHealing().quantity(9).identify().collect();
    new PotionOfMindVision().quantity(9).identify().collect();
    new PotionOfLiquidFlame().quantity(2).identify().collect();
    new PotionOfToxicGas().quantity(3).identify().collect();
    new PotionOfInvisibility().quantity(3).identify().collect();
    
    (new PlateArmor()).upgrade(9).identify().collect();
    (new AssassinsBlade()).upgrade(9).identify().collect();
    new MailArmor().upgrade(1).identify().collect();
    new Sword().upgrade(1).identify().collect();
    
    new Earthroot.Seed().quantity(3).identify().collect();
    
    (new TomeOfMastery()).identify().collect();
    (new ArmorKit()).identify().collect();

    (new SeedPouch()).identify().collect();
    
    new WandOfBlastWave().identify().collect();
    new WandOfCorruption().upgrade(10).identify().collect();
    new WandOfDisintegration().upgrade(2).identify().collect();
    
    // rings
    new HandOfTheElder().identify().collect();
    new RingOfEvasion().identify().collect();
    new RingOfForce().identify().collect();
    new RingOfForce().identify().collect();
    new RingOfAccuracy().identify().collect();
    
    new MasterThievesArmband().identify().collect();
  }

  public Badges.Badge masteryBadge() {
    switch (this) {
      case WARRIOR:
        return Badges.Badge.MASTERY_WARRIOR;
      case MAGE:
        return Badges.Badge.MASTERY_MAGE;
      case ROGUE:
        return Badges.Badge.MASTERY_ROGUE;
      case HUNTRESS:
        return Badges.Badge.MASTERY_HUNTRESS;
      case SORCERESS:
        return Badges.Badge.MASTERY_SORCERESS;
    }
    return null;
  }

  private static void initWarrior(Hero hero) {
    (hero.belongings.weapon = new WornShortsword()).identify();
    Dart darts = new Dart(8);
    darts.identify().collect();

    new Wine().collect();
    
    if (Badges.isUnlocked(Badges.Badge.TUTORIAL_WARRIOR)) {
      if (!Dungeon.isChallenged(Challenges.NO_ARMOR))
        hero.belongings.armor.affixSeal(new BrokenSeal());
      Dungeon.quickslot.setSlot(0, darts);
    } else {
      if (!Dungeon.isChallenged(Challenges.NO_ARMOR)) {
        BrokenSeal seal = new BrokenSeal();
        seal.collect();
        Dungeon.quickslot.setSlot(0, seal);
      }
      Dungeon.quickslot.setSlot(1, darts);
    }

    // new perk
    hero.heroPerk.add(HeroPerk.Perk.DRUNKARD);

    new PotionOfHealing().setKnown();
    
    // resists
    hero.addResistances(Damage.Element.FIRE, 1.1f);
    hero.addResistances(Damage.Element.LIGHT, .9f);
    hero.addResistances(Damage.Element.SHADOW, .8f, .9f);
  }

  private static void initMage(Hero hero) {
    MagesStaff staff;

    if (Badges.isUnlocked(Badges.Badge.TUTORIAL_MAGE)) {
      staff = new MagesStaff(new WandOfMagicMissile());
    } else {
      staff = new MagesStaff();
      new WandOfMagicMissile().identify().collect();
    }

    (hero.belongings.weapon = staff).identify();
    hero.belongings.weapon.activate(hero);

    Dungeon.quickslot.setSlot(0, staff);

    new ScrollOfUpgrade().setKnown();
    
    hero.addResistances(Damage.Element.FIRE, 1f, 1.2f);
    hero.addResistances(Damage.Element.POISON, .8f);
    hero.addResistances(Damage.Element.LIGHT, 1.1f);
  }

  private static void initRogue(Hero hero) {
    (hero.belongings.weapon = new Dagger()).identify();

    CloakOfShadows cloak = new CloakOfShadows();
    (hero.belongings.misc1 = cloak).identify();
    hero.belongings.misc1.activate(hero);

    Dart darts = new Dart(8);
    darts.identify().collect();

    Dungeon.quickslot.setSlot(0, cloak);
    Dungeon.quickslot.setSlot(1, darts);

    hero.heroPerk.add(HeroPerk.Perk.CRITICAL_STRIKE);
    hero.heroPerk.add(HeroPerk.Perk.KEEN);

    new ScrollOfMagicMapping().setKnown();
    
    hero.addResistances(Damage.Element.POISON, 1.2f);
    hero.addResistances(Damage.Element.ICE, .8f, .9f);
    hero.addResistances(Damage.Element.SHADOW, .9f, 1.1f);
  }

  private static void initHuntress(Hero hero) {

    (hero.belongings.weapon = new Knuckles()).identify();
    Boomerang boomerang = new Boomerang();
    boomerang.identify().collect();

    Dungeon.quickslot.setSlot(0, boomerang);

    hero.heroPerk.add(HeroPerk.Perk.NIGHT_VISION);
    hero.heroPerk.add(HeroPerk.Perk.SHOOTER);

    new PotionOfMindVision().setKnown();
    
    hero.addResistances(Damage.Element.POISON, 1.2f);
    hero.addResistances(Damage.Element.ICE, .9f);
    hero.addResistances(Damage.Element.ACID, .9f);
  }

  // extra classes
  private static void initSorceress(Hero hero) {
    // perks
    hero.heroPerk.add(HeroPerk.Perk.SHREWD);
    hero.heroPerk.add(HeroPerk.Perk.POSITIVE);

    (hero.belongings.weapon = new SorceressWand()).identify();

    ExtractionFlask flask = new ExtractionFlask();
    flask.identify().collect();

    // ranged weapon
    Dart darts = new Dart(6);
    darts.identify().collect();

    Dungeon.quickslot.setSlot(0, flask);
    Dungeon.quickslot.setSlot(1, darts);

    new PotionOfToxicGas().identify().collect();

    // resists and extra resists to poison
    for (int i = 0; i < Damage.Element.ELEMENT_COUNT; ++i) {
      hero.addResistances(1 << i, 1.25f, 1f);
    }
    hero.addResistances(Damage.Element.POISON, 2f);    
  }

  private static void initPerks(Hero hero) {
    if (hero.heroPerk.contain(HeroPerk.Perk.CRITICAL_STRIKE))
      hero.criticalChance_ += 5f / 100f;

  }

  public String title() {
    return Messages.get(HeroClass.class, title);
  }

  public String spritesheet() {

    switch (this) {
      case WARRIOR:
        return Assets.WARRIOR;
      case MAGE:
        return Assets.MAGE;
      case ROGUE:
        return Assets.ROGUE;
      case HUNTRESS:
        return Assets.HUNTRESS;
      case SORCERESS:
        return Assets.DPD_SORCERESS;
    }

    return null;
  }

  public String[] perks() {

    switch (this) {
      case WARRIOR:
        return new String[]{
                Messages.get(HeroClass.class, "warrior_perk1"),
                Messages.get(HeroClass.class, "warrior_perk2"),
                Messages.get(HeroClass.class, "warrior_perk3"),
                Messages.get(HeroClass.class, "warrior_perk4"),
                Messages.get(HeroClass.class, "warrior_perk5"),
        };
      case MAGE:
        return new String[]{
                Messages.get(HeroClass.class, "mage_perk1"),
                Messages.get(HeroClass.class, "mage_perk2"),
                Messages.get(HeroClass.class, "mage_perk3"),
                Messages.get(HeroClass.class, "mage_perk4"),
                Messages.get(HeroClass.class, "mage_perk5"),
        };
      case ROGUE:
        return new String[]{
                Messages.get(HeroClass.class, "rogue_perk1"),
                Messages.get(HeroClass.class, "rogue_perk2"),
                Messages.get(HeroClass.class, "rogue_perk3"),
                Messages.get(HeroClass.class, "rogue_perk4"),
                Messages.get(HeroClass.class, "rogue_perk5"),
                Messages.get(HeroClass.class, "rogue_perk6"),
        };
      case HUNTRESS:
        return new String[]{
                Messages.get(HeroClass.class, "huntress_perk1"),
                Messages.get(HeroClass.class, "huntress_perk2"),
                Messages.get(HeroClass.class, "huntress_perk3"),
                Messages.get(HeroClass.class, "huntress_perk4"),
                Messages.get(HeroClass.class, "huntress_perk5"),
        };
      case SORCERESS:
        return new String[]{
                Messages.get(HeroClass.class, "sorceress_perk1"),
                Messages.get(HeroClass.class, "sorceress_perk2"),
                Messages.get(HeroClass.class, "sorceress_perk3"),
                Messages.get(HeroClass.class, "sorceress_perk4"),
                Messages.get(HeroClass.class, "sorceress_perk5"),
                // Messages.get(HeroClass.class, "sorceress_perk6"),
        };
    }

    return null;
  }

  private static final String CLASS = "class";

  public void storeInBundle(Bundle bundle) {
    bundle.put(CLASS, toString());
  }

  public static HeroClass restoreInBundle(Bundle bundle) {
    String value = bundle.getString(CLASS);
    return value.length() > 0 ? valueOf(value) : ROGUE;
  }
}
