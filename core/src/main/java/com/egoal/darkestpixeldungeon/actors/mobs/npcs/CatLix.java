package com.egoal.darkestpixeldungeon.actors.mobs.npcs;


import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.Gold;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.Torch;
import com.egoal.darkestpixeldungeon.items.bags.PotionBandolier;
import com.egoal.darkestpixeldungeon.items.food.Food;
import com.egoal.darkestpixeldungeon.items.food.Humanity;
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey;
import com.egoal.darkestpixeldungeon.items.potions.*;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.CatLixSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndCatLix;
import com.egoal.darkestpixeldungeon.windows.WndQuest;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

// import javafx.scene.control.ButtonBar.ButtonData;

// import javax.microedition.khronos.opengles.GL;
import java.util.ArrayList;

public class CatLix extends NPC {
  {
    // name    =   Messages.get(this, "name");
    spriteClass = CatLixSprite.class;

    properties.add(Property.IMMOVABLE);
  }

  private boolean isAnswered_ = false;
  private boolean isPraised_ = false;

  public Gift gift = new Gift();

  public void setAnswered_(boolean praise) {
    isAnswered_ = true;
    isPraised_ = praise;

    // prepare rewards
    gift.identify();

    // gift.addItem(new Humanity());
    gift.addItem(new Food());

    ArrayList<Item> alItems = new ArrayList<>();
    if (isPraised_) {
      alItems.add(new ScrollOfIdentify());
      alItems.add(new ScrollOfMagicMapping());
      alItems.add(new ScrollOfRemoveCurse());
      alItems.add(new ScrollOfLullaby());
    } else {
      alItems.add(new PotionOfHealing());
      alItems.add(new PotionOfExperience());
      alItems.add(new PotionOfMindVision());
      alItems.add(new PotionOfInvisibility());
    }
    gift.addItem(alItems.get(Random.Int(alItems.size())));

    gift.addItem(new SkeletonKey(Dungeon.depth));
  }

  @Override
  public boolean interact() {
    sprite.turnTo(pos, Dungeon.hero.pos);

    if (!isAnswered_)
      GameScene.show(new WndCatLix(this));
    else {
      if (isPraised_) {
        GameScene.show(new WndQuest(this, Messages.get(this, "happy")));
      } else {
        GameScene.show(new WndQuest(this, Messages.get(this, "normal",
                Dungeon.hero.className())));
      }
    }

    return false;
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

  @Override
  protected boolean act() {
    throwItem();
    return super.act();
  }

  @Override
  public int defenseSkill(Char enemy) {
    return 1000;
  }

  @Override
  public int takeDamage(Damage dmg) {
    return 0;
  }

  @Override
  public void add(Buff buff) {
  }

  private static final String ANSWERED = "answered";
  private static final String PRAISED = "praised";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);

    bundle.put(ANSWERED, isAnswered_);
    bundle.put(PRAISED, isPraised_);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);

    isAnswered_ = bundle.getBoolean(ANSWERED);
    isPraised_ = bundle.getBoolean(PRAISED);
  }


  /* gift */
  public static class Gift extends Item {
    {
      stackable = true;
      defaultAction = AC_OPEN;

      name = Messages.get(this, "name");
      image = ItemSpriteSheet.DPD_CAT_GIFT;
    }

    private static final String AC_OPEN = "open";
    private static final float TIME_TO_OPEN = 1f;

    private ArrayList<Item> alItems_ = new ArrayList<Item>();

    @Override
    public String desc() {
      return Messages.get(this, "desc");
    }

    public void addItem(Item item) {
      alItems_.add(item);
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
      ArrayList<String> alActions = super.actions(hero);
      alActions.add(AC_OPEN);

      return alActions;
    }

    @Override
    public void execute(Hero hero, String action) {
      if (action.equals(AC_OPEN))
        open(hero);
      else
        super.execute(hero, action);
    }

    private void open(Hero hero) {
      detach(hero.belongings.backpack);
      hero.spend(TIME_TO_OPEN);
      hero.busy();

      GLog.i(Messages.get(this, "opened"));

      // give items
      for (Item item : alItems_) {
        if (item.doPickUp(hero)) {
          GLog.w(Messages.get(Dungeon.hero, "you_now_have", item.name()));
        } else
          Dungeon.level.drop(item, hero.pos).sprite.drop();
      }

      Sample.INSTANCE.play(Assets.SND_OPEN);
      hero.sprite.operate(hero.pos);
    }

    private static final String ALL_ITEM = "all_items";

    @Override
    public void storeInBundle(Bundle bundle) {
      super.storeInBundle(bundle);

      bundle.put(ALL_ITEM, alItems_);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
      super.restoreFromBundle(bundle);

      for (Bundlable item : bundle.getCollection(ALL_ITEM)) {
        if (item != null)
          alItems_.add((Item) item);
      }
    }
  }
}
