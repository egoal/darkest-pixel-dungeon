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

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.Statistics;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Dementage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.hero.HeroPerk;
import com.egoal.darkestpixeldungeon.effects.Wound;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.KGenerator;
import com.egoal.darkestpixeldungeon.items.rings.RingOfWealth;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Challenges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.buffs.Amok;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption;
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger;
import com.egoal.darkestpixeldungeon.actors.buffs.Sleep;
import com.egoal.darkestpixeldungeon.actors.buffs.SoulMark;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.effects.Surprise;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.egoal.darkestpixeldungeon.items.rings.RingOfAccuracy;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

import java.util.HashSet;

public abstract class Mob extends Char {

  {
    name = Messages.get(this, "name");
    actPriority = 2; //hero gets priority over mobs.
  }

  private static final String TXT_DIED = "You hear something died in the " +
          "distance";

  protected static final String TXT_NOTICE1 = "?!";
  protected static final String TXT_RAGE = "#$%^";
  protected static final String TXT_EXP = "%+dEXP";

  public AiState SLEEPING = new Sleeping();
  public AiState HUNTING = new Hunting();
  public AiState WANDERING = new Wandering();
  public AiState FLEEING = new Fleeing();
  public AiState PASSIVE = new Passive();
  public AiState state = SLEEPING;

  public Class<? extends CharSprite> spriteClass;

  protected int target = -1;

  protected int defenseSkill = 0;

  public int EXP = 1;
  public int maxLvl = Hero.MAX_LEVEL;

  protected Char enemy;
  protected boolean enemySeen;
  protected boolean alerted = false;

  protected static final float TIME_TO_WAKE_UP = 1f;

  public boolean hostile = true;  // 敌对
  public boolean ally = false;  // 同盟

  private static final String STATE = "state";
  private static final String SEEN = "seen";
  private static final String TARGET = "target";

