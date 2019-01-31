package com.egoal.darkestpixeldungeon.items.unclassified;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Chrome;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.hero.HeroSubClass;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.effects.particles.PurpleParticle;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.KindOfWeapon;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHighlyToxicGas;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfToxicGas;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Dazzling;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Unstable;
import com.egoal.darkestpixeldungeon.items.weapon.enchantments.Venomous;
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.egoal.darkestpixeldungeon.items.weapon.melee.SorceressWand;
import com.egoal.darkestpixeldungeon.items.weapon.melee.Spear;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.plants.Blindweed;
import com.egoal.darkestpixeldungeon.plants.Sorrowmoss;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.ui.ItemSlot;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndBag;
import com.egoal.darkestpixeldungeon.windows.WndMessage;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.HashMap;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by 93942 on 4/24/2018.
 */

public class ExtractionFlask extends Item {

  {
    image = ItemSpriteSheet.EXTRACTION_FLASK;

    defaultAction = AC_REFINE;
    unique = true;
  }

  public static final String AC_REFINE = "refine";
  public static final String AC_STRENGTHEN = "strengthen";

  private static final float TIME_TO_EXTRACT = 2;

  private int refinedTimes_ = 0;

  private static final String REFINED = "refined";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(REFINED, refinedTimes_);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    refinedTimes_ = bundle.getInt(REFINED);
  }

  @Override
  public ArrayList<String> actions(Hero hero) {
    ArrayList<String> actions = super.actions(hero);
    actions.add(AC_REFINE);
    // witch's perk
    if (hero.subClass == HeroSubClass.WITCH)
      actions.add(AC_STRENGTHEN);

    return actions;
  }

  @Override
  public void execute(Hero hero, String action) {
    super.execute(hero, action);

    if (action.equals(AC_REFINE)) {
      GameScene.show(new WndExtractionFlask(this, hero, WndExtractionFlask
              .MODE_REFINE));
    } else if (action.equals(AC_STRENGTHEN)) {
      GameScene.show(new WndExtractionFlask(this, hero, WndExtractionFlask
              .MODE_STRENGTHEN));
    }
  }

  @Override
  public String desc() {
    String desc = Messages.get(this, "desc", refinedTimes_);

    if (!cursed) {
      desc += "\n\n" + Messages.get(this, "desc_hint");
    } else {
      desc += "\n\n" + Messages.get(this, "desc_cursed");
    }

    return desc;
  }

  @Override
  public boolean isUpgradable() {
    return false;
  }

  public static String verifyItems(Item item1, Item item2, int mode) {
    if (mode == WndExtractionFlask.MODE_REFINE) {
      DewVial dv = Dungeon.hero.belongings.getItem(DewVial.class);
      if (dv == null || dv.getVolume() < minDewRequire()) {
        return Messages.get(ExtractionFlask.class, "no_water", minDewRequire());
      }
    } else if (mode == WndExtractionFlask.MODE_STRENGTHEN) {
      if (!item2.isIdentified()) {
        return Messages.get(ExtractionFlask.class, "not_identified");
      }
      Potion p = (Potion) item2;

      if (p.reinforced)
        return Messages.get(ExtractionFlask.class, "reinforced");
      else if (!p.canBeReinforced()) {
        return Messages.get(ExtractionFlask.class, "cannot_reinforce");
      }
    }
    return null;
  }

  public void refine(Item item1, Item item2) {
    // spend time
    curUser.sprite.operate(curUser.pos);
    curUser.sprite.centerEmitter().start(PurpleParticle.BURST, 0.05f, 10);
    curUser.spend(TIME_TO_EXTRACT);
    curUser.busy();

    // cast items
    item1.detach(Dungeon.hero.belongings.backpack);
    item2.detach(Dungeon.hero.belongings.backpack);
    DewVial dv = Dungeon.hero.belongings.getItem(DewVial.class);
    dv.setVolume(dv.getVolume() - minDewRequire());

    // more likely to be toxic gas
    Item potion = null;
    if (item1 instanceof Sorrowmoss.Seed || item2 instanceof Sorrowmoss.Seed)
      potion = new PotionOfToxicGas();
    else {
      if (Random.Int(15) == 0)
        GLog.w(Messages.get(ExtractionFlask.class, "refine_failed"));
      else
        potion = Random.Int(5) == 0 ? new PotionOfToxicGas() :
                Generator.random(Generator.Category.POTION);
    }

    if (potion != null) {
      GLog.p(Messages.get(ExtractionFlask.class, "refine", potion.name()));
      if (potion.doPickUp(Dungeon.hero)) {
      } else {
        Dungeon.level.drop(potion, Dungeon.hero.pos).sprite.drop();
      }

      // do inscribe
      KindOfWeapon kow = curUser.belongings.weapon;
      if (kow != null && kow instanceof Weapon) {
        // 0.2-> 0.5
        double x = Math.exp(refinedTimes_ / 4.);
        double ps = x / (x + 1.) * 0.5;

        Weapon wpn = (Weapon) kow;
        if (wpn.STRReq() < curUser.STR() && !wpn.cursed) {
          if (Random.Float() < ps) {
            // succeed
            switch (Random.Int(3)) {
              case 0:
                wpn.enchant(new Venomous());
                break;
              case 1:
                wpn.enchant(new Unstable());
                break;
              case 2:
                wpn.enchant();
                break;
            }
            GLog.w(Messages.get(ExtractionFlask.class, "inscribed"));
          }

        } else {
          GLog.w(Messages.get(ExtractionFlask.class, "cannot_inscribe"));
        }
      }

      // update times
      ++refinedTimes_;
    }
  }

  public void strengthen(Item item1, Item item2) {
    // spend time
    Dungeon.hero.sprite.centerEmitter().start(Speck.factory(Speck.FORGE), 
            0.05f, 10);
    Dungeon.hero.spend(TIME_TO_EXTRACT);
    Dungeon.hero.busy();

    // cast items
    item1.detach(Dungeon.hero.belongings.backpack);
    Item p = item2.detach(Dungeon.hero.belongings.backpack);

    ((Potion) p).reinforce();
    GLog.i(Messages.get(ExtractionFlask.class, "strengthen", item1.name(), 
            item2.name()));
    if (!p.doPickUp(Dungeon.hero)) {
      Dungeon.level.drop(p, Dungeon.hero.pos).sprite.drop();
    }
  }

  public static int minDewRequire() {
    return Dungeon.hero.subClass == HeroSubClass.WITCH ? 3 : 4;
  }

  // todo: may lost items, no restore
  private class WndExtractionFlask extends Window {

    private static final int BTN_SIZE = 36;
    private static final float GAP = 2;
    private static final float BTN_GAP = 10;
    private static final int WIDTH = 116;

    public static final int MODE_REFINE = 0;
    public static final int MODE_STRENGTHEN = 1;

    private ItemButton btnPressed_;
    private ItemButton btnItem1_;
    private ItemButton btnItem2_;
    private RedButton btnDone_;
    private int mode_;

    public WndExtractionFlask(final ExtractionFlask ef, Hero hero, int mode) {
      super();

      mode_ = mode;

      RenderedTextMultiline rtm =
              PixelScene.renderMultiline(Messages.get(this, "prompt"), 6);
      rtm.maxWidth(WIDTH);
      rtm.setPos(GAP, GAP);
      add(rtm);

      // first one is seed
      btnItem1_ = new ItemButton() {
        @Override
        protected void onClick() {
          btnPressed_ = btnItem1_;
          GameScene.selectItem(itemSelector, WndBag.Mode.SEED, Messages.get
                  (WndExtractionFlask.class, "select_seed"));
        }
      };
      btnItem1_.setRect((WIDTH - BTN_GAP) / 2 - BTN_SIZE, rtm.top() + rtm
              .height() + BTN_GAP, BTN_SIZE, BTN_SIZE);
      add(btnItem1_);

      // second one is seed or potion
      final WndBag.Mode wm = mode_ == MODE_REFINE ? WndBag.Mode.SEED : WndBag
              .Mode.POTION;
      final String tip = Messages.get(WndExtractionFlask.class,
              mode_ == MODE_REFINE ? "select_seed" : "select_potion");
      btnItem2_ = new ItemButton() {
        @Override
        protected void onClick() {
          btnPressed_ = btnItem2_;
          GameScene.selectItem(itemSelector, wm, tip);
        }
      };
      btnItem2_.setRect(btnItem1_.right() + BTN_GAP, btnItem1_.top(), 
              BTN_SIZE, BTN_SIZE);
      add(btnItem2_);

      btnDone_ = new RedButton(Messages.get(this, "done")) {
        @Override
        protected void onClick() {
          if (mode_ == MODE_REFINE)
            ef.refine(btnItem1_.item, btnItem2_.item);
          else if (mode_ == MODE_STRENGTHEN)
            ef.strengthen(btnItem1_.item, btnItem2_.item);

          // kill items
          btnItem1_.item(null);
          btnItem2_.item(null);

          hide();
        }
      };
      btnDone_.enable(false);
      btnDone_.setRect(0, btnItem1_.bottom() + BTN_GAP, WIDTH, 20);
      add(btnDone_);

      resize(WIDTH, (int) btnDone_.bottom());
    }

    protected WndBag.Listener itemSelector = new WndBag.Listener() {
      @Override
      public void onSelect(Item item) {
        if (item != null) {
          if (btnPressed_.item != null) {
            // give back
            if (!btnPressed_.item.collect()) {
              Dungeon.level.drop(btnPressed_.item, Dungeon.hero.pos);
            }
          }

          // take from the backpack
          btnPressed_.item(item.detach(Dungeon.hero.belongings.backpack));

          if (btnItem1_.item != null && btnItem2_.item != null) {
            String result = ExtractionFlask.verifyItems(btnItem1_.item, 
                    btnItem2_.item, mode_);
            if (result == null)
              btnDone_.enable(true);
            else {
              GameScene.show(new WndMessage(result));
              btnDone_.enable(false);
            }
          }
        }
      }
    };

    @Override
    public void destroy() {
      // when close, take back the items not used
      if (btnItem1_ != null && btnItem1_.item != null) {
        if (!btnItem1_.item.collect())
          Dungeon.level.drop(btnItem1_.item, Dungeon.hero.pos);
      }

      if (btnItem2_ != null && btnItem2_.item != null) {
        if (!btnItem2_.item.collect())
          Dungeon.level.drop(btnItem2_.item, Dungeon.hero.pos);
      }

      super.destroy();
    }

    // item button
    public class ItemButton extends Component {

      protected NinePatch bg;
      protected ItemSlot slot;

      public Item item = null;

      @Override
      protected void createChildren() {
        super.createChildren();

        bg = Chrome.get(Chrome.Type.BUTTON);
        add(bg);

        slot = new ItemSlot() {
          @Override
          protected void onTouchDown() {
            bg.brightness(1.2f);
            Sample.INSTANCE.play(Assets.SND_CLICK);
          }

          ;

          @Override
          protected void onTouchUp() {
            bg.resetColor();
          }

          @Override
          protected void onClick() {
            WndExtractionFlask.ItemButton.this.onClick();
          }
        };
        slot.enable(true);
        add(slot);
      }

      protected void onClick() {
      }

      @Override
      protected void layout() {
        super.layout();

        bg.x = x;
        bg.y = y;
        bg.size(width, height);

        slot.setRect(x + 2, y + 2, width - 4, height - 4);
      }

      ;

      public void item(Item item) {
        slot.item(this.item = item);
      }
    }
  }
}
