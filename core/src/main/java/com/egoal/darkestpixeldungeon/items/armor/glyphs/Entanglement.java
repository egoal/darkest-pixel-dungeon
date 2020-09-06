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
package com.egoal.darkestpixeldungeon.items.armor.glyphs;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.items.armor.Armor;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.actors.buffs.Roots;
import com.egoal.darkestpixeldungeon.effects.particles.EarthParticle;
import com.egoal.darkestpixeldungeon.plants.Earthroot;
import com.watabou.noosa.Camera;
import com.watabou.utils.Random;

public class Entanglement extends Armor.Glyph {

  private static ItemSprite.Glowing BROWN = new ItemSprite.Glowing(0x663300);

  @Override
  public Damage proc(Armor armor, Damage damage) {
    Char attacker = (Char) damage.from;
    Char defender = (Char) damage.to;

    int level = Math.max(0, armor.level());

    if (Random.Int(3) == 0) {

      Buff.prolong(defender, Roots.class, 5);
      Buff.affect(defender, Earthroot.Armor.class).level(5 + level);
      CellEmitter.bottom(defender.getPos()).start(EarthParticle.FACTORY, 0.05f, 8);
      Camera.main.shake(1, 0.4f);

    }

    return damage;
  }

  @Override
  public ItemSprite.Glowing glowing() {
    return BROWN;
  }

}
