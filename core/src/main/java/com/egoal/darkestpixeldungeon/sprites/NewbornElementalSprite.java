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
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class NewbornElementalSprite extends MobSprite {

  public NewbornElementalSprite() {
    super();

    texture(Assets.ELEMENTAL);

    int ofs = 21;

    TextureFilm frames = new TextureFilm(texture, 12, 14);

    setIdle(new MovieClip.Animation(10, true));
    getIdle().frames(frames, ofs + 0, ofs + 1, ofs + 2);

    setRun(new MovieClip.Animation(12, true));
    getRun().frames(frames, ofs + 0, ofs + 1, ofs + 3);

    setAttack(new MovieClip.Animation(15, false));
    getAttack().frames(frames, ofs + 4, ofs + 5, ofs + 6);

    setDie(new MovieClip.Animation(15, false));
    getDie().frames(frames, ofs + 7, ofs + 8, ofs + 9, ofs + 10, ofs + 11, ofs +
            12, ofs + 13, ofs + 12);

    play(getIdle());
  }

  @Override
  public void link(Char ch) {
    super.link(ch);
    add(CharSprite.State.BURNING);
  }

  @Override
  public void die() {
    super.die();
    remove(CharSprite.State.BURNING);
  }

  @Override
  public int blood() {
    return 0xFFFF7D13;
  }

}
