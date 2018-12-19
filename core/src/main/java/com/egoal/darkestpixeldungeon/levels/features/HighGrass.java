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
package com.egoal.darkestpixeldungeon.levels.features;

import com.egoal.darkestpixeldungeon.Challenges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Barkskin;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.particles.LeafParticle;
import com.egoal.darkestpixeldungeon.items.Dewdrop;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.armor.glyphs.Camouflage;
import com.egoal.darkestpixeldungeon.items.artifacts.SandalsOfNature;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.plants.BlandfruitBush;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

public class HighGrass {

  public static void trample(Level level, int pos, Char ch) {

    if (level.map[pos] != Terrain.HIGH_GRASS) {
      // already collected,
    } else {
      //!!! always set terrain before drop
      Level.set(pos, Terrain.HIGH_GRASS_COLLECTED);

      if (!Dungeon.isChallenged(Challenges.NO_HERBALISM)) {
        // the sandals artifact effect
        int naturalismLevel = 0;

        if (ch != null) {
          SandalsOfNature.Naturalism naturalism = ch.buff(SandalsOfNature
                  .Naturalism.class);
          if (naturalism != null) {
            if (!naturalism.isCursed()) {
              naturalismLevel = naturalism.itemLevel() + 1;
              naturalism.charge();
            } else {
              naturalismLevel = -1;
            }
          }
        }

        if (naturalismLevel >= 0) {
          // Seed, scales from 1/16 to 1/4
          // in the village level, more unlikely to drop seed
          int chance = Dungeon.depth == 0 ? 30 : (15 - naturalismLevel * 3);
          if (Random.Int(chance) == 0) {
            Level.set(pos, Terrain.GRASS);
            
            Item seed = Generator.random(Generator.Category.SEED);

            if (seed instanceof BlandfruitBush.Seed) {
              if (Random.Int(15) - Dungeon.limitedDrops.blandfruitSeed.count >=
                      0) {
                level.drop(seed, pos).sprite.drop();
                Dungeon.limitedDrops.blandfruitSeed.count++;
              }
            } else
              level.drop(seed, pos).sprite.drop();

            // if seed dropped, remove the high grass
          }

          // Dew, scales from 1/6 to 1/3
          // now it's 1/5->1/3, and can drop more than one!
          if (Random.Int(10 - naturalismLevel) < 2) {
//            int q;
//            switch (Random.Int(10)) {
//              case 0:
//              case 1:
//              case 2:
//                q = 2;
//                break;
//              case 3:
//                q = 3;
//                break;
//              default:
//                q = 1;
//            }
//
//            level.drop(new Dewdrop().quantity(q), pos);
            level.drop(new Dewdrop(), pos);
          }
        }
      }

      
      GameScene.updateMap(pos);
    }

    int leaves = 4;

    if (ch instanceof Hero) {
      Hero hero = (Hero) ch;

      // Barkskin
      if (hero.subClass == HeroSubClass.WARDEN) {
        Buff.affect(ch, Barkskin.class).level(ch.HT / 10);
        leaves += 4;
      }

      //Camouflage, 迷彩
      if (hero.belongings.armor != null && hero.belongings.armor.hasGlyph
              (Camouflage.class)) {
        Buff.affect(hero, Camouflage.Camo.class).set(2 + hero.belongings
                .armor.level());
        leaves += 4;
      }
    }

    CellEmitter.get(pos).burst(LeafParticle.LEVEL_SPECIFIC, leaves);
    //observe already happens when hero moves
    if (ch != Dungeon.hero)
      Dungeon.observe();
  }
}
