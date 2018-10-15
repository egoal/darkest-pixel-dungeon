package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.Statistics;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.MobSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndOptions;
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

    GameScene.show(new WndOptions(new MinstrelSprite(), name,  
            Messages.get(Minstrel.class, "hello"),
            Messages.get(Minstrel.class, "ac_sing"),
            Messages.get(Minstrel.class, "ac_yourself"),
            Messages.get(Minstrel.class, "ac_leave")){
      @Override
      protected void onSelect(int index){
        onSelectHello(index);
      }
    });
    
    return false;
  }

  // unbreakable
  @Override
  public boolean reset() {
    return true;
  }

  @Override
  protected boolean act() {
    // leave after some time
    if(Statistics.duration>1200f){
      die(null);
    }
    
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

  private void onSelectHello(int index){
    // 0 sing, 1 leave
    switch (index){
      case 0:
        final String[] poetries = new String[]{"away"};
        GameScene.show(new WndOptions(new MinstrelSprite(), name, 
                Messages.get(Minstrel.class, "select_poetry"),
                poetries){
          @Override
          protected void onSelect(int index){
            tell(Messages.get(Minstrel.class, "poetry_"+poetries[index]));
          }
        });
        break;
      case 1:
        tell(Messages.get(Minstrel.class, "introduction"));
        break;
      case 2:
        GLog.p(Messages.get(Minstrel.class, "farewell"));
        break;
    }
  }
  
  public static class MinstrelSprite extends MobSprite{
    
    public MinstrelSprite(){
      super();

      texture(Assets.MINSTREL);

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
