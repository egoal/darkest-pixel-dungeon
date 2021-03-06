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
package com.egoal.darkestpixeldungeon.ui;

import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.utils.BArray;
import com.egoal.darkestpixeldungeon.windows.WndBag;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Button;
import com.watabou.utils.PathFinder;

public class QuickSlotButton extends Button implements WndBag.Listener {
  private static final int NUM_BUTTONS = 8* 2;

  private static QuickSlotButton[] instance = new QuickSlotButton[NUM_BUTTONS];
  private int slotNum;

  private ItemSlot slot;

  private static Image crossB;
  private static Image crossM;

  private static boolean targeting = false;
  public static Char lastTarget = null;

  public QuickSlotButton(int slotNum) {
    super();
    this.slotNum = slotNum;
    item(select(slotNum));

    instance[slotNum] = this;
  }

  @Override
  public void destroy() {
    super.destroy();

    reset();
  }

  public static void reset() {
    instance = new QuickSlotButton[NUM_BUTTONS];

    lastTarget = null;
  }

  @Override
  protected void createChildren() {
    super.createChildren();

    slot = new ItemSlot() {
      @Override
      protected void onClick() {
        if (targeting) {
          int cell = autoAim(lastTarget, select(slotNum));

          if (cell != -1) {
            GameScene.handleCell(cell);
          } else {
            //couldn't auto-aim, just targetpos the position and hope for the best.
            GameScene.handleCell(lastTarget.getPos());
          }
        } else {
          Item item = select(slotNum);
          if (item.getUsesTargeting())
            useTargeting();
          item.execute(Dungeon.INSTANCE.getHero());
        }
      }

      @Override
      protected boolean onLongClick() {
        return QuickSlotButton.this.onLongClick();
      }

      @Override
      protected void onTouchDown() {
        getIcon().lightness(0.7f);
      }

      @Override
      protected void onTouchUp() {
        getIcon().resetColor();
      }
    };
    slot.showParams(true, false, true);
    add(slot);

    crossB = Icons.TARGET.get();
    crossB.visible = false;
    add(crossB);

    crossM = new Image();
    crossM.copy(crossB);
  }

  @Override
  protected void layout() {
    super.layout();

    slot.fill(this);

    crossB.x = x + (width - crossB.width) / 2;
    crossB.y = y + (height - crossB.height) / 2;
    PixelScene.align(crossB);
  }

  @Override
  protected void onClick() {
    GameScene.selectItem(this, WndBag.Mode.QUICKSLOT, Messages.get(this, 
            "select_item"));
  }

  @Override
  protected boolean onLongClick() {
    GameScene.selectItem(this, WndBag.Mode.QUICKSLOT, Messages.get(this, 
            "select_item"));
    return true;
  }

  private static Item select(int slotNum) {
    return Dungeon.INSTANCE.getQuickslot().getItem(slotNum);
  }

  @Override
  public void onSelect(Item item) {
    if (item != null) {
      Dungeon.INSTANCE.getQuickslot().setSlot(slotNum, item);
      refresh();
    }
  }

  public void item(Item item) {
    slot.item(item);
    enableSlot();
  }

  public void enable(boolean value) {
    active = value;
    if (value) {
      enableSlot();
    } else {
      slot.enable(false);
    }
  }

  private void enableSlot() {
    slot.enable(Dungeon.INSTANCE.getQuickslot().isNonePlaceholder(slotNum));
  }

  private void useTargeting() {

    if (lastTarget != null &&
            Actor.Companion.chars().contains(lastTarget) &&
            lastTarget.isAlive() &&
            Dungeon.INSTANCE.getVisible()[lastTarget.getPos()]) {

      targeting = true;
      lastTarget.getSprite().parent.add(crossM);
      crossM.point(DungeonTilemap.tileToWorld(lastTarget.getPos()));
      crossB.x = x + (width - crossB.width) / 2;
      crossB.y = y + (height - crossB.height) / 2;
      crossB.visible = true;

    } else {

      lastTarget = null;
      targeting = false;

    }

  }

  public static int autoAim(Char target) {
    //will use generic projectile logic if no item is specified
    return autoAim(target, new Item());
  }

  //FIXME: this is currently very expensive, should either optimize 
  // ballistica or this, or both
  public static int autoAim(Char target, Item item) {

    //first try to directly targetpos
    if (item.throwPos(Dungeon.INSTANCE.getHero(), target.getPos()) == target.getPos()) {
      return target.getPos();
    }

    //Otherwise pick nearby tiles to try and 'angle' the shot, auto-aim 
    // basically.
    PathFinder.buildDistanceMap(target.getPos(), BArray.not(new boolean[Dungeon.INSTANCE.getLevel().length()], null), 2);
    for (int i = 0; i < PathFinder.distance.length; i++) {
      if (PathFinder.distance[i] < Integer.MAX_VALUE
              && item.throwPos(Dungeon.INSTANCE.getHero(), i) == target.getPos())
        return i;
    }

    //couldn't find a cell, give up.
    return -1;
  }

  public static void refresh() {
    for (int i = 0; i < instance.length; i++) {
      if (instance[i] != null) {
        instance[i].item(select(i));
      }
    }
  }

  public static void target(Char target) {
    if (target != Dungeon.INSTANCE.getHero()) {
      lastTarget = target;

      HealthIndicator.instance.target(target);
    }
  }

  public static void cancel() {
    if (targeting) {
      crossB.visible = false;
      crossM.remove();
      targeting = false;
    }
  }
}
