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
package com.egoal.darkestpixeldungeon.windows;

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.ui.ItemSlot;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.ui.Window;

public class WndInfoItem extends Window {

  private static final float GAP = 2;

  private static final int WIDTH_P = 120;
  private static final int WIDTH_L = 144;

  public WndInfoItem(Heap heap) {

    super();

    if (heap.getType() == Heap.Type.HEAP) {

      Item item = heap.peek();

      int color = TITLE_COLOR;
      if (item.getLevelKnown() && item.level() > 0) {
        color = ItemSlot.UPGRADED;
      } else if (item.getLevelKnown() && item.level() < 0) {
        color = ItemSlot.DEGRADED;
      }
      fillFields(item.image(), item.glowing(), color, item.toString(), item
              .info());

    } else {

      fillFields(heap.image(), heap.glowing(), TITLE_COLOR, heap.toString(), 
              heap.info());

    }
  }

  public WndInfoItem(Item item) {

    super();

    int color = TITLE_COLOR;
    if (item.getLevelKnown() && item.level() > 0) {
      color = ItemSlot.UPGRADED;
    } else if (item.getLevelKnown() && item.level() < 0) {
      color = ItemSlot.DEGRADED;
    }

    fillFields(item.image(), item.glowing(), color, item.toString(), item.info());
  }

  private void fillFields(int image, ItemSprite.Glowing glowing, int 
          titleColor, String title, String info) {

    int width = DarkestPixelDungeon.landscape() ? WIDTH_L : WIDTH_P;

    IconTitle titlebar = new IconTitle();
    titlebar.icon(new ItemSprite(image, glowing));
    titlebar.label(Messages.titleCase(title), titleColor);
    titlebar.setRect(0, 0, width, 0);
    add(titlebar);

    RenderedTextMultiline txtInfo = PixelScene.renderMultiline(info, 6);
    txtInfo.maxWidth(width);
    txtInfo.setPos(titlebar.left(), titlebar.bottom() + GAP);
    add(txtInfo);

    resize(width, (int) (txtInfo.top() + txtInfo.height()));
  }
}
