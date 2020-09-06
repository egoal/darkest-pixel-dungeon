/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.levels.traps;

import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.actors.mobs.Statue;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.StatueSprite;
import com.egoal.darkestpixeldungeon.sprites.TrapSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class GuardianTrap extends Trap {

  {
    color = TrapSprite.RED;
    shape = TrapSprite.STARS;
  }

  @Override
  public void activate() {

    for (Mob mob : Dungeon.level.getMobs()) {
      mob.beckon(pos);
    }

    if (Dungeon.visible[pos]) {
      GLog.w(Messages.get(this, "alarm"));
      CellEmitter.center(pos).start(Speck.factory(Speck.SCREAM), 0.3f, 3);
    }

    Sample.INSTANCE.play(Assets.SND_ALERT);

    for (int i = 0; i < (Dungeon.depth - 5) / 5; i++) {
      Guardian guardian = new Guardian();
      guardian.state = guardian.WANDERING;
      guardian.setPos(Dungeon.level.randomRespawnCell());
      GameScene.add(guardian);
      guardian.beckon(Dungeon.hero.getPos());
    }

  }

  public static class Guardian extends Statue {

    {
      spriteClass = GuardianSprite.class;

      EXP = 0;
      state = WANDERING;
    }

    public Guardian() {
      super();

      getWeapon().enchant(null);
      getWeapon().degrade(getWeapon().level());
    }

    @Override
    public void beckon(int cell) {
      //Beckon works on these ones, unlike their superclass.
      notice();

      if (state != HUNTING) {
        state = WANDERING;
      }
      target = cell;
    }

  }

  public static class GuardianSprite extends StatueSprite {

    public GuardianSprite() {
      super();
      tint(0, 0, 1, 0.2f);
    }

    @Override
    public void resetColor() {
      super.resetColor();
      tint(0, 0, 1, 0.2f);
    }
  }
}
