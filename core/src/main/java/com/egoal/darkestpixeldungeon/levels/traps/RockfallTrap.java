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
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.TrapSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.security.cert.TrustAnchor;

public class RockfallTrap extends Trap {

  {
    color = TrapSprite.GREY;
    shape = TrapSprite.DIAMOND;
  }

  @Override
  public void activate() {
    fallRocks(pos);
  }

  public static void fallRocks(int pos) {
    boolean seen = false;

    for (int i : PathFinder.NEIGHBOURS9) {

      if (Level.Companion.getSolid()[pos + i])
        continue;

      if (Dungeon.visible[pos + i]) {
        CellEmitter.get(pos + i - Dungeon.level.width()).start(Speck.factory
                (Speck.ROCK), 0.07f, 10);
        if (!seen) {
          Camera.main.shake(3, 0.7f);
          Sample.INSTANCE.play(Assets.SND_ROCKS);
          seen = true;
        }
      }

      Char ch = Actor.Companion.findChar(pos + i);

      if (ch != null) {
        int damage = Random.NormalIntRange(Dungeon.depth, Dungeon.depth * 2);
        ch.takeDamage(ch.defendDamage(new Damage(damage, new RockfallTrap(), 
                ch)));

        Buff.Companion.prolong(ch, Paralysis.class, Paralysis.duration(ch) / 2);

        if (!ch.isAlive() && ch == Dungeon.hero) {
          Dungeon.fail(RockfallTrap.class);
          GLog.n(Messages.get(RockfallTrap.class, "ondeath"));
        }
      }
    }
    
    for(Mob mob: Dungeon.level.getMobs().toArray(new Mob[0])){
      if(Dungeon.level.distance(mob.getPos(), pos)< 12)
        mob.beckon(pos);
    }
    GLog.n(Messages.get(RockfallTrap.class, "roar"));
  }
}
