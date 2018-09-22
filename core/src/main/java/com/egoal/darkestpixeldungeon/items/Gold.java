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
package com.egoal.darkestpixeldungeon.items;

import android.text.StaticLayout;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.Statistics;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.artifacts.GoldPlatedStatue;
import com.egoal.darkestpixeldungeon.items.artifacts.MasterThievesArmband;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Gold extends Item {

  private static final String TXT_VALUE = "%+d";

  {
    image = ItemSpriteSheet.GOLD;
    stackable = true;
  }

  public Gold() {
    this(1);
  }

  public Gold(int value) {
    this.quantity = value;
  }

  @Override
  public ArrayList<String> actions(Hero hero) {
    return new ArrayList<String>();
  }

  @Override
  public boolean doPickUp(Hero hero) {
    int greedyCollect = 0;

    GoldPlatedStatue.Greedy greedy = hero.buff(GoldPlatedStatue.Greedy.class);
    if (greedy != null) {
      // greedy collect
      greedyCollect = greedy.extraCollect(quantity);
      if (greedyCollect == 0)
        greedyCollect = 1;
    }
    
    Dungeon.gold += quantity+greedyCollect;
    Statistics.goldCollected += quantity+greedyCollect;
    Badges.validateGoldCollected();

    MasterThievesArmband.Thievery thievery = hero.buff(MasterThievesArmband
            .Thievery.class);
    if (thievery != null)
      thievery.collect(quantity);

    GameScene.pickUp(this);
    
    
    if(greedyCollect>0) {
      hero.sprite.showStatus(CharSprite.NEUTRAL, "%+d(%+d)", quantity, greedyCollect);
    }else{
      hero.sprite.showStatus(CharSprite.NEUTRAL, TXT_VALUE, quantity);
    }
    hero.spendAndNext(TIME_TO_PICK_UP);

    Sample.INSTANCE.play(Assets.SND_GOLD, 1, 1, Random.Float(0.9f, 1.1f));

    return true;
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
  public Item random() {
    quantity = Random.Int(30 + Dungeon.depth * 10, 60 + Dungeon.depth * 20);
    return this;
  }

  private static final String VALUE = "value";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(VALUE, quantity);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    quantity = bundle.getInt(VALUE);
  }
}
