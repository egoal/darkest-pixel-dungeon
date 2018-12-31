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
package com.egoal.darkestpixeldungeon.scenes;

import android.content.Intent;
import android.net.Uri;

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.ui.Archs;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.effects.Flare;
import com.egoal.darkestpixeldungeon.ui.ExitButton;
import com.egoal.darkestpixeldungeon.ui.Icons;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.watabou.input.Touchscreen.Touch;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.TouchArea;

public class AboutScene extends PixelScene {

  private static final String TTL_DPD = "Darkest Pixel Dungeon";

  private static final String TXT_DPD = "Design, Code, & Graphics: Lix";

  private static final String LNK_DPD = "github.com/egoal";

  private static final String TTL_SHPX = "Shattered Pixel Dungeon";

  private static final String TTL_WATA = "Pixel Dungeon";

  private static final String TTL_THANKS = "Some sound tracks borrowed from " +
          "(and thanks to): ";
  private static final String TTL_YA = "Yet Another Pixel Dungeon";

  @Override
  public void create() {
    super.create();

    final float colWidth = Camera.main.width / (DarkestPixelDungeon.landscape
            () ? 2 : 1);
    final float colTop = (Camera.main.height / 2) - (DarkestPixelDungeon
            .landscape() ? 36 : 84);
    final float wataOffset = DarkestPixelDungeon.landscape() ? colWidth : 0;

    // add dpd sign
    Image lix = Icons.LIX.get();
    lix.x = (colWidth - lix.width()) / 2;
    lix.y = colTop;
    align(lix);
    add(lix);

    new Flare(7, 64.f).color(Window.DPD_COLOR, true).show(lix, 0)
            .angularSpeed = +30;

    RenderedText dpdTitle = renderText(TTL_DPD, 8);
    dpdTitle.hardlight(Window.DPD_COLOR);    // set the font color
    add(dpdTitle);
    dpdTitle.x = (colWidth - dpdTitle.width()) / 2;
    dpdTitle.y = lix.y + lix.height + 5;
    align(dpdTitle);

    RenderedTextMultiline dpdText = renderMultiline(TXT_DPD, 8);
    dpdText.maxWidth((int) Math.min(colWidth, 120));
    add(dpdText);
    dpdText.setPos((colWidth - dpdText.width()) / 2, dpdTitle.y + dpdTitle
            .height() + 12);
    dpdText.hardlight(Window.DPD_COLOR);
    align(dpdText);

    RenderedTextMultiline dpdlink = renderMultiline(LNK_DPD, 8);
    dpdlink.maxWidth(dpdText.maxWidth());
    dpdlink.hardlight(Window.DPD_COLOR);
    add(dpdlink);
    dpdlink.setPos((DarkestPixelDungeon.landscape() ? colWidth : 0) +
            (colWidth - dpdlink.width()) / 2, dpdText.bottom() + 6);
    align(dpdlink);

    TouchArea dpdhotArea = new TouchArea(dpdlink.left(), dpdlink.top(),
            dpdlink.width(), dpdlink.height()) {
      @Override
      protected void onClick(Touch touch) {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://" + LNK_DPD));
        Game.instance.startActivity(intent);
      }
    };
    add(dpdhotArea);

    // shattered pixel dungeon
    Image shpx = Icons.SHPX.get();
    if (DarkestPixelDungeon.landscape()) {
      shpx.y = colTop;
      shpx.x = lix.x + colWidth;
    } else {
      shpx.x = (colWidth - shpx.width()) / 2;
      shpx.y = dpdhotArea.y + dpdhotArea.height() + 20;
    }
    align(shpx);
    add(shpx);

    new Flare(7, 64).color(0x225511, true).show(shpx, 0).angularSpeed = +10;

    RenderedText shpxtitle = renderText(TTL_SHPX, 8);
    shpxtitle.hardlight(Window.SHPX_COLOR);
    add(shpxtitle);

    shpxtitle.x = (colWidth - shpxtitle.width()) / 2;
    if (DarkestPixelDungeon.landscape())
      shpxtitle.x += colWidth;
    shpxtitle.y = shpx.y + shpx.height + 5;
    align(shpxtitle);

    // pixel dungeon
    Image wata = Icons.WATA.get();
    wata.x = wataOffset + (colWidth - wata.width()) / 2;
    wata.y = shpxtitle.y + shpxtitle.height() + 8;
    align(wata);
    add(wata);

    new Flare(7, 64).color(0x112233, true).show(wata, 0).angularSpeed = +10;

    RenderedText wataTitle = renderText(TTL_WATA, 8);
    wataTitle.hardlight(Window.TITLE_COLOR);
    add(wataTitle);

    wataTitle.x = wataOffset + (colWidth - wataTitle.width()) / 2;
    wataTitle.y = wata.y + wata.height + 8;
    align(wataTitle);

    // assets pick cite
    RenderedTextMultiline thanksTitle = renderMultiline(TTL_THANKS, 6);
    thanksTitle.maxWidth((int) Math.min(colWidth, 120));
    thanksTitle.hardlight(Window.DPD_COLOR);
    add(thanksTitle);

    thanksTitle.setPos(wataOffset + (colWidth - thanksTitle.width()) / 2,
            wataTitle.y + wataTitle.height() + 8);
    align(thanksTitle);

    // yet another pixel dungeon
    RenderedText yaTitle = renderText(TTL_YA, 8);
    yaTitle.hardlight(Window.TITLE_COLOR);
    add(yaTitle);

    yaTitle.x = wataOffset + (colWidth - yaTitle.width()) / 2;
    yaTitle.y = thanksTitle.bottom() + 8;
    align(yaTitle);

    Archs archs = new Archs();
    archs.setSize(Camera.main.width, Camera.main.height);
    addToBack(archs);

    ExitButton btnExit = new ExitButton();
    btnExit.setPos(Camera.main.width - btnExit.width(), 0);
    add(btnExit);

    fadeIn();
  }

  @Override
  protected void onBackPressed() {
    DarkestPixelDungeon.switchNoFade(TitleScene.class);
  }
}
