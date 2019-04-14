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
package com.egoal.darkestpixeldungeon.windows;

import android.graphics.RectF;

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.items.food.MysteryMeat;
import com.egoal.darkestpixeldungeon.items.unclassified.Gold;
import com.egoal.darkestpixeldungeon.items.armor.Armor;
import com.egoal.darkestpixeldungeon.items.bags.Bag;
import com.egoal.darkestpixeldungeon.items.food.Blandfruit;
import com.egoal.darkestpixeldungeon.items.food.Food;
import com.egoal.darkestpixeldungeon.items.rings.Ring;
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.plants.Plant;
import com.egoal.darkestpixeldungeon.ui.ItemSlot;
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.hero.Belongings;
import com.egoal.darkestpixeldungeon.items.EquipableItem;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.bags.PotionBandolier;
import com.egoal.darkestpixeldungeon.items.bags.ScrollHolder;
import com.egoal.darkestpixeldungeon.items.bags.SeedPouch;
import com.egoal.darkestpixeldungeon.items.bags.WandHolster;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.items.wands.Wand;
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.egoal.darkestpixeldungeon.items.weapon.missiles.Boomerang;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSpriteSheet;
import com.egoal.darkestpixeldungeon.ui.Icons;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Image;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.audio.Sample;

public class WndBag extends WndTabbed {

  public static enum Mode {
    ALL,
    UNIDENTIFED,
    UNIDED_OR_CURSED,
    UPGRADEABLE,
    QUICKSLOT,
    FOR_SALE,
    WEAPON,
    ARMOR,
    ENCHANTABLE,
    WAND,
    SEED,
    FOOD,
    POTION,
    SCROLL,
    RING,
    EQUIPMENT,
    ALCHEMY
  }

  protected static final int COLS_P = 5;
  protected static final int COLS_L = 6;

  protected static final int SLOT_SIZE = 22;
  protected static final int SLOT_MARGIN = 1;

  protected static final int TITLE_HEIGHT = 12;

  private Listener listener;
  private Filter filter = null;// compatible for  lambda

  private WndBag.Mode mode;
  private String title;

  private int nCols;
  private int nRows;

  protected int count;
  protected int col;
  protected int row;

  private static Mode lastMode;
  private static Bag lastBag;

  public WndBag(Bag bag, Listener listener, Mode mode, String title) {
    super();

    create(bag, listener, mode, title);
  }

  public WndBag(Bag bag, Listener listener, String title, Filter filter) {
    super();
    this.filter = filter;

    create(bag, listener, Mode.ALL, title);
  }

  // second stage constructor...
  private void create(Bag bag, Listener listener, Mode mode, String title) {
    this.listener = listener;
    this.mode = mode;
    this.title = title;

    lastMode = mode;
    lastBag = bag;

    nCols = DarkestPixelDungeon.landscape() ? COLS_L : COLS_P;
    // +6+1 equipments and gold
    nRows = (Belongings.BACKPACK_SIZE + 6 + 1) / nCols +
            ((Belongings.BACKPACK_SIZE + 6 + 1) % nCols > 0 ? 1 : 0);

    int slotsWidth = SLOT_SIZE * nCols + SLOT_MARGIN * (nCols - 1);
    int slotsHeight = SLOT_SIZE * nRows + SLOT_MARGIN * (nRows - 1);

    RenderedText txtTitle = PixelScene.renderText(title != null ? title :
            Messages.titleCase(bag.name()), 9);
    txtTitle.hardlight(TITLE_COLOR);
    txtTitle.x = (int) (slotsWidth - txtTitle.width()) / 2;
    txtTitle.y = (int) (TITLE_HEIGHT - txtTitle.height()) / 2;
    add(txtTitle);

    placeItems(bag);

    resize(slotsWidth, slotsHeight + TITLE_HEIGHT);

    Belongings stuff = Dungeon.hero.getBelongings();
    Bag[] bags = {
            stuff.backpack,
            stuff.getItem(SeedPouch.class),
            stuff.getItem(ScrollHolder.class),
            stuff.getItem(PotionBandolier.class),
            stuff.getItem(WandHolster.class)};

    for (Bag b : bags) {
      if (b != null) {
        BagTab tab = new BagTab(b);
        add(tab);
        tab.select(b == bag);
      }
    }

    layoutTabs();
  }

