package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import android.provider.MediaStore;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.Journal;
import com.egoal.darkestpixeldungeon.actors.buffs.Weakness;
import com.egoal.darkestpixeldungeon.actors.hero.HeroPerk;
import com.egoal.darkestpixeldungeon.effects.Flare;
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.KGenerator;
import com.egoal.darkestpixeldungeon.items.unclassified.Stylus;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ScrollSellerSprite;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.function.DoubleUnaryOperator;

/**
 * Created by 93942 on 8/26/2018.
 */

public class ScrollSeller extends DPDShopKeeper {
  {
    spriteClass = ScrollSellerSprite.class;
  }

  private int availableCleanTimes = 1;

  @Override
  public DPDShopKeeper initSellItems() {
    addItemToSell(new ScrollOfIdentify());
    addItemToSell(new ScrollOfRemoveCurse());
    int cntItems = Random.Int(1, 4);
    for (int i = 0; i < cntItems; ++i) {
      addItemToSell(KGenerator.SCROLL.INSTANCE.generate());
    }

    Item wand = KGenerator.WAND.INSTANCE.generate();
    wand.cursed = false;
    addItemToSell(wand);
    if (Random.Float() < .25f) {
      Item wand2 = KGenerator.WAND.INSTANCE.generate();
      wand2.cursed = false;
      addItemToSell(wand2);
    }
      
    addItemToSell(new Stylus().identify());

    return this;
  }

  @Override
  public boolean interact() {
    Journal.add(Journal.Feature.SCROLL_SELLER);
    GameScene.show(new WndShop(this));

    return false;
  }

  private int feeClean() {
    return (int) (40 * (Dungeon.depth + 4) *
            (Dungeon.hero.heroPerk.contain(HeroPerk.Perk.SHREWD) ? .75 : 1.));
  }

  protected void onPlayerClean() {
    GameScene.selectItem(selectorClean, WndBag.Mode.UNIDED_OR_CURSED,
            Messages.get(ScrollSeller.class, "select_to_clean"));
  }

  private static final String AVAILABLE_CLEAN_TIMES = "available-clean-times";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(AVAILABLE_CLEAN_TIMES, availableCleanTimes);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    availableCleanTimes = bundle.getInt(AVAILABLE_CLEAN_TIMES);
  }

  private WndBag.Listener selectorClean = new WndBag.Listener() {
    @Override
    public void onSelect(Item item) {
      if (item != null) {
        --availableCleanTimes;
        Dungeon.gold -= feeClean();

        new Flare(6, 32).show(Dungeon.hero.sprite, 2f);
        boolean procced = ScrollOfRemoveCurse.uncurse(Dungeon.hero, item);

        if (procced) {
          Dungeon.hero.sprite.emitter().start(ShadowParticle.UP, 0.05f, 10);
          yell(Messages.get(ScrollSeller.class, "cleansed"));
        } else
          yell(Messages.get(ScrollSeller.class, "not_cleansed"));

      }
    }
  };

  public static class WndShop extends DPDShopKeeper.WndShop {
    public WndShop(final ScrollSeller ss) {
      super(ss);

      final int fee = ss.feeClean();

      RedButton btnClean = new RedButton(Messages.get(ScrollSeller.class,
              "clean", fee)) {
        @Override
        protected void onClick() {
          super.onClick();
          boolean canClean = ss.availableCleanTimes > 0 && Dungeon.gold >= fee;
          enable(canClean);
          if(canClean)
            ss.onPlayerClean();
        }
      };
      btnClean.setRect(0, height + 2f, width, 20f);
      btnClean.enable(ss.availableCleanTimes > 0 && Dungeon.gold >= fee);
      add(btnClean);
      resize(width, (int) btnClean.bottom());
    }
  }
}
