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
package com.egoal.darkestpixeldungeon.scenes;

import android.opengl.GLES20;

import com.egoal.darkestpixeldungeon.Badges;
import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.Statistics;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.hero.perks.LevelPerception;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.items.unclassified.Honeypot;
import com.egoal.darkestpixeldungeon.items.bags.SeedPouch;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.egoal.darkestpixeldungeon.levels.RegularLevel;
import com.egoal.darkestpixeldungeon.plants.Plant;
import com.egoal.darkestpixeldungeon.sprites.CharSprite;
import com.egoal.darkestpixeldungeon.sprites.DiscardedItemSprite;
import com.egoal.darkestpixeldungeon.sprites.DollSprite;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.sprites.PlantSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.FogOfWar;
import com.egoal.darkestpixeldungeon.effects.BannerSprites;
import com.egoal.darkestpixeldungeon.effects.BlobEmitter;
import com.egoal.darkestpixeldungeon.effects.EmoIcon;
import com.egoal.darkestpixeldungeon.effects.Flare;
import com.egoal.darkestpixeldungeon.effects.FloatingText;
import com.egoal.darkestpixeldungeon.effects.Ripple;
import com.egoal.darkestpixeldungeon.effects.SpellSprite;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.bags.PotionBandolier;
import com.egoal.darkestpixeldungeon.items.bags.ScrollHolder;
import com.egoal.darkestpixeldungeon.items.bags.WandHolster;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.levels.features.Chasm;
import com.egoal.darkestpixeldungeon.levels.traps.Trap;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.HeroSprite;
import com.egoal.darkestpixeldungeon.sprites.TrapSprite;
import com.egoal.darkestpixeldungeon.ui.ActionIndicator;
import com.egoal.darkestpixeldungeon.ui.AttackIndicator;
import com.egoal.darkestpixeldungeon.ui.Banner;
import com.egoal.darkestpixeldungeon.ui.BusyIndicator;
import com.egoal.darkestpixeldungeon.ui.CustomTileVisual;
import com.egoal.darkestpixeldungeon.ui.GameLog;
import com.egoal.darkestpixeldungeon.ui.HealthIndicator;
import com.egoal.darkestpixeldungeon.ui.LootIndicator;
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton;
import com.egoal.darkestpixeldungeon.ui.ResumeIndicator;
import com.egoal.darkestpixeldungeon.ui.StatusPane;
import com.egoal.darkestpixeldungeon.ui.Toast;
import com.egoal.darkestpixeldungeon.ui.Toolbar;
import com.egoal.darkestpixeldungeon.ui.Window;
import com.egoal.darkestpixeldungeon.windows.WndBag;
import com.egoal.darkestpixeldungeon.windows.WndGame;
import com.egoal.darkestpixeldungeon.windows.WndHero;
import com.egoal.darkestpixeldungeon.windows.WndInfoCell;
import com.egoal.darkestpixeldungeon.windows.WndInfoItem;
import com.egoal.darkestpixeldungeon.windows.WndInfoMob;
import com.egoal.darkestpixeldungeon.windows.WndInfoPlant;
import com.egoal.darkestpixeldungeon.windows.WndInfoTrap;
import com.egoal.darkestpixeldungeon.windows.WndMessage;
import com.egoal.darkestpixeldungeon.windows.WndOptions;
import com.egoal.darkestpixeldungeon.windows.WndStory;
import com.egoal.darkestpixeldungeon.windows.WndTradeItem;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.NoosaScript;
import com.watabou.noosa.NoosaScriptNoLighting;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.GameMath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import javax.microedition.khronos.opengles.GL10;

public class GameScene extends PixelScene {

  static GameScene scene;

  private SkinnedBlock water;
  private DungeonTilemap tiles;
  private FogOfWar fog;
  private DollSprite heroDoll;
  private HeroSprite hero;

  private StatusPane pane;

  private GameLog log;

