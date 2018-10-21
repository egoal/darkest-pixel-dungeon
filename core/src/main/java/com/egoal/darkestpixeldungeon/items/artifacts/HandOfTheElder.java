package com.egoal.darkestpixeldungeon.items.artifacts;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Roots;
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.MagicMissile;
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.rings.Ring;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.CellSelector;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by 93942 on 10/17/2018.
 */

public class HandOfTheElder extends Artifact {
  {
    image = ItemSpriteSheet.DPD_BONE_HAND;

    levelCap = 10;

    charge = 2;
    chargeCap = 2;

    defaultAction = AC_POINT;
    usesTargeting = true;
  }

  private static final String AC_WEAR = "wear";
  private static final String AC_POINT = "point";

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    if (level() < levelCap)
      actions.add(AC_WEAR);
    if (isEquipped(hero) && charge > 0)
      actions.add(AC_POINT);

    return actions;
  }

  @Override
  public void execute(Hero hero, String action) {
    super.execute(hero, action);

    if (action.equals(AC_POINT)) {
      curUser = hero;
      if (!isEquipped(hero)) {
        GLog.i(Messages.get(Artifact.class, "need_to_equip"));
        QuickSlotButton.cancel();
      } else if (charge < 1) {
        GLog.i(Messages.get(this, "no_charge"));
        QuickSlotButton.cancel();
      } else {
        GameScene.selectCell(pointer);
      }

    } else if (action.equals(AC_WEAR)) {
      if (cursed) {
        GLog.w(Messages.get(this, "cannot_wear"));
      } else if (level() < levelCap)
        GameScene.selectItem(itemSelector, WndBag.Mode.RING, Messages.get(this,
                "wear_prompt"));
    }
  }

  private void wearRing(Item ring) {
    upgrade(2);
    
    if (ring.cursed) {
      cursed = true;
      Sample.INSTANCE.play(Assets.SND_CURSED);
      GLog.p(Messages.get(this, "cursed_levelup", ring.name()));
    } else {
      Sample.INSTANCE.play(Assets.SND_ASTROLABE);
      GLog.p(Messages.get(this, "levelup", ring.name()));
    }
  }

  private void pointAt(final Char c) {
    // cost
    charge--;

    // effects
    curUser.sprite.zap(c.pos);
    curUser.spend(1f);
    curUser.busy();

    MagicMissile.slowness(curUser.sprite.parent, curUser.pos, c.pos, new Callback() {
      @Override
      public void call() {
        // affect
        float bonus = cursed ? 1.25f : 1f;
        if (c.properties().contains(Char.Property.MINIBOSS) ||
                c.properties().contains(Char.Property.BOSS))
          bonus = .5f;

        float duration = level() / 2 + 2;

        Buff.prolong(c, Roots.class, duration * bonus);
        Buff.prolong(c, Vulnerable.class, duration * bonus).ratio =
                (float) (1.25f * Math.pow(cursed ? 1.075 : 1.050, level()));
        
        curUser.next();
      }
    });
    
    Sample.INSTANCE.play(Assets.SND_DEGRADE, 1, 1, .8f);
  }

  @Override
  public String desc() {
    String desc = super.desc();
    if (isEquipped(Dungeon.hero)) {
      desc += "\n\n" + Messages.get(this, "desc_equipped");
    }

    return desc;
  }

  // point
  protected CellSelector.Listener pointer = new CellSelector.Listener() {
    @Override
    public void onSelect(Integer cell) {
      if (cell != null) {
        Char c = Actor.findChar(cell);
        if (c != null && c != curUser) {
          pointAt(c);
        }
      }
    }

    @Override
    public String prompt() {
      return Messages.get(HandOfTheElder.class, "point_prompt");
    }
  };

  // put on a ring
  protected WndBag.Listener itemSelector = new WndBag.Listener() {
    @Override
    public void onSelect(Item item) {
      if (item != null && item instanceof Ring) {
        if (item.isIdentified()) {
          Hero hero = Dungeon.hero;
          hero.sprite.operate(hero.pos);
          hero.busy();
          hero.spend(2f);
          hero.sprite.emitter().burst(ElmoParticle.FACTORY, 12);

          wearRing(item);

          item.detach(hero.belongings.backpack);
        } else
          GLog.w(Messages.get(HandOfTheElder.class, "unknown_ring"));
      }

    }
  };

  // buff
  @Override
  protected ArtifactBuff passiveBuff() {
    return new Recharge();
  }

  class Recharge extends ArtifactBuff {

    @Override
    public boolean act() {
      if (charge < chargeCap) {
        partialCharge += 0.025*Math.pow(1.05, level());

        if (partialCharge >= 1f) {
          charge++;
          partialCharge -= 1f;
          if (charge == chargeCap)
            partialCharge = 0;

          updateQuickslot();
        }
      } else
        partialCharge = 0;

      spend(TICK);
      return true;
    }

  }
}
