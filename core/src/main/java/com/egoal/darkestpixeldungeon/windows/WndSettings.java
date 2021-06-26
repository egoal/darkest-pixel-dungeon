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
import com.egoal.darkestpixeldungeon.messages.M;
import com.egoal.darkestpixeldungeon.ui.OptionSlider;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.ui.CheckBox;
import com.egoal.darkestpixeldungeon.ui.Toolbar;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;

public class WndSettings extends WndTabbed {

  private static final int WIDTH = 112;
  private static final int HEIGHT = 124;
  private static final int SLIDER_HEIGHT = 25;
  private static final int BTN_HEIGHT = 20;
  private static final int GAP_TINY = 2;
  private static final int GAP_SML = 5;
  private static final int GAP_LRG = 12;

  private ScreenTab screen;
  private UITab ui;
  private AudioTab audio;

  private static int last_index = 0;

  public WndSettings() {
    this(false);
  }

  public WndSettings(boolean settableLanguage) {
    super();

    screen = new ScreenTab();
    add(screen);

    ui = new UITab(settableLanguage);
    add(ui);

    audio = new AudioTab();
    add(audio);

    add(new LabeledTab(Messages.get(this, "ui")) {
      @Override
      protected void select(boolean value) {
        super.select(value);
        ui.visible = ui.active = value;
        if (value) last_index = 0;
      }
    });

    add(new LabeledTab(Messages.get(this, "screen")) {
      @Override
      protected void select(boolean value) {
        super.select(value);
        screen.visible = screen.active = value;
        if (value) last_index = 1;
      }
    });

    add(new LabeledTab(Messages.get(this, "audio")) {
      @Override
      protected void select(boolean value) {
        super.select(value);
        audio.visible = audio.active = value;
        if (value) last_index = 2;
      }
    });

    resize(WIDTH, HEIGHT);

    layoutTabs();

    select(last_index);

  }

  private class ScreenTab extends Group {

    public ScreenTab() {
      super();

      OptionSlider scale = new OptionSlider(Messages.get(this, "scale"),
              (int) Math.ceil(2 * Game.density) + "X",
              PixelScene.maxDefaultZoom + "X",
              (int) Math.ceil(2 * Game.density),
              PixelScene.maxDefaultZoom) {
        @Override
        protected void onChange() {
          if (getSelectedValue() != DarkestPixelDungeon.scale()) {
            DarkestPixelDungeon.scale(getSelectedValue());
            DarkestPixelDungeon.switchNoFade((Class<? extends PixelScene>)
                    DarkestPixelDungeon.scene().getClass(), new Game
                    .SceneChangeCallback() {
              @Override
              public void beforeCreate() {
                //do nothing
              }

              @Override
              public void afterCreate() {
                Game.scene().add(new WndSettings());
              }
            });
          }
        }
      };
      scale.setSelectedValue(PixelScene.defaultZoom);
      if ((int) Math.ceil(2 * Game.density) < PixelScene.maxDefaultZoom) {
        scale.setRect(0, 0, WIDTH, SLIDER_HEIGHT);
        add(scale);
      } else {
        scale.setRect(0, 0, 0, 0);
      }

      OptionSlider brightness = new OptionSlider(Messages.get(this,
              "brightness"),
              Messages.get(this, "dark"), Messages.get(this, "bright"), -2, 2) {
        @Override
        protected void onChange() {
          DarkestPixelDungeon.brightness(getSelectedValue());
        }
      };
      brightness.setSelectedValue(DarkestPixelDungeon.brightness());
      brightness.setRect(0, scale.bottom() + GAP_SML, WIDTH, SLIDER_HEIGHT);
      add(brightness);

      RedButton btnOrientation = new RedButton(DarkestPixelDungeon.landscape() ?
              Messages.get(this, "portrait") : Messages.get(this,
              "landscape")) {
        @Override
        protected void onClick() {
          if (DarkestPixelDungeon.landscape()) {
            DarkestPixelDungeon.landscape(false);
          } else {
            // warning
            parent.add(new WndMessage(Messages.get(WndSettings.ScreenTab
                    .class, "landscape_warning")) {
              @Override
              public void onBackPressed() {
                super.onBackPressed();
                DarkestPixelDungeon.landscape(true);
              }
            });
          }
        }
      };
      btnOrientation.setRect(0, brightness.bottom() + GAP_TINY, WIDTH,
              BTN_HEIGHT);
      add(btnOrientation);

      CheckBox chkImmersive = new CheckBox(Messages.get(this, "soft_keys")) {
        @Override
        protected void onClick() {
          super.onClick();
          DarkestPixelDungeon.immerse(checked());
        }
      };
      chkImmersive.setRect(0, btnOrientation.bottom() + GAP_TINY, WIDTH,
              BTN_HEIGHT);
      chkImmersive.checked(DarkestPixelDungeon.immersed());
      chkImmersive.enable(android.os.Build.VERSION.SDK_INT >= 19);
      add(chkImmersive);


      boolean enableDebug = false;
      if (!enableDebug)
        DarkestPixelDungeon.debug(false);
      // add debug checkbox
      CheckBox chkDebug = new CheckBox(Messages.get(this, "debug")) {
        @Override
        protected void onClick() {
          super.onClick();
          DarkestPixelDungeon.debug(checked());
          if (checked())
            parent.add(new WndMessage(Messages.get(
                    WndSettings.ScreenTab.class, "debug_warning")));
        }
      };
      chkDebug.setRect(0, chkImmersive.bottom() + GAP_TINY, WIDTH, BTN_HEIGHT);
      chkDebug.checked(DarkestPixelDungeon.debug());
      chkDebug.enable(enableDebug);
      add(chkDebug);
    }
  }