  public static WndBag lastBag(Listener listener, Mode mode, String title) {

    if (mode == lastMode && lastBag != null &&
            Dungeon.hero.getBelongings().backpack.contains(lastBag)) {

      return new WndBag(lastBag, listener, mode, title);

    } else {

      return new WndBag(Dungeon.hero.getBelongings().backpack, listener, mode,
              title);

    }
  }

  public static WndBag getBag(Class<? extends Bag> bagClass, Listener
          listener, Mode mode, String title) {
    Bag bag = Dungeon.hero.getBelongings().getItem(bagClass);
    return bag != null ?
            new WndBag(bag, listener, mode, title) :
            lastBag(listener, mode, title);
  }

  protected void placeItems(Bag container) {

    // Equipped items
    Belongings stuff = Dungeon.hero.getBelongings();
    placeItem(stuff.weapon != null ? stuff.weapon : new Placeholder
            (ItemSpriteSheet.WEAPON_HOLDER));
    placeItem(stuff.armor != null ? stuff.armor : new Placeholder
            (ItemSpriteSheet.ARMOR_HOLDER));
    placeItem(stuff.helmet != null ? stuff.helmet : new Placeholder
            (ItemSpriteSheet.HELMET_HOLDER));
    placeItem(stuff.misc1 != null ? stuff.misc1 : new Placeholder
            (ItemSpriteSheet.RING_HOLDER));
    placeItem(stuff.misc2 != null ? stuff.misc2 : new Placeholder
            (ItemSpriteSheet.RING_HOLDER));
    placeItem(stuff.misc3 != null ? stuff.misc3 : new Placeholder
            (ItemSpriteSheet.RING_HOLDER));

    boolean backpack = (container == Dungeon.hero.getBelongings().backpack);
    if (!backpack) {
      count = nCols;
      col = 1;
      row = 1;
    }

    // Items in the bag
    for (Item item : container.items) {
      placeItem(item);
    }

    // Free Space
    while (count - (backpack ? 6 : nCols) < container.size) {
      placeItem(null);
    }

    // Gold
    if (container == Dungeon.hero.getBelongings().backpack) {
      row = nRows - 1;
      col = nCols - 1;
      placeItem(new Gold(Dungeon.gold));
    }
  }

  protected void placeItem(final Item item) {

    int x = col * (SLOT_SIZE + SLOT_MARGIN);
    int y = TITLE_HEIGHT + row * (SLOT_SIZE + SLOT_MARGIN);

    add(new ItemButton(item).setPos(x, y));

    if (++col >= nCols) {
      col = 0;
      row++;
    }

    count++;
  }

  @Override
  public void onMenuPressed() {
    if (listener == null) {
      hide();
    }
  }

  @Override
  public void onBackPressed() {
    if (listener != null) {
      listener.onSelect(null);
    }
    super.onBackPressed();
  }

  @Override
  protected void onClick(Tab tab) {
    hide();
    GameScene.show(new WndBag(((BagTab) tab).bag, listener, mode, title));
  }

  @Override
  protected int tabHeight() {
    return 20;
  }

  private class BagTab extends Tab {

    private Image icon;

    private Bag bag;

    public BagTab(Bag bag) {
      super();

      this.bag = bag;

      icon = icon();
      add(icon);
    }

    @Override
    protected void select(boolean value) {
      super.select(value);
      icon.am = selected ? 1.0f : 0.6f;
    }

    @Override
    protected void layout() {
      super.layout();

      icon.copy(icon());
      icon.x = x + (width - icon.width) / 2;
      icon.y = y + (height - icon.height) / 2 - 2 - (selected ? 0 : 1);
      if (!selected && icon.y < y + CUT) {
        RectF frame = icon.frame();
        frame.top += (y + CUT - icon.y) / icon.texture.height;
        icon.frame(frame);
        icon.y = y + CUT;
      }
    }

    private Image icon() {
      if (bag instanceof SeedPouch) {
        return Icons.get(Icons.SEED_POUCH);
      } else if (bag instanceof ScrollHolder) {
        return Icons.get(Icons.SCROLL_HOLDER);
      } else if (bag instanceof WandHolster) {
        return Icons.get(Icons.WAND_HOLSTER);
      } else if (bag instanceof PotionBandolier) {
        return Icons.get(Icons.POTION_BANDOLIER);
      } else {
        return Icons.get(Icons.BACKPACK);
      }
    }
  }

  private static class Placeholder extends Item {
    {
      name = null;
    }

    public Placeholder(int image) {
      this.image = image;
    }

    @Override
    public boolean isIdentified() {
      return true;
    }

