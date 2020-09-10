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

import com.egoal.darkestpixeldungeon.Challenge;
import com.egoal.darkestpixeldungeon.QuickSlot;
import com.egoal.darkestpixeldungeon.actors.hero.Belongings;
import com.egoal.darkestpixeldungeon.actors.hero.perks.Perk;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.PixelScene;
import com.egoal.darkestpixeldungeon.sprites.HeroSprite;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.Rankings;
import com.egoal.darkestpixeldungeon.Statistics;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.ui.BadgesList;
import com.egoal.darkestpixeldungeon.ui.Icons;
import com.egoal.darkestpixeldungeon.ui.ItemSlot;
import com.egoal.darkestpixeldungeon.ui.PerkSlot;
import com.egoal.darkestpixeldungeon.ui.RedButton;
import com.egoal.darkestpixeldungeon.ui.RenderedTextMultiline;
import com.egoal.darkestpixeldungeon.ui.ScrollPane;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.RenderedText;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Button;

import java.util.Locale;

public class WndRanking extends WndTabbed {

  private static final int WIDTH = 91;
  private static final int HEIGHT = 182;

  private Thread thread;
  private String error = null;

  private Image busy;

  public WndRanking(final Rankings.Record rec) {

    super();
    resize(WIDTH, HEIGHT);

    thread = new Thread() {
      @Override
      public void run() {
        try {
          Badges.INSTANCE.loadGlobal();
          Rankings.INSTANCE.LoadGameData(rec);
        } catch (Exception e) {
          error = Messages.get(WndRanking.class, "error");
        }
      }
    };
    thread.start();

    busy = Icons.BUSY.get();
    busy.origin.set(busy.width / 2, busy.height / 2);
    busy.angularSpeed = 720;
    busy.x = (WIDTH - busy.width) / 2;
    busy.y = (HEIGHT - busy.height) / 2;
    add(busy);
  }

  @Override
  public void update() {
    super.update();

    if (thread != null && !thread.isAlive()) {
      thread = null;
      if (error == null) {
        remove(busy);
        createControls();
      } else {
        hide();
        Game.scene().add(new WndError(error));
      }
    }
  }

  private void createControls() {

    String[] labels =
            {Messages.get(this, "stats"), Messages.get(this, "perks"),
                    Messages.get(this, "items"), Messages.get(this, "badges")};
    Group[] pages =
            {new StatsTab(), new PerksTab(), new ItemsTab(), new BadgesTab()};

    for (int i = 0; i < pages.length; i++) {

      add(pages[i]);

      Tab tab = new RankingTab(labels[i], pages[i]);
      add(tab);
    }

    layoutTabs();

    select(0);
  }

  private class RankingTab extends LabeledTab {

    private Group page;

    public RankingTab(String label, Group page) {
      super(label);
      this.page = page;
    }

    @Override
    protected void select(boolean value) {
      super.select(value);
      if (page != null) {
        page.visible = page.active = selected;
      }
    }
  }

  private class StatsTab extends Group {

    private int GAP = 6;

    public StatsTab() {
      super();

      String heroClass = Dungeon.hero.className();

      IconTitle title = new IconTitle();
      title.icon(HeroSprite.avatar(Dungeon.hero.getHeroClass(), Dungeon.hero
              .tier
                      ()));
      title.label(Messages.get(this, "title", Dungeon.hero.getLvl(), heroClass)
              .toUpperCase(Locale.ENGLISH));
      title.color(Window.SHPX_COLOR);
      title.setRect(0, 0, WIDTH, 0);
      add(title);

      float pos = title.bottom();

      pos += GAP + GAP;

      pos = statSlot(this, Messages.get(this, "str"), Integer.toString
              (Dungeon.hero.getSTR()), pos);
      pos = statSlot(this, Messages.get(this, "health"), Integer.toString
              (Dungeon.hero.getHT()), pos);

      pos += GAP;

      pos = statSlot(this, Messages.get(this, "duration"), Integer.toString(
              (int) Statistics.INSTANCE.getDuration()), pos);

      pos += GAP;

      pos = statSlot(this, Messages.get(this, "depth"), Integer.toString
              (Statistics.INSTANCE.getDeepestFloor()), pos);
      pos = statSlot(this, Messages.get(this, "highest-damage"), Integer
              .toString(Statistics.INSTANCE.getHighestDamage()), pos);
      pos = statSlot(this, Messages.get(this, "enemies"), Integer.toString
              (Statistics.INSTANCE.getEnemiesSlain()), pos);
      pos = statSlot(this, Messages.get(this, "gold"), Integer.toString
              (Statistics.INSTANCE.getGoldCollected()), pos);

      pos += GAP;

      pos = statSlot(this, Messages.get(this, "food"), Integer.toString
              (Statistics.INSTANCE.getFoodEaten()), pos);
      pos = statSlot(this, Messages.get(this, "alchemy"), Integer.toString
              (Statistics.INSTANCE.getPotionsCooked()), pos);
      pos = statSlot(this, Messages.get(this, "ankhs"), Integer.toString
              (Statistics.INSTANCE.getAnkhsUsed()), pos);
    }

