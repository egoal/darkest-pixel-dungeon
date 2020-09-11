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
import com.egoal.darkestpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class BurningFistSprite extends MobSprite {

  public BurningFistSprite() {
    super();

    texture(Assets.BURNING);

    TextureFilm frames = new TextureFilm(texture, 24, 17);

    setIdle(new Animation(2, true));
    getIdle().frames(frames, 0, 0, 1);

    setRun(new Animation(3, true));
    getRun().frames(frames, 0, 1);

    setAttack(new Animation(8, false));
    getAttack().frames(frames, 0, 5, 6);

    setDie(new Animation(10, false));
    getDie().frames(frames, 0, 2, 3, 4);

    play(getIdle());
  }

  private int posToShoot;

  @Override
  public void attack(int cell) {
    posToShoot = cell;
    super.attack(cell);
  }

  @Override
  public void onComplete(Animation anim) {
    if (anim == getAttack()) {

      Sample.INSTANCE.play(Assets.SND_ZAP);
      MagicMissile.shadow(parent, getCh().getPos(), posToShoot,
              new Callback() {
                @Override
                public void call() {
                  getCh().onAttackComplete();
                }
              });

      idle();

    } else {
      super.onComplete(anim);
    }
  }
}
