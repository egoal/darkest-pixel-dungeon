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
package com.egoal.darkestpixeldungeon.actors.buffs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.Pushing;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.scenes.CellSelector;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.ui.ActionIndicator;
import com.egoal.darkestpixeldungeon.ui.AttackIndicator;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Combo extends Buff implements ActionIndicator.Action {

  private int count = 0;
  private float comboTime = 0f;
  private int misses = 0;

  @Override
  public int icon() {
    return BuffIndicator.COMBO;
  }

  @Override
  public String toString() {
    return Messages.get(this, "name");
  }

  public void hit() {

    count++;
    comboTime = 4f;
    misses = 0;

    if (count >= 2) {

      ActionIndicator.setAction(this);
      Badges.validateMasteryCombo(count);

      GLog.p(Messages.get(this, "combo", count));

    }

  }

  public void miss() {
    misses++;
    comboTime = 4f;
    if (misses >= 2) {
      detach();
    }
  }

  @Override
  public void detach() {
    super.detach();
    ActionIndicator.clearAction(this);
  }

  @Override
  public boolean act() {
    comboTime -= TICK;
    spend(TICK);
    if (comboTime <= 0) {
      detach();
    }
    return true;
  }

  @Override
  public String desc() {
    String desc = Messages.get(this, "desc");

    if (count >= 10) desc += "\n\n" + Messages.get(this, "fury_desc");
    else if (count >= 8) desc += "\n\n" + Messages.get(this, "crush_desc");
    else if (count >= 6) desc += "\n\n" + Messages.get(this, "slam_desc");
    else if (count >= 4) desc += "\n\n" + Messages.get(this, "cleave_desc");
    else if (count >= 2) desc += "\n\n" + Messages.get(this, "clobber_desc");

    return desc;
  }

  private static final String COUNT = "count";
  private static final String TIME = "combotime";
  private static final String MISSES = "misses";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(COUNT, count);
    bundle.put(TIME, comboTime);
    bundle.put(MISSES, misses);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    count = bundle.getInt(COUNT);
    if (count >= 2) ActionIndicator.setAction(this);
    comboTime = bundle.getFloat(TIME);
    misses = bundle.getInt(MISSES);
  }

  @Override
  public Image getIcon() {
    Image icon;
    if (((Hero) target).getBelongings().weapon != null) {
      icon = new ItemSprite(Dungeon.hero.getBelongings().weapon.image, null);
    } else {
      icon = new ItemSprite(new Item() {
        {
          image = ItemSpriteSheet.WEAPON_HOLDER;
        }
      });
    }

    if (count >= 10) icon.tint(0xFFFF0000);
    else if (count >= 8) icon.tint(0xFFFFCC00);
    else if (count >= 6) icon.tint(0xFFFFFF00);
    else if (count >= 4) icon.tint(0xFFCCFF00);
    else icon.tint(0xFF00FF00);

    return icon;
  }

  @Override
  public void doAction() {
    GameScene.selectCell(finisher);
  }

  private enum finisherType {
    CLOBBER, CLEAVE, SLAM, CRUSH, FURY;
  }

  private CellSelector.Listener finisher = new CellSelector.Listener() {

    private finisherType type;

    @Override
    public void onSelect(Integer cell) {
      if (cell == null) return;
      final Char enemy = Actor.findChar(cell);
      if (enemy == null || !((Hero) target).canAttack(enemy) || target
              .isCharmedBy(enemy)) {
        GLog.w(Messages.get(Combo.class, "bad_target"));
      } else {
        target.sprite.attack(cell, new Callback() {
          @Override
          public void call() {
            if (count >= 10) type = finisherType.FURY;
            else if (count >= 8) type = finisherType.CRUSH;
            else if (count >= 6) type = finisherType.SLAM;
            else if (count >= 4) type = finisherType.CLEAVE;
            else type = finisherType.CLOBBER;
            doAttack(enemy);
          }
        });
      }
    }

    private void doAttack(final Char enemy) {

      AttackIndicator.target(enemy);

      Damage dmg = target.giveDamage(enemy);
      switch (type) {
        case CLOBBER:
          dmg.value *= 0.6f;
          break;
        case CLEAVE:
          dmg.value *= 1.5f;
          break;
        case SLAM:
          // rolls twice
          Damage dmg2 = target.giveDamage(enemy);
          if (dmg2.value > dmg.value) dmg = dmg2;
          dmg.value *= 1.6f;
          break;
        case CRUSH:
          // roll 4 times, take the highest
          for (int i = 1; i < 4; ++i) {
            Damage dmgp = target.giveDamage(enemy);
            if (dmgp.value > dmg.value) dmg = dmgp;
          }
          dmg.value *= 2.5f;
          break;
        case FURY:
          dmg.value *= 0.6f;
          break;
      }

      // normal attack process
      if (dmg.isFeatured(Damage.Feature.PURE)) {
      } else
        dmg = enemy.defendDamage(dmg);
      dmg = target.attackProc(dmg);
      dmg = enemy.defenseProc(dmg);

      enemy.takeDamage(dmg);

      //special effects
      switch (type) {
        case CLOBBER:
          if (enemy.isAlive()) {
            if (!enemy.properties().contains(Char.Property.IMMOVABLE)) {
              for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
                int ofs = PathFinder.NEIGHBOURS8[i];
                if (enemy.pos - target.pos == ofs) {
                  int newPos = enemy.pos + ofs;
                  if ((Level.passable[newPos] || Level.avoid[newPos]) && 
                          Actor.findChar(newPos) == null) {

                    Actor.addDelayed(new Pushing(enemy, enemy.pos, newPos), -1);

                    enemy.pos = newPos;
                    // FIXME
                    if (enemy instanceof Mob) {
                      Dungeon.level.mobPress((Mob) enemy);
                    } else {
                      Dungeon.level.press(newPos, enemy);
                    }

                  }
                  break;
                }
              }
            }
            prolong(enemy, Vertigo.class, Random.NormalIntRange(1, 4));
          }
          break;
        case SLAM:
          target.SHLD = Math.max(target.SHLD, dmg.value / 2);
          break;
        default:
          //nothing
          break;
      }

      if (target.buff(FireImbue.class) != null)
        target.buff(FireImbue.class).proc(enemy);
      if (target.buff(EarthImbue.class) != null)
        target.buff(EarthImbue.class).proc(enemy);

      Sample.INSTANCE.play(Assets.SND_HIT, 1, 1, Random.Float(0.8f, 1.25f));
      enemy.sprite.bloodBurstA(target.sprite.center(), dmg.value);
      enemy.sprite.flash();

      if (!enemy.isAlive()) {
        GLog.i(Messages.capitalize(Messages.get(Char.class, "defeat", enemy
                .name)));
      }

      Hero hero = (Hero) target;

      //Post-attack behaviour
      switch (type) {
        case CLEAVE:
          if (!enemy.isAlive()) {
            //combo isn't reset, but rather increments with a cleave kill, 
            // and grants more time.
            hit();
            comboTime = 10f;
          } else {
            detach();
            ActionIndicator.clearAction(Combo.this);
          }
          hero.spendAndNext(hero.attackDelay());
          break;

        case FURY:
          count--;
          //fury attacks as many times as you have combo count
          if (count > 0 && enemy.isAlive()) {
            target.sprite.attack(enemy.pos, new Callback() {
              @Override
              public void call() {
                doAttack(enemy);
              }
            });
          } else {
            detach();
            ActionIndicator.clearAction(Combo.this);
            hero.spendAndNext(hero.attackDelay());
          }
          break;

        default:
          detach();
          ActionIndicator.clearAction(Combo.this);
          hero.spendAndNext(hero.attackDelay());
          break;
      }

    }

    @Override
    public String prompt() {
      if (count >= 10) return Messages.get(Combo.class, "fury_prompt");
      else if (count >= 8) return Messages.get(Combo.class, "crush_prompt");
      else if (count >= 6) return Messages.get(Combo.class, "slam_prompt");
      else if (count >= 4) return Messages.get(Combo.class, "cleave_prompt");
      else return Messages.get(Combo.class, "clobber_prompt");
    }
  };
}
