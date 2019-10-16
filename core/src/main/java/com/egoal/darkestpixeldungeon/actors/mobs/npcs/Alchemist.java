package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass;
import com.egoal.darkestpixeldungeon.effects.Flare;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle;
import com.egoal.darkestpixeldungeon.items.unclassified.DewVial;
import com.egoal.darkestpixeldungeon.items.unclassified.Gold;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.unclassified.PotionTestPaper;
import com.egoal.darkestpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.egoal.darkestpixeldungeon.items.potions.*;
import com.egoal.darkestpixeldungeon.items.weapon.curses.Fragile;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.sprites.AlchemistSprite;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.IconTitle;
import com.egoal.darkestpixeldungeon.windows.WndQuest;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Alchemist extends NPC.Unbreakable {
  {
    name = Messages.get(this, "name");
    spriteClass = AlchemistSprite.class;
  }

  @Override
  public boolean interact() {
    sprite.turnTo(pos, Dungeon.hero.pos);

    if (!Quest.hasGiven_) {
      // give quest
      GameScene.show(new WndQuest(this, Messages.get(this, "hello")) {
        @Override
        public void onBackPressed() {
          super.onBackPressed();

          Quest.hasGiven_ = true;
          Quest.hasCompleted_ = false;

          // drop dew vial
          DewVial dv = new DewVial();
          if (dv.doPickUp(Dungeon.hero)) {
            GLog.i(Messages.get(Dungeon.hero, "you_now_have", dv.name()));
          } else
            Dungeon.level.drop(dv, Dungeon.hero.pos).sprite.drop();

          Dungeon.limitedDrops.dewVial.drop();
        }
      });

      // todo: add journal

    } else {
      if (!Quest.hasCompleted_) {
        GameScene.show(new WndAlchemist(this));
      } else {
        tell(Messages.get(this, "farewell"));
      }
    }


    return false;
  }

  // drink and give reward
  public void drink() {
    DewVial dv = Dungeon.hero.getBelongings().getItem(DewVial.class);
    assert (dv != null);
    // drink
    int vol = dv.getVolume();
    // sprite.emitter().burst(Speck.factory(Speck.HEALING), vol>5?2:1);
    new Flare(6, 32).show(sprite, 2f);
    sprite.emitter().start(ShadowParticle.UP, 0.05f, 10);
    GLog.i(Messages.get(this, "drink"));

    Dungeon.hero.spend(1.0f);
    Dungeon.hero.busy();
    Sample.INSTANCE.play(Assets.SND_DRINK);
    // empty dew vial
    dv.empty();

    // give reward
    (new Gold(Random.Int(5, 15)*vol + 20)).doPickUp(Dungeon.hero);

    if (vol >= 5) {
      // give test papers
      for (int i = 0; i < vol / 5 && i < 2; ++i) {
        PotionTestPaper ptp = new PotionTestPaper();
        if (ptp.doPickUp(Dungeon.hero)) {
        } else {
          Dungeon.level.drop(ptp, Dungeon.hero.pos).sprite.drop();
        }
      }

      GLog.i(Messages.get(this, "reward_given"));
    }
    Quest.hasCompleted_ = true;
  }

  @Override
  public String description() {
    return Messages.get(this, "desc");
  }

  // unbreakable
  @Override
  public boolean reset() {
    return true;
  }

  public static class Quest {
    private static boolean hasGiven_ = false;
    private static boolean hasCompleted_ = false;

    public static void reset() {
      hasCompleted_ = false;
      hasGiven_ = false;
    }

    // serialization
    private static final String NODE = "alchemist";
    private static final String GIVEN = "given";
    private static final String COMPLETED = "completed";

    public static void storeInBundle(Bundle bundle) {
      Bundle node = new Bundle();
      node.put(GIVEN, hasGiven_);
      node.put(COMPLETED, hasCompleted_);

      bundle.put(NODE, node);
    }

    public static void restoreFromBundle(Bundle bundle) {
      Bundle node = bundle.getBundle(NODE);

      if (!node.isNull()) {
        hasGiven_ = node.getBoolean(GIVEN);
        hasCompleted_ = node.getBoolean(COMPLETED);
      } else
        reset();
    }

  }

  public class WndAlchemist extends Window {
    private Alchemist alchemist_;

    private static final int WIDTH = 120;
    private static final float GAP = 2.f;
    private static final int BTN_HEIGHT = 20;

    public WndAlchemist(Alchemist alch) {
      super();

      alchemist_ = alch;

      IconTitle titleBar = new IconTitle();
      titleBar.icon(new AlchemistSprite());
      titleBar.label(alchemist_.name);
      titleBar.setRect(0, 0, WIDTH, 0);
      add(titleBar);

      RenderedTextMultiline rtmMessage = PixelScene.renderMultiline(
              Messages.get(this, "back"), 6);
      rtmMessage.maxWidth(WIDTH);
      rtmMessage.setPos(0f, titleBar.bottom() + GAP);
      add(rtmMessage);

      // add buttons
      RedButton btnAgree = new RedButton(Messages.get(this, "yes")) {
        @Override
        protected void onClick() {
          onAnswered();
        }
      };
      btnAgree.setRect(0, rtmMessage.bottom() + GAP, WIDTH, BTN_HEIGHT);
      add(btnAgree);

      RedButton btnDisagree = new RedButton(Messages.get(this, "no")) {
        @Override
        protected void onClick() {
          hide();
          yell(Messages.get(WndAlchemist.class, "wait"));
        }
      };
      btnDisagree.setRect(0, btnAgree.bottom() + GAP, WIDTH, BTN_HEIGHT);
      add(btnDisagree);

      resize(WIDTH, (int) btnDisagree.bottom());
    }

    private void onAnswered() {
      hide();

      DewVial dv = Dungeon.hero.getBelongings().getItem(DewVial.class);
      if (dv == null) {
        GameScene.show(new WndQuest(alchemist_, Messages.get(this, 
                "bottle_miss")));
      } else {
        int vol = dv.getVolume();
        if (vol == 0) {
          GameScene.show(new WndQuest(alchemist_, Messages.get(this, "empty")));
        } else {
          String responds = "";
          if (vol < 5) {
            responds = Messages.get(this, "little");
          } else if (dv.getFull()) {
            responds = Messages.get(this, "full");
          } else
            responds = Messages.get(this, "enough");

          GameScene.show(new WndQuest(alchemist_, responds) {
            @Override
            public void onBackPressed() {
              super.onBackPressed();
              alchemist_.drink();
            }
          });
        }
      }

    }

  }


}
