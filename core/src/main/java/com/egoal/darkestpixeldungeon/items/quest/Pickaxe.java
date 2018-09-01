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

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Hunger;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.mobs.Bat;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class Pickaxe extends Weapon {

  public static final String AC_MINE = "MINE";

  public static final float TIME_TO_MINE = 2;

  private static final ItemSprite.Glowing BLOODY = new ItemSprite.Glowing
          (0x550000);

  {
    image = ItemSpriteSheet.PICKAXE;

    unique = true;

    defaultAction = AC_MINE;

  }

  public boolean bloodStained = false;

  @Override
  public int min(int lvl) {
    return 2;   //tier 2
  }

  @Override
  public int max(int lvl) {
    return 15;  //tier 2
  }

  @Override
  public int STRReq(int lvl) {
    return 14;  //tier 3
  }

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    actions.add(AC_MINE);
    return actions;
  }

  @Override
  public void execute(final Hero hero, String action) {

    super.execute(hero, action);

    if (action.equals(AC_MINE)) {

      if (Dungeon.depth < 11 || Dungeon.depth > 15) {
        GLog.w(Messages.get(this, "no_vein"));
        return;
      }

      for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {

        final int pos = hero.pos + PathFinder.NEIGHBOURS8[i];
        if (Dungeon.level.map[pos] == Terrain.WALL_DECO) {

          hero.spend(TIME_TO_MINE);
          hero.busy();

          hero.sprite.attack(pos, new Callback() {

            @Override
            public void call() {

              CellEmitter.center(pos).burst(Speck.factory(Speck.STAR), 7);
              Sample.INSTANCE.play(Assets.SND_EVOKE);

              Level.set(pos, Terrain.WALL);
              GameScene.updateMap(pos);

              DarkGold gold = new DarkGold();
              if (gold.doPickUp(Dungeon.hero)) {
                GLog.i(Messages.get(Dungeon.hero, "you_now_have", gold.name()));
              } else {
                Dungeon.level.drop(gold, hero.pos).sprite.drop();
              }

              Hunger hunger = hero.buff(Hunger.class);
              if (hunger != null && !hunger.isStarving()) {
                hunger.reduceHunger(-Hunger.STARVING / 10);
                BuffIndicator.refreshHero();
              }

              hero.onOperateComplete();
            }
          });

          return;
        }
      }

      GLog.w(Messages.get(this, "no_vein"));

    }
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
  public Damage proc(Damage dmg) {
    if (!bloodStained && dmg.to instanceof Bat && ((Char) dmg.to).HP <= dmg
            .value) {
      //fixme: may not die, since we use damage resistance
      bloodStained = true;
      updateQuickslot();
    }

    return dmg;
  }

  private static final String BLOODSTAINED = "bloodStained";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);

    bundle.put(BLOODSTAINED, bloodStained);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);

    bloodStained = bundle.getBoolean(BLOODSTAINED);
  }

  @Override
  public ItemSprite.Glowing glowing() {
    return bloodStained ? BLOODY : null;
  }

}
