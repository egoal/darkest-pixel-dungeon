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
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.watabou.noosa.TextureFilm;

public class PiranhaSprite extends MobSprite {

  public PiranhaSprite() {
    super();

    texture(Assets.PIRANHA);

    TextureFilm frames = new TextureFilm(texture, 12, 16);

    setIdle(new Animation(8, true));
    getIdle().frames(frames, 0, 1, 2, 1);

    setRun(new Animation(20, true));
    getRun().frames(frames, 0, 1, 2, 1);

    setAttack(new Animation(20, false));
    getAttack().frames(frames, 3, 4, 5, 6, 7, 8, 9, 10, 11);

    setDie(new Animation(4, false));
    getDie().frames(frames, 12, 13, 14);

    play(getIdle());
  }

  @Override
  public void onComplete(Animation anim) {
    super.onComplete(anim);

    if (anim == getAttack()) {
      GameScene.ripple(getCh().getPos());
    }
  }
}
