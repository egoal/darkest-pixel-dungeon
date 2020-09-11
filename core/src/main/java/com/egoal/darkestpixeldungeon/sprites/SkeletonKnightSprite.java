package com.egoal.darkestpixeldungeon.sprites;

import com.egoal.darkestpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

/**
 * Created by 93942 on 5/13/2018.
 */

public class SkeletonKnightSprite extends MobSprite {

  public SkeletonKnightSprite() {
    super();

    texture(Assets.SKELETON_KNIGHT);

    TextureFilm frames = new TextureFilm(texture, 12, 15);

    setIdle(new Animation(12, true));
    getIdle().frames(frames, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3);

    setRun(new Animation(15, true));
    getRun().frames(frames, 4, 5, 6, 7, 8, 9);

    setAttack(new Animation(15, false));
    getAttack().frames(frames, 14, 15, 16);

    setDie(new Animation(12, false));
    getDie().frames(frames, 10, 11, 12, 13);

    play(getIdle());
  }

  @Override
  public void die() {
    super.die();
  }

  @Override
  public int blood() {
    return 0xFFcccccc;
  }
}
