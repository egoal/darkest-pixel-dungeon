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
package com.egoal.darkestpixeldungeon.items.wands;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.Statistics;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Amok;
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding;
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.actors.buffs.Charm;
import com.egoal.darkestpixeldungeon.actors.buffs.Chill;
import com.egoal.darkestpixeldungeon.actors.buffs.Corruption;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.actors.buffs.Drowsy;
import com.egoal.darkestpixeldungeon.actors.buffs.FlavourBuff;
import com.egoal.darkestpixeldungeon.actors.buffs.Frost;
import com.egoal.darkestpixeldungeon.actors.buffs.MagicalSleep;
import com.egoal.darkestpixeldungeon.actors.buffs.Ooze;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.actors.buffs.PinCushion;
import com.egoal.darkestpixeldungeon.actors.buffs.Poison;
import com.egoal.darkestpixeldungeon.actors.buffs.Roots;
import com.egoal.darkestpixeldungeon.actors.buffs.Slow;
import com.egoal.darkestpixeldungeon.actors.buffs.SoulMark;
import com.egoal.darkestpixeldungeon.actors.buffs.Terror;
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo;
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable;
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness;
import com.egoal.darkestpixeldungeon.actors.mobs.Bee;
import com.egoal.darkestpixeldungeon.actors.mobs.King;
import com.egoal.darkestpixeldungeon.actors.mobs.Mimic;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.actors.mobs.Piranha;
import com.egoal.darkestpixeldungeon.actors.mobs.Statue;
import com.egoal.darkestpixeldungeon.actors.mobs.Swarm;
import com.egoal.darkestpixeldungeon.actors.mobs.Wraith;
import com.egoal.darkestpixeldungeon.actors.mobs.Yog;
import com.egoal.darkestpixeldungeon.effects.MagicMissile;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.armor.curses.Corrosion;
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.HashMap;

public class WandOfCorruption extends Wand {

  {
    image = ItemSpriteSheet.WAND_CORRUPTION;
  }

  private static final float MINOR_DEBUFF_WEAKEN = .8f;
  private static final HashMap<Class<? extends Buff>, Float> MINOR_DEBUFFS =
          new HashMap<>();

  static {
    MINOR_DEBUFFS.put(Weakness.class, 0f);  // in dpd, weakness can only 
    // attach hero
    MINOR_DEBUFFS.put(Cripple.class, 1f);
    MINOR_DEBUFFS.put(Blindness.class, 1f);
    MINOR_DEBUFFS.put(Terror.class, 1f);

    MINOR_DEBUFFS.put(Chill.class, 0f);
    MINOR_DEBUFFS.put(Ooze.class, 0f);
    MINOR_DEBUFFS.put(Roots.class, 0f);
    MINOR_DEBUFFS.put(Vertigo.class, 0f);
    MINOR_DEBUFFS.put(Drowsy.class, 0f);
    MINOR_DEBUFFS.put(Bleeding.class, 0f);
    MINOR_DEBUFFS.put(Burning.class, 0f);
    MINOR_DEBUFFS.put(Poison.class, 0f);
  }

  private static final float MAJOR_DEBUFF_WEAKEN = .667f;
  private static final HashMap<Class<? extends Buff>, Float> MAJOR_DEBUFFS =
          new HashMap<>();

  static {
    MAJOR_DEBUFFS.put(Amok.class, 3f);
    MAJOR_DEBUFFS.put(Slow.class, 2f);
    MAJOR_DEBUFFS.put(Paralysis.class, 1f);

    MAJOR_DEBUFFS.put(Charm.class, 0f);
    MAJOR_DEBUFFS.put(MagicalSleep.class, 0f);
    MAJOR_DEBUFFS.put(SoulMark.class, 0f);
    MAJOR_DEBUFFS.put(Frost.class, 0f);
  }


