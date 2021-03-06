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

public class KingSprite extends MobSprite {

  public KingSprite() {
    super();

    texture(Assets.KING);

    TextureFilm frames = new TextureFilm(texture, 16, 16);

    setIdle(new Animation(12, true));
    getIdle().frames(frames, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2);

    setRun(new Animation(15, true));
    getRun().frames(frames, 3, 4, 5, 6, 7, 8);

    setAttack(new Animation(15, false));
    getAttack().frames(frames, 9, 10, 11);

    setDie(new Animation(8, false));
    getDie().frames(frames, 12, 13, 14, 15);

    play(getIdle());
  }
}
