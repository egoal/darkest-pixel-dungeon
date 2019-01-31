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

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.buffs.Pressure;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.unclassified.Amulet;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.sprites.HeroSprite;
import com.egoal.darkestpixeldungeon.windows.WndHero;
import com.egoal.darkestpixeldungeon.windows.WndJournal;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.windows.WndGame;
import com.watabou.input.Touchscreen.Touch;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.TouchArea;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.ui.Button;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.ColorMath;

// status pane in the game scene
public class StatusPane extends Component {

  private NinePatch bg;
  private Image avatar;
  private float warning;

  private int lastTier = 0;

  private Image rawShielding;
  private Image shieldedHP;
  private Image hp;
  private Image exp;
  private Image san;

  private BossHealthBar bossHP;

  private int lastLvl = -1;

  private BitmapText level;
  private BitmapText depth;

  private DangerIndicator danger;
  private BuffIndicator buffs;
  private Compass compass;

  private ClockIndicator clock;

  private JournalButton btnJournal;
  private MenuButton btnMenu;

  private Toolbar.PickedUpItem pickedUp;

  @Override
  protected void createChildren() {

    bg = new NinePatch(Assets.DPD_STATUS, 0, 0, 128, 36, 85, 0, 45, 0);
    add(bg);

    // hero portrait
    add(new TouchArea(0, 1, 31, 31) {
      @Override
      protected void onClick(Touch touch) {
        Image sprite = Dungeon.hero.sprite;
        if (!sprite.isVisible()) {
          Camera.main.focusOn(sprite);
        }
        GameScene.show(new WndHero());
      }
    });

    // journal
    btnJournal = new JournalButton();
    add(btnJournal);

    btnMenu = new MenuButton();
    add(btnMenu);

    avatar = HeroSprite.avatar(Dungeon.hero.heroClass, lastTier);
    add(avatar);

    int compassTarget = Dungeon.level.exit;
    if (Dungeon.hero != null) {
      if (Dungeon.hero.belongings.getItem(Amulet.class) != null)
        compassTarget = Dungeon.level.entrance;
    }

    compass = new Compass(compassTarget);
    add(compass);

    // hp bar
    rawShielding = new Image(Assets.SHLD_BAR);
    rawShielding.alpha(0.5f);
    add(rawShielding);

    shieldedHP = new Image(Assets.SHLD_BAR);
    add(shieldedHP);

    hp = new Image(Assets.HP_BAR);
    add(hp);

    // sanity
    san = new Image(Assets.DPD_SAN_BAR);
    add(san);

    // exp bar
    exp = new Image(Assets.XP_BAR);
    add(exp);

    // boss hp
    bossHP = new BossHealthBar();
    add(bossHP);

    // the others
    level = new BitmapText(PixelScene.pixelFont);
    level.hardlight(0xFFEBA4);
    add(level);

    depth = new BitmapText(Integer.toString(Dungeon.depth), PixelScene
            .pixelFont);
    depth.hardlight(0xCACFC2);
    depth.measure();
    add(depth);

    danger = new DangerIndicator();
    add(danger);

    clock = new ClockIndicator();
    add(clock);

    buffs = new BuffIndicator(Dungeon.hero);
    add(buffs);

    add(pickedUp = new Toolbar.PickedUpItem());
  }

  @Override
  protected void layout() {

    height = 32;

    bg.size(width, bg.height);

    avatar.x = bg.x + 15 - avatar.width / 2f;
    avatar.y = bg.y + 16 - avatar.height / 2f;
    PixelScene.align(avatar);

    compass.x = avatar.x + avatar.width / 2f - compass.origin.x;
    compass.y = avatar.y + avatar.height / 2f - compass.origin.y;
    PixelScene.align(compass);

    hp.x = shieldedHP.x = rawShielding.x = 30;
    hp.y = shieldedHP.y = rawShielding.y = 3;

    san.x = hp.x;
    san.y = 8;
    // san.scale.x	=	0;

    bossHP.setPos(6 + (width - bossHP.width()) / 2, 20);

    depth.x = width - 35.5f - depth.width() / 2f;
    depth.y = 8f - depth.baseLine() / 2f;
    PixelScene.align(depth);

    danger.setPos(width - danger.width(), 20);

    clock.setPos(width - clock.width(), danger.bottom() + 4);

    buffs.setPos(34, 12);

    btnJournal.setPos(width - 42, 1);

    btnMenu.setPos(width - btnMenu.width(), 1);
  }

