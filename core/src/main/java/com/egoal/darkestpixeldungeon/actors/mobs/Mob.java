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
import com.egoal.darkestpixeldungeon.actors.buffs.Disarm;
import com.egoal.darkestpixeldungeon.actors.buffs.Rage;
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.hero.perks.Assassin;
import com.egoal.darkestpixeldungeon.effects.Wound;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.rings.RingOfWealth;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.buffs.Amok;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption;
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger;
import com.egoal.darkestpixeldungeon.actors.buffs.Sleep;
import com.egoal.darkestpixeldungeon.actors.buffs.SoulMark;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
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
import java.util.Stack;

public abstract class Mob extends Char {

  {
    name = Messages.get(this, "name");
    actPriority = 2; //hero gets priority over mobs.

    camp = Camp.ENEMY;
  }

  public AiState SLEEPING = new Sleeping();
  public AiState HUNTING = new Hunting();
  public AiState WANDERING = new Wandering();
  public AiState FLEEING = new Fleeing();
  public AiState PASSIVE = new Passive();
  public AiState FOLLOW_HERO = new FollowHero();
  public AiState state = SLEEPING;

  public Class<? extends CharSprite> spriteClass;

  protected int target = -1;

  public int EXP = 1;
  public int maxLvl = Hero.MAX_LEVEL;

  protected Char enemy;
  protected boolean enemySeen;
  protected boolean alerted = false;

  protected static final float TIME_TO_WAKE_UP = 1f;

    protected Object loot = null;
    public float lootChance = 0;

    public boolean isLiving = true; // is living things?

