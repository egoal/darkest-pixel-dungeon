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

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Shuriken;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.Callback;

public class TenguSprite extends MobSprite {

  private Animation cast;

  public TenguSprite() {
    super();

    texture(Assets.TENGU);

    TextureFilm frames = new TextureFilm(texture, 14, 16);

    setIdle(new Animation(2, true));
    getIdle().frames(frames, 0, 0, 0, 1);

    setRun(new Animation(15, false));
    getRun().frames(frames, 2, 3, 4, 5, 0);

    setAttack(new Animation(15, false));
    getAttack().frames(frames, 6, 7, 7, 0);

    cast = getAttack().clone();

    setDie(new Animation(8, false));
    getDie().frames(frames, 8, 9, 10, 10, 10, 10, 10, 10);

    play(getRun().clone());
  }

  @Override
  public void move(int from, int to) {

    place(to);

    play(getRun());
    turnTo(from, to);

    setMoving(true);

    if (Level.Companion.getWater()[to]) {
      GameScene.ripple(to);
    }

  }

  @Override
  public void attack(int cell) {
    if (!Dungeon.INSTANCE.getLevel().adjacent(cell, getCh().getPos())) {

      final Char enemy = Actor.Companion.findChar(cell);

      ((MissileSprite) parent.recycle(MissileSprite.class)).
              reset(getCh().getPos(), cell, new Shuriken(), new Callback() {
                @Override
                public void call() {
                  getCh().next();
                  if (enemy != null) getCh().attack(enemy);
                }
              });

      play(cast);
      turnTo(getCh().getPos(), cell);

    } else {

      super.attack(cell);

    }
  }

  @Override
  public void onComplete(Animation anim) {
    if (anim == getRun()) {
      synchronized (this) {
        setMoving(false);
        idle();

        notifyAll();
      }
    } else {
      super.onComplete(anim);
    }
  }
  
  public static class Phantom extends TenguSprite{
    public Phantom() {
      super();

      TextureFilm frames = new TextureFilm(texture, 14, 16);

      // reverse run...
      setDie(new Animation(15, false));
      getDie().frames(frames, 0, 5, 4, 3, 2);

      play(getRun().clone());
    }
  }
}
