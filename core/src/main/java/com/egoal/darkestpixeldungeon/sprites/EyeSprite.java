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
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.effects.Beam;
import com.egoal.darkestpixeldungeon.actors.mobs.Eye;
import com.egoal.darkestpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;

public class EyeSprite extends MobSprite {

  private int zapPos;

  private Animation charging;
  private Emitter chargeParticles;

  public EyeSprite() {
    super();

    texture(Assets.EYE);

    TextureFilm frames = new TextureFilm(texture, 16, 18);

    setIdle(new Animation(8, true));
    getIdle().frames(frames, 0, 1, 2);

    charging = new Animation(12, true);
    charging.frames(frames, 3, 4);

    chargeParticles = centerEmitter();
    chargeParticles.autoKill = false;
    chargeParticles.pour(MagicMissile.MagicParticle.ATTRACTING, 0.05f);
    chargeParticles.on = false;

    setRun(new Animation(12, true));
    getRun().frames(frames, 5, 6);

    setAttack(new Animation(8, false));
    getAttack().frames(frames, 4, 3);
    setZap(getAttack().clone());

    setDie(new Animation(8, false));
    getDie().frames(frames, 7, 8, 9);

    play(getIdle());
  }

  @Override
  public void link(Char ch) {
    super.link(ch);
    if (((Eye) ch).getBeamCharged()) play(charging);
  }

  @Override
  public void update() {
    super.update();
    chargeParticles.pos(center());
    chargeParticles.visible = visible;
  }

  public void charge(int pos) {
    turnTo(getCh().getPos(), pos);
    play(charging);
  }

  @Override
  public void play(Animation anim) {
    chargeParticles.on = anim == charging;
    super.play(anim);
  }

  @Override
  public void zap(int pos) {
    zapPos = pos;
    super.zap(pos);
  }

  @Override
  public void onComplete(Animation anim) {
    super.onComplete(anim);

    if (anim == getZap()) {
      if (Dungeon.visible[getCh().getPos()] || Dungeon.visible[zapPos]) {
        parent.add(new Beam.DeathRay(center(), DungeonTilemap
                .tileCenterToWorld(zapPos)));
      }
      ((Eye) getCh()).deathGaze();
      getCh().next();
    } else if (anim == getDie()) {
      chargeParticles.killAndErase();
    }
  }
}
