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
import com.egoal.darkestpixeldungeon.actors.mobs.Warlock;
import com.egoal.darkestpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class WarlockSprite extends MobSprite {

  public WarlockSprite() {
    super();

    texture(Assets.WARLOCK);

    TextureFilm frames = new TextureFilm(texture, 12, 15);

    setIdle(new Animation(2, true));
    getIdle().frames(frames, 0, 0, 0, 1, 0, 0, 1, 1);

    setRun(new Animation(15, true));
    getRun().frames(frames, 0, 2, 3, 4);

    setAttack(new Animation(12, false));
    getAttack().frames(frames, 0, 5, 6);

    setZap(getAttack().clone());

    setDie(new Animation(15, false));
    getDie().frames(frames, 0, 7, 8, 8, 9, 10);

    play(getIdle());
  }

  public void zap(int cell) {

    turnTo(getCh().getPos(), cell);
    play(getZap());

    MagicMissile.shadow(parent, getCh().getPos(), cell,
            new Callback() {
              @Override
              public void call() {
                ((Warlock) getCh()).onZapComplete();
              }
            });
    Sample.INSTANCE.play(Assets.SND_ZAP);
  }

  @Override
  public void onComplete(Animation anim) {
    if (anim == getZap()) {
      idle();
    }
    super.onComplete(anim);
  }
}
