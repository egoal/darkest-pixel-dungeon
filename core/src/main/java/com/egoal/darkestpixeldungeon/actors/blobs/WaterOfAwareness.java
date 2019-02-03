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
package com.egoal.darkestpixeldungeon.actors.blobs;

import com.egoal.darkestpixeldungeon.actors.buffs.Awareness;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.BlobEmitter;
import com.egoal.darkestpixeldungeon.effects.Identification;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.Journal;
import com.egoal.darkestpixeldungeon.items.Item;
import com.watabou.noosa.audio.Sample;

public class WaterOfAwareness extends WellWater {

  @Override
  protected boolean affectHero(Hero hero) {

    Sample.INSTANCE.play(Assets.SND_DRINK);
    emitter.parent.add(new Identification(DungeonTilemap.tileCenterToWorld
            (getPos())));

    hero.belongings.observe();

    for (int i = 0; i < Dungeon.level.length(); i++) {

      int terr = Dungeon.level.map[i];
      if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {

        Dungeon.level.discover(i);

        if (Dungeon.visible[i]) {
          GameScene.discoverTile(i, terr);
        }
      }
    }

    Buff.affect(hero, Awareness.class, Awareness.DURATION);
    Dungeon.observe();

    Dungeon.hero.interrupt();

    GLog.p(Messages.get(this, "procced"));

    Journal.remove(Journal.Feature.WELL_OF_AWARENESS);

    return true;
  }

  @Override
  protected Item affectItem(Item item) {
    if (item.isIdentified()) {
      return null;
    } else {
      item.identify();
      Badges.validateItemLevelAquired(item);

      emitter.parent.add(new Identification(DungeonTilemap.tileCenterToWorld
              (getPos())));

      Journal.remove(Journal.Feature.WELL_OF_AWARENESS);

      return item;
    }
  }

  @Override
  public void use(BlobEmitter emitter) {
    super.use(emitter);
    emitter.pour(Speck.factory(Speck.QUESTION), 0.3f);
  }

  @Override
  public String tileDesc() {
    return Messages.get(this, "desc");
  }
}
