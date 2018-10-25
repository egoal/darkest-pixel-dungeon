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

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.scenes.ChangesScene;
import com.egoal.darkestpixeldungeon.Assets;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;

public class ChangesButton extends Button {

  protected Image image;
  private float time = 0f;
  private boolean blink = false;

  public ChangesButton() {
    super();

    width = image.width;
    height = image.height;
  }

  @Override
  protected void createChildren() {
    super.createChildren();

    image = Icons.NOTES.get();
    add(image);
  }

  @Override
  protected void layout() {
    super.layout();

    image.x = x;
    image.y = y;
  }

  @Override
  protected void onTouchDown() {
    image.brightness(1.5f);
    Sample.INSTANCE.play(Assets.SND_CLICK);
  }

  @Override
  protected void onTouchUp() {
    image.resetColor();
  }

  public void setBlink(boolean b) {
    blink = b;
  }

  @Override
  public void update() {
    super.update();

    if (blink)
      image.am = (float) Math.sin((time += 3 * Game.elapsed)) / 2f + .5f + .2f;
  }

  @Override
  protected void onClick() {
    DarkestPixelDungeon.switchNoFade(ChangesScene.class);
  }
}