  @Override
  public void update() {
    super.update();

    if (needsCompassUpdate) {
      needsCompassUpdate = true;
      compass.visible = false;
      compass.update();
    }

    float health = Dungeon.hero.HP;
    float shield = Dungeon.hero.SHLD;
    float max = Dungeon.hero.HT;

    Pressure p = Dungeon.hero.buff(Pressure.class);
    if (p != null) {
      san.scale.x = Math.max(0, p.pressure / Pressure.MAX_PRESSURE);
    }

    // the avatar effect
    if (!Dungeon.hero.isAlive()) {
      avatar.tint(0x000000, 0.5f);
    } else if ((health / max) < 0.3f) {
      warning += Game.elapsed * 5f * (0.4f - (health / max));
      warning %= 1f;
      avatar.tint(ColorMath.interpolate(warning, 0x660000, 0xCC0000,
              0x660000), 0.5f);
    } else if (p.getLevel() == Pressure.Level.NERVOUS || p.getLevel() ==
            Pressure.Level.COLLAPSE) {
      warning += Game.elapsed * 5f * (0.4f - (health / max));
      warning %= 1f;
      avatar.tint(ColorMath.interpolate(warning, 0x333333, 0x666666,
              0x333333), 0.5f);
    } else {
      avatar.resetColor();
    }

    // bars
    hp.scale.x = Math.max(0, (health - shield) / max);
    shieldedHP.scale.x = health / max;
    rawShielding.scale.x = shield / max;

    exp.scale.x = (width / exp.width) * Dungeon.hero.exp / Dungeon.hero
            .maxExp();

    if (Dungeon.hero.lvl != lastLvl) {

      if (lastLvl != -1) {
        Emitter emitter = (Emitter) recycle(Emitter.class);
        emitter.revive();
        emitter.pos(27, 27);
        emitter.burst(Speck.factory(Speck.STAR), 12);
      }

      lastLvl = Dungeon.hero.lvl;
      level.text(Integer.toString(lastLvl));
      level.measure();
      level.x = 27.5f - level.width() / 2f;
      level.y = 28.0f - level.baseLine() / 2f;
      PixelScene.align(level);
    }

    int tier = Dungeon.hero.tier();
    if (tier != lastTier) {
      lastTier = tier;
      avatar.copy(HeroSprite.avatar(Dungeon.hero.heroClass, tier));
    }
  }

  public void pickup(Item item) {
    pickedUp.reset(item,
            btnJournal.icon.x + btnJournal.icon.width() / 2f,
            btnJournal.icon.y + btnJournal.icon.height() / 2f,
            true);
  }

  //fixme: due to the design, this is used as an interface, not a good idea.
  public static boolean needsKeyUpdate = false;
  public static boolean needsCompassUpdate = false;

  private static class JournalButton extends Button {

    private Image bg;
    //used to display key state to the player
    private Image icon;

    public JournalButton() {
      super();

      width = bg.width + 13; //includes the depth display to the left
      height = bg.height + 4;
    }

    @Override
    protected void createChildren() {
      super.createChildren();

      bg = new Image(Assets.DPD_MENU, 2, 2, 13, 11);
      add(bg);

      icon = new Image(Assets.DPD_MENU, 31, 0, 11, 7);
      add(icon);
      needsKeyUpdate = true;
    }

    @Override
    protected void layout() {
      super.layout();

      bg.x = x + 13;
      bg.y = y + 2;

      icon.x = bg.x + (bg.width() - icon.width()) / 2f;
      icon.y = bg.y + (bg.height() - icon.height()) / 2f;
      PixelScene.align(icon);
    }

    @Override
    public void update() {
      super.update();
      if (needsKeyUpdate)
        updateKeyDisplay();
    }

    public void updateKeyDisplay() {
      needsKeyUpdate = false;

      boolean foundKeys = false;
      boolean blackKey = false;
      boolean specialKey = false;
      int ironKeys = 0;
      for (int i = 1; i <= Math.min(Dungeon.depth, 25); i++) {
        if (Dungeon.hero.belongings.ironKeys[i] > 0 || Dungeon.hero
                .belongings.specialKeys[i] > 0) {
          foundKeys = true;

          if (i < Dungeon.depth) {
            blackKey = true;

          } else {
            if (Dungeon.hero.belongings.specialKeys[i] > 0) {
              specialKey = true;
            }
            ironKeys = Dungeon.hero.belongings.ironKeys[i];
          }
        }
      }

      if (!foundKeys) {
        icon.frame(31, 0, 11, 7);
      } else {
        int left = 46, top = 0, width = 0, height = 7;
        if (blackKey) {
          left = 43;
          width += 3;
        }
        if (specialKey) {
          top = 8;
          width += 3;
        }
        width += ironKeys * 3;
        width = Math.min(width, 9);
        icon.frame(left, top, width, height);
      }
      layout();

    }

    @Override
    protected void onTouchDown() {
      bg.brightness(1.5f);
      icon.brightness(1.5f);
      Sample.INSTANCE.play(Assets.SND_CLICK);
    }

    @Override
    protected void onTouchUp() {
      bg.resetColor();
      icon.resetColor();
    }

    @Override
    protected void onClick() {
      GameScene.show(new WndJournal());
    }

  }

  private static class MenuButton extends Button {

    private Image image;

    public MenuButton() {
      super();

      width = image.width + 4;
      height = image.height + 4;
    }

    @Override
    protected void createChildren() {
      super.createChildren();

      image = new Image(Assets.DPD_MENU, 17, 2, 12, 11);
      add(image);
    }

    @Override
    protected void layout() {
      super.layout();

      image.x = x + 2;
      image.y = y + 2;
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

    @Override
    protected void onClick() {
      GameScene.show(new WndGame());
    }
  }
}
