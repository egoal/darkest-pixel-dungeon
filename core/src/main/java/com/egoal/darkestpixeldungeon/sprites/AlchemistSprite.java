package com.egoal.darkestpixeldungeon.sprites;

import com.egoal.darkestpixeldungeon.Assets;
import com.watabou.noosa.TextureFilm;

public class AlchemistSprite extends MobSprite {

  public AlchemistSprite() {
    super();

    texture(Assets.ALCHEMIST);

    TextureFilm frames = new TextureFilm(texture, 12, 14);   // width & height

    setIdle(new Animation(5, true));
    getIdle().frames(frames, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4,
            1, 2, 3, 4, 4, 4);

    setRun(new Animation(20, true));
    getRun().frames(frames, 0);

    setDie(new Animation(20, false));
    getDie().frames(frames, 0);

    play(getIdle());
  }
}
