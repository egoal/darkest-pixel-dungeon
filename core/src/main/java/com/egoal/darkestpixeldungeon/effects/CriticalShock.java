package com.egoal.darkestpixeldungeon.effects;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

/**
 * Created by 93942 on 10/27/2018.
 */

public class CriticalShock extends Image {
  private static final float MOVE_OUT_DONE = .1f;
  private static final float STATIC_DONE = .6f;
  private static final float FADE_OUT_DONE = 1f;

  private static final float MOVE_OFFSET = 8f;
  private static final float SCALE_OFFSET = .25f;
  private static final float INIT_ALPHA = .8f;

  private Char target;

  private int color;
  private float passed;
  private float dx, dy; // moving direction
  private float strength; // how strong the effect is.

  public CriticalShock(Char c) {
    target = c;

    // copy image.
    flipHorizontal = c.sprite.flipHorizontal;
    flipVertical = c.sprite.flipVertical;
    copy(c.sprite);
    origin.set(c.sprite.width / 2f, c.sprite.height / 2);

    color = c.sprite.blood();
    passed = 0;

    tint(color, .5f);
    alpha(INIT_ALPHA);
  }

  public CriticalShock set(float dir, float str) {
    dx = (float) Math.cos(dir);
    dy = (float) Math.sin(dir);
    strength = str;

    return this;
  }

  @Override
  public void update() {
    super.update();

    passed += Game.elapsed;

    if (passed < MOVE_OUT_DONE) {
      float pct = passed / MOVE_OUT_DONE;
      float ds = (MOVE_OFFSET * strength) * pct;
      x = target.sprite.x + ds * dx;
      y = target.sprite.y + ds * dy;
      scale.set(1f + (SCALE_OFFSET * strength) * pct);
      alpha(INIT_ALPHA * pct);
    } else if (passed < STATIC_DONE) {
    } else if (passed < FADE_OUT_DONE) {
      alpha((1 - (passed - STATIC_DONE) / (FADE_OUT_DONE - STATIC_DONE)) *
              INIT_ALPHA);
    } else {
      killAndErase();
    }
  }

  public static void show(Char ch, float dir, float power) {
    if (!ch.sprite.visible || ch.sprite.parent == null) return; // already removed.

    CriticalShock cs = new CriticalShock(ch).set(dir, power);

    ch.sprite.parent.addToBack(cs);
  }
}
