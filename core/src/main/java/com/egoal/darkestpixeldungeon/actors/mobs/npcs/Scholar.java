package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ScholarSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndQuest;

/**
 * Created by 93942 on 4/29/2018.
 */

public class Scholar extends NPC {

  {
    name = Messages.get(this, "name");
    spriteClass = ScholarSprite.class;
  }

  /// do something
  @Override
  public boolean interact() {
    sprite.turnTo(pos, Dungeon.hero.pos);
    tell(Messages.get(this, "hello"));

    return false;
  }

  // unbreakable
  @Override
  public boolean reset() {
    return true;
  }

  @Override
  protected boolean act() {
    throwItem();
    return super.act();
  }

  @Override
  public int defenseSkill(Char enemy) {
    return 1000;
  }

  @Override
  public int takeDamage(Damage dmg) {
    return 0;
  }

  @Override
  public void add(Buff buff) {
  }

  private void tell(String text) {
    GameScene.show(new WndQuest(this, text));
  }

  // others


}
