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
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.watabou.noosa.Game;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class AttackIndicator extends Tag {

  private static final float ENABLED = 1.0f;
  private static final float DISABLED = 0.3f;

  private static float delay;

  private static AttackIndicator instance;

  private CharSprite sprite = null;

  private static Mob lastTarget;
  private ArrayList<Mob> candidates = new ArrayList<Mob>();

  public AttackIndicator() {
    super(0xFF4C4C);

    instance = this;
    lastTarget = null;

    setSize(24, 24);
    visible(false);
    enable(false);
  }

  @Override
  protected void createChildren() {
    super.createChildren();
  }

  @Override
  protected void layout() {
    super.layout();

    if (sprite != null) {
      sprite.x = x + (width - sprite.width()) / 2;
      sprite.y = y + (height - sprite.height()) / 2;
      PixelScene.align(sprite);
    }
  }

  @Override
  public void update() {
    super.update();

    if (!bg.visible) {
      enable(false);
      if (delay > 0f) delay -= Game.elapsed;
      if (delay <= 0f) active = false;
    } else {
      delay = 0.75f;
      active = true;

      if (Dungeon.INSTANCE.getHero().isAlive()) {

        enable(Dungeon.INSTANCE.getHero().getReady());

      } else {
        visible(false);
        enable(false);
      }
    }
  }

  private void checkEnemies() {

    candidates.clear();
    int v = Dungeon.INSTANCE.getHero().visibleEnemies();
    for (int i = 0; i < v; i++) {
      Mob mob = Dungeon.INSTANCE.getHero().visibleEnemy(i);
      if (Dungeon.INSTANCE.getHero().canAttack(mob)) {
        candidates.add(mob);
      }
    }

    if (!candidates.contains(lastTarget)) {
      if (candidates.isEmpty()) {
        lastTarget = null;
      } else {
        active = true;
        lastTarget = Random.element(candidates);
        updateImage();
        flash();
      }
    } else {
      if (!bg.visible) {
        active = true;
        flash();
      }
    }

    visible(lastTarget != null);
    enable(bg.visible);
  }

  private void updateImage() {

    if (sprite != null) {
      sprite.killAndErase();
      sprite = null;
    }

    try {
      sprite = lastTarget.getSpriteClass().newInstance();
      active = true;
      sprite.idle();
      sprite.paused = true;
      add(sprite);

      sprite.x = x + (width - sprite.width()) / 2 + 1;
      sprite.y = y + (height - sprite.height()) / 2;
      PixelScene.align(sprite);

    } catch (Exception e) {
      DarkestPixelDungeon.reportException(e);
    }
  }

  private boolean enabled = true;

  private void enable(boolean value) {
    enabled = value;
    if (sprite != null) {
      sprite.alpha(value ? ENABLED : DISABLED);
    }
  }

  private void visible(boolean value) {
    bg.visible = value;
    if (sprite != null) {
      sprite.visible = value;
    }
  }

  @Override
  protected void onClick() {
    if (enabled) {
      if (Dungeon.INSTANCE.getHero().handle(lastTarget.getPos())) {
        Dungeon.INSTANCE.getHero().next();
      }
    }
  }

  public static void target(Char target) {
    lastTarget = (Mob) target;
    instance.updateImage();

    HealthIndicator.instance.target(target);
  }

  public static void updateState() {
    instance.checkEnemies();
  }
}
