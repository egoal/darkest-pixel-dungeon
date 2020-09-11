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
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public class GreatCrabSprite extends MobSprite {

  public GreatCrabSprite() {
    super();

    texture(Assets.CRAB);

    TextureFilm frames = new TextureFilm(texture, 16, 16);

    setIdle(new MovieClip.Animation(5, true));
    getIdle().frames(frames, 16, 17, 16, 18);

    setRun(new MovieClip.Animation(10, true));
    getRun().frames(frames, 19, 20, 21, 22);

    setAttack(new MovieClip.Animation(12, false));
    getAttack().frames(frames, 23, 24, 25);

    setDie(new MovieClip.Animation(12, false));
    getDie().frames(frames, 26, 27, 28, 29);

    play(getIdle());
  }

  @Override
  public int blood() {
    return 0xFFFFEA80;
  }
}
