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

public class BanditSprite extends MobSprite {

  public BanditSprite() {
    super();

    texture(Assets.THIEF);
    TextureFilm film = new TextureFilm(texture, 12, 13);

    setIdle(new Animation(1, true));
    getIdle().frames(film, 21, 21, 21, 22, 21, 21, 21, 21, 22);

    setRun(new Animation(15, true));
    getRun().frames(film, 21, 21, 23, 24, 24, 25);

    setDie(new Animation(10, false));
    getDie().frames(film, 25, 27, 28, 29, 30);

    setAttack(new Animation(12, false));
    getAttack().frames(film, 31, 32, 33);

    idle();
  }
}