  private class UITab extends Group {

    public UITab(boolean lang) {
      super();

      RenderedText barDesc = PixelScene.renderText(Messages.get(this, "mode")
              , 9);
      barDesc.x = (WIDTH - barDesc.width()) / 2;
      PixelScene.align(barDesc);
      add(barDesc);

      RedButton btnSplit = new RedButton(Messages.get(this, "split")) {
        @Override
        protected void onClick() {
          DarkestPixelDungeon.toolbarMode(Toolbar.Mode.SPLIT.name());
          Toolbar.updateLayout();
        }
      };
      btnSplit.setRect(1, barDesc.y + barDesc.baseLine() + GAP_TINY, 36, 16);
      add(btnSplit);

      RedButton btnGrouped = new RedButton(Messages.get(this, "group")) {
        @Override
        protected void onClick() {
          DarkestPixelDungeon.toolbarMode(Toolbar.Mode.GROUP.name());
          Toolbar.updateLayout();
        }
      };
      btnGrouped.setRect(btnSplit.right() + 1, barDesc.y + barDesc.baseLine()
              + GAP_TINY, 36, 16);
      add(btnGrouped);
      btnGrouped.enable(false);

      RedButton btnCentered = new RedButton(Messages.get(this, "center")) {
        @Override
        protected void onClick() {
          DarkestPixelDungeon.toolbarMode(Toolbar.Mode.CENTER.name());
          Toolbar.updateLayout();
        }
      };
      btnCentered.setRect(btnGrouped.right() + 1, barDesc.y + barDesc
              .baseLine() + GAP_TINY, 36, 16);
      add(btnCentered);
      btnCentered.enable(false);

      CheckBox chkFlipToolbar = new CheckBox(Messages.get(this,
              "flip_toolbar")) {
        @Override
        protected void onClick() {
          super.onClick();
          DarkestPixelDungeon.flipToolbar(checked());
          Toolbar.updateLayout();
        }
      };
      chkFlipToolbar.setRect(0, btnGrouped.bottom() + GAP_TINY, WIDTH / 2 -
              1, BTN_HEIGHT);
      chkFlipToolbar.checked(DarkestPixelDungeon.flipToolbar());
      add(chkFlipToolbar);
      chkFlipToolbar.enable(false);

      final CheckBox chkFlipTags = new CheckBox(Messages.get(this,
              "flip_indicators")) {
        @Override
        protected void onClick() {
          super.onClick();
          DarkestPixelDungeon.flipTags(checked());
          GameScene.layoutTags();
        }
      };
      chkFlipTags.setRect(WIDTH / 2 + 1, chkFlipToolbar.top(), WIDTH / 2 - 1,
              BTN_HEIGHT);
      chkFlipTags.checked(DarkestPixelDungeon.flipTags());
      add(chkFlipTags);
      chkFlipTags.enable(false);

      float btm = chkFlipTags.bottom();
      if (lang) {
        RedButton btnLanguage = new RedButton(Messages.get(this, "language")) {
          @Override
          protected void onClick() {
            ((WndSettings) parent.parent).parent.add(new WndLangs());
            ((WndSettings) parent.parent).hide();
            // parent.add(new WndLangs());
          }
        };
        btnLanguage.setRect(0, chkFlipToolbar.bottom() + GAP_TINY, WIDTH,
                BTN_HEIGHT);
        add(btnLanguage);

        btm = btnLanguage.bottom();
      }

      OptionSlider slots = new OptionSlider(Messages.get(this, "quickslots"),
              "0", "8", 0, 8) {
        @Override
        protected void onChange() {
          DarkestPixelDungeon.quickSlots(getSelectedValue());
          Toolbar.updateLayout();
        }
      };
      slots.setSelectedValue(DarkestPixelDungeon.quickSlots());
      slots.setRect(0, btm + GAP_TINY, WIDTH, SLIDER_HEIGHT);
      add(slots);

      CheckBox chkMoreSlots = new CheckBox(M.INSTANCE.L(this, "more_slots")){
        @Override
        protected void onClick() {
          super.onClick();
          DarkestPixelDungeon.moreQuickSlots(checked());
          Toolbar.updateLayout();
        }
      };
      chkMoreSlots.setRect(0, slots.bottom()+ GAP_SML, WIDTH, BTN_HEIGHT);
      chkMoreSlots.checked(DarkestPixelDungeon.moreQuickSlots());
      add(chkMoreSlots);

      CheckBox chkFont = new CheckBox(Messages.get(this, "smooth_font")) {
        @Override
        protected void onClick() {
          super.onClick();
          DarkestPixelDungeon.switchNoFade((Class<? extends PixelScene>)
                  DarkestPixelDungeon.scene().getClass(), new Game
                  .SceneChangeCallback() {
            @Override
            public void beforeCreate() {
              DarkestPixelDungeon.classicFont(!checked());
            }

            @Override
            public void afterCreate() {
              Game.scene().add(new WndSettings());
            }
          });
        }
      };
      chkFont.setRect(0, chkMoreSlots.bottom() + GAP_SML, WIDTH, BTN_HEIGHT);
      chkFont.checked(!DarkestPixelDungeon.classicFont());
      add(chkFont);


    }

  }

