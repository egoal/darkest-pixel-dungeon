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

import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.effects.Lightning;
import com.egoal.darkestpixeldungeon.effects.particles.SparkParticle;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.wands.Wand;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.TrapSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class LightningTrap extends Trap {

  {
    color = TrapSprite.TEAL;
    shape = TrapSprite.CROSSHAIR;
  }

  @Override
  public void activate() {

    Char ch = Actor.findChar(pos);

    if (ch != null) {
      ch.takeDamage(new Damage(Math.max(1, Random.Int(ch.HP / 3, 2 * ch.HP / 
              3)), this, ch).type(Damage.Type.MAGICAL).addElement(Damage.Element.LIGHT));
      
      if (ch == Dungeon.hero) {

        Camera.main.shake(2, 0.3f);

        if (!ch.isAlive()) {
          Dungeon.fail(getClass());
          GLog.n(Messages.get(this, "ondeath"));
        }
      }

      ArrayList<Lightning.Arc> arcs = new ArrayList<>();
      arcs.add(new Lightning.Arc(pos - Dungeon.level.width(), pos + Dungeon
              .level.width()));
      arcs.add(new Lightning.Arc(pos - 1, pos + 1));

      ch.sprite.parent.add(new Lightning(arcs, null));
    }

    Heap heap = Dungeon.level.getHeaps().get(pos);
    if (heap != null) {
      //TODO: this should probably charge staffs too
      Item item = heap.items.peek();
      if (item instanceof Wand) {
        Wand wand = (Wand) item;
        ((Wand) item).curCharges += (int) Math.ceil((wand.maxCharges - wand
                .curCharges) / 2f);
      }
    }

    CellEmitter.center(pos).burst(SparkParticle.FACTORY, Random.IntRange(3, 4));
  }
}
