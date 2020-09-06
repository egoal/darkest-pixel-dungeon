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
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Sheep;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.TrapSprite;
import com.egoal.darkestpixeldungeon.utils.BArray;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class FlockTrap extends Trap {

  {
    color = TrapSprite.WHITE;
    shape = TrapSprite.WAVES;
  }


  @Override
  public void activate() {
    ActivateAt(pos);
  }

  public static void ActivateAt(final int pos) {
    //use an actor as we want to put this on a slight delay so all chars get 
    // a chance to act this turn first.
    Actor.Companion.add(new Actor() {

      {
        setActPriority(3);
      }

      protected boolean act() {
        PathFinder.buildDistanceMap(pos, BArray.not(Level.Companion.getSolid(), null), 2);
        for (int i = 0; i < PathFinder.distance.length; i++) {
          if (PathFinder.distance[i] < Integer.MAX_VALUE)
            if (Dungeon.level.insideMap(i) && Actor.Companion.findChar(i) == null && !
                    (Level.Companion.getPit()[i])) {
              Sheep sheep = new Sheep();
              sheep.setLifespan(2 + Random.Int(Dungeon.depth + 10));
              sheep.setPos(i);
              GameScene.add(sheep);
              CellEmitter.get(i).burst(Speck.factory(Speck.WOOL), 4);
            }
        }
        Sample.INSTANCE.play(Assets.SND_PUFF);
        Actor.Companion.remove(this);
        return true;
      }
    });
  }
}
