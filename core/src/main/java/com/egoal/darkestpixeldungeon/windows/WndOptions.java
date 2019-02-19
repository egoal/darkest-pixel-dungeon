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
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

public class WndOptions extends Window {

  private static final int WIDTH_P = 120;
  private static final int WIDTH_L = 144;

  private static final int MARGIN = 2;
  private static final int BUTTON_HEIGHT = 20;

  public WndOptions(String title, String message, String... options) {
    super();

    int width = DarkestPixelDungeon.landscape() ? WIDTH_L : WIDTH_P;

    RenderedTextMultiline tfTitle = PixelScene.renderMultiline(title, 9);
    tfTitle.hardlight(TITLE_COLOR);
    tfTitle.setPos(MARGIN, MARGIN);
    tfTitle.maxWidth(width - MARGIN * 2);
    add(tfTitle);

    float pos = addMessageAndOptions(tfTitle.bottom() + MARGIN, width,
            message, options);

    resize(width, (int) pos);
  }

  public WndOptions(Image icon, String title, String message, String...
          options) {
    super();

    int width = DarkestPixelDungeon.landscape() ? WIDTH_L : WIDTH_P;

    IconTitle ic = new IconTitle(icon, title);
    ic.setRect(0, 0, width, 0);
    add(ic);

    float pos = addMessageAndOptions(ic.bottom() + MARGIN, width, message, 
            options);

    resize(width, (int) pos);
  }

  protected void onSelect(int index){}

  private float addMessageAndOptions(float pos, int width, String message,
                                     String... options) {
    if (message.length() > 0)
      pos = addMessage(pos, width, message);

    return addOptions(pos, width, options);
  }

  private float addMessage(float pos, int width, String message) {
    RenderedTextMultiline rtm = PixelScene.renderMultiline(6);
    rtm.text(message, width - MARGIN * 2);
    rtm.setPos(MARGIN, pos);
    add(rtm);

    return rtm.bottom() + MARGIN;
  }

  private float addOptions(float pos, int width, String... options) {
    for (int i = 0; i < options.length; ++i) {
      final int index = i;
      RedButton btn = new RedButton(options[i]) {
        @Override
        protected void onClick() {
          hide();
          onSelect(index);
        }
      };
      btn.setRect(MARGIN, pos, width - MARGIN * 2, BUTTON_HEIGHT);
      add(btn);

      pos += BUTTON_HEIGHT + MARGIN;
    }

    return pos;
  }
}