  private class AudioTab extends Group {

    public AudioTab() {
      OptionSlider musicVol = new OptionSlider(Messages.get(this,
              "music_vol"), "0", "10", 0, 10) {
        @Override
        protected void onChange() {
          Music.INSTANCE.volume(getSelectedValue() / 10f);
          DarkestPixelDungeon.musicVol(getSelectedValue());
        }
      };
      musicVol.setSelectedValue(DarkestPixelDungeon.musicVol());
      musicVol.setRect(0, 0, WIDTH, SLIDER_HEIGHT);
      add(musicVol);

      CheckBox musicMute = new CheckBox(Messages.get(this, "music_mute")) {
        @Override
        protected void onClick() {
          super.onClick();
          DarkestPixelDungeon.music(!checked());
        }
      };
      musicMute.setRect(0, musicVol.bottom() + GAP_SML, WIDTH, BTN_HEIGHT);
      musicMute.checked(!DarkestPixelDungeon.music());
      add(musicMute);


      OptionSlider SFXVol = new OptionSlider(Messages.get(this, "sfx_vol"),
              "0", "10", 0, 10) {
        @Override
        protected void onChange() {
          Sample.INSTANCE.volume(getSelectedValue() / 10f);
          DarkestPixelDungeon.SFXVol(getSelectedValue());
        }
      };
      SFXVol.setSelectedValue(DarkestPixelDungeon.SFXVol());
      SFXVol.setRect(0, musicMute.bottom() + GAP_LRG, WIDTH, SLIDER_HEIGHT);
      add(SFXVol);

      CheckBox btnSound = new CheckBox(Messages.get(this, "sfx_mute")) {
        @Override
        protected void onClick() {
          super.onClick();
          DarkestPixelDungeon.soundFx(!checked());
          Sample.INSTANCE.play(Assets.SND_CLICK);
        }
      };
      btnSound.setRect(0, SFXVol.bottom() + GAP_SML, WIDTH, BTN_HEIGHT);
      btnSound.checked(!DarkestPixelDungeon.soundFx());
      add(btnSound);

      resize(WIDTH, (int) btnSound.bottom());
    }

  }
}
