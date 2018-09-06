package com.egoal.darkestpixeldungeon.actors.buffs;

import android.widget.GridLayout;

import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.BuffIndicator;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

/**
 * Created by 93942 on 9/6/2018.
 */

public class Mending extends Buff {
  {
    type = buffType.POSITIVE;
  }

  public int recoveryValue = 0;

  public Mending set(int value) {
    recoveryValue = value;
    return this;
  }

  @Override
  public boolean attachTo(Char target) {
    if (super.attachTo(target) && !target.immunizedBuffs().contains(Mending
            .class)) {
      if (target instanceof Hero)
        GLog.p(Messages.get(this, "start_mending"));

      return true;
    }

    return false;
  }

  @Override
  public boolean act() {
    int v = (int) Math.ceil(recoveryValue / 2f);
    recoveryValue -=  v;
    if (v <= 1) {
      detach();
    } else {
      target.HP = Math.min(target.HT, target.HP + v);
      target.sprite.emitter().start(Speck.factory(Speck.HEALING), .4f, 4);
    }

    spend(1f);

    return true;
  }

  @Override
  public int icon(){ return BuffIndicator.BLESS; }
  
  @Override
  public String toString() {
    return Messages.get(this, "name");
  }

  @Override
  public String desc() {
    return Messages.get(this, "desc", recoveryValue);
  }

  private static final String RECOVERY_VALUE = "recovery_value";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(RECOVERY_VALUE, recoveryValue);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    recoveryValue = bundle.getInt(RECOVERY_VALUE);
  }
}
