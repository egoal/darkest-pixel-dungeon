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

import com.egoal.darkestpixeldungeon.items.weapon.missiles.CurareDart;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

public class GnollTricksterSprite extends MobSprite {

  private Animation cast;

  public GnollTricksterSprite() {
    super();

    texture(Assets.GNOLL);

    TextureFilm frames = new TextureFilm(texture, 12, 15);

    setIdle(new MovieClip.Animation(2, true));
    getIdle().frames(frames, 21, 21, 21, 22, 21, 21, 22, 22);

    setRun(new MovieClip.Animation(12, true));
    getRun().frames(frames, 25, 26, 27, 28);

    setAttack(new MovieClip.Animation(12, false));
    getAttack().frames(frames, 23, 24, 21);

    cast = getAttack().clone();

    setDie(new MovieClip.Animation(12, false));
    getDie().frames(frames, 29, 30, 31);

    play(getIdle());
  }

  @Override
  public void attack(int cell) {
    if (!Dungeon.INSTANCE.getLevel().adjacent(cell, getCh().getPos())) {

      ((MissileSprite) parent.recycle(MissileSprite.class)).
              reset(getCh().getPos(), cell, new CurareDart(), new Callback() {
                @Override
                public void call() {
                  getCh().onAttackComplete();
                }
              });

      play(cast);
      turnTo(getCh().getPos(), cell);

    } else {

      super.attack(cell);

    }
  }
}
