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
package com.egoal.darkestpixeldungeon.sprites;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.watabou.noosa.TextureFilm;

public class ElementalSprite extends MobSprite {

  public ElementalSprite() {
    super();

    texture(Assets.ELEMENTAL);

    TextureFilm frames = new TextureFilm(texture, 12, 14);

    setIdle(new Animation(10, true));
    getIdle().frames(frames, 0, 1, 2);

    setRun(new Animation(12, true));
    getRun().frames(frames, 0, 1, 3);

    setAttack(new Animation(15, false));
    getAttack().frames(frames, 4, 5, 6);

    setDie(new Animation(15, false));
    getDie().frames(frames, 7, 8, 9, 10, 11, 12, 13, 12);

    play(getIdle());
  }

  @Override
  public void link(Char ch) {
    super.link(ch);
    add(State.BURNING);
  }

  @Override
  public void die() {
    super.die();
    remove(State.BURNING);
  }

  @Override
  public int blood() {
    return 0xFFFF7D13;
  }
}
