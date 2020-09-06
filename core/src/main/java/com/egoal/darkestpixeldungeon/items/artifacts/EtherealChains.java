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
package com.egoal.darkestpixeldungeon.items.artifacts;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.Chains;
import com.egoal.darkestpixeldungeon.effects.Pushing;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.mechanics.Ballistica;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.CellSelector;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class EtherealChains extends Artifact {

  public static final String AC_CAST = "CAST";

  {
    image = ItemSpriteSheet.ARTIFACT_CHAINS;

    levelCap = 5;
    exp = 0;

    charge = 5;

    defaultAction = AC_CAST;
    usesTargeting = true;
  }

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    if (isEquipped(hero) && charge > 0 && !cursed)
      actions.add(AC_CAST);
    return actions;
  }

  @Override
  public void execute(Hero hero, String action) {

    super.execute(hero, action);

    if (action.equals(AC_CAST)) {

      curUser = hero;

      if (!isEquipped(hero)) {
        GLog.i(Messages.get(Artifact.class, "need_to_equip"));
        QuickSlotButton.cancel();

      } else if (charge < 1) {
        GLog.i(Messages.get(this, "no_charge"));
        QuickSlotButton.cancel();

      } else if (cursed) {
        GLog.w(Messages.get(this, "cursed"));
        QuickSlotButton.cancel();

      } else {
        GameScene.selectCell(caster);
      }

    }
  }

  private CellSelector.Listener caster = new CellSelector.Listener() {

    @Override
    public void onSelect(Integer target) {
      if (target != null && (Dungeon.level.getVisited()[target] || Dungeon.level.getMapped()[target])) {

        //ballistica does not go through walls on pre-rework boss arenas
        // egoal: no, ur free now
//        int missileProperties = Dungeon.bossLevel() ? Ballistica.PROJECTILE :
//                Ballistica.STOP_CHARS | Ballistica.STOP_TARGET;
        int missileProperties = Ballistica.STOP_CHARS| Ballistica.STOP_TARGET;

        final Ballistica chain = new Ballistica(curUser.getPos(), target,
                missileProperties);

        //determine if we're grabbing an enemy, pulling to a location, or 
        // doing nothing.
        if (Actor.Companion.findChar(chain.collisionPos) != null) {
          int newPos = -1;
          for (int i : chain.subPath(1, chain.dist)) {
            if (!Level.Companion.getSolid()[i] && Actor.Companion.findChar(i) == null) {
              newPos = i;
              break;
            }
          }
          if (newPos == -1) {
            GLog.w(Messages.get(EtherealChains.class, "does_nothing"));
          } else {
            final int newMobPos = newPos;
            final Char affected = Actor.Companion.findChar(chain.collisionPos);
            int chargeUse = Dungeon.level.distance(affected.getPos(), newMobPos);
            if (chargeUse > charge) {
              GLog.w(Messages.get(EtherealChains.class, "no_charge"));
              return;
            } else if (affected.properties().contains(Char.Property
                    .IMMOVABLE)) {
              GLog.w(Messages.get(EtherealChains.class, "cant_pull"));
              return;
            } else {
              charge -= chargeUse;
              updateQuickslot();
            }
            curUser.busy();
            curUser.getSprite().parent.add(new Chains(curUser.getPos(), affected.getPos(),
                    new Callback() {
              public void call() {
                Actor.Companion.add(new Pushing(affected, affected.getPos(), newMobPos, new
                        Callback() {
                  public void call() {
                    Dungeon.level.press(newMobPos, affected);
                  }
                }));
                affected.setPos(newMobPos);
                Dungeon.observe();
                GameScene.updateFog();
                curUser.spendAndNext(1f);
              }
            }));
          }

        } else if (Level.Companion.getSolid()[chain.path.get(chain.dist)]
                || (chain.dist > 0 && Level.Companion.getSolid()[chain.path.get(chain.dist -
                1)])
                || (chain.path.size() > chain.dist + 1 && Level.Companion.getSolid()[chain
                .path.get(chain.dist + 1)])
                //if the player is trying to grapple the edge of the map, let
                // them.
                || (chain.path.size() == chain.dist + 1)) {
          int newPos = -1;
          for (int i : chain.subPath(1, chain.dist)) {
            if (!Level.Companion.getSolid()[i] && Actor.Companion.findChar(i) == null) newPos = i;
          }
          if (newPos == -1) {
            GLog.w(Messages.get(EtherealChains.class, "does_nothing"));
          } else {
            final int newHeroPos = newPos;
            int chargeUse = Dungeon.level.distance(curUser.getPos(), newHeroPos);
            if (chargeUse > charge) {
              GLog.w(Messages.get(EtherealChains.class, "no_charge"));
              return;
            } else {
              charge -= chargeUse;
              updateQuickslot();
            }
            curUser.busy();
            curUser.getSprite().parent.add(new Chains(curUser.getPos(), target, new
                    Callback() {
              public void call() {
                Actor.Companion.add(new Pushing(curUser, curUser.getPos(), newHeroPos, new
                        Callback() {
                  public void call() {
                    Dungeon.level.press(newHeroPos, curUser);
                  }
                }));
                curUser.spendAndNext(1f);
                curUser.setPos(newHeroPos);
                Dungeon.observe();
                GameScene.updateFog();
              }
            }));
          }

        } else {
          GLog.i(Messages.get(EtherealChains.class, "nothing_to_grab"));
        }

      }

    }

    @Override
    public String prompt() {
      return Messages.get(EtherealChains.class, "prompt");
    }
  };

  @Override
  protected ArtifactBuff passiveBuff() {
    return new chainsRecharge();
  }

  @Override
  public String desc() {
    String desc = super.desc();

    if (isEquipped(Dungeon.hero)) {
      desc += "\n\n";
      if (cursed)
        desc += Messages.get(this, "desc_cursed");
      else
        desc += Messages.get(this, "desc_equipped");
    }
    return desc;
  }

  public class chainsRecharge extends ArtifactBuff {

    @Override
    public boolean act() {
      int chargeTarget = 5 + (level() * 2);
      LockedFloor lock = target.buff(LockedFloor.class);
      if (charge < chargeTarget && !cursed && (lock == null || lock.regenOn()
      )) {
        partialCharge += 1 / (40f - (chargeTarget - charge) * 2f);
      } else if (cursed && Random.Int(100) == 0) {
        Buff.prolong(target, Cripple.class, 10f);
      }

      if (partialCharge >= 1) {
        partialCharge--;
        charge++;
      }

      updateQuickslot();

      spend(Actor.TICK);

      return true;
    }

    public void gainExp(float levelPortion) {
      if (cursed) return;

      exp += Math.round(levelPortion * 100);

      //past the soft charge cap, gaining  charge from leveling is slowed.
      if (charge > 5 + (level() * 2)) {
        levelPortion *= (5 + ((float) level() * 2)) / charge;
      }
      partialCharge += levelPortion * 10f;

      if (exp > 100 + level() * 50 && level() < levelCap) {
        exp -= 100 + level() * 50;
        GLog.p(Messages.get(this, "levelup"));
        upgrade();
      }

    }
  }
}
