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
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.effects.particles.SparkParticle;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.ShamanSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Shaman extends Mob implements Callback {

  private static final float TIME_TO_ZAP = 1f;

  {
    spriteClass = ShamanSprite.class;

    HP = HT = 18;
    defenseSkill = 8;

    EXP = 6;
    maxLvl = 14;

    loot = Generator.Category.SCROLL;
    lootChance = 0.33f;

    addResistances(Damage.Element.LIGHT, 1.25f);
  }

  @Override
  public Damage giveDamage(Char target) {
    return new Damage(Random.NormalIntRange(2, 8), this, target).addElement
            (Damage.Element.LIGHT);
  }

  @Override
  public Damage defendDamage(Damage dmg) {
    dmg.value -= Random.NormalIntRange(0, 4);
    return dmg;
  }

  @Override
  public int attackSkill(Char target) {
    return 11;
  }

  @Override
  protected boolean canAttack(Char enemy) {
    return new Ballistica(pos, enemy.pos, Ballistica.MAGIC_BOLT).collisionPos
            == enemy.pos;
  }

  @Override
  protected boolean doAttack(Char enemy) {

    if (Dungeon.level.distance(pos, enemy.pos) <= 1) {

      return super.doAttack(enemy);

    } else {

      boolean visible = Level.fieldOfView[pos] || Level.fieldOfView[enemy.pos];
      if (visible) {
        sprite.zap(enemy.pos);
      }

      spend(TIME_TO_ZAP);

      Damage dmg = new Damage(Random.NormalIntRange(3, 10),
              this, enemy).type(Damage.Type.MAGICAL).addElement(Damage
              .Element.LIGHT);

      if (enemy.checkHit(dmg)) {
        if (Level.water[enemy.pos] && !enemy.flying) {
          dmg.value *= 1.5f;
        }
        enemy.takeDamage(dmg);

        enemy.sprite.centerEmitter().burst(SparkParticle.FACTORY, 3);
        enemy.sprite.flash();

        if (enemy == Dungeon.hero) {

          Camera.main.shake(2, 0.3f);

          if (!enemy.isAlive()) {
            Dungeon.fail(getClass());
            GLog.n(Messages.get(this, "zap_kill"));
          }
        }
      } else {
        enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
      }

      return !visible;
    }
  }

  @Override
  public void call() {
    next();
  }
}
