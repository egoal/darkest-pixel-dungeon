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
package com.egoal.darkestpixeldungeon.ui;

import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.items.unclassified.Torch;
import com.egoal.darkestpixeldungeon.scenes.CellSelector;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndBag;
import com.egoal.darkestpixeldungeon.windows.WndCatalogs;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;
import com.watabou.noosa.ui.Component;

import java.util.NavigableMap;

public class Toolbar extends Component {

  private Tool btnWait;
  private Tool btnSearch;
  private Tool btnInventory;
  private QuickslotTool[] btnQuick;
  private Tool btnSwitchSlots;

  private PickedUpItem pickedUp;

  private boolean lastEnabled = true;
  private boolean examining = false;

  private static Toolbar instance;

  private static final int NUM_QUICK_SLOTS = 8;
  private static final int TAB_QUICK_SLOTS = 2;
  private static final int TOTAL_SLOTS_COUNT = NUM_QUICK_SLOTS* TAB_QUICK_SLOTS;

  private int currentQuickSlotTab = 0;

  public enum Mode {
    SPLIT,
    GROUP,
    CENTER
  }

  public Toolbar() {
    super();

    instance = this;

    height = btnInventory.height();
  }

  @Override
  protected void createChildren() {

    add(btnWait = new Tool(24, 0, 20, 24) {
      @Override
      protected void onClick() {
        examining = false;
        Dungeon.INSTANCE.getHero().rest(false);
      }

      protected boolean onLongClick() {
        examining = false;
        Dungeon.INSTANCE.getHero().rest(true);
        return true;
      }

      ;
    });

    add(btnSearch = new Tool(44, 0, 20, 24) {
      @Override
      protected void onClick() {
        if (!examining) {
          GameScene.selectCell(informer);
          examining = true;
        } else {
          informer.onSelect(null);
          Dungeon.INSTANCE.getHero().search(true);
        }
      }

      @Override
      protected boolean onLongClick() {
        Dungeon.INSTANCE.getHero().search(true);
        return true;
      }
    });

    btnSwitchSlots = new Tool(125, 0, 15, 24){
      @Override
      protected void onClick() {
        Sample.INSTANCE.play(Assets.SND_CLICK);
        currentQuickSlotTab = (currentQuickSlotTab+ 1)% TAB_QUICK_SLOTS;
        updateLayout();
      }
    };
    add(btnSwitchSlots);

    btnQuick = new QuickslotTool[TOTAL_SLOTS_COUNT];
    for(int i=TOTAL_SLOTS_COUNT-1; i>=0; --i)
      add(btnQuick[i]= new QuickslotTool(64, 0, 22, 24, i));

    add(btnInventory = new Tool(0, 0, 24, 26) {
      private GoldIndicator gold;

      @Override
      protected void onClick() {
        GameScene.show(new WndBag(Dungeon.INSTANCE.getHero().getBelongings().getBackpack(), null,
                WndBag.Mode.ALL, null));
      }

      protected boolean onLongClick() {
        GameScene.show(new WndCatalogs());
        return true;
      }

      ;

      @Override
      protected void createChildren() {
        super.createChildren();
        gold = new GoldIndicator();
        add(gold);
      }

      ;

      @Override
      protected void layout() {
        super.layout();
        gold.fill(this);
      }

      ;
    });

    add(pickedUp = new PickedUpItem());
  }

  @Override
  protected void layout() {
    // the ys for slots: extra slots is put outside the screen
    int visibleY = (int)y + 2;
    int invisibleY = (int)y + 26;

    int slots = DarkestPixelDungeon.quickSlots();
    boolean moreSlots = DarkestPixelDungeon.moreQuickSlots();

    for(int t=0; t< TAB_QUICK_SLOTS; ++t) {
      for (int i = 0; i < NUM_QUICK_SLOTS; ++i) {
        QuickslotTool slot = btnQuick[t* NUM_QUICK_SLOTS + i];
        slot.border(0, 0);
        slot.frame(88, 0, 18, 24);
        if(i== slots-1 && !moreSlots){
          slot.border(0, 0);
          slot.frame(86, 0, 20, 24);
        }
      }
    }

    if(moreSlots){
      btnSwitchSlots.frame(125+ 15* currentQuickSlotTab, 0, 15, 24);
    }

    // Mode.valueOf(DarkestPixelDungeon.toolbarMode())
    // todo: many enable mode angin, i mean, someday...
    // split mode



    float right = width;

    if (!DarkestPixelDungeon.landscape() && slots > 3) {
      // if more than 3 quick buttons is used, move up
      // 4 lines, text height is 6
      btnSearch.setPos(x, y - btnQuick[0].height() - 6 * 5);
      btnWait.setPos(x, btnSearch.top() - btnSearch.height());
    } else {
      // left bottom corner
      btnWait.setPos(x, y + 2);
      btnSearch.setPos(btnWait.right(), btnWait.top());
    }

    // bottom right
    btnInventory.setPos(right - btnInventory.width(), y);

    // layout the quick slots
    float switchRight = 0f;

    for(int t=0; t<TAB_QUICK_SLOTS; ++t){
      boolean isCurrent = t== currentQuickSlotTab;
      int index = t* NUM_QUICK_SLOTS;

      if(!isCurrent){
        for(int i=0; i<NUM_QUICK_SLOTS; ++i) {
          btnQuick[index+ i].setPos(0f, invisibleY);
        }
      }else{
        float left = btnInventory.left();
        for(int i=0; i<NUM_QUICK_SLOTS; ++i){
          QuickslotTool slot = btnQuick[index+ i];
          slot.setPos(left- slot.width(), i< slots? visibleY: invisibleY);
          left = slot.left();

          if((i+1)== slots) switchRight= left;
        }
      }
    }
    btnSwitchSlots.setPos(switchRight- btnSwitchSlots.width(), moreSlots? visibleY: invisibleY);
  }

