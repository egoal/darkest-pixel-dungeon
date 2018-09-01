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
package com.egoal.darkestpixeldungeon.items.quest;

import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.actors.mobs.Wraith;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CorpseDust extends Item {

  {
    image = ItemSpriteSheet.DUST;

    cursed = true;
    cursedKnown = true;

    unique = true;
  }

  @Override
  public ArrayList<String> actions(Hero hero) {
    return new ArrayList<>(); //yup, no dropping this one
  }

  @Override
  public boolean isUpgradable() {
    return false;
  }

  @Override
  public boolean isIdentified() {
    return true;
  }

  @Override
  public boolean doPickUp(Hero hero) {
    if (super.doPickUp(hero)) {
      GLog.n("You feel a shiver run down your spine.");
      Buff.affect(hero, DustGhostSpawner.class);
      return true;
    }
    return false;
  }

  @Override
  protected void onDetach() {
    DustGhostSpawner spawner = Dungeon.hero.buff(DustGhostSpawner.class);
    if (spawner != null) {
      spawner.dispel();
    }
  }

  public static class DustGhostSpawner extends Buff {

    int spawnPower = 0;

    @Override
    public boolean act() {
      spawnPower++;
      int wraiths = 1; //we include the wraith we're trying to spawn
      for (Mob mob : Dungeon.level.mobs) {
        if (mob instanceof Wraith) {
          wraiths++;
        }
      }

      int powerNeeded = Math.min(25, wraiths * wraiths);

      if (powerNeeded <= spawnPower) {
        spawnPower -= powerNeeded;
        int pos = 0;
        do {
          pos = Random.Int(Dungeon.level.length());
        }
        while (!Dungeon.visible[pos] || !Level.passable[pos] || Actor
                .findChar(pos) != null);
        Wraith.spawnAt(pos);
        Sample.INSTANCE.play(Assets.SND_CURSED);
      }

      spend(TICK);
      return true;
    }

    public void dispel() {
      detach();
      for (Mob mob : Dungeon.level.mobs.toArray(new Mob[0])) {
        if (mob instanceof Wraith) {
          mob.die(null);
        }
      }
    }

    private static String SPAWNPOWER = "spawnpower";

    @Override
    public void storeInBundle(Bundle bundle) {
      super.storeInBundle(bundle);
      bundle.put(SPAWNPOWER, spawnPower);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
      super.restoreFromBundle(bundle);
      spawnPower = bundle.getInt(SPAWNPOWER);
    }
  }

}
