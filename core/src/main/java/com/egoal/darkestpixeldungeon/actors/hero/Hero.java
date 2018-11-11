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

import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Berserk;
import com.egoal.darkestpixeldungeon.actors.buffs.Bless;
import com.egoal.darkestpixeldungeon.actors.buffs.Dementage;
import com.egoal.darkestpixeldungeon.actors.buffs.Fury;
import com.egoal.darkestpixeldungeon.actors.buffs.Ignorant;
import com.egoal.darkestpixeldungeon.actors.buffs.Light;
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure;
import com.egoal.darkestpixeldungeon.actors.buffs.SharpVision;
import com.egoal.darkestpixeldungeon.actors.buffs.ViewMark;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.items.artifacts.RiemannianManifoldShield;
import com.egoal.darkestpixeldungeon.items.artifacts.UrnOfShadow;
import com.egoal.darkestpixeldungeon.items.artifacts.MaskOfMadness;
import com.egoal.darkestpixeldungeon.items.rings.RingOfCritical;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfEnchanting;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Bones;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.GamesInProgress;
import com.egoal.darkestpixeldungeon.Statistics;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Barkskin;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Combo;
import com.egoal.darkestpixeldungeon.actors.buffs.Drowsy;
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger;
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.actors.buffs.Regeneration;
import com.egoal.darkestpixeldungeon.actors.buffs.SnipersMark;
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.NPC;
import com.egoal.darkestpixeldungeon.effects.CheckedCell;
import com.egoal.darkestpixeldungeon.effects.Flare;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.Amulet;
import com.egoal.darkestpixeldungeon.items.Ankh;
import com.egoal.darkestpixeldungeon.items.Dewdrop;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Heap.Type;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.KindOfWeapon;
import com.egoal.darkestpixeldungeon.items.armor.Armor;
import com.egoal.darkestpixeldungeon.items.armor.glyphs.AntiMagic;
import com.egoal.darkestpixeldungeon.items.armor.glyphs.Flow;
import com.egoal.darkestpixeldungeon.items.armor.glyphs.Obfuscation;
import com.egoal.darkestpixeldungeon.items.armor.glyphs.Swiftness;
import com.egoal.darkestpixeldungeon.items.armor.glyphs.Viscosity;
import com.egoal.darkestpixeldungeon.items.artifacts.CapeOfThorns;
import com.egoal.darkestpixeldungeon.items.artifacts.DriedRose;
import com.egoal.darkestpixeldungeon.items.artifacts.EtherealChains;
import com.egoal.darkestpixeldungeon.items.artifacts.HornOfPlenty;
import com.egoal.darkestpixeldungeon.items.artifacts.TalismanOfForesight;
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.egoal.darkestpixeldungeon.items.keys.Key;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfMight;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfStrength;
import com.egoal.darkestpixeldungeon.items.rings.RingOfElements;
import com.egoal.darkestpixeldungeon.items.rings.RingOfEvasion;
import com.egoal.darkestpixeldungeon.items.rings.RingOfForce;
import com.egoal.darkestpixeldungeon.items.rings.RingOfFuror;
import com.egoal.darkestpixeldungeon.items.rings.RingOfHaste;
import com.egoal.darkestpixeldungeon.items.rings.RingOfMight;
import com.egoal.darkestpixeldungeon.items.rings.RingOfTenacity;
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.items.weapon.melee.BattleGloves;
import com.egoal.darkestpixeldungeon.items.weapon.melee.Flail;
import com.egoal.darkestpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.features.AlchemyPot;
import com.egoal.darkestpixeldungeon.levels.features.Chasm;
import com.egoal.darkestpixeldungeon.levels.features.EnchantingStation;
import com.egoal.darkestpixeldungeon.levels.features.Sign;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.plants.Earthroot;
import com.egoal.darkestpixeldungeon.plants.Sungrass;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.scenes.InterlevelScene;
import com.egoal.darkestpixeldungeon.scenes.SurfaceScene;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.HeroSprite;
import com.egoal.darkestpixeldungeon.ui.AttackIndicator;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton;
import com.egoal.darkestpixeldungeon.ui.StatusPane;
import com.egoal.darkestpixeldungeon.utils.BArray;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndMessage;
import com.egoal.darkestpixeldungeon.windows.WndResurrect;
import com.egoal.darkestpixeldungeon.windows.WndTradeItem;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Hero extends Char {

  {
    actPriority = 0; //acts at priority 0, baseline for the rest of behaviour.
  }

  public static final int MAX_LEVEL = 30;

  public static final int STARTING_STR = 10;

  private static final float TIME_TO_REST = 1f;
  private static final float TIME_TO_SEARCH = 2f;

  public HeroClass heroClass = HeroClass.ROGUE;
  public HeroSubClass subClass = HeroSubClass.NONE;
  public HeroPerk heroPerk = new HeroPerk(0);  // no perk


  private int attackSkill = 10;
  private int defenseSkill = 5;

  public boolean ready = false;
  private boolean damageInterrupt = true;
  public HeroAction curAction = null;
  public HeroAction lastAction = null;

  private Char enemy;

  // the followers will follow hero during level switch
  // the cache, no need to store in bundle
  private static final int MAX_FOLLOWERS = 3;
  private ArrayList<Char> followers_ = new ArrayList<Char>();

  private Item theKey;

  public boolean resting = false;

  public MissileWeapon rangedWeapon = null;
  public Belongings belongings;

  public int STR;
  public boolean weakened = false;

  public float awareness;
  public float criticalChance_;

  public int lvl = 1;
  public int exp = 0;

  private ArrayList<Mob> visibleEnemies;

  //This list is maintained so that some logic checks can be skipped
  // for enemies we know we aren't seeing normally, resultign in better 
  // performance
  public ArrayList<Mob> mindVisionEnemies = new ArrayList<>();

  public Hero() {
    super();
    name = Messages.get(this, "name");

    HP = HT = 22;
    STR = STARTING_STR;
    awareness = 0.1f;

    belongings = new Belongings(this);

    visibleEnemies = new ArrayList<Mob>();
  }

  public int STR() {
    int STR = this.STR;

    STR += RingOfMight.getBonus(this, RingOfMight.Might.class);

    return weakened ? STR - 2 : STR;
  }
  
  // view control
  @Override
  public int viewDistance(){
    if(buff(SharpVision.class)!=null) return seeDistance();
    
    int vd = Dungeon.level.viewDistance;
    if(Dungeon.level.feeling == Level.Feeling.DARK)
      vd /= 2;
    
    if(buff(Light.class)!=null){
      vd = Math.max(vd, Light.DISTANCE);
    }
    
    if(heroPerk.contain(HeroPerk.Perk.NIGHT_VISION))
      vd += 1;
    
    return GameMath.clamp(vd, 1, 9);
  }

  @Override
  public int seeDistance(){
    int sd = Dungeon.level.seeDistance;
    
    if(heroPerk.contain(HeroPerk.Perk.NIGHT_VISION))
      sd += 1;

    return GameMath.clamp(sd, 1, 9);
  }
  
  private static final String ATTACK = "attackSkill";
  private static final String DEFENSE = "defenseSkill";
  private static final String STRENGTH = "STR";
  private static final String LEVEL = "lvl";
  private static final String EXPERIENCE = "exp";
  private static final String CRITICAL = "critical";

  private static final String RESISTANCE_MAGICAL = "resistance_magical";
  private static final String RESISTANCE_NORMAL = "resistance_normal";

  @Override
  public void storeInBundle(Bundle bundle) {

    super.storeInBundle(bundle);

    heroClass.storeInBundle(bundle);
    subClass.storeInBundle(bundle);
    heroPerk.storeInBundle(bundle);

    bundle.put(ATTACK, attackSkill);
    bundle.put(DEFENSE, defenseSkill);

    bundle.put(STRENGTH, STR);

    bundle.put(LEVEL, lvl);
    bundle.put(EXPERIENCE, exp);

    bundle.put(CRITICAL, criticalChance_);

    // bundle.put(FOLLOWERS, followers_);
    bundle.put(RESISTANCE_MAGICAL, resistanceMagical);
    bundle.put(RESISTANCE_NORMAL, resistanceNormal);

    belongings.storeInBundle(bundle);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);

    heroClass = HeroClass.restoreInBundle(bundle);
    subClass = HeroSubClass.restoreInBundle(bundle);
    heroPerk = HeroPerk.restoreFromBundle(bundle);

    attackSkill = bundle.getInt(ATTACK);
    defenseSkill = bundle.getInt(DEFENSE);

    STR = bundle.getInt(STRENGTH);
    updateAwareness();

    lvl = bundle.getInt(LEVEL);
    exp = bundle.getInt(EXPERIENCE);

    criticalChance_ = bundle.getFloat(CRITICAL);

    resistanceMagical = bundle.getFloatArray(RESISTANCE_MAGICAL);
    resistanceNormal = bundle.getFloatArray(RESISTANCE_NORMAL);

    belongings.restoreFromBundle(bundle);
  }

  public void holdFollowers(Level level) {
    followers_.clear();

    // bring the ghost
    for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
      if (mob instanceof DriedRose.GhostHero) {
        level.mobs.remove(mob);
        followers_.add(mob);
        break;
      }
    }

    // bring the controlled
    for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
      if (mob.buff(Dementage.class) != null) {
        level.mobs.remove(mob);
        followers_.add(mob);
        if (followers_.size() == MAX_FOLLOWERS)
          break;
      }
    }
  }

  public void restoreFollowers(Level level, int heropos) {
    final int MAX_TRAILS = 1000;  // avoid hang...
    int cntTrails = 0;
    for (Char c : followers_) {
      if (c != null) {
        level.mobs.add((Mob) c);
        do {
          c.pos = heropos + PathFinder.NEIGHBOURS8[Random.Int(8)];
          if (++cntTrails >= MAX_TRAILS)
            break;
        } while (level.solid[c.pos] || level.findMob(c.pos) != null);
      }
    }
    followers_.clear();
  }

  public static void preview(GamesInProgress.Info info, Bundle bundle) {
    info.level = bundle.getInt(LEVEL);
  }

  public String className() {
    return subClass == null || subClass == HeroSubClass.NONE ? heroClass
            .title() : subClass.title();
  }

  public String givenName() {
    return name.equals(Messages.get(this, "name")) ? className() : name;
  }

  public void live() {
    Buff.affect(this, Regeneration.class);
    Buff.affect(this, Hunger.class);
    Buff.affect(this, Pressure.class);
  }

  public int tier() {
    return belongings.armor == null ? 0 : belongings.armor.tier;
  }

  public boolean shoot(Char enemy, MissileWeapon wep) {

    rangedWeapon = wep;
    boolean result = attack(enemy);
    Invisibility.dispel();
    rangedWeapon = null;

    return result;
  }

  @Override
  public int attackSkill(Char target) {
    float accuracy = 1;
    if (rangedWeapon != null && Dungeon.level.distance(pos, target.pos) == 1) {
      accuracy *= 0.5f;
    }

    // pressure
    Pressure p = buff(Pressure.class);
    switch (p.getLevel()) {
      case CONFIDENT:
        accuracy *= 1.2f;
        break;
      case NORMAL:
        break;
      case NERVOUS:
        accuracy *= .8f;
        break;
      case COLLAPSE:
        accuracy *= .2f;
        break;
    }

    KindOfWeapon wep = rangedWeapon != null ? rangedWeapon : belongings.weapon;
    if (wep != null) {
      return (int) (attackSkill * accuracy * wep.accuracyFactor(this));
    } else {
      return (int) (attackSkill * accuracy);
    }
  }

  @Override
  public int defenseSkill(Char enemy) {

    int bonus = RingOfEvasion.getBonus(this, RingOfEvasion.Evasion.class);

    float evasion = (float) Math.pow(1.125, bonus);
    if (paralysed > 0) {
      evasion /= 2;
    }

    if (heroClass == HeroClass.SORCERESS)
      evasion *= 0.75;

    // pressure
    Pressure p = buff(Pressure.class);
    switch (p.getLevel()) {
      case CONFIDENT:
        break;
      case NORMAL:
        break;
      case NERVOUS:
        evasion *= .8f;
        break;
      case COLLAPSE:
        evasion *= .0f;
        break;
    }

    int aEnc = belongings.armor != null ? belongings.armor.STRReq() - STR() :
            10 - STR();
    if (aEnc > 0) {
      // wear heavy armor
      return (int) (defenseSkill * evasion / Math.pow(1.5, aEnc));
    } else {

      bonus = 0;
      if (heroClass == HeroClass.ROGUE) bonus += -aEnc;

      if (belongings.armor != null && belongings.armor.hasGlyph(Swiftness
              .class))
        bonus += 5 + belongings.armor.level() * 1.5f;

      return Math.round((defenseSkill + bonus) * evasion);
    }
  }

  // defense
  @Override
  public Damage defendDamage(Damage dmg) {
    if (dmg.type == Damage.Type.MENTAL) {
      // do nothing, todo: mental defense
    } else {
      if (belongings.weapon != null)
        dmg = belongings.weapon.defendDamage(dmg);

      int dr = 0;

      if (belongings.armor != null) {
        dr = Random.NormalIntRange(belongings.armor.DRMin(), belongings.armor
                .DRMax());
        if (STR() < belongings.armor.STRReq()) {
          // ware heavy armor
          dr -= 2 * (belongings.armor.STRReq() - STR());
          dr = Math.max(dr, 0);
        }
      }

      // barkskin buff
      Barkskin bark = buff(Barkskin.class);
      if (bark != null) dr += Random.NormalIntRange(0, bark.level());

      dmg.value -= dr;
    }

    return dmg;
  }

  @Override
  public Damage giveDamage(Char enemy) {
    Damage dmg = new Damage(0, this, enemy);

    // ring of force
    int bonus = RingOfForce.getBonus(this, RingOfForce.Force.class);

    KindOfWeapon wep = rangedWeapon != null ? rangedWeapon : belongings.weapon;
    if (wep != null) {
      dmg = wep.giveDamage(this, enemy);
      // battle gloves
      if (wep instanceof BattleGloves && bonus > 0) {
        dmg.value += RingOfForce.damageRoll(this);
      } else
        dmg.value += bonus;
    } else {
      // bare hand
      if (bonus != 0) {
        dmg.value = RingOfForce.damageRoll(this);
      } else {
        dmg.value = Random.NormalIntRange(1, Math.max(STR() - 8, 1));
      }
    }

    // critical
    int bonusCritical = RingOfCritical.getBonus(this, RingOfCritical.Critical
            .class);
    float theCriticalChance = criticalChance_ * (float) Math.pow(1.15,
            bonusCritical);

    if (!dmg.isFeatured(Damage.Feature.CRITCIAL) && Random.Float() <
            theCriticalChance) {
      dmg.value *= 1.5f;
      dmg.addFeature(Damage.Feature.CRITCIAL);
    }

    // pressure
    Pressure p = buff(Pressure.class);
    switch (p.getLevel()) {
      case CONFIDENT:
        if (!dmg.isFeatured(Damage.Feature.CRITCIAL) && Random.Int(10) == 0) {
          // critical, 
          dmg.value *= 1.5;
          dmg.addFeature(Damage.Feature.CRITCIAL);
        } else
          dmg.value *= 1.1;
        break;
      case NORMAL:
        break;
      case NERVOUS:
        break;
      case COLLAPSE:
        dmg.value *= .5f;
        break;
    }

    MaskOfMadness.Madness madness = buff(MaskOfMadness.Madness.class);
    if (madness != null)
      dmg = madness.procOutcomingDamage(dmg);

    if (dmg.value < 0) dmg.value = 0;

    // berserker perk
    if (subClass == HeroSubClass.BERSERKER)
      dmg.value = Buff.affect(this, Berserk.class).damageFactor(dmg.value);

    if (buff(Fury.class) != null)
      dmg.value *= 1.5f;

    return dmg;
  }

  @Override
  public float speed() {

    float speed = super.speed();

    int hasteLevel = RingOfHaste.getBonus(this, RingOfHaste.Haste.class);

    if (hasteLevel != 0)
      speed *= Math.pow(1.2, hasteLevel);

    Armor armor = belongings.armor;

    if (armor != null) {

      if (armor.hasGlyph(Swiftness.class)) {
        speed *= (1.1f + 0.01f * belongings.armor.level());
      } else if (armor.hasGlyph(Flow.class) && Level.water[pos]) {
        speed *= (1.5f + 0.05f * belongings.armor.level());
      }
    }

    int aEnc = armor != null ? armor.STRReq() - STR() : 0;
    if (aEnc > 0) {

      return (float) (speed / Math.pow(1.2, aEnc));

    } else {

      return ((HeroSprite) sprite).sprint(subClass == HeroSubClass.FREERUNNER
              && !isStarving()) ?
              invisible > 0 ?
                      2f * speed :
                      1.5f * speed :
              speed;

    }
  }

  public boolean canSurpriseAttack() {
    if (belongings.weapon == null || !(belongings.weapon instanceof Weapon))
      return true;

    if (STR() < ((Weapon) belongings.weapon).STRReq())
      return false;

    if (belongings.weapon instanceof Flail && rangedWeapon == null)
      return false;

    return true;
  }

  public boolean canAttack(Char enemy) {
    if (enemy == null || pos == enemy.pos)
      return false;

    //can always attack adjacent enemies
    if (Dungeon.level.adjacent(pos, enemy.pos))
      return true;

    //note: check weapon range
    KindOfWeapon wep = Dungeon.hero.belongings.weapon;

    if (wep != null && Dungeon.level.distance(pos, enemy.pos) <= wep
            .reachFactor(this)) {

      boolean[] passable = BArray.not(Level.solid, null);
      for (Mob m : Dungeon.level.mobs)
        passable[m.pos] = false;

      PathFinder.buildDistanceMap(enemy.pos, passable, wep.reachFactor(this));

      return PathFinder.distance[pos] <= wep.reachFactor(this);

    } else {
      return false;
    }
  }

  public float attackDelay() {
    KindOfWeapon wep = rangedWeapon != null ? rangedWeapon : belongings.weapon;
    if (wep != null) {

      return wep.speedFactor(this);

    } else {
      //Normally putting furor speed on unarmed attacks would be unnecessary
      //But there's going to be that one guy who gets a furor+force ring combo
      //This is for that one guy, you shall get your fists of fury!
      int bonus = RingOfFuror.getBonus(this, RingOfFuror.Furor.class);
      return (float) (0.25 + (1 - 0.25) * Math.pow(0.8, bonus));
    }
  }

  @Override
  public void spend(float time) {
    TimekeepersHourglass.timeFreeze buff = buff(TimekeepersHourglass
            .timeFreeze.class);
    if (!(buff != null && buff.processTime(time)))
      super.spend(time);
  }

  public void spendAndNext(float time) {
    busy();
    spend(time);
    next();
  }

  @Override
  public boolean act() {

    super.act();

    if (paralysed > 0) {

      curAction = null;

      spendAndNext(TICK);
      return false;
    }

    checkVisibleMobs();


    if (curAction == null) {

      if (resting) {
        spend(TIME_TO_REST);
        next();
        return false;
      }

      ready();
      return false;

    } else {

      resting = false;

      ready = false;

      if (curAction instanceof HeroAction.Move) {

        return actMove((HeroAction.Move) curAction);

      } else if (curAction instanceof HeroAction.Interact) {

        return actInteract((HeroAction.Interact) curAction);

      } else if (curAction instanceof HeroAction.Buy) {

        return actBuy((HeroAction.Buy) curAction);

      } else if (curAction instanceof HeroAction.PickUp) {

        return actPickUp((HeroAction.PickUp) curAction);

      } else if (curAction instanceof HeroAction.OpenChest) {

        return actOpenChest((HeroAction.OpenChest) curAction);

      } else if (curAction instanceof HeroAction.Unlock) {

        return actUnlock((HeroAction.Unlock) curAction);

      } else if (curAction instanceof HeroAction.Descend) {

        return actDescend((HeroAction.Descend) curAction);

      } else if (curAction instanceof HeroAction.Ascend) {

        return actAscend((HeroAction.Ascend) curAction);

      } else if (curAction instanceof HeroAction.Attack) {

        return actAttack((HeroAction.Attack) curAction);

      } else if (curAction instanceof HeroAction.Cook) {

        return actCook((HeroAction.Cook) curAction);

      } else if (curAction instanceof HeroAction.Enchant) {
        return actEnchant((HeroAction.Enchant) curAction);
      }
    }

    return false;
  }

  public void busy() {
    ready = false;
  }

  private void ready() {
    if (sprite.looping()) sprite.idle();
    curAction = null;
    damageInterrupt = true;
    ready = true;

    AttackIndicator.updateState();

    GameScene.ready();
  }

  public void interrupt() {
    if (isAlive() && curAction != null && curAction instanceof HeroAction
            .Move && curAction.dst != pos) {
      lastAction = curAction;
    }
    curAction = null;
  }

  public void resume() {
    curAction = lastAction;
    lastAction = null;
    damageInterrupt = false;
    next();
  }

  private boolean actMove(HeroAction.Move action) {

    if (getCloser(action.dst)) {

      return true;

    } else {
      if (Dungeon.level.map[pos] == Terrain.SIGN) {
        Sign.read(pos);
      }
      ready();

      return false;
    }
  }

  private boolean actInteract(HeroAction.Interact action) {

    NPC npc = action.npc;

    if (Dungeon.level.adjacent(pos, npc.pos)) {

      ready();
      sprite.turnTo(pos, npc.pos);
      return npc.interact();

    } else {

      if (Level.fieldOfView[npc.pos] && getCloser(npc.pos)) {

        return true;

      } else {
        ready();
        return false;
      }

    }
  }

  private boolean actBuy(HeroAction.Buy action) {
    int dst = action.dst;
    if (pos == dst || Dungeon.level.adjacent(pos, dst)) {

      ready();

      Heap heap = Dungeon.level.heaps.get(dst);
      if (heap != null && heap.type == Type.FOR_SALE && heap.size() == 1) {
        GameScene.show(new WndTradeItem(heap, true));
      }

      return false;

    } else if (getCloser(dst)) {

      return true;

    } else {
      ready();
      return false;
    }
  }

  private boolean actCook(HeroAction.Cook action) {
    int dst = action.dst;
    if (Dungeon.visible[dst]) {

      ready();
      AlchemyPot.operate(this, dst);
      return false;

    } else if (getCloser(dst)) {

      return true;

    } else {
      ready();
      return false;
    }
  }

  private boolean actEnchant(HeroAction.Enchant action) {
    int dst = action.dst;
    if (Dungeon.level.adjacent(dst, pos)) {
      ready();
      EnchantingStation.operate(this);

      return false;
    } else if (getCloser(dst)) {
      return true;
    } else {
      ready();
      return false;
    }
  }

  private boolean actPickUp(HeroAction.PickUp action) {
    int dst = action.dst;
    if (pos == dst) {

      Heap heap = Dungeon.level.heaps.get(pos);
      if (heap != null) {
        Item item = heap.peek();
        if (item.doPickUp(this)) {
          heap.pickUp();

          if (item instanceof Dewdrop
                  || item instanceof TimekeepersHourglass.sandBag
                  || item instanceof DriedRose.Petal
                  || item instanceof Key) {
            //Do Nothing
          } else {

            boolean important =
                    ((item instanceof ScrollOfUpgrade) && ((Scroll) item)
                            .isKnown()) ||
                            ((item instanceof PotionOfStrength || item
                                    instanceof PotionOfMight) && ((Potion)
                                    item).isKnown());
            if (important) {
              GLog.p(Messages.get(this, "you_now_have", item.name()));
            } else {
              GLog.i(Messages.get(this, "you_now_have", item.name()));
            }
          }

          if (!heap.isEmpty()) {
            GLog.i(Messages.get(this, "something_else"));
          }
          curAction = null;
        } else {
          heap.sprite.drop();
          ready();
        }
      } else {
        ready();
      }

      return false;

    } else if (getCloser(dst)) {

      return true;

    } else {
      ready();
      return false;
    }
  }

  private boolean actOpenChest(HeroAction.OpenChest action) {
    int dst = action.dst;
    if (Dungeon.level.adjacent(pos, dst) || pos == dst) {

      Heap heap = Dungeon.level.heaps.get(dst);
      if (heap != null && (heap.type != Type.HEAP && heap.type != Type
              .FOR_SALE)) {

        if ((heap.type == Type.LOCKED_CHEST || heap.type == Type.CRYSTAL_CHEST)
                && belongings.specialKeys[Dungeon.depth] < 1) {

          GLog.w(Messages.get(this, "locked_chest"));
          ready();
          return false;

        }

        switch (heap.type) {
          case TOMB:
            Sample.INSTANCE.play(Assets.SND_TOMB);
            Camera.main.shake(1, 0.5f);
            break;
          case SKELETON:
          case REMAINS:
            break;
          default:
            Sample.INSTANCE.play(Assets.SND_UNLOCK);
        }

        spend(Key.TIME_TO_UNLOCK);
        sprite.operate(dst);

      } else {
        ready();
      }

      return false;

    } else if (getCloser(dst)) {

      return true;

    } else {
      ready();
      return false;
    }
  }

  private boolean actUnlock(HeroAction.Unlock action) {
    int doorCell = action.dst;
    if (Dungeon.level.adjacent(pos, doorCell)) {

      boolean hasKey = false;
      int door = Dungeon.level.map[doorCell];

      if (door == Terrain.LOCKED_DOOR
              && belongings.ironKeys[Dungeon.depth] > 0) {

        hasKey = true;

      } else if (door == Terrain.LOCKED_EXIT
              && belongings.specialKeys[Dungeon.depth] > 0) {

        hasKey = true;

      }

      if (hasKey) {

        spend(Key.TIME_TO_UNLOCK);
        sprite.operate(doorCell);

        Sample.INSTANCE.play(Assets.SND_UNLOCK);

      } else {
        GLog.w(Messages.get(this, "locked_door"));
        ready();
      }

      return false;

    } else if (getCloser(doorCell)) {

      return true;

    } else {
      ready();
      return false;
    }
  }

  private boolean actDescend(HeroAction.Descend action) {
    int stairs = action.dst;
    if (pos == stairs && pos == Dungeon.level.exit) {

      if (Dungeon.depth == 0) {
        // leave village
      }

      curAction = null;

      Buff buff = buff(TimekeepersHourglass.timeFreeze.class);
      if (buff != null) buff.detach();

      InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
      Game.switchScene(InterlevelScene.class);

      return false;

    } else if (getCloser(stairs)) {

      return true;

    } else {
      ready();
      return false;
    }
  }

  private boolean actAscend(HeroAction.Ascend action) {
    int stairs = action.dst;
    if (pos == stairs && pos == Dungeon.level.entrance) {

      if (Dungeon.depth == 0) {
        if (belongings.getItem(Amulet.class) == null) {
          GameScene.show(new WndMessage(Messages.get(this, "leave_village")));
          ready();
        } else {
          // end game
          Dungeon.win(Amulet.class);
          Dungeon.deleteGame(Dungeon.hero.heroClass, true);
          Game.switchScene(SurfaceScene.class);
        }

      } else if (Dungeon.depth == 1 && belongings.getItem(Amulet.class) ==
              null) {
        GameScene.show(new WndMessage(Messages.get(this, "leave")));
        ready();
      } else {

        curAction = null;

        Hunger hunger = buff(Hunger.class);
        if (hunger != null && !hunger.isStarving()) {
          hunger.reduceHunger(-Hunger.STARVING / 10);
        }

        Buff buff = buff(TimekeepersHourglass.timeFreeze.class);
        if (buff != null) buff.detach();

        InterlevelScene.mode = InterlevelScene.Mode.ASCEND;
        Game.switchScene(InterlevelScene.class);
      }

      return false;

    } else if (getCloser(stairs)) {

      return true;

    } else {
      ready();
      return false;
    }
  }

  private boolean actAttack(HeroAction.Attack action) {

    enemy = action.target;

    if (enemy.isAlive() && canAttack(enemy) && !isCharmedBy(enemy)) {
      // can attack, show animation
      Invisibility.dispel();
      spend(attackDelay());
      sprite.attack(enemy.pos);

      return false;

    } else {

      if (Level.fieldOfView[enemy.pos] && getCloser(enemy.pos)) {

        return true;

      } else {
        ready();
        return false;
      }

    }
  }

  public Char enemy() {
    return enemy;
  }

  public void rest(boolean fullRest) {
    spendAndNext(TIME_TO_REST);
    if (!fullRest) {
      sprite.showStatus(CharSprite.DEFAULT, Messages.get(this, "wait"));
    }
    resting = fullRest;
  }

  @Override
  public Damage attackProc(Damage dmg) {
    KindOfWeapon wep = rangedWeapon != null ? rangedWeapon : belongings.weapon;
    if (wep != null)
      dmg = wep.proc(dmg);

    // critical damage recover pressure
    if (dmg.isFeatured(Damage.Feature.CRITCIAL) && dmg.value > 0 && Random
            .Int(20) == 0)
      recoverSanity(Math.min(Random.Int(dmg.value / 6) + 1, 10));

    // sniper perk
    if (subClass == HeroSubClass.SNIPER && rangedWeapon != null) {
      Char target = (Char) dmg.to;
      Buff.prolong(this, SnipersMark.class, attackDelay() * 1.1f).object =
              target.id();
      Buff.prolong(target, ViewMark.class, attackDelay() * 1.1f).observer =
              this.id();
    }

    return dmg;
  }

  @Override
  public Damage defenseProc(Damage dmg) {
    Earthroot.Armor ea = buff(Earthroot.Armor.class);
    if (ea != null)
      dmg.value = ea.absorb(dmg.value);

    Sungrass.Health health = buff(Sungrass.Health.class);
    if (health != null)
      health.absorb(dmg.value);

    if (belongings.armor != null)
      dmg = belongings.armor.proc(dmg);

    return dmg;
  }

  @Override
  public int takeDamage(Damage dmg) {
    // freeze self
    if (buff(TimekeepersHourglass.timeStasis.class) != null)
      return 0;

    // interrupt action or resting
    if (!(dmg.from instanceof Hunger || dmg.from instanceof Viscosity
            .DeferedDamage) && damageInterrupt) {
      interrupt();
      resting = false;
    }

    // interrupt sleep
    if (this.buff(Drowsy.class) != null) {
      Buff.detach(this, Drowsy.class);
      GLog.w(Messages.get(this, "pain_resist"));
    }

    CapeOfThorns.Thorns thorns = buff(CapeOfThorns.Thorns.class);
    if (thorns != null)
      dmg = thorns.proc(dmg);

    MaskOfMadness.Madness madness = buff(MaskOfMadness.Madness.class);
    if (madness != null)
      dmg = madness.procIncomingDamage(dmg);

    // 韧性之戒
    int tenacity = RingOfTenacity.getBonus(this, RingOfTenacity.Tenacity.class);
    if (tenacity != 0)
      dmg.value = (int) Math.ceil((float) dmg.value * Math.pow(0.85, tenacity
              * ((float) (HT - HP) / HT)));

    // berserk
    // note: resistance move to resistDamage

    if (dmg.type == Damage.Type.MENTAL)
      return takeMentalDamage(dmg);
    else {
      // not mental damage
      int dmgtoken = super.takeDamage(dmg);

      if (!isAlive()) return dmgtoken;

      // extra deal with mental damage
      if (dmgtoken > 0) {
        Damage dmgMental = new Damage(0, dmg.from, dmg.to).type(Damage.Type
                .MENTAL);

        if (dmg.from instanceof Char && !Dungeon.visible[((Char) dmg.from)
                .pos]) {
          // when hit from nowhere
          dmgMental.value += Random.Int(1, 4);
        }
        if (dmg.isFeatured(Damage.Feature.CRITCIAL)) {
          // when take critical damage, up pressure
          if (dmg.type != Damage.Type.MENTAL)
            dmgMental.value += Random.Int(1, 6);
        }
        if (!heroPerk.contain(HeroPerk.Perk.FEARLESS) && HP < HT / 4 &&
                dmg.from instanceof Mob && dmg.value > 0) {
          // when health is low	
          dmgMental.value += Random.Int(1, 4);
        }

        // not greater than 10
        dmgMental.value = Math.min(10, dmgMental.value);
        
        takeMentalDamage(dmgMental);
      }

      return dmgtoken;
    }
  }

  @Override
  public Damage resistDamage(Damage dmg) {
    // note: immunities is processed in super
    {
      RiemannianManifoldShield.Recharge r = buff(RiemannianManifoldShield
              .Recharge.class);
      if (r != null && r.isCursed())
        return dmg;
    }

    // resistance
    if (dmg.type == Damage.Type.MAGICAL) {
      //! resist magical, resist magical only
      if (belongings.armor != null && belongings.armor.hasGlyph(AntiMagic
              .class)) {
        dmg.value *= .75f;
      }
    }

    // resistance of ring, 
    RingOfElements.Resistance r = buff(RingOfElements.Resistance.class);
    if (r != null)
      dmg = r.resist(dmg);

    return super.resistDamage(dmg);
  }

  @Override
  public HashSet<Class<?>> immunizedBuffs() {
    HashSet<Class<?>> hs = new HashSet<>();
    for (Buff buff : buffs()) {
      hs.addAll(buff.immunities);
    }

    return hs;
  }

  public void recoverSanity(int value) {
    int rv = (int) buff(Pressure.class).downPressure(value);

    if (rv > 0)
      sprite.showStatus(0xFFFFFF, Integer.toString(rv));
  }

  protected int takeMentalDamage(Damage dmg) {
    if (dmg.value <= 0) return 0;

    if (dmg.isFeatured(Damage.Feature.ACCURATE)) {
    } else {
      // warrior
      if (heroClass == HeroClass.WARRIOR)
        dmg.value += Random.Int(0, 1);

      if (heroPerk.contain(HeroPerk.Perk.POSITIVE) && Random.Float() < .15f) {
        dmg.value = 0;
        sprite.showStatus(CharSprite.DEFAULT, Messages.get(this,
                "mental_resist"));
      } else if (subClass == HeroSubClass.STARGAZER && Random.Float() < .1f) {
        dmg.value = 0;
        sprite.showStatus(CharSprite.DEFAULT, Messages.get(this,
                "mental_resist"));
      }
    }

    // keep in mind that SAN is pressure, it increases
    int rv = (int) buff(Pressure.class).upPressure(dmg.value);

    // final int NORMAL	=	0x361936;
    final int WARNING = 0x0A0A0A;

    if (rv > 0 && buff(Ignorant.class) == null) {
      sprite.showStatus(WARNING, Integer.toString(rv));
    }

    return rv;
  }

  private void checkVisibleMobs() {
    ArrayList<Mob> visible = new ArrayList<>();

    boolean newMob = false;

    Mob target = null;
    for (Mob m : Dungeon.level.mobs) {
      if (Level.fieldOfView[m.pos] && m.hostile) {
        visible.add(m);
        if (!visibleEnemies.contains(m)) {
          newMob = true;
        }

        if (!mindVisionEnemies.contains(m) && QuickSlotButton.autoAim(m) !=
                -1) {
          if (target == null) {
            target = m;
          } else if (distance(target) > distance(m)) {
            target = m;
          }
        }
      }
    }

    if (target != null && (QuickSlotButton.lastTarget == null ||
            !QuickSlotButton.lastTarget.isAlive() ||
            !Dungeon.visible[QuickSlotButton.lastTarget.pos])) {
      QuickSlotButton.target(target);
    }

    if (newMob) {
      interrupt();
      resting = false;
    }

    visibleEnemies = visible;
  }

  public int visibleEnemies() {
    return visibleEnemies.size();
  }

  public Mob visibleEnemy(int index) {
    return visibleEnemies.get(index % visibleEnemies.size());
  }

  private boolean getCloser(final int target) {

    if (target == pos)
      return false;

    if (rooted) {
      Camera.main.shake(1, 1f);
      return false;
    }

    int step = -1;

    if (Dungeon.level.adjacent(pos, target)) {

      path = null;

      if (Actor.findChar(target) == null) {
        if (Level.pit[target] && !flying && !Level.solid[target]) {
          if (!Chasm.jumpConfirmed) {
            Chasm.heroJump(this);
            interrupt();
          } else {
            Chasm.heroFall(target);
          }
          return false;
        }
        if (Level.passable[target] || Level.avoid[target]) {
          step = target;
        }
      }

    } else {

      boolean newPath = false;
      if (path == null || path.isEmpty() || !Dungeon.level.adjacent(pos, path
              .getFirst()))
        newPath = true;
      else if (path.getLast() != target)
        newPath = true;
      else {
        //checks 2 cells ahead for validity.
        //Note that this is shorter than for mobs, so that mobs usually yield
        // to the hero
        for (int i = 0; i < Math.min(path.size(), 2); i++) {
          int cell = path.get(i);
          if (!Level.passable[cell] || ((i != path.size() - 1) && Dungeon
                  .visible[cell] && Actor.findChar(cell) != null)) {
            newPath = true;
            break;
          }
        }
      }

      if (newPath) {

        int len = Dungeon.level.length();
        boolean[] p = Level.passable;
        boolean[] v = Dungeon.level.visited;
        boolean[] m = Dungeon.level.mapped;
        boolean[] passable = new boolean[len];
        for (int i = 0; i < len; i++) {
          passable[i] = p[i] && (v[i] || m[i]);
        }

        path = Dungeon.findPath(this, pos, target, passable, Level.fieldOfView);
      }

      if (path == null) return false;
      step = path.removeFirst();

    }

    if (step != -1) {

      sprite.move(pos, step);
      move(step);
      spend(1 / speed());

      return true;

    } else {

      return false;

    }

  }

  public boolean handle(int cell) {

    if (cell == -1) {
      return false;
    }

    Char ch;
    Heap heap;

    if (Dungeon.level.map[cell] == Terrain.ALCHEMY && cell != pos) {

      curAction = new HeroAction.Cook(cell);

    } else if (Dungeon.level.map[cell] == Terrain.ENCHANTING_STATION && cell
            != pos) {
      curAction = new HeroAction.Enchant(cell);
    } else if (Level.fieldOfView[cell] && (ch = Actor.findChar(cell))
            instanceof Mob) {

      if (ch instanceof NPC) {
        curAction = new HeroAction.Interact((NPC) ch);
      } else {
        curAction = new HeroAction.Attack(ch);
      }

    } else if ((heap = Dungeon.level.heaps.get(cell)) != null
            //moving to an item doesn't auto-pickup when enemies are near...
            && (visibleEnemies.size() == 0 || cell == pos ||
            //...but only for standard heaps, chests and similar open as normal.
            (heap.type != Type.HEAP && heap.type != Type.FOR_SALE))) {

      switch (heap.type) {
        case HEAP:
          curAction = new HeroAction.PickUp(cell);
          break;
        case FOR_SALE:
          curAction = heap.size() == 1 && heap.peek().price() > 0 ?
                  new HeroAction.Buy(cell) :
                  new HeroAction.PickUp(cell);
          break;
        default:
          curAction = new HeroAction.OpenChest(cell);
      }

    } else if (Dungeon.level.map[cell] == Terrain.LOCKED_DOOR || Dungeon
            .level.map[cell] == Terrain.LOCKED_EXIT) {

      curAction = new HeroAction.Unlock(cell);

    } else if (cell == Dungeon.level.exit && Dungeon.depth < 26) {

      curAction = new HeroAction.Descend(cell);

    } else if (cell == Dungeon.level.entrance) {

      curAction = new HeroAction.Ascend(cell);

    } else {

      curAction = new HeroAction.Move(cell);
      lastAction = null;

    }

    return true;
  }

  public void earnExp(int exp) {

    this.exp += exp;
    float percent = exp / (float) maxExp();

    EtherealChains.chainsRecharge chains = buff(EtherealChains.chainsRecharge
            .class);
    if (chains != null) chains.gainExp(percent);

    HornOfPlenty.hornRecharge horn = buff(HornOfPlenty.hornRecharge.class);
    if (horn != null) horn.gainCharge(percent);

    if (subClass == HeroSubClass.BERSERKER)
      Buff.affect(this, Berserk.class).recover(percent);

    boolean levelUp = false;
    while (this.exp >= maxExp()) {
      this.exp -= maxExp();
      if (lvl < MAX_LEVEL) {
        levelUp = true;

        updateLevelStates();

      } else {
        Buff.prolong(this, Bless.class, 30f);
        this.exp = 0;

        GLog.p(Messages.get(this, "level_cap"));
        Sample.INSTANCE.play(Assets.SND_LEVELUP);
      }

      if (lvl < 10) {
        updateAwareness();
      }
    }

    if (levelUp) {

      GLog.p(Messages.get(this, "new_level"), lvl);
      sprite.showStatus(CharSprite.POSITIVE, Messages.get(Hero.class,
              "level_up"));
      Sample.INSTANCE.play(Assets.SND_LEVELUP);

      Badges.validateLevelReached();
    }
  }

  // called when level up
  private void updateLevelStates() {
    lvl++;
    int dHT = 0;
    if (lvl <= 5)
      dHT = 6;
    else if (lvl <= 20)
      dHT = 5;
    else if (lvl <= 30)
      dHT = 4;
    else
      dHT = 1;
    HT += dHT;
    HP += dHT;

    attackSkill++;
    defenseSkill++;

    criticalChance_ += 0.4f / 100f;

    // recover sanity
    recoverSanity(Math.min(Random.IntRange(1, lvl/2), (int) (buff(Pressure
            .class).pressure * 0.3f)));
  }

  public int maxExp() {
    return 5 + lvl * 5;
  }

  void updateAwareness() {
    double w = heroPerk.contain(HeroPerk.Perk.KEEN) ? 0.85 : 0.90;

    awareness = (float) (1 - Math.pow(w, (1 + Math.min(lvl, 9)) * 0.5));
  }

  public boolean isStarving() {
    return buff(Hunger.class) != null && ((Hunger) buff(Hunger.class))
            .isStarving();
  }

  @Override
  public void add(Buff buff) {

    if (buff(TimekeepersHourglass.timeStasis.class) != null)
      return;

    //* check buff immunities
    // immunities
    for (Buff b : buffs()) {
      for (Class<?> im : b.immunities) {
        if (buff.getClass() == im)
          return;
      }
    }

    super.add(buff);

    if (sprite != null) {
      String msg = buff.heroMessage();
      if (msg != null) {
        GLog.w(msg);
      }

      if (buff instanceof Paralysis || buff instanceof Vertigo) {
        interrupt();
      }

    }

    BuffIndicator.refreshHero();
  }

  @Override
  public void remove(Buff buff) {
    super.remove(buff);

    BuffIndicator.refreshHero();
  }

  @Override
  public int stealth() {
    int stealth = super.stealth();

    stealth += RingOfEvasion.getBonus(this, RingOfEvasion.Evasion.class);

    if (belongings.armor != null && belongings.armor.hasGlyph(Obfuscation
            .class)) {
      stealth += belongings.armor.level();
    }
    return stealth;
  }

  @Override
  public void die(Object cause) {

    curAction = null;

    Ankh ankh = null;

    //look for ankhs in player inventory, prioritize ones which are blessed.
    for (Item item : belongings) {
      if (item instanceof Ankh) {
        if (ankh == null || ((Ankh) item).isBlessed()) {
          ankh = (Ankh) item;
        }
      }
    }

    if (ankh != null && ankh.isBlessed()) {
      this.HP = HT / 4;

      //ensures that you'll get to act first in almost any case, to prevent 
      // reviving and then instantly dieing again.
      Buff.detach(this, Paralysis.class);
      spend(-cooldown());

      new Flare(8, 32).color(0xFFFF66, true).show(sprite, 2f);
      CellEmitter.get(this.pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3);

      ankh.detach(belongings.backpack);

      Sample.INSTANCE.play(Assets.SND_TELEPORT);
      GLog.w(Messages.get(this, "revive"));
      Statistics.ankhsUsed++;

      DriedRose.GhostHero gh = DriedRose.GhostHero.instance();
      if(gh!=null)
        gh.sayAnhk();
      
      return;
    }

    Actor.fixTime();
    super.die(cause);

    if (ankh == null) {

      reallyDie(cause);

    } else {

      Dungeon.deleteGame(Dungeon.hero.heroClass, false);
      GameScene.show(new WndResurrect(ankh, cause));

    }
  }

  public static void reallyDie(Object cause) {

    int length = Dungeon.level.length();
    int[] map = Dungeon.level.map;
    boolean[] visited = Dungeon.level.visited;
    boolean[] discoverable = Level.discoverable;

    for (int i = 0; i < length; i++) {

      int terr = map[i];

      if (discoverable[i]) {

        visited[i] = true;
        if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {
          Dungeon.level.discover(i);
        }
      }
    }

    Bones.leave();

    Dungeon.observe();

    Dungeon.hero.belongings.identify();

    int pos = Dungeon.hero.pos;

    ArrayList<Integer> passable = new ArrayList<Integer>();
    for (Integer ofs : PathFinder.NEIGHBOURS8) {
      int cell = pos + ofs;
      if ((Level.passable[cell] || Level.avoid[cell]) && Dungeon.level.heaps
              .get(cell) == null) {
        passable.add(cell);
      }
    }
    Collections.shuffle(passable);

    ArrayList<Item> items = new ArrayList<Item>(Dungeon.hero.belongings
            .backpack.items);
    for (Integer cell : passable) {
      if (items.isEmpty()) {
        break;
      }

      Item item = Random.element(items);
      Dungeon.level.drop(item, cell).sprite.drop(pos);
      items.remove(item);
    }

    GameScene.gameOver();

    if (cause instanceof Hero.Doom) {
      ((Hero.Doom) cause).onDeath();
    }

    Dungeon.deleteGame(Dungeon.hero.heroClass, true);
  }

  @Override
  public boolean isAlive() {
    if (subClass == HeroSubClass.BERSERKER) {
      Berserk berserk = buff(Berserk.class);
      if (berserk != null && berserk.berserking()) {
        return true;
      }
    }
    return super.isAlive();
  }

  @Override
  public void move(int step) {
    super.move(step);

    if (!flying) {

      if (Level.water[pos]) {
        Sample.INSTANCE.play(Assets.SND_WATER, 1, 1, Random.Float(0.8f, 1.25f));
      } else {
        Sample.INSTANCE.play(Assets.SND_STEP);
      }
      Dungeon.level.press(pos, this);
    }
  }

  @Override
  public void onMotionComplete() {
    Dungeon.observe();
    search(false);
  }

  @Override
  public void onAttackComplete() {

    AttackIndicator.target(enemy);

    boolean hit = attack(enemy);

    // warrior sub-class perk
    if (subClass == HeroSubClass.GLADIATOR) {
      if (hit) {
        Buff.affect(this, Combo.class).hit();
      } else {
        Combo combo = buff(Combo.class);
        if (combo != null) combo.miss();
      }
    }

    curAction = null;

    super.onAttackComplete();
  }

  public void onMobDied(Mob mob) {
    // warlock 
    UrnOfShadow uos = belongings.getItem(UrnOfShadow.class);
    if (uos != null) {
      uos.collectSoul(mob);
    }
    
    if(mob.properties().contains(Property.BOSS)){
      // slay a boss
      DriedRose.GhostHero gh = DriedRose.GhostHero.instance();
      if(gh!=null)
        gh.sayBossBeaten();
    }
  }

  // called when killed a char by attack
  public void onKillChar(Char ch) {
    // may recover pressure
    if (ch.properties().contains(Property.BOSS))
      recoverSanity(Random.IntRange(6, 12));
    else if (ch instanceof Mob && ((Mob) ch).maxLvl >= lvl && Random.Int(10)
            == 0) {
      recoverSanity(Random.IntRange(1, 6));
    }

    MaskOfMadness.Madness madness = buff(MaskOfMadness.Madness.class);
    if (madness != null)
      madness.onEmenySlayed(ch);

    GLog.i(Messages.capitalize(Messages.get(Char.class, "defeat", ch.name)));
  }

  @Override
  public void onOperateComplete() {

    if (curAction instanceof HeroAction.Unlock) {

      int doorCell = ((HeroAction.Unlock) curAction).dst;
      int door = Dungeon.level.map[doorCell];

      if (door == Terrain.LOCKED_DOOR) {
        belongings.ironKeys[Dungeon.depth]--;
        Level.set(doorCell, Terrain.DOOR);
      } else {
        belongings.specialKeys[Dungeon.depth]--;
        Level.set(doorCell, Terrain.UNLOCKED_EXIT);
      }
      StatusPane.needsKeyUpdate = true;

      Level.set(doorCell, door == Terrain.LOCKED_DOOR ? Terrain.DOOR :
              Terrain.UNLOCKED_EXIT);
      GameScene.updateMap(doorCell);

    } else if (curAction instanceof HeroAction.OpenChest) {

      Heap heap = Dungeon.level.heaps.get(((HeroAction.OpenChest) curAction)
              .dst);
      if (heap.type == Type.SKELETON || heap.type == Type.REMAINS) {
        Sample.INSTANCE.play(Assets.SND_BONES);
      } else if (heap.type == Type.LOCKED_CHEST || heap.type == Type
              .CRYSTAL_CHEST) {
        belongings.specialKeys[Dungeon.depth]--;
      }
      StatusPane.needsKeyUpdate = true;
      heap.open(this);
    }
    curAction = null;

    super.onOperateComplete();
  }

  public boolean search(boolean intentional) {

    boolean smthFound = false;

    int positive = 0;
    int negative = 0;

    int distance = 1 + positive + negative;

    float level = intentional ? (2 * awareness - awareness * awareness) :
            awareness;
    if (distance <= 0) {
      level /= 2 - distance;
      distance = 1;
    }

    int cx = pos % Dungeon.level.width();
    int cy = pos / Dungeon.level.width();
    int ax = cx - distance;
    if (ax < 0) {
      ax = 0;
    }
    int bx = cx + distance;
    if (bx >= Dungeon.level.width()) {
      bx = Dungeon.level.width() - 1;
    }
    int ay = cy - distance;
    if (ay < 0) {
      ay = 0;
    }
    int by = cy + distance;
    if (by >= Dungeon.level.height()) {
      by = Dungeon.level.height() - 1;
    }

    TalismanOfForesight.Foresight foresight = buff(TalismanOfForesight
            .Foresight.class);

    //cursed talisman of foresight makes unintentionally finding things 
    // impossible.
    if (foresight != null && foresight.isCursed()) {
      level = -1;
    }

    for (int y = ay; y <= by; y++) {
      for (int x = ax, p = ax + y * Dungeon.level.width(); x <= bx; x++, p++) {

        if (Dungeon.visible[p]) {

          if (intentional) {
            sprite.parent.addToBack(new CheckedCell(p));
          }

          if (Level.secret[p] && (intentional || Random.Float() < level)) {

            int oldValue = Dungeon.level.map[p];

            GameScene.discoverTile(p, oldValue);

            Dungeon.level.discover(p);

            ScrollOfMagicMapping.discover(p);

            smthFound = true;

            if (foresight != null && !foresight.isCursed())
              foresight.charge();
          }
        }
      }
    }


    if (intentional) {
      sprite.showStatus(CharSprite.DEFAULT, Messages.get(this, "search"));
      sprite.operate(pos);
      if (foresight != null && foresight.isCursed()) {
        GLog.n(Messages.get(this, "search_distracted"));
        spendAndNext(TIME_TO_SEARCH * 3);
      } else {
        spendAndNext(TIME_TO_SEARCH);
      }

    }

    if (smthFound) {
      GLog.w(Messages.get(this, "noticed_smth"));
      Sample.INSTANCE.play(Assets.SND_SECRET);
      interrupt();
    }

    return smthFound;
  }

  public void resurrect(int resetLevel) {

    HP = HT;
    Dungeon.gold = 0;
    exp = 0;

    belongings.resurrect(resetLevel);

    live();
  }

  @Override
  public void next() {
    if (isAlive())
      super.next();
  }

  public static interface Doom {
    public void onDeath();
  }
}