  public static void updateLayout() {
    if (instance != null) instance.layout();
  }

  @Override
  public void update() {
    super.update();

    if (lastEnabled != Dungeon.INSTANCE.getHero().getReady()) {
      lastEnabled = Dungeon.INSTANCE.getHero().getReady();

      for (Gizmo tool : members) {
        if (tool instanceof Tool) {
          ((Tool) tool).enable(lastEnabled);
        }
      }
    }

    if (!Dungeon.INSTANCE.getHero().isAlive()) {
      btnInventory.enable(true);
    }
  }

  public void pickup(Item item) {
    pickedUp.reset(item,
            btnInventory.centerX(),
            btnInventory.centerY(),
            false);
  }

  private static CellSelector.Listener informer = new CellSelector.Listener() {
    @Override
    public void onSelect(Integer cell) {
      instance.examining = false;
      GameScene.examineCell(cell);
    }

    @Override
    public String prompt() {
      return Messages.get(Toolbar.class, "examine_prompt");
    }
  };

  private static class Tool extends Button {

    private static final int BGCOLOR = 0x7B8073;

    private Image base;

    public Tool(int x, int y, int width, int height) {
      super();

      hotArea.blockWhenInactive = true;
      frame(x, y, width, height);
    }

    public void frame(int x, int y, int width, int height) {
      base.frame(x, y, width, height);

      this.width = width;
      this.height = height;
    }

    @Override
    protected void createChildren() {
      super.createChildren();

      base = new Image(Assets.TOOLBAR);
      add(base);
    }

    @Override
    protected void layout() {
      super.layout();

      base.x = x;
      base.y = y;
    }

    @Override
    protected void onTouchDown() {
      base.brightness(1.4f);
    }

    @Override
    protected void onTouchUp() {
      if (active) {
        base.resetColor();
      } else {
        base.tint(BGCOLOR, 0.7f);
      }
    }

    public void enable(boolean value) {
      if (value != active) {
        if (value) {
          base.resetColor();
        } else {
          base.tint(BGCOLOR, 0.7f);
        }
        active = value;
      }
    }
  }

  private static class QuickslotTool extends Tool {

    private QuickSlotButton slot;
    private int borderLeft = 2;
    private int borderRight = 2;

    public QuickslotTool(int x, int y, int width, int height, int slotNum) {
      super(x, y, width, height);

      slot = new QuickSlotButton(slotNum);
      add(slot);
    }

    public void border(int left, int right) {
      borderLeft = left;
      borderRight = right;
      layout();
    }

    @Override
    protected void layout() {
      super.layout();
      slot.setRect(x + borderLeft, y + 2, width - borderLeft - borderRight,
              height - 4);
    }

    @Override
    public void enable(boolean value) {
      super.enable(value);
      slot.enable(value);
    }
  }

  public static class PickedUpItem extends ItemSprite {

    private static final float DISTANCE = DungeonTilemap.SIZE;
    private static final float DURATION = 0.2f;

    private float dstX;
    private float dstY;
    private float left;

    private boolean rising = false;

    public PickedUpItem() {
      super();

      originToCenter();

      active =
              visible =
                      false;
    }

    public void reset(Item item, float dstX, float dstY, boolean rising) {
      view(item);

      active =
              visible =
                      true;

      this.rising = rising;

      this.dstX = dstX - ItemSprite.SIZE / 2;
      this.dstY = dstY - ItemSprite.SIZE / 2;
      left = DURATION;

      x = this.dstX - DISTANCE;
      if (rising) y = this.dstY + DISTANCE;
      else y = this.dstY - DISTANCE;
      alpha(1);
    }

    @Override
    public void update() {
      super.update();

      if ((left -= Game.elapsed) <= 0) {

        visible =
                active =
                        false;
        if (emitter != null) emitter.on = false;

      } else {
        float p = left / DURATION;
        scale.set((float) Math.sqrt(p));
        float offset = DISTANCE * p;

        x = dstX - offset;
        if (rising) y = dstY + offset;
        else y = dstY - offset;
      }
    }
  }
}
