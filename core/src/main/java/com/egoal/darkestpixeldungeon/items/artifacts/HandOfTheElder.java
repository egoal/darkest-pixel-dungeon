package com.egoal.darkestpixeldungeon.items.artifacts;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.blobs.ConfusionGas;
import com.egoal.darkestpixeldungeon.actors.buffs.Bleeding;
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.Burning;
import com.egoal.darkestpixeldungeon.actors.buffs.Cripple;
import com.egoal.darkestpixeldungeon.actors.buffs.FlavourBuff;
import com.egoal.darkestpixeldungeon.actors.buffs.Frost;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.actors.buffs.Poison;
import com.egoal.darkestpixeldungeon.actors.buffs.Roots;
import com.egoal.darkestpixeldungeon.actors.buffs.Shock;
import com.egoal.darkestpixeldungeon.actors.buffs.Slow;
import com.egoal.darkestpixeldungeon.actors.buffs.Vertigo;
import com.egoal.darkestpixeldungeon.actors.buffs.Vulnerable;
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.MagicMissile;
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.rings.Ring;
import com.egoal.darkestpixeldungeon.items.rings.RingOfAccuracy;
import com.egoal.darkestpixeldungeon.items.rings.RingOfCritical;
import com.egoal.darkestpixeldungeon.items.rings.RingOfElements;
import com.egoal.darkestpixeldungeon.items.rings.RingOfEvasion;
import com.egoal.darkestpixeldungeon.items.rings.RingOfForce;
import com.egoal.darkestpixeldungeon.items.rings.RingOfHaste;
import com.egoal.darkestpixeldungeon.items.rings.RingOfMight;
import com.egoal.darkestpixeldungeon.items.rings.RingOfWealth;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.CellSelector;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

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

  private ArrayList<Class> rings = new ArrayList<>();

  private static final HashMap<Class<? extends Ring>,
          Class<? extends FlavourBuff>> RingsToBuffs = new HashMap<>();

  static {
    RingsToBuffs.put(RingOfAccuracy.class, Shock.class);
    RingsToBuffs.put(RingOfCritical.class, Weakness.class);
    RingsToBuffs.put(RingOfElements.class, Frost.class);
    RingsToBuffs.put(RingOfEvasion.class, Paralysis.class);
    RingsToBuffs.put(RingOfForce.class, Vertigo.class);
    RingsToBuffs.put(RingOfHaste.class, Slow.class);
    RingsToBuffs.put(RingOfWealth.class, Blindness.class);
  }

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);

    if (isEquipped(hero)) {
      if (level() < levelCap)
        actions.add(AC_WEAR);
      if (charge > 0)
        actions.add(AC_POINT);
    }

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

  private void wearRing(Ring ring) {
    upgrade(2);

    if (ring.cursed) {
      cursed = true;
      Sample.INSTANCE.play(Assets.SND_CURSED);
      GLog.p(Messages.get(this, "cursed_levelup", ring.name()));
    } else {
      Sample.INSTANCE.play(Assets.SND_ASTROLABE);
      GLog.p(Messages.get(this, "levelup", ring.name()));
    }


    rings.add(ring.getClass());
  }

  private void pointAt(final Char c) {
    // cost
    charge--;

    // effects
    curUser.sprite.zap(c.pos);
    curUser.spend(1f);
    curUser.busy();

    MagicMissile.slowness(curUser.sprite.parent, curUser.pos, c.pos, new
            Callback() {
              @Override
              public void call() {
                // affect
                float bonus = cursed ? 1.25f : 1f;
                if (c.properties().contains(Char.Property.MINIBOSS) ||
                        c.properties().contains(Char.Property.BOSS))
                  bonus = .5f;

                float duration = (level() / 2 + 2) * bonus;

                // root & damage
                int dmgValue = Random.Int(c.HT / 10, c.HT / 5);
                c.takeDamage(new Damage(dmgValue, curUser, c).type
                        (Damage.Type.MAGICAL).addElement(Damage.Element
                        .SHADOW));

                if (c.isAlive()) {
                  Buff.prolong(c, Roots.class, duration);

                  // add buffs refer to the rings
                  for (int i = 0; i < rings.size(); ++i) {
                    Class<? extends FlavourBuff> buff = RingsToBuffs.get(rings
                            .get(i));
                    if (buff == null)
                      buff = Cripple.class;

                    if (buff == Vulnerable.class)
                      Buff.append(c, Vulnerable.class, duration).ratio = 1.5f;
                    else
                      Buff.append(c, buff, duration);
                  }
                }

                curUser.next();
              }
            });

    Sample.INSTANCE.play(Assets.SND_ASTROLABE, 1, 1, .8f);
  }

  @Override
  public String desc() {
    String desc = super.desc();
    if (isEquipped(Dungeon.hero)) {
      desc += "\n\n" + Messages.get(this, "desc_equipped");
      if (rings.size() > 0) {
        desc += "\n\n" + Messages.get(this, "desc_rings");
        for (int i = 0; i < rings.size(); ++i)
          desc += "\n" + Messages.get(rings.get(i), "name");
      }
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
        if (!item.isIdentified()) {
          GLog.w(Messages.get(HandOfTheElder.class, "unknown_ring"));
          return;
        }

        for (int i = 0; i < rings.size(); ++i) {
          if (rings.get(i).equals(item.getClass())) {
            GLog.w(Messages.get(HandOfTheElder.class, "duplicate_ring"));
            return;
          }
        }

        Hero hero = Dungeon.hero;
        hero.sprite.operate(hero.pos);
        hero.busy();
        hero.spend(2f);
        hero.sprite.emitter().burst(ElmoParticle.FACTORY, 12);

        wearRing((Ring) item);

        item.detach(hero.belongings.backpack);
      }
    }
  };

  // 
  private static final String RINGS = "rings";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(RINGS, rings.toArray(new Class[rings.size()]));
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    rings.clear();
    Collections.addAll(rings, bundle.getClassArray(RINGS));
  }

  // buff
  @Override
  protected ArtifactBuff passiveBuff() {
    return new Recharge();
  }

  class Recharge extends ArtifactBuff {

    @Override
    public boolean act() {
      if (charge < chargeCap) {
        partialCharge += 0.025 * Math.pow(1.05, level());

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
