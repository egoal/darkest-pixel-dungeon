package com.egoal.darkestpixeldungeon.items.scrolls;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Invisibility;
import com.egoal.darkestpixeldungeon.actors.buffs.Light;
import com.egoal.darkestpixeldungeon.actors.buffs.Shock;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.ExpandHalo;
import com.egoal.darkestpixeldungeon.items.wands.WandOfBlastWave;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

/**
 * Created by 93942 on 10/15/2018.
 */

public class ScrollOfLight extends Scroll {
  {
    initials = 12;

    bones = true;
  }

  private static final int AFFECT_RANGE = 8;

  @Override
  protected void doRead() {

    // light!
    new ExpandHalo(8f, 48f).show(curUser.sprite, .5f);
    Sample.INSTANCE.play(Assets.SND_READ);
    Invisibility.dispel();

    // give light, shock nearby mobs
    Buff.prolong(curUser, Light.class, 10);

    for (Mob mob : Dungeon.level.mobs) {
      if (Level.fieldOfView[mob.pos]) {
        int dis = Dungeon.level.distance(mob.pos, curUser.pos);
        if (dis <= AFFECT_RANGE && mob.isAlive()) {
          // blind and stun back
          Buff.prolong(mob, Shock.class, AFFECT_RANGE - dis);

          Ballistica b = new Ballistica(curUser.pos, mob.pos, Ballistica
                  .MAGIC_BOLT);
          if (b.path.size() > b.dist + 1) {
            // extend the path...
            Ballistica bb = new Ballistica(mob.pos, b.path.get(b.dist + 1), 
                    Ballistica.MAGIC_BOLT);
            WandOfBlastWave.throwChar(mob, bb, AFFECT_RANGE - dis);
          }
        }
      }
    }

    GLog.i(Messages.get(this, "cast"));

    setKnown();
    readAnimation();
  }


}

