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
package com.egoal.darkestpixeldungeon.items.weapon.melee;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndBag;

import java.util.ArrayList;

public class Sword extends MeleeWeapon {

  {
    image = ItemSpriteSheet.SWORD;

    tier = 3;
  }

  private static final String AC_DUAL = "dual";

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    if (isIdentified() && !cursed)
      actions.add(AC_DUAL);

    return actions;
  }

  @Override
  public void execute(Hero hero, String action) {
    super.execute(hero, action);

    if (action.equals(AC_DUAL)) {
      GameScene.selectItem(itemSelector, mode, Messages.get(this, 
              "select_other"));
    }
  }

  private static WndBag.Mode mode = WndBag.Mode.WEAPON;
  private static WndBag.Listener itemSelector = new WndBag.Listener() {
    @Override
    public void onSelect(Item item) {
      if (item instanceof Sword && curItem != item) {
        if (item.isIdentified() && !item.cursed) {
          PairSwords ps = new PairSwords((Sword) curItem, (Sword) item);
          if(!ps.doPickUp(Dungeon.hero))
            Dungeon.level.drop(ps, Dungeon.hero.pos).sprite.drop();
          
          if(curItem.isEquipped(Dungeon.hero))
            ((Sword) curItem).doUnequip(Dungeon.hero, false);
          curItem.detach(Dungeon.hero.belongings.backpack);

          if(item.isEquipped(Dungeon.hero))
            ((Sword) item).doUnequip(Dungeon.hero, false);
          item.detach(Dungeon.hero.belongings.backpack);
          
          GLog.p(Messages.get(Sword.class, "paired"));
          
        } else {
          GLog.w(Messages.get(Sword.class, "not_familiar"));
        }
      }
    }
  };

}
