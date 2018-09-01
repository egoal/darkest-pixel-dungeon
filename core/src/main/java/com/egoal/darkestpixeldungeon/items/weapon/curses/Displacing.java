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
package com.egoal.darkestpixeldungeon.items.weapon.curses;

import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.watabou.utils.Random;

public class Displacing extends Weapon.Enchantment {

  private static ItemSprite.Glowing BLACK = new ItemSprite.Glowing(0x000000);

  @Override
  public Damage proc(Weapon weapon, Damage damage) {
    Char defender = (Char) damage.to;
    Char attacker = (Char) damage.from;

    if (Random.Int(12) == 0 && !defender.properties().contains(Char.Property
            .IMMOVABLE)) {
      int count = 10;
      int newPos;
      do {
        newPos = Dungeon.level.randomRespawnCell();
        if (count-- <= 0) {
          break;
        }
      } while (newPos == -1);

      if (newPos != -1 && !Dungeon.bossLevel()) {

        if (Dungeon.visible[defender.pos]) {
          CellEmitter.get(defender.pos).start(Speck.factory(Speck.LIGHT), 
                  0.2f, 3);
        }

        defender.pos = newPos;
        defender.sprite.place(defender.pos);
        defender.sprite.visible = Dungeon.visible[defender.pos];

        damage.value = 0;
        return damage;

      }
    }

    return damage;
  }

  @Override
  public boolean curse() {
    return true;
  }

  @Override
  public ItemSprite.Glowing glowing() {
    return BLACK;
  }

}
