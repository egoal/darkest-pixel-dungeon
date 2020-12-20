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
package com.egoal.darkestpixeldungeon.effects;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PointF;

public class Swap extends Actor {

  private Char ch1;
  private Char ch2;

  private Effect eff1;
  private Effect eff2;

  private float delay;

  public Swap(Char ch1, Char ch2) {

    this.ch1 = ch1;
    this.ch2 = ch2;

    delay = Dungeon.INSTANCE.getLevel().distance(ch1.getPos(), ch2.getPos()) * 0.1f;

    eff1 = new Effect(ch1.getSprite(), ch1.getPos(), ch2.getPos());
    eff2 = new Effect(ch2.getSprite(), ch2.getPos(), ch1.getPos());
    Sample.INSTANCE.play(Assets.SND_TELEPORT);
  }

  @Override
  protected boolean act() {
    return false;
  }

  private void finish(Effect eff) {
    if (eff == eff1) {
      eff1 = null;
    }
    if (eff == eff2) {
      eff2 = null;
    }

    if (eff1 == null && eff2 == null) {
      Actor.Companion.remove(this);
      next();

      int pos = ch1.getPos();
      ch1.setPos(ch2.getPos());
      ch2.setPos(pos);

      if (!ch1.getFlying()) {
        if (ch1 instanceof Mob) {
          Dungeon.INSTANCE.getLevel().mobPress((Mob) ch1);
        } else {
          Dungeon.INSTANCE.getLevel().press(ch1.getPos(), ch1);
        }
      }
      if (!ch2.getFlying()) {
        if (ch2 instanceof Mob) {
          Dungeon.INSTANCE.getLevel().mobPress((Mob) ch2);
        } else {
          Dungeon.INSTANCE.getLevel().press(ch2.getPos(), ch2);
        }
      }

      if (ch1 == Dungeon.INSTANCE.getHero() || ch2 == Dungeon.INSTANCE.getHero()) {
        Dungeon.INSTANCE.observe();
        GameScene.updateFog();
      }
    }
  }

  private class Effect extends Visual {

    private CharSprite sprite;
    private PointF end;
    private float passed;

    public Effect(CharSprite sprite, int from, int to) {
      super(0, 0, 0, 0);

      this.sprite = sprite;

      point(sprite.worldToCamera(from));
      end = sprite.worldToCamera(to);

      speed.set(2 * (end.x - x) / delay, 2 * (end.y - y) / delay);
      acc.set(-speed.x / delay, -speed.y / delay);

      passed = 0;

      sprite.parent.add(this);
    }

    @Override
    public void update() {
      super.update();

      if ((passed += Game.elapsed) < delay) {
        sprite.x = x;
        sprite.y = y;

      } else {

        sprite.point(end);

        killAndErase();
        finish(this);

      }
    }
  }

}