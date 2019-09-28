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
package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.ImpSprite;

public class ImpShopkeeper extends Shopkeeper {

  {
    spriteClass = ImpSprite.class;
  }

  private boolean seenBefore = false;

  @Override
  protected boolean act() {

    if (!seenBefore && Dungeon.visible[pos]) {
      yell(Messages.get(this, "greetings", Dungeon.hero.givenName()));
      seenBefore = true;
    }

    return super.act();
  }

  @Override
  public void flee() {
    for (Heap heap : Dungeon.level.getHeaps().values()) {
      if (heap.type == Heap.Type.FOR_SALE) {
        CellEmitter.get(heap.pos).burst(ElmoParticle.FACTORY, 4);
        heap.destroy();
      }
    }

    destroy();

    sprite.emitter().burst(Speck.factory(Speck.WOOL), 15);
    sprite.killAndErase();
  }
}
