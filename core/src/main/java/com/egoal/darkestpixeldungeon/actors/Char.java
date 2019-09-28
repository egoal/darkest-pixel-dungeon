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
package com.egoal.darkestpixeldungeon.actors;

import com.egoal.darkestpixeldungeon.Statistics;
import com.egoal.darkestpixeldungeon.actors.buffs.Bless;
import com.egoal.darkestpixeldungeon.actors.buffs.Chill;
import com.egoal.darkestpixeldungeon.actors.buffs.Drunk;
import com.egoal.darkestpixeldungeon.actors.buffs.Frost;
import com.egoal.darkestpixeldungeon.actors.buffs.Ignorant;
import com.egoal.darkestpixeldungeon.actors.buffs.LifeLink;
import com.egoal.darkestpixeldungeon.actors.buffs.Light;
import com.egoal.darkestpixeldungeon.actors.buffs.MustDodge;
import com.egoal.darkestpixeldungeon.actors.buffs.ResistAny;
import com.egoal.darkestpixeldungeon.actors.buffs.Roots;
import com.egoal.darkestpixeldungeon.actors.buffs.SharpVision;
import com.egoal.darkestpixeldungeon.actors.buffs.Shock;
import com.egoal.darkestpixeldungeon.actors.buffs.Unbalance;
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.CriticalShock;
import com.egoal.darkestpixeldungeon.effects.WeaponFlash;
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Charm;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.actors.buffs.EarthImbue;
import com.egoal.darkestpixeldungeon.actors.buffs.FireImbue;
import com.egoal.darkestpixeldungeon.actors.buffs.MagicalSleep;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.actors.buffs.Slow;
import com.egoal.darkestpixeldungeon.actors.buffs.Speed;
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo;
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.features.Door;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public abstract class Char extends Actor {

  public int pos = 0;

  public CharSprite sprite;

  public String name = "mob";

  public int HT;    // max hp
  public int HP;
  public int SHLD;

  public float atkSkill = 0f;
  public float defSkill = 0f;

  protected float baseSpeed = 1;
  protected PathFinder.Path path;

  public int paralysed = 0;
  public boolean rooted = false;
  public boolean flying = false;
  public int invisible = 0;

  // resistances
  public float magicalResistance = 0f;
  public float[] elementalResistance = new float[Damage.Element.ELEMENT_COUNT];

  private HashSet<Buff> buffs = new HashSet<>();

  {
    Arrays.fill(elementalResistance, 0f);
  }

  @Override
  protected boolean act() {
    Dungeon.level.updateFieldOfView(this, Level.Companion.getFieldOfView());
    return false;
  }

  private static final String POS = "pos";
  private static final String TAG_HP = "HP";
  private static final String TAG_HT = "HT";
  private static final String TAG_SHLD = "SHLD";
  private static final String BUFFS = "buffs";

  @Override
  public void storeInBundle(Bundle bundle) {

    super.storeInBundle(bundle);

    bundle.put(POS, pos);
    bundle.put(TAG_HP, HP);
    bundle.put(TAG_HT, HT);
    bundle.put(TAG_SHLD, SHLD);
    bundle.put(BUFFS, buffs);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {

    super.restoreFromBundle(bundle);

    pos = bundle.getInt(POS);
    HP = bundle.getInt(TAG_HP);
    HT = bundle.getInt(TAG_HT);
    SHLD = bundle.getInt(TAG_SHLD);

    for (Bundlable b : bundle.getCollection(BUFFS)) {
      if (b != null) {
        ((Buff) b).attachTo(this);
      }
    }
  }

  public int viewDistance() {
    if (buff(SharpVision.class) != null) return seeDistance();

    int vd = 0;
    switch (Statistics.INSTANCE.getClock().getState()) {
      case Day:
        vd = 6;
        break;
      case Night:
      case MidNight:
        vd = 3;
        break;
    }

    if (buff(Drunk.class) != null) vd -= 1;

    return vd;
  }

  public int seeDistance() {
    return 8;
  }

  public boolean attack(Char enemy) {
    if (enemy == null || !enemy.isAlive()) return false;

    boolean visibleFight = Dungeon.visible[pos] || Dungeon.visible[enemy.pos];

    Damage dmg = giveDamage(enemy);
    if (enemy.checkHit(dmg)) {

      // enemy armor defense

      // sniper's perk,
      if (this instanceof Hero && ((Hero) this).getRangedWeapon() != null && (
              (Hero) this).getSubClass() == HeroSubClass.SNIPER) {
        // sniper's perk: ignore defence
      } else if (dmg.type!= Damage.Type.MAGICAL && !dmg.isFeatured(Damage.Feature.PURE))
        dmg = enemy.defendDamage(dmg);

      dmg = attackProc(dmg);

      ResistAny ra = enemy.buff(ResistAny.class);
      if (ra != null && dmg.type != Damage.Type.MENTAL) {
        ra.resist();
        dmg.value = 0;
      }
      dmg = enemy.defenseProc(dmg);

      if (visibleFight && !TimekeepersHourglass.Companion.IsTimeStopped()) {
        if (dmg.type == Damage.Type.NORMAL && dmg.isFeatured(Damage.Feature
                .CRITICAL) && dmg.value > 0)
          Sample.INSTANCE.play(Assets.SND_CRITICAL, 1, 1, 1f);
        else
          Sample.INSTANCE.play(Assets.SND_HIT, 1, 1, Random.Float(0.8f, 1.25f));
      }

      // may died in proc
      if (!enemy.isAlive()) return true;

      // camera shake, todo: add more effects here
      float shake = 0f;
      if (enemy == Dungeon.hero)
        shake = dmg.value / (enemy.HT / 4);
      if (shake > 1f)
        Camera.main.shake(GameMath.INSTANCE.gate(1, shake, 5), .3f);

      // take!
      int value = enemy.takeDamage(dmg);
      if (value < 0) {
        // ^^^ this is the only case when time stop! not a good design, but
        // works for now.
        enemy.sprite.flash(); // let the player know, "i hit it"
        return true;
      }

      // buffs, dont know why this piece of code exists,
      // maybe the mage? or the attack effect?
      if (buff(FireImbue.class) != null)
        buff(FireImbue.class).proc(enemy);
      if (buff(EarthImbue.class) != null)
        buff(EarthImbue.class).proc(enemy);

      if (this == Dungeon.hero)
        Statistics.INSTANCE.setHighestDamage(
                Math.max(Statistics.INSTANCE.getHighestDamage(), value));

      // effects
      // burst blood
      if (dmg.isFeatured(Damage.Feature.CRITICAL)) {
        enemy.sprite.bloodBurstB(sprite.center(), value);
        enemy.sprite.spriteBurst(sprite.center(), value);
        enemy.sprite.flash();
      } else {
        enemy.sprite.bloodBurstA(sprite.center(), value);
        enemy.sprite.flash();
      }

      // WeaponFlash.Companion.Flash(this, enemy);

      if (!enemy.isAlive() && visibleFight) {
        if (enemy == Dungeon.hero) {
          // hero die
          Dungeon.fail(getClass());
          GLog.n(Messages.capitalize(Messages.get(Char.class, "kill", name)));
        } else if (this == Dungeon.hero) {
          // killed by hero
          Dungeon.hero.onKillChar(enemy);
        }
      }

      return true;
    } else {
      // missed
      if (visibleFight) {
        String str = enemy.defenseVerb();
        enemy.sprite.showStatus(CharSprite.NEUTRAL, str);

        Sample.INSTANCE.play(Assets.SND_MISS);
      }

      return false;
    }
  }

  public Damage giveDamage(Char enemy) {
    // default normal damage
    return new Damage(1, this, enemy);
  }

  public Damage defendDamage(Damage dmg) {
    // normal defend, do nothing
    return dmg;
  }

  public boolean checkHit(Damage dmg) {
    Char attacker = (Char) dmg.from;
    Char defender = (Char) dmg.to;

    // shocked, must miss
    if (attacker.buff(Shock.class) != null) return false;

    if (defender.buff(Unbalance.class) != null) return true;

    // must dodge, cannot hit
    MustDodge md = defender.buff(MustDodge.class);
    if (md != null && md.canDodge(dmg))
      return false;

    // when from no where, be accurate
    if (dmg.from instanceof Mob && !Dungeon.visible[((Char) dmg.from).pos])
      dmg.addFeature(Damage.Feature.ACCURATE);

    if (dmg.isFeatured(Damage.Feature.ACCURATE))
      return true;

    float acuRoll = Random.Float(attacker.attackSkill(defender));
    float defRoll = Random.Float(defender.defenseSkill(attacker));

    // buff fix
    if (attacker.buffs(Bless.class) != null) acuRoll *= 1.2f;
    if (defender.buffs(Bless.class) != null) defRoll *= 1.2f;
    if (defender.buffs(Roots.class) != null) defRoll *= .5f;

    float bonus = 1.f;
    if (dmg.type == Damage.Type.MAGICAL || dmg.type == Damage.Type.MENTAL)
      bonus = 2f;

    return bonus * acuRoll >= defRoll;
  }

  public Damage attackProc(Damage dmg) {
    return dmg;
  }

  public Damage defenseProc(Damage dmg) {
    return dmg;
  }

  public int takeDamage(Damage dmg) {
    // time freeze
    TimekeepersHourglass.TimeFreeze tf = Dungeon.hero.buff
            (TimekeepersHourglass.TimeFreeze.class);
    if (tf != null) {
      tf.addDelayedDamage(dmg);
      return -1; //! be negative
    }

    // life link
    LifeLink ll = buff(LifeLink.class);
    if (ll != null) {
      Actor a = Actor.findById(ll.linker);
      if (a instanceof Char) {
        ((Char) a).takeDamage(dmg);
        ((Char) a).sprite.showStatus(0x000000, Messages.get(LifeLink.class,
                "transform"));

        return 0;
      }
    }

    // currently, only hero suffer from mental damage
    if (!isAlive() || dmg.value < 0 || (dmg.type == Damage.Type.MENTAL && !
            (this instanceof Hero)))
      return 0;

    // vulnerable
    Vulnerable v = buff(Vulnerable.class);
    if (v != null) v.procDamage(dmg);

    // buffs shall remove when take damage
    if (this.buff(Frost.class) != null)
      Buff.detach(this, Frost.class);
    if (this.buff(MagicalSleep.class) != null)
      Buff.detach(this, MagicalSleep.class);
    if (dmg.from instanceof Char && isCharmedBy((Char) dmg.from))
      Buff.detach(this, Charm.class);

    // immunities, resistance
    if (!dmg.isFeatured(Damage.Feature.PURE)) dmg = resistDamage(dmg);

    // buffs when take damage
    if (buff(Paralysis.class) != null) {
      if (Random.Int(dmg.value) >= Random.Int(HP)) {
        Buff.detach(this, Paralysis.class);
        if (Dungeon.visible[pos])
          GLog.i(Messages.get(Char.class, "out_of_paralysis", name));
      }
    }

    // deal with types
    //todo: the damage number can have different colour refer to the element
    // they carry
    switch (dmg.type) {
      case NORMAL:
        // physical
        if (SHLD >= dmg.value)
          SHLD -= dmg.value;
        else {
          HP -= (dmg.value - SHLD);
          SHLD = 0;
        }
        break;
      case MAGICAL:
        HP -= dmg.value;
        break;
    }

    //note: this is a important setting
    if (HP < 0) HP = 0;

    // show damage value
    if (buff(Ignorant.class) == null) {
      if (dmg.value > 0 || dmg.from instanceof Char) {
        String number = Integer.toString(dmg.value);
        int color = HP > HT / 4 ? CharSprite.WARNING : CharSprite.NEGATIVE;

        if (dmg.isFeatured(Damage.Feature.CRITICAL))
          number += "!";

        sprite.showStatus(color, number);
      }
    }

    if (!isAlive())
      die(dmg.from);

    return dmg.value;
  }

  public void addResistances(int element, float r) {
      for(int i=0; i< Damage.Element.ELEMENT_COUNT; ++i)
          if((element & (0x01<< i))!= 0)
              elementalResistance[i] = r;
  }

  protected Damage resistDamage(Damage dmg) {
    ResistAny ra = buff(ResistAny.class);
    if (ra != null && dmg.type != Damage.Type.MENTAL) {
      ra.resist();
      dmg.value = 0;
      return dmg;
    }

    for (Class<?> im : immunizedBuffs())
      if (dmg.from.getClass() == im) {
        dmg.value = 0;
        return dmg;
      }

    // elemental resistance
    for(int of=0; of< Damage.Element.ELEMENT_COUNT; ++of)
      if(dmg.isFeatured(0x01<< of))
          dmg.value -= Math.round(dmg.value* elementalResistance[of]);

    if(dmg.type== Damage.Type.MAGICAL)
      dmg.value -= Math.round(dmg.value* magicalResistance);

    if(dmg.value<0) dmg.value = 0;

    return dmg;
  }

  // attack or edoge ratio
  public float attackSkill(Char target) {
    return atkSkill;
  }

  public float defenseSkill(Char enemy) {
    return defSkill;
  }

  public String defenseVerb() {
    return Messages.get(this, "def_verb");
  }

  public float speed() {
    return buff(Cripple.class) == null ? baseSpeed : baseSpeed * 0.5f;
  }

  public void destroy() {
    HP = 0;
    remove(this);
  }

  public void die(Object src) {
    destroy();
    sprite.die();
  }

  public boolean isAlive() {
    return HP > 0;
  }

  @Override
  protected void spend(float time) {

    float timeScale = 1f;
    if (buff(Slow.class) != null) {
      timeScale *= 0.5f;
      //slowed and chilled do not stack
    } else if (buff(Chill.class) != null) {
      timeScale *= buff(Chill.class).speedFactor();
    }
    if (buff(Speed.class) != null) {
      timeScale *= 2.0f;
    }

    super.spend(time / timeScale);
  }

  public synchronized HashSet<Buff> buffs() {
    return new HashSet<>(buffs);
  }

  @SuppressWarnings("unchecked")
  public synchronized <T extends Buff> HashSet<T> buffs(Class<T> c) {
    HashSet<T> filtered = new HashSet<>();
    for (Buff b : buffs) {
      if (c.isInstance(b)) {
        filtered.add((T) b);
      }
    }
    return filtered;
  }

  @SuppressWarnings("unchecked")
  public synchronized <T extends Buff> T buff(Class<T> c) {
    for (Buff b : buffs) {
      if (c.isInstance(b)) {
        return (T) b;
      }
    }
    return null;
  }

  public synchronized boolean isCharmedBy(Char ch) {
    int chID = ch.id();
    for (Buff b : buffs) {
      if (b instanceof Charm && ((Charm) b).object == chID) {
        return true;
      }
    }
    return false;
  }

  public void add(Buff buff) {

    buffs.add(buff);
    Actor.add(buff);

    if (sprite != null)
      switch (buff.type) {
        case POSITIVE:
          sprite.showStatus(CharSprite.POSITIVE, buff.toString());
          break;
        case NEGATIVE:
          sprite.showStatus(CharSprite.NEGATIVE, buff.toString());
          break;
        case NEUTRAL:
          sprite.showStatus(CharSprite.NEUTRAL, buff.toString());
          break;
        case SILENT:
        default:
          break; //show nothing
      }

  }

  public void remove(Buff buff) {

    buffs.remove(buff);
    Actor.remove(buff);

  }

  public void remove(Class<? extends Buff> buffClass) {
    for (Buff buff : buffs(buffClass)) {
      remove(buff);
    }
  }

  @Override
  protected void onRemove() {
    for (Buff buff : buffs.toArray(new Buff[buffs.size()])) {
      buff.detach();
    }
  }

  public void updateSpriteState() {
    for (Buff buff : buffs) {
      buff.fx(true);
    }
  }

  public int stealth() {
    return 0;
  }

  public void move(int step) {

    if (Dungeon.level.adjacent(step, pos) && buff(Vertigo.class) != null) {
      sprite.interruptMotion();
      int newPos = pos + PathFinder.NEIGHBOURS8[Random.Int(8)];
      if (!(Level.Companion.getPassable()[newPos] || Level.Companion.getAvoid()[newPos]) || findChar
              (newPos) != null)
        return;
      else {
        sprite.move(pos, newPos);
        step = newPos;
      }
    }

    if (Dungeon.level.getMap()[pos] == Terrain.OPEN_DOOR) {
      Door.INSTANCE.Leave(pos, this);
    }

    pos = step;

    if (flying && Dungeon.level.getMap()[pos] == Terrain.DOOR) {
      Door.INSTANCE.Enter(pos, this);
    }

    if (this != Dungeon.hero) {
      sprite.visible = Dungeon.visible[pos];
    }
  }

  public int distance(Char other) {
    return Dungeon.level.distance(pos, other.pos);
  }

  public void onMotionComplete() {
    //Does nothing by default
    //The main actor thread already accounts for motion,
    // so calling next() here isn't necessary (see Actor.process)
  }

  //note: called when the animation is done
  public void onAttackComplete() {
    next();
  }

  public void onOperateComplete() {
    next();
  }

  private static final HashSet<Class<?>> EMPTY = new HashSet<>();

  public HashSet<Class<?>> immunizedBuffs() {
    return EMPTY;
  }

  protected HashSet<Property> properties = new HashSet<>();

  public HashSet<Property> properties() {
    return properties;
  }

  public enum Property {
    BOSS,
    MINIBOSS,
    UNDEAD,
    DEMONIC,
    MACHINE,
    IMMOVABLE,
    PHANTOM,
  }

  // used on damage attacker, avoid null usage...
  public static class Nobody extends Char {
    public static Nobody INSTANCE = new Nobody();
  }
}
