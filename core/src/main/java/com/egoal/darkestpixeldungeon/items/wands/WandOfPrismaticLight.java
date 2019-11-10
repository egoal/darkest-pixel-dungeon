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
package com.egoal.darkestpixeldungeon.items.wands;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.actors.buffs.Light;
import com.egoal.darkestpixeldungeon.actors.buffs.ViewMark;
import com.egoal.darkestpixeldungeon.effects.Beam;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.effects.particles.RainbowParticle;
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class WandOfPrismaticLight extends DamageWand {

  {
    image = ItemSpriteSheet.WAND_PRISMATIC_LIGHT;

    collisionProperties = Ballistica.MAGIC_BOLT;
  }

  public int min(int lvl) {
    return 1 + lvl;
  }

  public int max(int lvl) {
    return 7 + 3 * lvl;
  }

  @NotNull
  @Override
  public Damage giveDamage(@NotNull Char enemy) {
    return super.giveDamage(enemy).addElement(Damage.Element.HOLY);
  }

  @Override
  protected void onZap(Ballistica beam) {
    Char ch = Actor.findChar(beam.collisionPos);
    if (ch != null) {
      affectTarget(ch);
    }
    affectMap(beam);

    Buff.affect(curUser, Light.class).prolong(4f + level() * 4f);
  }

  private void affectTarget(Char ch) {
    // view mark
    Buff.prolong(ch, ViewMark.class, 4f + level()).observer = curUser.id();

    //three in (5+lvl) chance of failing
    if (Random.Int(5 + level()) >= 3) {
      Buff.prolong(ch, Blindness.class, 2f + (level() * 0.333f));
      ch.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 6);
    }

    if (ch.properties().contains(Char.Property.DEMONIC) || ch.properties()
            .contains(Char.Property.UNDEAD)) {
      ch.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10 + level());
      Sample.INSTANCE.play(Assets.SND_BURNING);

    } else {
      ch.sprite.centerEmitter().burst(RainbowParticle.BURST, 10 + level());
    }

    ch.takeDamage(giveDamage(ch));
  }

  private void affectMap(Ballistica beam) {
    boolean noticed = false;
    for (int c : beam.subPath(0, beam.dist)) {
      for (int n : PathFinder.NEIGHBOURS9) {
        int cell = c + n;

        if (Level.Companion.getDiscoverable()[cell])
          Dungeon.level.getMapped()[cell] = true;

        int terr = Dungeon.level.getMap()[cell];
        if ((Terrain.flags[terr] & Terrain.SECRET) != 0) {

          Dungeon.level.discover(cell);

          GameScene.discoverTile(cell, terr);
          ScrollOfMagicMapping.discover(cell);

          noticed = true;
        }
      }

      CellEmitter.center(c).burst(RainbowParticle.BURST, Random.IntRange(1, 2));
    }
    if (noticed)
      Sample.INSTANCE.play(Assets.SND_SECRET);

    GameScene.updateFog();
  }

  @Override
  public void fx(Ballistica beam, Callback callback) {
    curUser.sprite.parent.add(new Beam.LightRay(
            DungeonTilemap.tileCenterToWorld(beam.sourcePos),
            DungeonTilemap.tileCenterToWorld(beam.collisionPos)));
    callback.call();
  }

  @Override
  public void onHit(MagesStaff staff, Damage damage) {
    //cripples enemy
    Buff.prolong((Char) damage.to, Cripple.class, 1f + staff.level());
  }

  @Override
  public void staffFx(MagesStaff.StaffParticle particle) {
    particle.color(Random.Int(0x1000000));
    particle.am = 0.3f;
    particle.setLifespan(1f);
    particle.speed.polar(Random.Float(PointF.PI2), 2f);
    particle.setSize(1f, 2.5f);
    particle.radiateXY(1f);
  }

}