  private BusyIndicator busy;

  private static CellSelector cellSelector;

  private Group terrain;
  private Group customTiles;
  private Group levelVisuals;
  private Group ripples;
  private Group plants;
  private Group traps;
  private Group heaps;
  private Group mobs;
  private Group emitters;
  private Group effects;
  private Group gases;
  private Group spells;
  private Group statuses;
  private Group emoicons;

  private Toolbar toolbar;
  private Toast prompt;

  private AttackIndicator attack;
  private LootIndicator loot;
  private ActionIndicator action;
  private ResumeIndicator resume;

  @Override
  public void create() {
    Music.INSTANCE.play(Dungeon.level.trackMusic(), true);

    Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f);

    DarkestPixelDungeon.lastClass(Dungeon.hero.getHeroClass().ordinal());

    super.create();
    Camera.main.zoom(GameMath.INSTANCE.gate(minZoom, defaultZoom + DarkestPixelDungeon
            .zoom(), maxZoom));

    scene = this;

    terrain = new Group();
    add(terrain);

    water = new SkinnedBlock(
            Dungeon.level.width() * DungeonTilemap.SIZE,
            Dungeon.level.height() * DungeonTilemap.SIZE,
            Dungeon.level.waterTex()) {

      @Override
      protected NoosaScript script() {
        return NoosaScriptNoLighting.get();
      }

      @Override
      public void draw() {
        //water has no alpha component, this improves performance
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ZERO);
        super.draw();
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
      }
    };
    terrain.add(water);

    tiles = new DungeonTilemap();
    terrain.add(tiles);

    ripples = new Group();
    terrain.add(ripples);

    customTiles = new Group();
    terrain.add(customTiles);

    for (CustomTileVisual visual : Dungeon.level.getCustomTiles()) {
      addCustomTile(visual.create());
    }

    levelVisuals = Dungeon.level.addVisuals();
    add(levelVisuals);

    traps = new Group();
    add(traps);

    int size = Dungeon.level.getTraps().size();
    for (int i = 0; i < size; i++) {
      addTrapSprite(Dungeon.level.getTraps().valueAt(i));
    }

    plants = new Group();
    add(plants);

    size = Dungeon.level.getPlants().size();
    for (int i = 0; i < size; i++) {
      addPlantSprite(Dungeon.level.getPlants().valueAt(i));
    }

    heaps = new Group();
    add(heaps);

    size = Dungeon.level.getHeaps().size();
    for (int i = 0; i < size; i++) {
      addHeapSprite(Dungeon.level.getHeaps().valueAt(i));
    }

    emitters = new Group();
    effects = new Group();
    emoicons = new Group();

    mobs = new Group();
    add(mobs);

    for (Mob mob : Dungeon.level.getMobs()) {
      addMobSprite(mob);
      if (Statistics.INSTANCE.getAmuletObtained()) {
        mob.beckon(Dungeon.hero.pos);
      }
    }

    add(emitters);
    add(effects);

    gases = new Group();
    add(gases);

    for (Blob blob : Dungeon.level.getBlobs().values()) {
      blob.emitter = null;
      addBlobSprite(blob);
    }

    fog = new FogOfWar(Dungeon.level.width(), Dungeon.level.height());
    add(fog);

    spells = new Group();
    add(spells);

    statuses = new Group();
    add(statuses);

    add(emoicons);

