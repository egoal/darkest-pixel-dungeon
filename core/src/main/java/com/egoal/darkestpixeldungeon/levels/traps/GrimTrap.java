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
package com.egoal.darkestpixeldungeon.levels.traps;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.TrapSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class GrimTrap extends Trap {

  {
    color = TrapSprite.GREY;
    shape = TrapSprite.LARGE_DOT;
  }

  @Override
  public Trap hide() {
    //cannot hide this trap
    return reveal();
  }

  @Override
  public void activate() {
    Char target = Actor.Companion.findChar(pos);

    //find the closest char that can be aimed at
    if (target == null) {
      for (Char ch : Actor.Companion.chars()) {
        Ballistica bolt = new Ballistica(pos, ch.getPos(), Ballistica.PROJECTILE);
        if (bolt.collisionPos == ch.getPos() &&
                (target == null || Dungeon.level.distance(pos, ch.getPos()) <
                        Dungeon.level.distance(pos, target.getPos()))) {
          target = ch;
        }
      }
    }

    if (target != null) {
      final Char finalTarget = target;
      final GrimTrap trap = this;
      MagicMissile.shadow(target.getSprite().parent, pos, target.getPos(), new Callback
              () {
        @Override
        public void call() {
          if (!finalTarget.isAlive()) return;
          if (finalTarget == Dungeon.hero) {
            //almost kill the player
            if (((float) finalTarget.getHP() / finalTarget.getHT()) >= 0.9f) {
              finalTarget.takeDamage(new Damage((finalTarget.getHP() - 1),
                      trap, finalTarget).addFeature(Damage.Feature.PURE));
              //kill 'em
            } else {
              finalTarget.takeDamage(new Damage((finalTarget.getHP()),
                      trap, finalTarget).addFeature(Damage.Feature.PURE));
            }
            Sample.INSTANCE.play(Assets.SND_CURSED);
            if (!finalTarget.isAlive()) {
              Dungeon.fail(GrimTrap.class);
              GLog.n(Messages.get(GrimTrap.class, "ondeath"));
            }
          } else {
            finalTarget.takeDamage(new Damage((finalTarget.getHP()),
                    trap, finalTarget).addFeature(Damage.Feature.PURE));
            Sample.INSTANCE.play(Assets.SND_BURNING);
          }
          finalTarget.getSprite().emitter().burst(ShadowParticle.UP, 10);
          if (!finalTarget.isAlive()) finalTarget.next();
        }
      });
    } else {
      CellEmitter.get(pos).burst(ShadowParticle.UP, 10);
      Sample.INSTANCE.play(Assets.SND_BURNING);
    }
  }
}
