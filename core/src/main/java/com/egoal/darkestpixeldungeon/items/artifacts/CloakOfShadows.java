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


import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.unclassified.GreatBlueprint;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class CloakOfShadows extends Artifact implements GreatBlueprint
        .Enchantable {

  {
    image = ItemSpriteSheet.ARTIFACT_CLOAK;

    exp = 0;
    levelCap = 14;

    charge = level() + 6;
    partialCharge = 0;
    chargeCap = level() + 6;

    cooldown = 0;

    defaultAction = AC_STEALTH;

    unique = true;
    bones = false;
  }

  private boolean stealthed = false;
  public boolean enhanced = false;

  public static final String AC_STEALTH = "STEALTH";

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    if (isEquipped(hero) && charge > 1)
      actions.add(AC_STEALTH);
    return actions;
  }

  @Override
  public void execute(Hero hero, String action) {

    super.execute(hero, action);

    if (action.equals(AC_STEALTH)) {

      if (!stealthed) {
        if (!isEquipped(hero))
          GLog.i(Messages.get(Artifact.class, "need_to_equip"));
        else if (cooldown > 0) GLog.i(Messages.get(this, "cooldown", cooldown));
        else if (charge <= 1) GLog.i(Messages.get(this, "no_charge"));
        else {
          stealthed = true;
          hero.spend(1f);
          hero.busy();
          Sample.INSTANCE.play(Assets.SND_MELD);
          activeBuff = activeBuff();
          activeBuff.attachTo(hero);
          if (hero.sprite.parent != null) {
            hero.sprite.parent.add(new AlphaTweener(hero.sprite, 0.4f, 0.4f));
          } else {
            hero.sprite.alpha(0.4f);
          }
          hero.sprite.operate(hero.pos);
        }
      } else {
        stealthed = false;
        activeBuff.detach();
        activeBuff = null;
        hero.spend(1f);
        hero.sprite.operate(hero.pos);
      }

    }
  }

  @Override
  public void activate(Char ch) {
    super.activate(ch);
    if (stealthed) {
      activeBuff = activeBuff();
      activeBuff.attachTo(ch);
    }
  }

  @Override
  public boolean doUnequip(Hero hero, boolean collect, boolean single) {
    if (super.doUnequip(hero, collect, single)) {
      stealthed = false;
      return true;
    } else
      return false;
  }

  @Override
  protected ArtifactBuff passiveBuff() {
    return new cloakRecharge();
  }

  @Override
  protected ArtifactBuff activeBuff() {
    return new cloakStealth();
  }

  @Override
  public Item upgrade() {
    chargeCap++;
    return super.upgrade();
  }

  private static final String STEALTHED = "stealthed";
  private static final String COOLDOWN = "cooldown";
  private static final String ENHANCED = "enhanced";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(STEALTHED, stealthed);
    bundle.put(COOLDOWN, cooldown);
    bundle.put(ENHANCED, enhanced);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    stealthed = bundle.getBoolean(STEALTHED);
    cooldown = bundle.getInt(COOLDOWN);
    enhanced = bundle.getBoolean(ENHANCED);
    if (enhanced) enchantByBlueprint();

    //for pre-0.4.1 saves which may have over-levelled cloaks
    if (level() == 15) {
      level(14);
      chargeCap = 20;
    }
  }

  @Override
  public int price() {
    return 0;
  }

  @Override
  public void enchantByBlueprint() {
    enhanced = true;
    image = ItemSpriteSheet.ARTIFACT_CLOAK_ENHANCED;
  }

  @Override
  public String desc() {
    String desc = super.desc();
    if (enhanced) desc += "\n\n" + Messages.get(this, "enhanced_desc");
    return desc;
  }

  public class cloakRecharge extends ArtifactBuff {
    @Override
    public boolean act() {
      if (charge < chargeCap) {
        LockedFloor lock = target.buff(LockedFloor.class);
        if (!stealthed && (lock == null || lock.regenOn()))
          partialCharge += (1f / (50 - (chargeCap - charge)));

        if (partialCharge >= 1) {
          charge++;
          partialCharge -= 1;
          if (charge == chargeCap) {
            partialCharge = 0;
          }

        }
      } else
        partialCharge = 0;

      if (cooldown > 0)
        cooldown--;

      updateQuickslot();

      spend(TICK);

      return true;
    }

    public boolean enhanced() {
      return enhanced;
    }

    @Override
    public void fx(boolean on) {
//      if(enhanced) {
//        if (on) target.sprite.add(CharSprite.State.BLUR);
//        else target.sprite.remove(CharSprite.State.BLUR);
//      }
    }
  }

  public class cloakStealth extends ArtifactBuff {
    int turnsToCost = 0;

    @Override
    public int icon() {
      return BuffIndicator.INVISIBLE;
    }

    @Override
    public boolean attachTo(Char target) {
      if (super.attachTo(target)) {
        target.invisible++;
        return true;
      } else {
        return false;
      }
    }

    @Override
    public boolean act() {
      if (turnsToCost == 0) charge--;
      if (charge <= 0) {
        detach();
        GLog.w(Messages.get(this, "no_charge"));
        ((Hero) target).interrupt();
      }

      if (turnsToCost == 0) exp += 10 + ((Hero) target).getLvl();

      if (exp >= (level() + 1) * 40 && level() < levelCap) {
        upgrade();
        exp -= level() * 40;
        GLog.p(Messages.get(this, "levelup"));
      }

      if (turnsToCost == 0) turnsToCost = 2;
      else turnsToCost--;
      updateQuickslot();

      spend(TICK);

      return true;
    }

    public void dispel() {
      charge--;

      exp += 10 + ((Hero) target).getLvl();

      if (exp >= (level() + 1) * 40 && level() < levelCap) {
        upgrade();
        exp -= level() * 40;
        GLog.p(Messages.get(this, "levelup"));
      }

      updateQuickslot();
      detach();
    }

    @Override
    public void fx(boolean on) {
      if (on) target.sprite.add(CharSprite.State.INVISIBLE);
      else if (target.invisible == 0)
        target.sprite.remove(CharSprite.State.INVISIBLE);
    }

    @Override
    public String toString() {
      return Messages.get(this, "name");
    }

    @Override
    public String desc() {
      return Messages.get(this, "desc");
    }

    @Override
    public void detach() {
      if (target.invisible > 0)
        target.invisible--;
      stealthed = false;
      cooldown = 6 - (level() / 4);

      updateQuickslot();
      super.detach();
    }
  }
}
