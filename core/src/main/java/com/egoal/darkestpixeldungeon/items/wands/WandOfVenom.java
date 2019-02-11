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
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.VenomGas;
import com.egoal.darkestpixeldungeon.effects.MagicMissile;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Venomous;
import com.egoal.darkestpixeldungeon.items.weapon.melee.MagesStaff;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

public class WandOfVenom extends Wand {

  {
    image = ItemSpriteSheet.WAND_VENOM;

    collisionProperties = Ballistica.STOP_TARGET | Ballistica.STOP_TERRAIN;
  }

  @Override
  protected void onZap(Ballistica bolt) {
    Blob venomGas = Blob.seed(bolt.collisionPos, 50 + 10 * level(), VenomGas
            .class);
    ((VenomGas) venomGas).setStrength(level() + 1);
    GameScene.add(venomGas);
  }

  @Override
  public void fx(Ballistica bolt, Callback callback) {
    MagicMissile.poison(curUser.sprite.parent, bolt.sourcePos, bolt
            .collisionPos, callback);
    Sample.INSTANCE.play(Assets.SND_ZAP);
  }

  @Override
  public void onHit(MagesStaff staff, Damage damage) {
    //acts like venomous enchantment
    new Venomous().proc(staff, damage);
  }

  @Override
  public void staffFx(MagesStaff.StaffParticle particle) {
    particle.color(0x8844FF);
    particle.am = 0.6f;
    particle.setLifespan(0.6f);
    particle.acc.set(0, 40);
    particle.setSize(0f, 3f);
    particle.shuffleXY(2f);
  }

}