    @Override
    public boolean isEquipped(Hero hero) {
      return true;
    }
  }

  private class ItemButton extends ItemSlot {

    private static final int NORMAL = 0x9953564D;
    private static final int EQUIPPED = 0x9991938C;

    private Item item;
    private ColorBlock bg;

    public ItemButton(Item item) {

      super(item);

      this.item = item;
      if (item instanceof Gold) {
        bg.visible = false;
      }

      width = height = SLOT_SIZE;
    }

    @Override
    protected void createChildren() {
      bg = new ColorBlock(SLOT_SIZE, SLOT_SIZE, NORMAL);
      add(bg);

      super.createChildren();
    }

    @Override
    protected void layout() {
      bg.x = x;
      bg.y = y;

      super.layout();
    }

    @Override
    public void item(Item item) {

      super.item(item);
      if (item != null) {

        bg.texture(TextureCache.createSolid(item.isEquipped(Dungeon.hero) ?
                EQUIPPED : NORMAL));
        if (item.cursed && item.cursedKnown) {
          bg.ra = +0.3f;
          bg.ga = -0.15f;
        } else if (!item.isIdentified()) {
          //^ not identified, but mark if cursedKnown
          if ((item instanceof EquipableItem || item instanceof Wand) && item
                  .cursedKnown)
            bg.ba = 0.3f;
          else {
            bg.ra = 0.3f;
            bg.ba = 0.3f;
          }
        }

        if (item.name() == null) {
          enable(false);
        } else {
          enable(filter != null ? filter.enable(item) : filterByMode(item));
          //extra logic for cursed weapons or armor
          if (!active && mode == Mode.UNIDED_OR_CURSED) {
            if (item instanceof Weapon) {
              Weapon w = (Weapon) item;
              enable(w.hasCurseEnchant());
            }
            if (item instanceof Armor) {
              Armor a = (Armor) item;
              enable(a.hasCurseGlyph());
            }
          }
        }
      } else {
        bg.color(NORMAL);
      }
    }
    
    private boolean filterByMode(Item item){
      switch (mode){
        case FOR_SALE:
          return item.price()>0 && !(item.isEquipped(Dungeon.hero) && item.cursed);
        case UPGRADEABLE:
          return item.isUpgradable();
        case UNIDENTIFED:
          return !item.isIdentified();
        case UNIDED_OR_CURSED:
          return (item instanceof EquipableItem || item instanceof Wand) && 
                  (!item.isIdentified() || item.cursed);
        case QUICKSLOT:
          return item.defaultAction!=null;
        case WEAPON:
          return item instanceof MeleeWeapon || item instanceof Boomerang;
        case ARMOR:
          return item instanceof Armor;
        case ENCHANTABLE:
          return item instanceof MeleeWeapon || item instanceof Boomerang|| item instanceof Armor;
        case WAND:
          return item instanceof Wand;
        case SEED:
          return item instanceof Plant.Seed;
        case FOOD:
          return item instanceof Food;
        case POTION:
          return item instanceof Potion;
        case SCROLL:
          return item instanceof Scroll;
        case EQUIPMENT:
          return item instanceof EquipableItem;
        case RING:
          return item instanceof Ring;
        case ALCHEMY:
          return item instanceof Plant.Seed || item instanceof MysteryMeat || 
                  (item instanceof Blandfruit && ((Blandfruit) item).getPotionAttrib()==null);
        case ALL:
          return true;
      }
      
      return false;
    }

    @Override
    protected void onTouchDown() {
      bg.brightness(1.5f);
      Sample.INSTANCE.play(Assets.SND_CLICK, 0.7f, 0.7f, 1.2f);
    }

    ;

    protected void onTouchUp() {
      bg.brightness(1.0f);
    }

    ;

    @Override
    protected void onClick() {
      if (!lastBag.contains(item) && !item.isEquipped(Dungeon.hero)) {

        hide();

      } else if (listener != null) {

        hide();
        listener.onSelect(item);

      } else {

        GameScene.show(new WndItem(WndBag.this, item));

      }
    }

    @Override
    protected boolean onLongClick() {
      if (listener == null && item.defaultAction != null) {
        hide();
        Dungeon.quickslot.setSlot(0, item);
        QuickSlotButton.refresh();
        return true;
      } else {
        return false;
      }
    }
  }

  public interface Listener {
    void onSelect(Item item);
  }

  public interface Filter {
    boolean enable(Item item);
  }
}
