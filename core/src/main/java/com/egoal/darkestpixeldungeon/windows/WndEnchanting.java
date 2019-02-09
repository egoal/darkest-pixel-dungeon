package com.egoal.darkestpixeldungeon.windows;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Chrome;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.items.EquipableItem;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.features.EnchantingStation;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.ui.Icons;
import com.egoal.darkestpixeldungeon.ui.ItemSlot;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;

/**
 * Created by 93942 on 7/26/2018.
 */

public class WndEnchanting extends Window {

  private static final int WIDTH = 116;
  private static final float GAP = 2;
  private static final int BTN_SIZE = 36;
  private static final float BTN_GAP = 30;

  private ItemButton btnItemSrc_;
  private ItemButton btnItemTgt_;
  private RedButton btnDone_;
  private ItemButton btnPressed_;

  public WndEnchanting() {
    super();

    IconTitle titlebar = new IconTitle();
    titlebar.icon(DungeonTilemap.tile(Terrain.ENCHANTING_STATION));
    titlebar.label(Messages.get(this, "select"));
    titlebar.setRect(0, 0, WIDTH, 0);
    add(titlebar);

    btnItemSrc_ = new ItemButton() {
      @Override
      protected void onClick() {
        btnPressed_ = btnItemSrc_;
        GameScene.selectItem(itemSelector, WndBag.Mode.ENCHANTABLE,
                Messages.get(WndEnchanting.class, "select_source"));
      }
    };
    btnItemSrc_.setRect((WIDTH - BTN_GAP) / 2 - BTN_SIZE, titlebar.bottom() +
            BTN_GAP, BTN_SIZE, BTN_SIZE);
    add(btnItemSrc_);

    Image arrow = Icons.get(Icons.RESUME);
    add(arrow);
    arrow.x = WIDTH / 2 - arrow.width() / 2;
    arrow.y = btnItemSrc_.centerY() - arrow.height() / 2;

    btnItemTgt_ = new ItemButton() {
      @Override
      protected void onClick() {
        btnPressed_ = btnItemTgt_;
        GameScene.selectItem(itemSelector, WndBag.Mode.ENCHANTABLE,
                Messages.get(WndEnchanting.class, "select_target"));
      }
    };
    btnItemTgt_.setRect(btnItemSrc_.right() + BTN_GAP, btnItemSrc_.top(),
            BTN_SIZE, BTN_SIZE);
    add(btnItemTgt_);

    btnDone_ = new RedButton(Messages.get(this, "transform")) {
      @Override
      protected void onClick() {
        if (EnchantingStation.INSTANCE.Transform(btnItemSrc_.item, btnItemTgt_.item)) {
          // destroy source, collect target
          btnItemSrc_.item(null);
          // btnItemTgt_.item(null);
          hide();
        }
      }
    };
    btnDone_.enable(false);
    btnDone_.setRect(0, btnItemTgt_.bottom() + BTN_GAP, WIDTH, 20);
    add(btnDone_);

    resize(WIDTH, (int) btnDone_.bottom());
  }

  @Override
  public void destroy() {
    // when close, take back the items not used
    if (btnItemSrc_ != null && btnItemSrc_.item != null) {
      if (!btnItemSrc_.item.collect())
        Dungeon.level.drop(btnItemSrc_.item, Dungeon.hero.pos);
    }

    if (btnItemTgt_ != null && btnItemTgt_.item != null) {
      if (!btnItemTgt_.item.collect())
        Dungeon.level.drop(btnItemTgt_.item, Dungeon.hero.pos);
    }

    super.destroy();
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
        // the equipped item showed be take off, in case lack of room space
        if (item instanceof EquipableItem && item.isEquipped(Dungeon.hero)) {
          GameScene.show(new WndMessage(Messages.get(WndEnchanting.class,
                  "first_unequip")));
          return;
        }

        btnPressed_.item(item.detach(Dungeon.hero.belongings.backpack));

        if (btnItemSrc_.item != null && btnItemTgt_.item != null) {
          String result = EnchantingStation.INSTANCE.CanTransform(
                  btnItemSrc_.item, btnItemTgt_.item);
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

  private static class ItemButton extends Component {
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
          WndEnchanting.ItemButton.this.onClick();
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

    public void item(Item item) {
      slot.item(this.item = item);
    }
  }
}
