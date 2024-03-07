package com.egoal.darkestpixeldungeon.effects;

import com.watabou.noosa.Game;
import com.watabou.noosa.Visual;

/**
 * Created by 93942 on 10/20/2018.
 */

public class ExpandHalo extends Halo {
  private float lifespan, duration = 0f;
  private Visual target;
  private final float minRadius, maxRadius;

  public ExpandHalo(float rmin, float rmax, int color) {
    super(rmin, color, 1f);

    minRadius = rmin;
    maxRadius = rmax;
  }

  public ExpandHalo show(Visual visual, float duration) {
    target = visual;
    visual.parent.addToBack(this);

    lifespan = this.duration = duration;

    return this;
  }

  @Override
  public void update() {
    super.update();

    if (duration > 0f) {
      if ((lifespan -= Game.elapsed) > 0f) {
        float passed = 1 - lifespan / duration;
        float r = (minRadius - maxRadius) * (passed - 1) * (passed - 1) +
                maxRadius;

        radius(r);
        alpha(1-passed);
      } else {
        killAndErase();
      }

      point(target.x + target.width / 2, target.y + target.height / 2);
    }
  }

}
