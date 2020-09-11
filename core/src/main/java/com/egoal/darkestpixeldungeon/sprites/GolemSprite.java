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
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle;
import com.watabou.noosa.TextureFilm;

public class GolemSprite extends MobSprite {

  public GolemSprite() {
    super();

    texture(Assets.GOLEM);

    TextureFilm frames = new TextureFilm(texture, 16, 16);

    setIdle(new Animation(4, true));
    getIdle().frames(frames, 0, 1);

    setRun(new Animation(12, true));
    getRun().frames(frames, 2, 3, 4, 5);

    setAttack(new Animation(10, false));
    getAttack().frames(frames, 6, 7, 8);

    setDie(new Animation(15, false));
    getDie().frames(frames, 9, 10, 11, 12, 13);

    play(getIdle());
  }

  @Override
  public int blood() {
    return 0xFF80706c;
  }

  @Override
  public void onComplete(Animation anim) {
    if (anim == getDie()) {
      emitter().burst(ElmoParticle.FACTORY, 4);
    }
    super.onComplete(anim);
  }
}