  @Override
  public void storeInBundle(Bundle bundle) {

    super.storeInBundle(bundle);

    if (state == SLEEPING) {
      bundle.put(STATE, Sleeping.TAG);
    } else if (state == WANDERING) {
      bundle.put(STATE, Wandering.TAG);
    } else if (state == HUNTING) {
      bundle.put(STATE, Hunting.TAG);
    } else if (state == FLEEING) {
      bundle.put(STATE, Fleeing.TAG);
    } else if (state == PASSIVE) {
      bundle.put(STATE, Passive.TAG);
    }
    bundle.put(SEEN, enemySeen);
    bundle.put(TARGET, target);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {

    super.restoreFromBundle(bundle);

    String state = bundle.getString(STATE);
    if (state.equals(Sleeping.TAG)) {
      this.state = SLEEPING;
    } else if (state.equals(Wandering.TAG)) {
      this.state = WANDERING;
    } else if (state.equals(Hunting.TAG)) {
      this.state = HUNTING;
    } else if (state.equals(Fleeing.TAG)) {
      this.state = FLEEING;
    } else if (state.equals(Passive.TAG)) {
      this.state = PASSIVE;
    }

    enemySeen = bundle.getBoolean(SEEN);

    target = bundle.getInt(TARGET);
  }

  public CharSprite sprite() {
    CharSprite sprite = null;
    try {
      sprite = spriteClass.newInstance();
    } catch (Exception e) {
      DarkestPixelDungeon.reportException(e);
    }
    return sprite;
  }

  @Override
  protected boolean act() {

    super.act();  // update field of view

    boolean justAlerted = alerted;
    alerted = false;

    sprite.hideAlert();

    if (paralysed > 0) {
      enemySeen = false;
      spend(TICK);
      return true;
    }

    enemy = chooseEnemy();

    boolean enemyInFOV = enemy != null && enemy.isAlive() && Level
            .fieldOfView[enemy.pos] && enemy.invisible <= 0;

    return state.act(enemyInFOV, justAlerted);
  }

  protected Char chooseEnemy() {

    Terror terror = buff(Terror.class);
    if (terror != null) {
      Char source = (Char) Actor.findById(terror.object);
      if (source != null) {
        return source;
      }
    }

    //find a new enemy if..
    boolean newEnemy = false;
    //we have no enemy, or the current one is dead
    if (enemy == null || !enemy.isAlive() || state == WANDERING)
      newEnemy = true;
      //We are corrupted, and current enemy is either the hero or another 
      // corrupted character.
    else if (buff(Corruption.class) != null && (enemy == Dungeon.hero ||
            enemy.buff(Corruption.class) != null))
      newEnemy = true;
      //We are amoked and current enemy is the hero
    else if (buff(Amok.class) != null && enemy == Dungeon.hero)
      newEnemy = true;

    if (newEnemy) {

      HashSet<Char> enemies = new HashSet<>();

      //if the mob is corrupted...
      if (buff(Corruption.class) != null) {

        //look for enemy mobs to attack, which are also not corrupted
        for (Mob mob : Dungeon.level.mobs)
          if (mob != this && Level.fieldOfView[mob.pos] && mob.hostile && mob
                  .buff(Corruption.class) == null)
            enemies.add(mob);
        if (enemies.size() > 0) return Random.element(enemies);

        //otherwise go for nothing
        return null;

        //if the mob is amoked...
      } else if (buff(Amok.class) != null) {

        //try to find an enemy mob to attack first.
        for (Mob mob : Dungeon.level.mobs)
          if (mob != this && Level.fieldOfView[mob.pos] && mob.hostile)
            enemies.add(mob);
        if (enemies.size() > 0) return Random.element(enemies);

        //try to find ally mobs to attack second.
        for (Mob mob : Dungeon.level.mobs)
          if (mob != this && Level.fieldOfView[mob.pos] && mob.ally)
            enemies.add(mob);
        if (enemies.size() > 0) return Random.element(enemies);

          //if there is nothing, go for the hero
        else return Dungeon.hero;

      } else {

        //try to find ally mobs to attack.
        for (Mob mob : Dungeon.level.mobs)
          if (mob != this && Level.fieldOfView[mob.pos] && mob.ally)
            enemies.add(mob);

        //and add the hero to the list of targets.
        enemies.add(Dungeon.hero);

        //target one at random.
        return Random.element(enemies);

      }

    } else
      return enemy;
  }

  protected boolean moveSprite(int from, int to) {

    if (sprite.isVisible() && (Dungeon.visible[from] || Dungeon.visible[to])) {
      sprite.move(from, to);
      return true;
    } else {
      sprite.place(to);
      return true;
    }
  }

  @Override
  public void add(Buff buff) {
    super.add(buff);
    if (buff instanceof Amok) {
      if (sprite != null) {
        sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "rage"));
      }
      state = HUNTING;
    } else if (buff instanceof Terror) {
      state = FLEEING;
    } else if (buff instanceof Sleep) {
      state = SLEEPING;
      this.sprite().showSleep();
      postpone(Sleep.SWS);
    }
  }

  @Override
  public void remove(Buff buff) {
    super.remove(buff);
    if (buff instanceof Terror) {
      sprite.showStatus(CharSprite.NEGATIVE, Messages.get(this, "rage"));
      state = HUNTING;
    }
  }

  protected boolean canAttack(Char enemy) {
    return Dungeon.level.adjacent(pos, enemy.pos);
  }

  protected boolean getCloser(int target) {
    if (rooted || target == pos) return false;

    int step = -1;

    if (Dungeon.level.adjacent(pos, target)) {

      path = null;

      if (Actor.findChar(target) == null && Level.passable[target])
        step = target;

    } else {

      boolean newPath = false;
      if (path == null || path.isEmpty() ||
              !Dungeon.level.adjacent(pos, path.getFirst()) ||
              path.size() > 2 * Dungeon.level.distance(pos, target))
        newPath = true;
      else if (path.getLast() != target) {
        //if the new target is adjacent to the end of the path, adjust for that
        //rather than scrapping the whole path. Unless the path is very long,
        //in which case re-checking will likely result in a much better path
        if (Dungeon.level.adjacent(target, path.getLast())) {
          int last = path.removeLast();

          if (path.isEmpty()) {
            if (Dungeon.level.adjacent(target, pos))
              path.add(target);
            else {
              path.add(last);
              path.add(target);
            }
          } else {
            if (path.getLast() == target) {
            } else if (Dungeon.level.adjacent(target, path.getLast()))
              path.add(target);
            else {
              path.add(last);
              path.add(target);
            }

          }
        } else {
          newPath = true;
        }
      }


      if (!newPath) {
        //checks the next 4 cells in the path for validity
        int lookAhead = GameMath.clamp(path.size() - 1, 1, 4);
        for (int i = 0; i < lookAhead; ++i) {
          int c = path.get(i);
          if (!Level.passable[c] ||
                  (Dungeon.visible[c] && Actor.findChar(c) != null)) {
            newPath = true;
            break;
          }
        }
      }

      if (newPath)
        path = Dungeon.findPath(this, pos, target, Level.passable,
                Level.fieldOfView);

      // if the path is too long, don't go there
      if (path == null || (state == HUNTING && path.size() >
              Math.max(9, 2 * Dungeon.level.distance(pos, target))))
        return false;

      step = path.removeFirst();
    }
    if (step != -1) {
      move(step);
      return true;
    } else {
      return false;
    }
  }

  protected boolean getFurther(int target) {
    int step = Dungeon.flee(this, pos, target, Level.passable, Level
            .fieldOfView);
    if (step != -1) {
      move(step);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void updateSpriteState() {
    super.updateSpriteState();
    if (Dungeon.hero.buff(TimekeepersHourglass.timeFreeze.class) != null)
      sprite.add(CharSprite.State.PARALYSED);
  }

  @Override
  public void move(int step) {
    super.move(step);

    if (!flying) {
      Dungeon.level.mobPress(this);
    }
  }

  protected float attackDelay() {
    return 1f;
  }

  protected boolean doAttack(Char enemy) {

    boolean visible = Dungeon.visible[pos];

    if (visible) {
      sprite.attack(enemy.pos);
    } else {
      attack(enemy);
    }

    spend(attackDelay());

    return !visible;
  }

  @Override
  public void onAttackComplete() {
    attack(enemy);
    super.onAttackComplete();
  }

  @Override
  public int defenseSkill(Char enemy) {
    boolean seen = enemySeen || (enemy == Dungeon.hero && !Dungeon.hero
            .canSurpriseAttack());
    if (seen && paralysed == 0) {
      int defenseSkill = this.defenseSkill;
      int penalty = RingOfAccuracy.getBonus(enemy, RingOfAccuracy.Accuracy
              .class);
      if (penalty != 0 && enemy == Dungeon.hero)
        defenseSkill *= Math.pow(0.75, penalty);
      return defenseSkill;
    } else {
      return 0;
    }
  }

  @Override
  public Damage defenseProc(Damage dmg) {
    Char enemy = (Char) dmg.from;
    if (!enemySeen && enemy == Dungeon.hero && Dungeon.hero.canSurpriseAttack
            ()) {
      // surprise attack!
      if (((Hero) enemy).heroPerk.contain(HeroPerk.Perk.ASSASSIN)) {
        // assassin perk
        dmg.value *= 1.25f;
        Wound.hit(this);
      } else
        Surprise.hit(this);
    }

    // attack by a closer but not current enemy, switch
    if (this.enemy == null ||
            (enemy != this.enemy && Dungeon.level.distance(pos, enemy.pos) <
                    Dungeon.level.distance(pos, this.enemy.pos))) {
      aggro(enemy);
      target = enemy.pos; // enemy set, not null 
    }

    // process buff: soul mark
    if (buff(SoulMark.class) != null) {
      int restoration = Math.min(dmg.value, HP);
      Dungeon.hero.buff(Hunger.class).satisfy(restoration * 0.5f);
      Dungeon.hero.HP = (int) Math.ceil(Math.min(Dungeon.hero.HT, Dungeon
              .hero.HP + restoration * .3f));
      Dungeon.hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 1);
    }

    return dmg;
  }

  public boolean surprisedBy(Char enemy) {
    return !enemySeen && enemy == Dungeon.hero;
  }

  public boolean isFollower() {
    return buff(Dementage.class) != null;
  }

  public void aggro(Char ch) {
    enemy = ch;
    if (state != PASSIVE) {
      state = HUNTING;
    }
  }

  @Override
  public int takeDamage(Damage dmg) {
    Terror.recover(this);

    if (state == SLEEPING)
      state = WANDERING;
    if (state != HUNTING)
      alerted = true;

    return super.takeDamage(dmg);
  }

  @Override
  public void destroy() {

    super.destroy();

    Dungeon.level.mobs.remove(this);

    if (Dungeon.hero.isAlive()) {
      if (hostile) {
        Statistics.INSTANCE.setEnemiesSlain(Statistics.INSTANCE
                .getEnemiesSlain() + 1);
        Badges.validateMonstersSlain();
        Statistics.INSTANCE.setQualifiedForNoKilling(false);

        if (Dungeon.level.feeling == Level.Feeling.DARK) {
          Statistics.INSTANCE.setNightHunt(Statistics.INSTANCE.getNightHunt()
                  + 1);
        } else {
          Statistics.INSTANCE.setNightHunt(0);
        }
        Badges.validateNightHunter();
      }

      Dungeon.hero.onMobDied(this);
      // give exp
      int exp = exp();
      if (exp > 0) {
        Dungeon.hero.sprite.showStatus(CharSprite.POSITIVE, Messages.get
                (this, "exp", exp));
        Dungeon.hero.earnExp(exp);
      }
    }
  }

  public int exp() {
    int dlvl = Dungeon.hero.lvl - maxLvl;
    if (dlvl < 0) return EXP;
    return EXP / (2 + dlvl);
  }

  @Override
  public void die(Object cause) {

    super.die(cause);

    float lootChance = this.lootChance;
    int bonus = RingOfWealth.getBonus(Dungeon.hero, RingOfWealth.Wealth.class);
    lootChance *= Math.pow(1.15, bonus);

    if (Random.Float() < lootChance && Dungeon.hero.lvl <= maxLvl + 2) {
      Item loot = createLoot();
      if (loot != null)
        Dungeon.level.drop(loot, pos).sprite.drop();
    }

    if (Dungeon.hero.isAlive() && !Dungeon.visible[pos]) {
      GLog.i(Messages.get(this, "died"));
    }
  }

  protected Object loot = null;
  protected float lootChance = 0;

  @SuppressWarnings("unchecked")
  protected Item createLoot() {
    Item item;
    if (loot instanceof Generator.Category) {

      item = Generator.random((Generator.Category) loot);

    } else if (loot instanceof KGenerator.ItemGenerator) {
      item = ((KGenerator.ItemGenerator) loot).generate();
    } else if (loot instanceof Class<?>) {

      item = Generator.random((Class<? extends Item>) loot);

    } else {

      item = (Item) loot;

    }
    return item;
  }

  public boolean reset() {
    return false;
  }

  public void beckon(int cell) {

    notice();

    if (state != HUNTING) {
      state = WANDERING;
    }
    target = cell;
  }

  public String description() {
    return Messages.get(this, "desc");
  }

  public void notice() {
    sprite.showAlert();
  }

  public void yell(String str) {
    GLog.n("%s: \"%s\" ", name, str);
  }

  //returns true when a mob sees the hero, and is currently targeting them.
  public boolean focusingHero() {
    return enemySeen && (target == Dungeon.hero.pos);
  }

  public interface AiState {
    boolean act(boolean enemyInFOV, boolean justAlerted);

    String status();
  }

  protected class Sleeping implements AiState {

    public static final String TAG = "SLEEPING";

    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
      if (enemyInFOV && Random.Int(distance(enemy) / 2 + 1 + enemy.stealth() +
              (enemy.flying ? 2 : 0)) == 0) {

        enemySeen = true;

        notice();
        state = HUNTING;
        target = enemy.pos;

        if (Dungeon.isChallenged(Challenges.SWARM_INTELLIGENCE)) {
          for (Mob mob : Dungeon.level.mobs) {
            if (mob != Mob.this) {
              mob.beckon(target);
            }
          }
        }

        spend(TIME_TO_WAKE_UP);

      } else {

        enemySeen = false;

        spend(TICK);

      }
      return true;
    }

    @Override
    public String status() {
      return Messages.get(this, "status", name);
    }
  }

  protected class Wandering implements AiState {

    public static final String TAG = "WANDERING";

    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
      if (enemyInFOV && (justAlerted || Random.Float(distance(enemy) / 2f +
              enemy.stealth()) < 1f)) {

        enemySeen = true;

        notice();
        state = HUNTING;
        target = enemy.pos;

      } else {

        enemySeen = false;

        int oldPos = pos;
        if (target != -1 && getCloser(target)) {
          spend(1 / speed());
          return moveSprite(oldPos, pos);
        } else {
          target = Dungeon.level.randomDestination();
          spend(TICK);
        }

      }
      return true;
    }

    @Override
    public String status() {
      return Messages.get(this, "status", name);
    }
  }

  protected class Hunting implements AiState {

    public static final String TAG = "HUNTING";

    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
      enemySeen = enemyInFOV;
      if (enemyInFOV && !isCharmedBy(enemy) && canAttack(enemy)) {

        return doAttack(enemy);

      } else {

        if (enemyInFOV) {
          target = enemy.pos;
        } else if (enemy == null) {
          state = WANDERING;
          target = Dungeon.level.randomDestination();
          return true;
        }

        int oldPos = pos;
        if (target != -1 && getCloser(target)) {

          spend(1 / speed());
          return moveSprite(oldPos, pos);

        } else {

          spend(TICK);
          state = WANDERING;
          target = Dungeon.level.randomDestination();
          return true;
        }
      }
    }

    @Override
    public String status() {
      return Messages.get(this, "status", name);
    }
  }

  protected class Fleeing implements AiState {

    public static final String TAG = "FLEEING";

    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
      enemySeen = enemyInFOV;
      //loses target when 0-dist rolls a 6 or greater.
      if (enemy == null || !enemyInFOV && 1 + Random.Int(Dungeon.level
              .distance(pos, target)) >= 6) {
        target = -1;
      } else {
        target = enemy.pos;
      }

      int oldPos = pos;
      if (target != -1 && getFurther(target)) {

        spend(1 / speed());
        return moveSprite(oldPos, pos);

      } else {

        spend(TICK);
        nowhereToRun();

        return true;
      }
    }

    protected void nowhereToRun() {
    }

    @Override
    public String status() {
      return Messages.get(this, "status", name);
    }
  }

  protected class Passive implements AiState {

    public static final String TAG = "PASSIVE";

    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
      enemySeen = false;
      spend(TICK);
      return true;
    }

    @Override
    public String status() {
      return Messages.get(this, "status", name);
    }
  }
}

