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
    idle = new MovieClip.Animation(2, true);
    idle.frames(frames, 0, 1, 2, 3);

    run = new MovieClip.Animation(20, true);
    run.frames(frames, 0);

    die = new MovieClip.Animation(20, true);
    die.frames(frames, 0);

    play(idle);
  }
}
