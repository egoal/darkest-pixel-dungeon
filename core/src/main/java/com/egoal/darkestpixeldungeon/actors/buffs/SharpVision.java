package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;

/**
 * Created by 93942 on 10/30/2018.
 */

public class SharpVision extends FlavourBuff{
  public static final float DURATION = 50f;
  
  {
    type = buffType.POSITIVE;
  }

  @Override
  public int icon() {
    return BuffIndicator.MIND_VISION;
  }

  @Override
  public String toString() {
    return Messages.get(this, "name");
  }

  @Override
  public void detach() {
    super.detach();
    Dungeon.observe();
    GameScene.updateFog();
  }

  @Override
  public String desc() {
    return Messages.get(this, "desc", dispTurns());
  }
}
