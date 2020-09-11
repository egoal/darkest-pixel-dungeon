package com.egoal.darkestpixeldungeon.sprites;

import com.egoal.darkestpixeldungeon.Assets;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/**
 * Created by 93942 on 4/29/2018.
 */

public class ScholarSprite extends MobSprite {
  public ScholarSprite() {
    super();

    texture(Assets.SCHOLAR);

    // set animations
    TextureFilm frames = new TextureFilm(texture, 12, 15);
    setIdle(new MovieClip.Animation(2, true));
    getIdle().frames(frames, 0, 1, 2, 3);

    setRun(new MovieClip.Animation(20, true));
    getRun().frames(frames, 0);

    setDie(new MovieClip.Animation(20, true));
    getDie().frames(frames, 0);

    play(getIdle());
  }
}
