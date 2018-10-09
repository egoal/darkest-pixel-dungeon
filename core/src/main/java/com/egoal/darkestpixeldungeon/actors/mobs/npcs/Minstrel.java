package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.MobSprite;
import com.egoal.darkestpixeldungeon.windows.WndQuest;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

/**
 * Created by 93942 on 10/10/2018.
 */

public class Minstrel extends NPC{
  {
    spriteClass = MinstrelSprite.class;
  }

  @Override
  public boolean interact() {
    sprite.turnTo(pos, Dungeon.hero.pos);

    tell(Messages.get(this, "poetry_away"));
    
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

  public static class MinstrelSprite extends MobSprite{
    
    public MinstrelSprite(){
      super();

      texture(Assets.DPD_MINSTREL);

      // set animations
      TextureFilm frames = new TextureFilm(texture, 12, 15);
      idle = new MovieClip.Animation(1, true);
      idle.frames(frames, 0, 1);

      run = new MovieClip.Animation(20, true);
      run.frames(frames, 0);

      die = new MovieClip.Animation(20, true);
      die.frames(frames, 0);

      play(idle);
    }
    
  }
}
