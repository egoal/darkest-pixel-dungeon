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
import com.watabou.utils.Random;

public class MonkSprite extends MobSprite {

  private Animation kick;

  public MonkSprite() {
    super();

    texture(Assets.MONK);

    TextureFilm frames = new TextureFilm(texture, 15, 14);

    setIdle(new Animation(6, true));
    getIdle().frames(frames, 1, 0, 1, 2);

    setRun(new Animation(15, true));
    getRun().frames(frames, 11, 12, 13, 14, 15, 16);

    setAttack(new Animation(12, false));
    getAttack().frames(frames, 3, 4, 3, 4);

    kick = new Animation(10, false);
    kick.frames(frames, 5, 6, 5);

    setDie(new Animation(15, false));
    getDie().frames(frames, 1, 7, 8, 8, 9, 10);

    play(getIdle());
  }

  @Override
  public void attack(int cell) {
    super.attack(cell);
    if (Random.Float() < 0.5f) {
      play(kick);
    }
  }

  @Override
  public void onComplete(Animation anim) {
    super.onComplete(anim == kick ? getAttack() : anim);
  }
}
