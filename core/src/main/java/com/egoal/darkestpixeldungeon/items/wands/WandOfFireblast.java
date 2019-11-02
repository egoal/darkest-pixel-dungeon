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
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.Fire;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Blazing;
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class WandOfFireblast extends DamageWand {

  {
    image = ItemSpriteSheet.WAND_FIREBOLT;

    collisionProperties = Ballistica.STOP_TERRAIN;
  }

  //1x/1.5x/2.25x damage
  public int min(int lvl) {
    return (int) Math.round((1 + lvl) * Math.pow(1.5f, chargesPerCast() - 1));
  }

  //1x/1.5x/2.25x damage
  public int max(int lvl) {
    return (int) Math.round((5 + 3 * lvl) * Math.pow(1.5f, chargesPerCast() -
            1));
  }

  @NotNull
  @Override
  public Damage giveDamage(@NotNull Char enemy) {
    return super.giveDamage(enemy).addElement(Damage.Element.FIRE);
  }

  //the actual affected cells
  private HashSet<Integer> affectedCells;
  //the cells to trace fire shots to, for visual effects.
  private HashSet<Integer> visualCells;
  private int direction = 0;

  @Override
  protected void onZap(Ballistica bolt) {

    for (int cell : affectedCells) {

      if (Level.Companion.getFlamable()[cell] || !Dungeon.level.adjacent(bolt.sourcePos, cell))
        GameScene.add(Blob.seed(cell, 1 + chargesPerCast(), Fire.class));
      Char ch = Actor.findChar(cell);
      if (ch != null) {

        ch.takeDamage(giveDamage(ch));
        Buff.affect(ch, Burning.class).reignite(ch);
        switch (chargesPerCast()) {
          case 1:
            break; //no effects
          case 2:
            Buff.affect(ch, Cripple.class, 4f);
            break;
          case 3:
            Buff.affect(ch, Paralysis.class, 4f);
            break;
        }
      }
    }
  }

  //burn... BURNNNNN!.....
  private void spreadFlames(int cell, float strength) {
    if (strength >= 0 && Level.Companion.getPassable()[cell]) {
      affectedCells.add(cell);
      if (strength >= 1.5f) {
        visualCells.remove(cell);
        spreadFlames(cell + PathFinder.CIRCLE[left(direction)], strength -
                1.5f);
        spreadFlames(cell + PathFinder.CIRCLE[direction], strength - 1.5f);
        spreadFlames(cell + PathFinder.CIRCLE[right(direction)], strength -
                1.5f);
      } else {
        visualCells.add(cell);
      }
    } else if (!Level.Companion.getPassable()[cell])
      visualCells.add(cell);
  }

  private int left(int direction) {
    return direction == 0 ? 7 : direction - 1;
  }

  private int right(int direction) {
    return direction == 7 ? 0 : direction + 1;
  }

  @Override
  public void onHit(MagesStaff staff, Damage damage) {
    //acts like blazing enchantment
    new Blazing().proc(staff, damage);
  }

  @Override
  public void fx(Ballistica bolt, Callback callback) {
    //need to perform flame spread logic here so we can determine what cells 
    // to put flames in.
    affectedCells = new HashSet<>();
    visualCells = new HashSet<>();

    // 4/6/9 distance
    int maxDist = (int) (4 * Math.pow(1.5, (chargesPerCast() - 1)));
    int dist = Math.min(bolt.dist, maxDist);

    for (int i = 0; i < PathFinder.CIRCLE.length; i++) {
      if (bolt.sourcePos + PathFinder.CIRCLE[i] == bolt.path.get(1)) {
        direction = i;
        break;
      }
    }

    float strength = maxDist;
    for (int c : bolt.subPath(1, dist)) {
      strength--; //as we start at dist 1, not 0.
      affectedCells.add(c);
      if (strength > 1) {
        spreadFlames(c + PathFinder.CIRCLE[left(direction)], strength - 1);
        spreadFlames(c + PathFinder.CIRCLE[direction], strength - 1);
        spreadFlames(c + PathFinder.CIRCLE[right(direction)], strength - 1);
      } else {
        visualCells.add(c);
      }
    }

    //going to call this one manually
    visualCells.remove(bolt.path.get(dist));

    for (int cell : visualCells) {
      //this way we only get the cells at the tip, much better performance.
      MagicMissile.fire(curUser.sprite.parent, bolt.sourcePos, cell, null);
    }
    MagicMissile.fire(curUser.sprite.parent, bolt.sourcePos, bolt.path.get
            (dist), callback);
    Sample.INSTANCE.play(Assets.SND_ZAP);
  }

  @Override
  protected int chargesPerCast() {
    //consumes 30% of current charges, rounded up, with a minimum of one.
    return Math.max(1, (int) Math.ceil(curCharges * 0.3f));
  }

  @Override
  public String statsDesc() {
    if (levelKnown)
      return Messages.get(this, "stats_desc", chargesPerCast(), min(), max());
    else
      return Messages.get(this, "stats_desc", chargesPerCast(), min(0), max(0));
  }

  @Override
  public void staffFx(MagesStaff.StaffParticle particle) {
    particle.color(0xEE7722);
    particle.am = 0.5f;
    particle.setLifespan(0.6f);
    particle.acc.set(0, -40);
    particle.setSize(0f, 3f);
    particle.shuffleXY(2f);
  }

}