    private float statSlot(Group parent, String label, String value, float
            pos) {

      RenderedText txt = PixelScene.renderText(label, 7);
      txt.y = pos;
      parent.add(txt);

      txt = PixelScene.renderText(value, 7);
      txt.x = WIDTH * 0.65f;
      txt.y = pos;
      PixelScene.align(txt);
      parent.add(txt);

      return pos + GAP + txt.baseLine();
    }
  }

  private class PerksTab extends Group {
    public PerksTab() {
      super();

      int i = 0;
      for (final Perk perk : Dungeon.hero.getHeroPerk().getPerks()) {
        float x = i % 4 * 23f;
        float y = i / 4 * 23f;

        PerkSlot ps = new PerkSlot(perk) {
          @Override
          protected void onClick() {
            Game.scene().add(new WndMessage(perk.description()));
          }
        };
        ps.setRect(x, y, 22f, 22f);

        add(ps);
        ++i;
      }
    }
  }

  private class ItemsTab extends Group {

    private float pos;

    public ItemsTab() {
      super();

      Belongings stuff = Dungeon.hero.getBelongings();
      if (stuff.getWeapon() != null) {
        addItem(stuff.getWeapon());
      }
      if (stuff.getArmor() != null) {
        addItem(stuff.getArmor());
      }
      if (stuff.getHelmet() != null)
        addItem(stuff.getHelmet());
      if (stuff.getMisc1() != null) {
        addItem(stuff.getMisc1());
      }
      if (stuff.getMisc2() != null) {
        addItem(stuff.getMisc2());
      }
      if (stuff.getMisc3() != null) {
        addItem(stuff.getMisc3());
      }

      // quickslots
      pos = 0;
      for (int i = 0; i < QuickSlot.SIZE; i++) {
        float posx = i % 4 * (22 + 1);
        float posy = i / 4 * (22 + 1) + 22 * 6 + 6;
        if (Dungeon.quickslot.getItem(i) != null) {
          QuickSlotButton slot = new QuickSlotButton(Dungeon.quickslot
                  .getItem(i));

          slot.setRect(posx, posy, 22, 22);

          add(slot);

        } else {
          ColorBlock bg = new ColorBlock(22, 22, 0x9953564D);
          bg.x = posx;
          bg.y = posy;
          add(bg);
        }
        // pos += 22+1;
      }
    }

    private void addItem(Item item) {
      ItemButton slot = new ItemButton(item);
      slot.setRect(0, pos, width, ItemButton.HEIGHT);
      add(slot);

      pos += slot.height() + 1;
    }
  }

  private class BadgesTab extends Group {

    public BadgesTab() {
      super();

      camera = WndRanking.this.camera;

      if (Dungeon.hero.getChallenge() != null) {
        //todo: redesign & use icon
        Challenge challenge = Dungeon.hero.getChallenge();
        RenderedText title = PixelScene.renderText(challenge.title(), 6);
        title.x = 2f;
        title.y = 2f;
        add(title);
        RenderedTextMultiline desc = PixelScene.renderMultiline(challenge.desc(), 6);
        desc.maxWidth(WIDTH - 4);
        desc.setPos(2f, title.y + title.height() + 2f);
        add(desc);
      } else {
        ScrollPane list = new BadgesList(false);
        add(list);

        list.setSize(WIDTH, HEIGHT);
      }
    }
  }

  private class ItemButton extends Button {

    public static final int HEIGHT = 22;  // same as bag

    private Item item;

    private ItemSlot slot;
    private ColorBlock bg;
    private RenderedText name;

    public ItemButton(Item item) {

      super();

      this.item = item;

      slot.item(item);
      if (item.getCursed() && item.getCursedKnown()) {
        bg.ra = +0.2f;
        bg.ga = -0.1f;
      } else if (!item.isIdentified()) {
        bg.ra = 0.1f;
        bg.ba = 0.1f;
      }
    }

    @Override
    protected void createChildren() {

      bg = new ColorBlock(HEIGHT, HEIGHT, 0x9953564D);
      add(bg);

      slot = new ItemSlot();
      add(slot);

      name = PixelScene.renderText("?", 7);
      add(name);

      super.createChildren();
    }

    @Override
    protected void layout() {
      bg.x = x;
      bg.y = y;

      slot.setRect(x, y, HEIGHT, HEIGHT);
      PixelScene.align(slot);

      name.x = slot.right() + 2;
      name.y = y + (height - name.baseLine()) / 2;
      PixelScene.align(name);

      String str = Messages.titleCase(item.name());
      name.text(str);
      if (name.width() > width - name.x) {
        do {
          str = str.substring(0, str.length() - 1);
          name.text(str + "...");
        } while (name.width() > width - name.x);
      }

      super.layout();
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
      Game.scene().add(new WndItem(null, item));
    }
  }

  private class QuickSlotButton extends ItemSlot {

    public static final int HEIGHT = 22;

    private Item item;
    private ColorBlock bg;

    QuickSlotButton(Item item) {
      super(item);
      this.item = item;
    }

    @Override
    protected void createChildren() {
      bg = new ColorBlock(HEIGHT, HEIGHT, 0x9953564D);
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
      Game.scene().add(new WndItem(null, item));
    }
  }
}
