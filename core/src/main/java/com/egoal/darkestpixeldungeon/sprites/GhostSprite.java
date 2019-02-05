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

import android.opengl.GLES20;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.GhostHero;
import com.egoal.darkestpixeldungeon.effects.MagicMissile;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.effects.particles.ShaftParticle;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;

import javax.microedition.khronos.opengles.GL10;

public class GhostSprite extends MobSprite {

  public GhostSprite() {
    super();

    texture(Assets.GHOST);

    TextureFilm frames = new TextureFilm(texture, 14, 15);

    idle = new Animation(5, true);
    idle.frames(frames, 0, 1);

    run = new Animation(10, true);
    run.frames(frames, 0, 1);

    attack = new Animation(10, false);
    attack.frames(frames, 0, 2, 3);

    zap = attack.clone();

    die = new Animation(8, false);
    die.frames(frames, 0, 4, 5, 6, 7);

    play(idle);
  }

  @Override
  public void draw() {
    GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
    super.draw();
    GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
  }

  @Override
  public void die() {
    super.die();
    emitter().start(ShaftParticle.FACTORY, 0.3f, 4);
    emitter().start(Speck.factory(Speck.LIGHT), 0.2f, 3);
  }

  @Override
  public int blood() {
    return 0xFFFFFF;
  }

  @Override
  public void zap(int cell) {
    turnTo(ch.pos, cell);
    play(zap);

    MagicMissile.whiteLight(parent, ch.pos, cell, (GhostHero)ch);
    Sample.INSTANCE.play(Assets.SND_ZAP);
  }

  @Override
  public void onComplete(Animation anim) {
    if (anim == zap)
      idle();
    super.onComplete(anim);
  }
}