  @Override
  protected void onZap(Ballistica bolt) {
    Char ch = Actor.findChar(bolt.collisionPos);

    if (ch != null && ch instanceof Mob) {
      Mob enemy = (Mob) ch;

      float corruptingpower = 2 + level();

      float enemyResist = 1 + enemy.EXP;

      if (ch instanceof Mimic || ch instanceof Statue)
        enemyResist = 1 + Dungeon.depth;
      else if (ch instanceof Piranha || ch instanceof Bee)
        enemyResist = 1 + Dungeon.depth / 2f;
      else if (ch instanceof Wraith)
        enemyResist = .5f + Dungeon.depth / 8f;
      else if (ch instanceof Yog.Larva || ch instanceof King.Undead)
        enemyResist = 1 + 30;
      else if (ch instanceof Swarm)
        enemyResist = 1 + 3;

      //100% health: 3x resist   75%: 2.1x resist   50%: 1.5x resist   25%: 
      // 1.1x resist
      enemyResist *= 1 + 2 * Math.pow(enemy.HP / (float) enemy.HT, 2);

      //debuffs placed on the enemy reduce their resistance
      for (Buff buff : enemy.buffs()) {
        if (MAJOR_DEBUFFS.containsKey(buff.getClass()))
          enemyResist *= MAJOR_DEBUFF_WEAKEN;
        else if (MINOR_DEBUFFS.containsKey(buff.getClass()))
          enemyResist *= MINOR_DEBUFF_WEAKEN;
        else if (buff.type == Buff.buffType.NEGATIVE)
          enemyResist *= MINOR_DEBUFF_WEAKEN;
      }

      //cannot re-corrupt or doom an enemy, so give them a major debuff instead
      if (enemy.buff(Corruption.class) != null) {
        enemyResist = corruptingpower * .99f;
      }

      if (corruptingpower > enemyResist)
        corruptEnemy(enemy);
      else {
        float debuffchance = corruptingpower / enemyResist;
        if (Random.Float() < debuffchance)
          debuffEnemy(enemy, MAJOR_DEBUFFS);
        else
          debuffEnemy(enemy, MINOR_DEBUFFS);
      }
    } else {
      // no press effect in dpd
      // Dungeon.level.press(bolt.collisionPos, null);
    }
  }

  private void debuffEnemy(Mob enemy, HashMap<Class<? extends Buff>, Float>
          category) {
    HashMap<Class<? extends Buff>, Float> debuffs = new HashMap<>(category);
    for (Buff existing : enemy.buffs()) {
      if (debuffs.containsKey(existing.getClass())) {
        debuffs.put(existing.getClass(), 0f);
      }
    }
    for (Class<? extends Buff> toAssign : debuffs.keySet()) {
      if (debuffs.get(toAssign) > 0 && enemy.immunizedBuffs().contains
              (toAssign)) {
        debuffs.put(toAssign, 0f);
      }
    }

    //all buffs with a > 0 chance are flavor buffs
    Class<? extends FlavourBuff> debuffCls = (Class<? extends FlavourBuff>)
            Random.chances(debuffs);

    if (debuffCls != null)
      Buff.append(enemy, debuffCls, 6 + level() * 3);
    else {
      //if no debuff can be applied (all are present), then go up one tier
      if (category == MINOR_DEBUFFS) debuffEnemy(enemy, MAJOR_DEBUFFS);
      else if (category == MAJOR_DEBUFFS) corruptEnemy(enemy);
    }
  }

  private void corruptEnemy(Mob enemy) {
    //cannot re-corrupt or doom an enemy, so give them a major debuff instead
    if (enemy.buff(Corruption.class) != null || enemy.buff(Vulnerable.class)
            != null) {
      GLog.w(Messages.get(this, "already_corrupted"));
      return;
    }

    boolean canbecorruptted = !enemy.immunizedBuffs().contains(Corruption.class)
            && !enemy.properties().contains(Char.Property.BOSS) &&
            !enemy.properties().contains(Char.Property.MINIBOSS) &&
            !enemy.properties().contains(Char.Property.MACHINE);

    if (canbecorruptted) {
      enemy.HP = enemy.HT;
      for (Buff buff : enemy.buffs()) {
        if (buff.type == Buff.buffType.NEGATIVE && !(buff instanceof SoulMark))
          buff.detach();
          // what is this?
        else if (buff instanceof PinCushion)
          buff.detach();
      }
      Buff.affect(enemy, Corruption.class);

      // in dpd, enemy would not dead directly...
//      Statistics.enemiesSlain++;
//      Badges.validateMonstersSlain();
//      Statistics.qualifiedForNoKilling  = false;
//      if(enemy.EXP>0 && curUser.lvl<=enemy.maxLvl){
//        curUser.sprite.showStatus(CharSprite.POSITIVE, Messages.get(enemy, 
// "exp", enemy.EXP));
//        curUser.earnExp(enemy.EXP);
//      }

    } else {
      // in dpd, i give the vulnerable
      Buff.prolong(enemy, Vulnerable.class, Vulnerable.DURATION*2).ratio = 1.5f;
    }
  }

  @Override
  public void onHit(MagesStaff staff, Damage damage) {
    // lvl 0 - 25%
    // lvl 1 - 40%
    // lvl 2 - 50%
    if (Random.Int(level() + 4) >= 3) {
      Buff.prolong((Char)damage.to, Amok.class, 3 + level());
    }
  }

  @Override
  protected void fx(Ballistica bolt, Callback callback) {
    MagicMissile.shadow(curUser.sprite.parent, bolt.sourcePos, bolt
            .collisionPos, callback);
    Sample.INSTANCE.play(Assets.SND_ZAP);
  }

  @Override
  public void staffFx(MagesStaff.StaffParticle particle) {
    particle.color(0);
    particle.am = 0.6f;
    particle.setLifespan(0.8f);
    particle.acc.set(0, 20);
    particle.setSize(0f, 3f);
    particle.shuffleXY(2f);
  }

}
