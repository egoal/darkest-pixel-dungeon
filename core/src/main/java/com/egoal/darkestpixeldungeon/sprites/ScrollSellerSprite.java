package com.egoal.darkestpixeldungeon.sprites;

import com.egoal.darkestpixeldungeon.Assets;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/**
 * Created by 93942 on 9/1/2018.
 */

public class ScrollSellerSprite extends MobSprite {
  public ScrollSellerSprite() {
    super();

    texture(Assets.SCROLL_SELLER);

    // set animations
    TextureFilm frames = new TextureFilm(texture, 12, 15);
    idle = new MovieClip.Animation(1, true);
    idle.frames(frames, 0, 1);

    die = new MovieClip.Animation(20, false);
    die.frames(frames, 0);

    run = idle.clone();
    attack = idle.clone();

    play(idle);
  }
}