    public int minDamage = 0, maxDamage = 0;
  public Damage.Type typeDamage = Damage.Type.NORMAL;
    public int minDefense = 0, maxDefense = 0;

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
    } else if(state==FOLLOW_HERO)
      bundle.put(STATE, FollowHero.TAG);
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
    } else if(state.equals(FollowHero.TAG))
      this.state = FOLLOW_HERO;

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

    boolean enemyInFOV = enemy != null && enemy.isAlive() && Level.Companion.getFieldOfView()[enemy.pos] && enemy.invisible <= 0;

    return state.act(enemyInFOV, justAlerted);
  }

    @Override
    public Damage giveDamage(Char enemy) {
        // default normal damage
        return new Damage(Random.NormalIntRange(minDamage, maxDamage), this, enemy).type(typeDamage);
    }

    @Override
    public Damage defendDamage(Damage dmg) {
        // normal defend, do nothing
        dmg.value -= Random.NormalIntRange(minDefense, maxDefense);
        return dmg;
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

      // if amok
      if (buff(Amok.class) != null) {

        //try to find an enemy mob to attack first.
        for (Mob mob : Dungeon.level.getMobs())
          if (mob != this && Level.Companion.getFieldOfView()[mob.pos] && mob.camp==Camp.ENEMY)
            enemies.add(mob);
        if (enemies.size() > 0) return Random.element(enemies);

        //try to find ally mobs to attack second.
        for (Mob mob : Dungeon.level.getMobs())
          if (mob != this && Level.Companion.getFieldOfView()[mob.pos] && mob.camp==Camp.HERO)
            enemies.add(mob);
        if (enemies.size() > 0) return Random.element(enemies);

          //if there is nothing, go for the hero
        else return Dungeon.hero;
      } else {
        // try to find mobs not on its side
        // if the mob is corrupted, then it on Camp.HERO
        for(Mob mob: Dungeon.level.getMobs()){
          if(mob.camp!= Camp.NEUTRAL && mob.camp!= camp && Level.Companion.getFieldOfView()[mob.pos])
            enemies.add(mob);
        }
        if(Dungeon.hero.camp!= camp) enemies.add(Dungeon.hero);

        //target one at random.
        if(enemies.size()> 0) return Random.element(enemies);
        return null;
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
  
  public void swapPosition(Hero hero){
    int curpos = pos;
    moveSprite(pos, hero.pos);
    move(hero.pos);
    
    hero.sprite.move(pos, curpos);
    hero.move(curpos);
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

      if (Actor.findChar(target) == null && Level.Companion.getPassable()[target])
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
        int lookAhead = GameMath.INSTANCE.clamp(path.size() - 1, 1, 4);
        for (int i = 0; i < lookAhead; ++i) {
          int c = path.get(i);
          if (!Level.Companion.getPassable()[c] ||
                  (Dungeon.visible[c] && Actor.findChar(c) != null)) {
            newPath = true;
            break;
          }
        }
      }

      if (newPath)
        path = Dungeon.findPath(this, pos, target, Level.Companion.getPassable(),
                Level.Companion.getFieldOfView());

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
    int step = Dungeon.flee(this, pos, target, Level.Companion.getPassable(), Level.Companion.getFieldOfView());
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
    if (Dungeon.hero.buff(TimekeepersHourglass.TimeFreeze.class) != null)
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
    return buff(Rage.class) == null ? 1f : 0.667f;
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
  public float attackSkill(Char target) {
    return atkSkill;
  }

  @Override
  public float defenseSkill(Char enemy) {
    boolean seen = enemySeen || (enemy == Dungeon.hero && !Dungeon.hero
            .canSurpriseAttack());
    if (seen && paralysed == 0) {
      int defenseSkill = (int)defSkill;
      int penalty = RingOfAccuracy.Companion.getBonus(enemy, RingOfAccuracy.Accuracy.class);
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
      if (((Hero) enemy).getHeroPerk().has(Assassin.class)) {
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
    SoulMark sm = buff(SoulMark.class);
    if(sm!=null) sm.affectHero(Dungeon.hero,  Math.min(dmg.value, HP));

    return dmg;
  }

  public boolean surprisedBy(Char enemy) {
    return !enemySeen && enemy == Dungeon.hero;
  }

  public void aggro(Char ch) {
    enemy = ch;
    if (state != PASSIVE) {
      state = HUNTING;
    }
  }

  @Override
  protected Damage resistDamage(Damage dmg) {
    if(dmg.isFeatured(Damage.Feature.DEATH) && properties.contains(Property.BOSS))
      dmg.value /= 4;
    return super.resistDamage(dmg);
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

    Dungeon.level.getMobs().remove(this);

    if (Dungeon.hero.isAlive()) {
      if(camp== Camp.ENEMY){
        Statistics.INSTANCE.setEnemiesSlain(Statistics.INSTANCE.getEnemiesSlain() + 1);
        Badges.INSTANCE.validateMonstersSlain();
        Statistics.INSTANCE.setQualifiedForNoKilling(false);

        if (Dungeon.level.getFeeling() == Level.Feeling.DARK) {
          Statistics.INSTANCE.setNightHunt(Statistics.INSTANCE.getNightHunt() + 1);
        } else {
          Statistics.INSTANCE.setNightHunt(0);
        }
        Badges.INSTANCE.validateNightHunter();
      }

      Dungeon.hero.onMobDied(this);
      // give exp
      int exp = exp();
      if (exp > 0) {
        Dungeon.hero.sprite.showStatus(CharSprite.POSITIVE, Messages.get(this, "exp", exp));
        Dungeon.hero.earnExp(exp);
      }
    }
  }

  public int exp() {
    int dlvl = Dungeon.hero.getLvl() - maxLvl;
    if (dlvl < 0) return EXP;
    return EXP / (2 + dlvl);
  }

  @Override
  public void die(Object cause) {
    super.die(cause);

    float lootChance = this.lootChance;
    int bonus = RingOfWealth.Companion.getBonus(Dungeon.hero, RingOfWealth.Wealth.class);
    lootChance *= Math.pow(1.15, bonus);

    if (Random.Float() < lootChance && Dungeon.hero.getLvl() <= maxLvl + 2) {
      Item loot = createLoot();
      if (loot != null)
        Dungeon.level.drop(loot, pos).getSprite().drop();
    }

    if (Dungeon.hero.isAlive() && !Dungeon.visible[pos]) {
      GLog.i(Messages.get(this, "died"));
    }
  }

  @SuppressWarnings("unchecked")
  protected Item createLoot() {
    Item item = null;
    if (loot instanceof Generator.ItemGenerator) {
      item = ((Generator.ItemGenerator) loot).generate();
    } else if (loot instanceof Class<?>) {
      try {
        item = ((Class<? extends Item>) loot).newInstance().random();
      } catch (Exception ignored) {
      }
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
    GLog.n("%s: \"%s\"", name, str);
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

  public class Hunting implements AiState {

    public static final String TAG = "HUNTING";

    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
      enemySeen = enemyInFOV;
      if (enemyInFOV && !isCharmedBy(enemy) && buff(Disarm.class)==null && canAttack(enemy)) {

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

  protected class FollowHero implements AiState{
    public static final String TAG = "FOLLOW_HERO";

    @Override
    public boolean act(boolean enemyInFOV, boolean justAlerted) {
      if(enemyInFOV && (justAlerted || Random.Float(distance(enemy)/2f+ enemy.stealth())<1f)){
        // switch to hunting
        enemySeen = true;
        notice();
        state = HUNTING;
        target = enemy.pos;
      }else{
        enemySeen = false;

        if(Dungeon.level.distance(Dungeon.hero.pos, pos)>2){
          target = Dungeon.hero.pos;
        }

        int oldPos = pos;
        if(target!=-1 && getCloser(target)){
          spend(1/speed());
          return moveSprite(oldPos, pos);
        }else {
          // follow hero
          target = Dungeon.hero.pos;
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
}

