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
import com.watabou.noosa.TextureFilm;

public class AcidicSprite extends ScorpioSprite {

  public AcidicSprite() {
    super();

    texture(Assets.SCORPIO);

    TextureFilm frames = new TextureFilm(texture, 18, 17);

    setIdle(new Animation(12, true));
    getIdle().frames(frames, 14, 14, 14, 14, 14, 14, 14, 14, 15, 16, 15, 16, 15, 16);

    setRun(new Animation(4, true));
    getRun().frames(frames, 19, 20);

    setAttack(new Animation(15, false));
    getAttack().frames(frames, 14, 17, 18);

    setZap(getAttack().clone());

    setDie(new Animation(12, false));
    getDie().frames(frames, 14, 21, 22, 23, 24);

    play(getIdle());
  }

  @Override
  public int blood() {
    return 0xFF66FF22;
  }
}
