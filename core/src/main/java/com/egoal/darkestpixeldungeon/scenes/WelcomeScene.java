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

import android.opengl.GLES20;
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.Rankings;
import com.egoal.darkestpixeldungeon.effects.BannerSprites;
import com.egoal.darkestpixeldungeon.effects.Fireball;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;

import javax.microedition.khronos.opengles.GL10;
import java.util.UUID;

public class WelcomeScene extends PixelScene {

  @Override
  public void create() {
    super.create();

    final int previousVersion = DarkestPixelDungeon.version();

    if (DarkestPixelDungeon.versionCode == previousVersion) {
      DarkestPixelDungeon.switchNoFade(TitleScene.class);
      return;
    }

    uiCamera.visible = false;

    int w = Camera.main.width;
    int h = Camera.main.height;

    Image title = BannerSprites.get(BannerSprites.Type.DPD_PIXEL_DUNGEON);
    title.brightness(0.6f);
    add(title);

    float topRegion = Math.max(95f, h * 0.45f);

    title.x = (w - title.width()) / 2f;
    if (DarkestPixelDungeon.landscape())
      title.y = (topRegion - title.height()) / 2f;
    else
      title.y = 16 + (topRegion - title.height() - 16) / 2f;

    align(title);

    Image signs = new Image(BannerSprites.get(BannerSprites.Type
            .DPD_PIXEL_DUNGEON_SIGNS)) {
      private float time = 0;

      @Override
      public void update() {
        super.update();
        am = (float) Math.sin(-(time += Game.elapsed));
      }

      @Override
      public void draw() {
        GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
        super.draw();
        GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
      }
    };
    signs.x = title.x + (title.width() - signs.width()) / 2f;
    signs.y = title.y;
    add(signs);

    DarkRedButton okay = new DarkRedButton(Messages.get(this, "continue")) {
      @Override
      protected void onClick() {
        super.onClick();
        updateVersion(previousVersion);
        DarkestPixelDungeon.switchScene(TitleScene.class);
      }
    };

    if (previousVersion != 0) {
      DarkRedButton changes = new DarkRedButton(Messages.get(this, 
              "changelist")) {
        @Override
        protected void onClick() {
          super.onClick();
          updateVersion(previousVersion);
          DarkestPixelDungeon.switchScene(ChangesScene.class);
        }
      };
      okay.setRect(title.x, h - 20, (title.width() / 2) - 2, 16);
      okay.textColor(0xBBBB33);
      add(okay);

      changes.setRect(okay.right() + 2, h - 20, (title.width() / 2) - 2, 16);
      changes.textColor(0xBBBB33);
      add(changes);
    } else {
      okay.setRect(title.x, h - 20, title.width(), 16);
      okay.textColor(0xBBBB33);
      add(okay);
    }

    RenderedTextMultiline text = renderMultiline(6);
    String message;

    if (previousVersion == 0)
      message = Messages.get(this, "welcome_msg");
    else {
      message = Messages.get(this, "update_intro");
      message += "\n\n" + Messages.get(this, "update_msg");
      message += "\n\n" + Messages.get(this, "suggest_msg");
    }

    text.text(message, w - 20);
    float textSpace = h - title.y - (title.height() - 10) - okay.height() - 2;
    text.setPos((w - text.width()) / 2f, title.y + (title.height() - 10) + (
            (textSpace - text.height()) / 2));
    add(text);

    DarkestPixelDungeon.changeListChecked(false);
  }

  private void updateVersion(int previousVersion) {
    DarkestPixelDungeon.version(DarkestPixelDungeon.versionCode);
  }

  private void placeTorch(float x, float y) {
    Fireball fb = new Fireball();
    fb.setPos(x, y);
    add(fb);
  }

  private class DarkRedButton extends RedButton {
    {
      bg.brightness(0.4f);
    }

    DarkRedButton(String text) {
      super(text);
    }

    @Override
    protected void onTouchDown() {
      bg.brightness(0.5f);
      Sample.INSTANCE.play(Assets.SND_CLICK);
    }

    @Override
    protected void onTouchUp() {
      super.onTouchUp();
      bg.brightness(0.4f);
    }
  }
}
