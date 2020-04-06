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

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.scenes.InterlevelScene;
import com.egoal.darkestpixeldungeon.scenes.RankingsScene;
import com.egoal.darkestpixeldungeon.scenes.TitleScene;
import com.egoal.darkestpixeldungeon.ui.Icons;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.watabou.noosa.Game;

import java.io.IOException;

public class WndGame extends Window {

  private static final int WIDTH = 120;
  private static final int BTN_HEIGHT = 20;
  private static final int GAP = 2;

  private int pos;

  public WndGame() {

    super();

    addButton(new RedButton(Messages.get(this, "settings")) {
      @Override
      protected void onClick() {
        hide();
        GameScene.show(new WndSettings(false));
      }
    });

    // Restart
    if (!Dungeon.hero.isAlive()) {

      RedButton btnStart;
      addButton(btnStart = new RedButton(Messages.get(this, "start")) {
        @Override
        protected void onClick() {
          Dungeon.hero = null;
          InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
          InterlevelScene.noStory = true;
          Game.switchScene(InterlevelScene.class);
        }
      });
      btnStart.icon(Icons.Companion.get(Dungeon.hero.getHeroClass()));

      addButton(new RedButton(Messages.get(this, "rankings")) {
        @Override
        protected void onClick() {
          InterlevelScene.mode = InterlevelScene.Mode.DESCEND;
          Game.switchScene(RankingsScene.class);
        }
      });
    }

    addButtons(
            // Main menu
            new RedButton(Messages.get(this, "menu")) {
              @Override
              protected void onClick() {
                try {
                  Dungeon.saveAll();
                } catch (IOException e) {
                  DarkestPixelDungeon.reportException(e);
                }
                Game.switchScene(TitleScene.class);
              }
            },
            // Quit
            new RedButton(Messages.get(this, "exit")) {
              @Override
              protected void onClick() {
                Game.instance.finish();
              }
            }
    );

    // Cancel
    addButton(new RedButton(Messages.get(this, "return")) {
      @Override
      protected void onClick() {
        hide();
      }
    });

    resize(WIDTH, pos);
  }

  private void addButton(RedButton btn) {
    add(btn);
    btn.setRect(0, pos > 0 ? pos += GAP : 0, WIDTH, BTN_HEIGHT);
    pos += BTN_HEIGHT;
  }

  private void addButtons(RedButton btn1, RedButton btn2) {
    add(btn1);
    btn1.setRect(0, pos > 0 ? pos += GAP : 0, (WIDTH - GAP) / 2, BTN_HEIGHT);
    add(btn2);
    btn2.setRect(btn1.right() + GAP, btn1.top(), WIDTH - btn1.right() - GAP, BTN_HEIGHT);
    pos += BTN_HEIGHT;
  }
}