//    heroDoll = new DollSprite();
//    heroDoll.place(Dungeon.hero.pos+ 1);
//    heroDoll.updateArmor();
//    mobs.add(heroDoll);
//    heroDoll.addComponencts();

    hero = new HeroSprite();
    hero.place(Dungeon.hero.pos);
    hero.updateArmor();
    mobs.add(hero);

    add(new HealthIndicator());

    add(cellSelector = new CellSelector(tiles));

    pane = new StatusPane();
    pane.camera = uiCamera;
    pane.setSize(uiCamera.width, 0);
    add(pane);

    toolbar = new Toolbar();
    toolbar.camera = uiCamera;
    toolbar.setRect(0, uiCamera.height - toolbar.height(), uiCamera.width,
            toolbar.height());
    add(toolbar);

    attack = new AttackIndicator();
    attack.camera = uiCamera;
    add(attack);

    loot = new LootIndicator();
    loot.camera = uiCamera;
    add(loot);

    action = new ActionIndicator();
    action.camera = uiCamera;
    add(action);

    resume = new ResumeIndicator();
    resume.camera = uiCamera;
    add(resume);

    log = new GameLog();
    log.camera = uiCamera;
    add(log);

    layoutTags();

    busy = new BusyIndicator();
    busy.camera = uiCamera;
    busy.x = 1;
    busy.y = pane.bottom() + 1;
    add(busy);

    switch (InterlevelScene.mode) {
      case RESURRECT:
        ScrollOfTeleportation.Companion.appear(Dungeon.hero, Dungeon.level.getEntrance());
        new Flare(8, 32).color(0xFFFF66, true).show(hero, 2f);
        break;
      case RETURN:
        ScrollOfTeleportation.Companion.appear(Dungeon.hero, Dungeon.hero.pos);
        break;
      case FALL:
        Chasm.INSTANCE.HeroLand();
        break;
      case DESCEND:
        switch (Dungeon.depth) {
          case 1:
            WndStory.showChapter(WndStory.ID_SEWERS);
            break;
          case 6:
            WndStory.showChapter(WndStory.ID_PRISON);
            break;
          case 11:
            WndStory.showChapter(WndStory.ID_CAVES);
            break;
          case 16:
            WndStory.showChapter(WndStory.ID_CITY);
            break;
          case 22:
            WndStory.showChapter(WndStory.ID_HALLS);
            break;
        }
        if (Dungeon.hero.isAlive() && Dungeon.depth != 22) {
          Badges.validateNoKilling();
        }
        break;
      default:
    }

    ArrayList<Item> dropped = Dungeon.droppedItems.get(Dungeon.depth);
    if (dropped != null) {
      for (Item item : dropped) {
        int pos = Dungeon.level.randomRespawnCell();
        if (item instanceof Potion) {
          ((Potion) item).shatter(pos);
        } else if (item instanceof Plant.Seed) {
          Dungeon.level.plant((Plant.Seed) item, pos);
        } else if (item instanceof Honeypot) {
          Dungeon.level.drop(((Honeypot) item).shatter(null, pos), pos);
        } else {
          Dungeon.level.drop(item, pos);
        }
      }
      Dungeon.droppedItems.remove(Dungeon.depth);
    }

    Dungeon.hero.next();

    Camera.main.target = hero;

    if (InterlevelScene.mode != InterlevelScene.Mode.NONE) {
      if (Dungeon.depth < Statistics.INSTANCE.getDeepestFloor()) {
        GLog.h(Messages.get(this, "welcome_back"), Dungeon.depth);
      } else {
        GLog.h(Messages.get(this, "welcome"), Dungeon.depth);
        Sample.INSTANCE.play(Assets.SND_DESCEND);
      }

      switch (Dungeon.level.getFeeling()) {
        case CHASM:
          GLog.w(Messages.get(this, "chasm"));
          break;
        case WATER:
          GLog.w(Messages.get(this, "water"));
          break;
        case GRASS:
          GLog.w(Messages.get(this, "grass"));
          break;
        case DARK:
          GLog.w(Messages.get(this, "dark"));
          break;
        default:
      }

      if(Dungeon.level instanceof RegularLevel &&
              Dungeon.hero.getHeroPerk().has(LevelPerception.class) &&
              ((RegularLevel) Dungeon.level).secretDoors()>0)
        GLog.n(Messages.get(this, "secrets"));

    //   GLog.n(Messages.format("left artifacts: %d", Generator.ARTIFACT.INSTANCE.left()));

      InterlevelScene.mode = InterlevelScene.Mode.NONE;

      fadeIn();
    }
  }

  public void destroy() {

    freezeEmitters = false;

    scene = null;
    Badges.saveGlobal();

    super.destroy();
  }

  @Override
  public synchronized void pause() {
    try {
      Dungeon.saveAll();
      Badges.saveGlobal();
    } catch (IOException e) {
      DarkestPixelDungeon.reportException(e);
    }
  }

  private Thread t;

  @Override
  public synchronized void update() {
    if (Dungeon.hero == null || scene == null) {
      return;
    }

    super.update();

    if (!freezeEmitters) water.offset(0, -5 * Game.elapsed);

    if (!Actor.processing() && (t == null || !t.isAlive()) && Dungeon.hero
            .isAlive()) {
      t = new Thread() {
        @Override
        public void run() {
          Actor.process();
        }
      };
      //if cpu time is limited, game should prefer drawing the current frame
      t.setPriority(Thread.NORM_PRIORITY - 1);
      t.start();
    }

    if (Dungeon.hero.getReady() && Dungeon.hero.paralysed == 0) {
      log.newLine();
    }

    if (tagAttack != attack.active ||
            tagLoot != loot.visible ||
            tagAction != action.visible ||
            tagResume != resume.visible) {

      //we only want to change the layout when new tags pop in, not when 
      // existing ones leave.
      boolean tagAppearing = (attack.active && !tagAttack) ||
              (loot.visible && !tagLoot) ||
              (action.visible && !tagAction) ||
              (resume.visible && !tagResume);

      tagAttack = attack.active;
      tagLoot = loot.visible;
      tagAction = action.visible;
      tagResume = resume.visible;

      if (tagAppearing) layoutTags();
    }

    cellSelector.enable(Dungeon.hero.getReady());
  }

  private boolean tagAttack = false;
  private boolean tagLoot = false;
  private boolean tagAction = false;
  private boolean tagResume = false;

  public static void layoutTags() {

    if (scene == null) return;

    float tagLeft = DarkestPixelDungeon.flipTags() ? 0 : uiCamera.width -
            scene.attack.width();

    if (DarkestPixelDungeon.flipTags()) {
      scene.log.setRect(scene.attack.width(), scene.toolbar.top(), uiCamera
              .width - scene.attack.width(), 0);
    } else {
      scene.log.setRect(0, scene.toolbar.top(), uiCamera.width - scene.attack
              .width(), 0);
    }

    float pos = scene.toolbar.top();

    if (scene.tagAttack) {
      scene.attack.setPos(tagLeft, pos - scene.attack.height());
      scene.attack.flip(tagLeft == 0);
      pos = scene.attack.top();
    }

    if (scene.tagLoot) {
      scene.loot.setPos(tagLeft, pos - scene.loot.height());
      scene.loot.flip(tagLeft == 0);
      pos = scene.loot.top();
    }

    if (scene.tagAction) {
      scene.action.setPos(tagLeft, pos - scene.action.height());
      scene.action.flip(tagLeft == 0);
      pos = scene.action.top();
    }

    if (scene.tagResume) {
      scene.resume.setPos(tagLeft, pos - scene.resume.height());
      scene.resume.flip(tagLeft == 0);
    }
  }

  @Override
  protected void onBackPressed() {
    if (!cancel()) {
      add(new WndGame());
    }
  }

  @Override
  protected void onMenuPressed() {
    if (Dungeon.hero.getReady()) {
      selectItem(null, WndBag.Mode.ALL, null);
    }
  }

  public void addCustomTile(CustomTileVisual visual) {
    customTiles.add(visual.create());
  }

  private void addHeapSprite(Heap heap) {
    ItemSprite sprite = heap.sprite = (ItemSprite) heaps.recycle(ItemSprite
            .class);
    sprite.revive();
    sprite.link(heap);
    heaps.add(sprite);
  }

  private void addDiscardedSprite(Heap heap) {
    heap.sprite = (DiscardedItemSprite) heaps.recycle(DiscardedItemSprite
            .class);
    heap.sprite.revive();
    heap.sprite.link(heap);
    heaps.add(heap.sprite);
  }

  private void addPlantSprite(Plant plant) {
    (plant.sprite = (PlantSprite) plants.recycle(PlantSprite.class)).reset
            (plant);
  }

  private void addTrapSprite(Trap trap) {
    (trap.sprite = (TrapSprite) traps.recycle(TrapSprite.class)).reset(trap);
    trap.sprite.visible = trap.visible;
  }

  private void addBlobSprite(final Blob gas) {
    if (gas.emitter == null) {
      gases.add(new BlobEmitter(gas));
    }
  }

  private void addMobSprite(Mob mob) {
    CharSprite sprite = mob.sprite();
    sprite.visible = Dungeon.visible[mob.pos];
    mobs.add(sprite);
    sprite.link(mob);
  }

  private void prompt(String text) {

    if (prompt != null) {
      prompt.killAndErase();
      prompt = null;
    }

    if (text != null) {
      prompt = new Toast(text) {
        @Override
        protected void onClose() {
          cancel();
        }
      };
      prompt.camera = uiCamera;
      prompt.setPos((uiCamera.width - prompt.width()) / 2, uiCamera.height -
              60);
      add(prompt);
    }
  }

  private void showBanner(Banner banner) {
    banner.camera = uiCamera;
    banner.x = align(uiCamera, (uiCamera.width - banner.width) / 2);
    banner.y = align(uiCamera, (uiCamera.height - banner.height) / 3);
    addToFront(banner);
  }

  // -------------------------------------------------------

  public static void add(Plant plant) {
    if (scene != null) {
      scene.addPlantSprite(plant);
    }
  }

  public static void add(Trap trap) {
    if (scene != null) {
      scene.addTrapSprite(trap);
    }
  }

  public static void add(Blob gas) {
    Actor.add(gas);
    if (scene != null) {
      scene.addBlobSprite(gas);
    }
  }

  public static void add(Heap heap) {
    if (scene != null) {
      scene.addHeapSprite(heap);
    }
  }

  public static void discard(Heap heap) {
    if (scene != null) {
      scene.addDiscardedSprite(heap);
    }
  }

  public static void add(Mob mob) {
    Dungeon.level.getMobs().add(mob);
    Actor.add(mob);
    scene.addMobSprite(mob);
  }

  public static void add(Mob mob, float delay) {
    Dungeon.level.getMobs().add(mob);
    Actor.addDelayed(mob, delay);
    scene.addMobSprite(mob);
  }

  public static void add(EmoIcon icon) {
    scene.emoicons.add(icon);
  }

  public static void effect(Visual effect) {
    scene.effects.add(effect);
  }

  public static Ripple ripple(int pos) {
    if (scene != null) {
      Ripple ripple = (Ripple) scene.ripples.recycle(Ripple.class);
      ripple.reset(pos);
      return ripple;
    } else {
      return null;
    }
  }

  public static SpellSprite spellSprite() {
    return (SpellSprite) scene.spells.recycle(SpellSprite.class);
  }

  public static Emitter emitter() {
    if (scene != null) {
      Emitter emitter = (Emitter) scene.emitters.recycle(Emitter.class);
      emitter.revive();
      return emitter;
    } else {
      return null;
    }
  }

  public static FloatingText status() {
    return scene != null ? (FloatingText) scene.statuses.recycle(FloatingText
            .class) : null;
  }

  public static void pickUp(Item item) {
    scene.toolbar.pickup(item);
  }

  public static void pickUpJournal(Item item) {
    scene.pane.pickup(item);
  }

  public static void resetMap() {
    if (scene != null) {
      scene.tiles.map(Dungeon.level.getMap(), Dungeon.level.width());
    }
    updateFog();
  }

  //updates the whole map
  public static void updateMap() {
    if (scene != null) {
      scene.tiles.updateMap();
    }
  }

  public static void updateMap(int cell) {
    if (scene != null) {
      scene.tiles.updateMapCell(cell);
    }
  }

  public static void discoverTile(int pos, int oldValue) {
    if (scene != null) {
      scene.tiles.discover(pos, oldValue);
    }
  }

  public static void show(Window wnd) {
    cancelCellSelector();
    scene.addToFront(wnd);
  }

  public static void updateFog() {
    if (scene != null)
      scene.fog.updateFog();
  }

  public static void updateFog(int x, int y, int w, int h) {
    if (scene != null) {
      scene.fog.updateFogArea(x, y, w, h);
    }
  }

  public static void afterObserve() {
    if (scene != null) {
      for (Mob mob : Dungeon.level.getMobs()) {
        if (mob.sprite != null)
          mob.sprite.visible = Dungeon.visible[mob.pos];
      }
    }
  }

  public static void flash(int color) {
    scene.fadeIn(0xFF000000 | color, true);
  }

  // color blender
  private static ColorLayer colorLayer = null;

  public static void setColorLayer(int color) {
    if (colorLayer != null) resetColorLayer();
    colorLayer = new ColorLayer(color);
    scene.add(colorLayer);
  }

  public static void resetColorLayer() {
    if (colorLayer != null) colorLayer.remove();
    colorLayer = null;
  }

  public static class ColorLayer extends ColorBlock {
    public ColorLayer(int color) {
      super(uiCamera.width, uiCamera.height, color);
      camera = uiCamera;
    }

    @Override
    public void draw() {
      GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
      super.draw();
      GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    }
  }
  // ^^^ may not a good idea...


  public static void gameOver() {
    Banner gameOver = new Banner(BannerSprites.get(BannerSprites.Type
            .GAME_OVER));
    gameOver.show(0x000000, 1f);
    scene.showBanner(gameOver);

    Sample.INSTANCE.play(Assets.SND_DEATH);
  }

  public static void bossSlain() {
    if (Dungeon.hero.isAlive()) {
      Banner bossSlain = new Banner(BannerSprites.get(BannerSprites.Type
              .BOSS_SLAIN));
      bossSlain.show(0xFFFFFF, 0.3f, 5f);
      scene.showBanner(bossSlain);

      Sample.INSTANCE.play(Assets.SND_BOSS);
    }
  }

  public static void handleCell(int cell) {
    cellSelector.select(cell);
  }

  public static void selectCell(CellSelector.Listener listener) {
    cellSelector.listener = listener;
    if (scene != null)
      scene.prompt(listener.prompt());
  }

  private static boolean cancelCellSelector() {
    if (cellSelector.listener != null && cellSelector.listener !=
            defaultCellListener) {
      cellSelector.cancel();
      return true;
    } else {
      return false;
    }
  }

  public static WndBag selectItem(WndBag.Listener listener, WndBag.Mode mode,
                                  String title) {
    cancelCellSelector();

    WndBag wnd =
            mode == WndBag.Mode.SEED ?
                    WndBag.getBag(SeedPouch.class, listener, mode, title) :
                    mode == WndBag.Mode.SCROLL ?
                            WndBag.getBag(ScrollHolder.class, listener, mode,
                                    title) :
                            mode == WndBag.Mode.POTION ?
                                    WndBag.getBag(PotionBandolier.class,
                                            listener, mode, title) :
                                    mode == WndBag.Mode.WAND ?
                                            WndBag.getBag(WandHolster.class,
                                                    listener, mode, title) :
                                            WndBag.lastBag(listener, mode,
                                                    title);

    scene.addToFront(wnd);

    return wnd;
  }

  public static WndBag selectItem(WndBag.Listener listener, String title,
                                  WndBag.Filter filter) {
    cancelCellSelector();
    WndBag wnd = new WndBag(Dungeon.hero.getBelongings().backpack, listener,
            title, filter);
    scene.addToFront(wnd);
    
    return wnd;
  }

  static boolean cancel() {
    if (Dungeon.hero.getCurAction() != null || Dungeon.hero.getResting()) {

      Dungeon.hero.setCurAction(null);
      Dungeon.hero.setResting(false);
      return true;

    } else {

      return cancelCellSelector();

    }
  }

  public static void ready() {
    selectCell(defaultCellListener);
    QuickSlotButton.cancel();
  }

  public static void examineCell(Integer cell) {
    if (cell == null) {
      return;
    }

    if (cell < 0 || cell > Dungeon.level.length() || (!Dungeon.level.getVisited()[cell] && !Dungeon.level.getMapped()[cell])) {
      GameScene.show(new WndMessage(Messages.get(GameScene.class,
              "dont_know")));
      return;
    }

    ArrayList<String> names = new ArrayList<>();
    final ArrayList<Object> objects = new ArrayList<>();

    if (cell == Dungeon.hero.pos) {
      objects.add(Dungeon.hero);
      names.add(Dungeon.hero.className().toUpperCase(Locale.ENGLISH));
    } else {
      if (Dungeon.visible[cell]) {
        Mob mob = (Mob) Actor.findChar(cell);
        if (mob != null) {
          objects.add(mob);
          names.add(Messages.titleCase(mob.name));
        }
      }
    }

    Heap heap = Dungeon.level.getHeaps().get(cell);
    if (heap != null && heap.seen) {
      objects.add(heap);
      names.add(Messages.titleCase(heap.toString()));
    }

    Plant plant = Dungeon.level.getPlants().get(cell);
    if (plant != null) {
      objects.add(plant);
      names.add(Messages.titleCase(plant.getPlantName()));
    }

    Trap trap = Dungeon.level.getTraps().get(cell);
    if (trap != null && trap.visible) {
      objects.add(trap);
      names.add(Messages.titleCase(trap.name));
    }

    if (objects.isEmpty()) {
      GameScene.show(new WndInfoCell(cell));
    } else if (objects.size() == 1) {
      examineObject(objects.get(0));
    } else {
      GameScene.show(new WndOptions(Messages.get(GameScene.class,
              "choose_examine"),
              Messages.get(GameScene.class, "multiple_examine"), names
              .toArray(new String[names.size()])) {
        @Override
        protected void onSelect(int index) {
          examineObject(objects.get(index));
        }
      });

    }
  }

  public static void examineObject(Object o) {
    if (o == Dungeon.hero) {
      GameScene.show(new WndHero());
    } else if (o instanceof Mob) {
      GameScene.show(new WndInfoMob((Mob) o));
    } else if (o instanceof Heap) {
      Heap heap = (Heap) o;
      if (heap.type == Heap.Type.FOR_SALE && heap.size() == 1 && heap.peek()
              .price() > 0) {
        GameScene.show(new WndTradeItem(heap, false));
      } else {
        GameScene.show(new WndInfoItem(heap));
      }
    } else if (o instanceof Plant) {
      GameScene.show(new WndInfoPlant((Plant) o));
    } else if (o instanceof Trap) {
      GameScene.show(new WndInfoTrap((Trap) o));
    } else {
      GameScene.show(new WndMessage(Messages.get(GameScene.class,
              "dont_know")));
    }
  }


  private static final CellSelector.Listener defaultCellListener = new
          CellSelector.Listener() {
            @Override
            public void onSelect(Integer cell) {
              if (Dungeon.hero.handle(cell)) {
                Dungeon.hero.next();
              }
            }

            @Override
            public String prompt() {
              return null;
            }
          };
}
