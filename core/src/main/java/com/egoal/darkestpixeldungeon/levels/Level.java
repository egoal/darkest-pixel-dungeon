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
package com.egoal.darkestpixeldungeon.levels;

import com.egoal.darkestpixeldungeon.*;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Shadows;
import com.egoal.darkestpixeldungeon.actors.buffs.ViewMark;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.effects.Halo;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.armor.Armor;
import com.egoal.darkestpixeldungeon.items.artifacts.AlchemistsToolkit;
import com.egoal.darkestpixeldungeon.items.food.Food;
import com.egoal.darkestpixeldungeon.items.food.Wine;
import com.egoal.darkestpixeldungeon.items.rings.RingOfWealth;
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfMagicalInfusion;
import com.egoal.darkestpixeldungeon.levels.features.HighGrass;
import com.egoal.darkestpixeldungeon.plants.Plant;
import com.egoal.darkestpixeldungeon.ui.CustomTileVisual;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.blobs.Alchemy;
import com.egoal.darkestpixeldungeon.actors.blobs.Blob;
import com.egoal.darkestpixeldungeon.actors.blobs.WellWater;
import com.egoal.darkestpixeldungeon.actors.buffs.Awareness;
import com.egoal.darkestpixeldungeon.actors.buffs.Blindness;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.buffs.LockedFloor;
import com.egoal.darkestpixeldungeon.actors.buffs.MindVision;
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass;
import com.egoal.darkestpixeldungeon.actors.mobs.Bestiary;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.particles.FlowParticle;
import com.egoal.darkestpixeldungeon.effects.particles.WindParticle;
import com.egoal.darkestpixeldungeon.items.Dewdrop;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.Stylus;
import com.egoal.darkestpixeldungeon.items.Torch;
import com.egoal.darkestpixeldungeon.items.artifacts.DriedRose;
import com.egoal.darkestpixeldungeon.items.artifacts.TimekeepersHourglass;
import com.egoal.darkestpixeldungeon.items.bags.ScrollHolder;
import com.egoal.darkestpixeldungeon.items.bags.SeedPouch;
import com.egoal.darkestpixeldungeon.items.food.Blandfruit;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfHealing;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfMight;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfStrength;
import com.egoal.darkestpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.egoal.darkestpixeldungeon.levels.features.Chasm;
import com.egoal.darkestpixeldungeon.levels.features.Door;
import com.egoal.darkestpixeldungeon.levels.painters.Painter;
import com.egoal.darkestpixeldungeon.levels.traps.Trap;
import com.egoal.darkestpixeldungeon.mechanics.ShadowCaster;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.plants.BlandfruitBush;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.ItemSprite;
import com.egoal.darkestpixeldungeon.utils.BArray;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public abstract class Level implements Bundlable {

  public static enum Feeling {
    NONE,
    CHASM,
    WATER,
    GRASS,
    DARK
  }

  protected int width;
  protected int height;
  protected int length;

  protected static final float TIME_TO_RESPAWN = 50;

  public int version;
  public int[] map;
  public boolean[] visited;
  public boolean[] mapped;

  // public int viewDistance = Dungeon.isChallenged( Challenges.DARKNESS ) ? 
  // 3: 8;
  public int viewDistance = 4;
  public int seeDistance = 8;

  //FIXME should not be static!
  public static boolean[] fieldOfView;

  public static boolean[] passable;
  public static boolean[] losBlocking;
  public static boolean[] flamable;
  public static boolean[] secret;
  public static boolean[] solid;
  public static boolean[] avoid;
  public static boolean[] water;
  public static boolean[] pit;
  public static boolean[] luminary;  // the luminaries in the level
  public static boolean[] lighted;  // is lighted by the luminaries

  public static boolean[] discoverable;

  public Feeling feeling = Feeling.NONE;

  public int entrance;
  public int exit;

  //when a boss level has become locked.
  public boolean locked = false;

  public HashSet<Mob> mobs;
  public SparseArray<Heap> heaps;
  public HashMap<Class<? extends Blob>, Blob> blobs;
  public SparseArray<Plant> plants;
  public SparseArray<Trap> traps;
  public HashSet<CustomTileVisual> customTiles;

  protected ArrayList<Item> itemsToSpawn = new ArrayList<>();

  // visuals is added each time the scene is created, 
  // so, no need to keep track on them in the bundle
  protected Group visuals;
  //todo: rework on this awful things...
  protected ArrayList<LightVisual> visualLights = new ArrayList<>();

  public int color1 = 0x004400;
  public int color2 = 0x88CC44;

  //FIXME this is sloppy. Should be able to keep track of this without static
  // variables
  protected static boolean pitRoomNeeded = false;
  public static boolean weakFloorCreated = false;

  private static final String VERSION = "version";
  private static final String MAP = "map";
  private static final String VISITED = "visited";
  private static final String MAPPED = "mapped";
  private static final String ENTRANCE = "entrance";
  private static final String EXIT = "exit";
  private static final String LOCKED = "locked";
  private static final String HEAPS = "heaps";
  private static final String PLANTS = "plants";
  private static final String TRAPS = "traps";
  private static final String CUSTOM_TILES = "customTiles";
  private static final String MOBS = "mobs";
  private static final String BLOBS = "blobs";
  private static final String FEELING = "feeling";

  public void create() {

    setupSize();
    PathFinder.setMapSize(width(), height());
    passable = new boolean[length()];
    losBlocking = new boolean[length()];
    flamable = new boolean[length()];
    secret = new boolean[length()];
    solid = new boolean[length()];
    avoid = new boolean[length()];
    water = new boolean[length()];
    pit = new boolean[length()];

    luminary = new boolean[length()];
    lighted = new boolean[length()];

    map = new int[length()];
    visited = new boolean[length()];
    Arrays.fill(visited, false);
    mapped = new boolean[length()];
    Arrays.fill(mapped, false);

    if (Dungeon.depth > 0 && (!(Dungeon.bossLevel() || Dungeon.depth == 21))) {
      addItemToSpawn(Generator.random(Generator.Category.FOOD));

      // special items
      int bonus = RingOfWealth.getBonus(Dungeon.hero, RingOfWealth.Wealth
              .class);

      if (Dungeon.posNeeded()) {
        if (Random.Float() > Math.pow(0.925, bonus))
          addItemToSpawn(new PotionOfMight());
        else
          addItemToSpawn(new PotionOfStrength());
        Dungeon.limitedDrops.strengthPotions.count++;
      }
      if (Dungeon.souNeeded()) {
        if (Random.Float() > Math.pow(0.925, bonus))
          addItemToSpawn(new ScrollOfMagicalInfusion());
        else
          addItemToSpawn(new ScrollOfUpgrade());
        Dungeon.limitedDrops.upgradeScrolls.count++;
      }
      if (Dungeon.asNeeded()) {
        if (Random.Float() > Math.pow(0.925, bonus))
          addItemToSpawn(new Stylus());
        addItemToSpawn(new Stylus());
        Dungeon.limitedDrops.arcaneStyli.count++;
      }
      if (Dungeon.wineNeeded()) {
        if (Random.Float() > Math.pow(0.925, bonus))
          addItemToSpawn(new Wine());
        addItemToSpawn(new Wine());
        Dungeon.limitedDrops.wine.count++;
      }
      if (Dungeon.scrollOfLullabyNeed()) {
        if (Random.Float() > Math.pow(0.925, bonus))
          addItemToSpawn(new ScrollOfLullaby());
        addItemToSpawn(new ScrollOfLullaby());
        Dungeon.limitedDrops.lullabyScrolls.count++;
      }

      DriedRose rose = Dungeon.hero.belongings.getItem(DriedRose.class);
      if (rose != null && !rose.cursed) {
        //this way if a rose is dropped later in the game, player still has a
        // chance to max it out.
        int petalsNeeded = (int) Math.ceil((float) ((Dungeon.depth / 2) -
                rose.droppedPetals) / 3);

        for (int i = 1; i <= petalsNeeded; i++) {
          //the player may miss a single petal and still max their rose.
          if (rose.droppedPetals < 11) {
            addItemToSpawn(new DriedRose.Petal());
            rose.droppedPetals++;
          }
        }
      }

      if (Dungeon.depth > 1) {
        switch (Random.Int(10)) {
          case 0:
            if (!Dungeon.bossLevel(Dungeon.depth + 1)) {
              feeling = Feeling.CHASM;
            }
            break;
          case 1:
            feeling = Feeling.WATER;
            break;
          case 2:
            feeling = Feeling.GRASS;
            break;
          case 3:
          case 4:
            feeling = Feeling.DARK;
            viewDistance /= 2;
            break;
        }

        // give extra torches
        int torchCount = 0;
        int torchSpawnTime = 10;
        for (int i = 0; i < torchSpawnTime; ++i) {
          int PROB_NUM = 4 * (torchCount + 1) * (torchCount + 1) +
                  (Dungeon.depth + 1) * Dungeon.depth / 20;
          // now the expectation is 1.3->0.2 with 1->25
          if (Random.Int(PROB_NUM) == 0) {
            addItemToSpawn(new Torch());
            ++torchCount;
          }
        }

        // extra wine
        if (Random.Int(10) == 0)
          addItemToSpawn(new Wine());
      }
    }

    boolean pitNeeded = Dungeon.depth > 1 && weakFloorCreated;

    do {
      Arrays.fill(map, feeling == Feeling.CHASM ? Terrain.CHASM : Terrain.WALL);

      pitRoomNeeded = pitNeeded;
      weakFloorCreated = false;

      mobs = new HashSet<>();
      heaps = new SparseArray<>();
      blobs = new HashMap<>();
      plants = new SparseArray<>();
      traps = new SparseArray<>();
      customTiles = new HashSet<>();

    } while (!build());

    decorate();

    buildFlagMaps();
    cleanWalls();

    createMobs();
    createItems();
  }

  protected void setupSize() {
    if (width == 0 || height == 0)
      width = height = 36;
    length = width * height;
  }

  public void reset() {

    for (Mob mob : mobs.toArray(new Mob[0])) {
      if (!mob.reset()) {
        mobs.remove(mob);
      }
    }
    createMobs();
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {

    version = bundle.getInt(VERSION);

    if (bundle.contains("width") && bundle.contains("height")) {
      width = bundle.getInt("width");
      height = bundle.getInt("height");
    } else
      width = height = 36; //default sizes
    length = width * height;
    PathFinder.setMapSize(width(), height());

    mobs = new HashSet<>();
    heaps = new SparseArray<>();
    blobs = new HashMap<>();
    plants = new SparseArray<>();
    traps = new SparseArray<>();
    customTiles = new HashSet<>();

    map = bundle.getIntArray(MAP);

    visited = bundle.getBooleanArray(VISITED);
    mapped = bundle.getBooleanArray(MAPPED);

    entrance = bundle.getInt(ENTRANCE);
    exit = bundle.getInt(EXIT);

    locked = bundle.getBoolean(LOCKED);

    weakFloorCreated = false;

    Collection<Bundlable> collection = bundle.getCollection(HEAPS);
    for (Bundlable h : collection) {
      Heap heap = (Heap) h;
      if (!heap.isEmpty())
        heaps.put(heap.pos, heap);
    }

    collection = bundle.getCollection(PLANTS);
    for (Bundlable p : collection) {
      Plant plant = (Plant) p;
      plants.put(plant.pos, plant);
    }

    collection = bundle.getCollection(TRAPS);
    for (Bundlable p : collection) {
      Trap trap = (Trap) p;
      traps.put(trap.pos, trap);
    }

    collection = bundle.getCollection(CUSTOM_TILES);
    for (Bundlable p : collection) {
      CustomTileVisual vis = (CustomTileVisual) p;
      customTiles.add(vis);
    }

    collection = bundle.getCollection(MOBS);
    for (Bundlable m : collection) {
      Mob mob = (Mob) m;
      if (mob != null) {
        mobs.add(mob);
      }
    }

    collection = bundle.getCollection(BLOBS);
    for (Bundlable b : collection) {
      Blob blob = (Blob) b;
      blobs.put(blob.getClass(), blob);
    }

    feeling = bundle.getEnum(FEELING, Feeling.class);
    if (feeling == Feeling.DARK)
      viewDistance = (int) Math.ceil(viewDistance / 2f);

    buildFlagMaps();
    cleanWalls();
  }

  @Override
  public void storeInBundle(Bundle bundle) {
    bundle.put(VERSION, Game.versionCode);
    bundle.put("width", width);
    bundle.put("height", height);
    bundle.put(MAP, map);
    bundle.put(VISITED, visited);
    bundle.put(MAPPED, mapped);
    bundle.put(ENTRANCE, entrance);
    bundle.put(EXIT, exit);
    bundle.put(LOCKED, locked);
    bundle.put(HEAPS, heaps.values());
    bundle.put(PLANTS, plants.values());
    bundle.put(TRAPS, traps.values());
    bundle.put(CUSTOM_TILES, customTiles);
    bundle.put(MOBS, mobs);
    bundle.put(BLOBS, blobs.values());
    bundle.put(FEELING, feeling);
  }

  public int tunnelTile() {
    return feeling == Feeling.CHASM ? Terrain.EMPTY_SP : Terrain.EMPTY;
  }

  public int width() {
    if (width == 0)
      setupSize();
    return width;
  }

  public int height() {
    if (height == 0)
      setupSize();
    return height;
  }

  public int length() {
    if (length == 0)
      setupSize();
    return length;
  }

  public String tilesTex() {
    return null;
  }

  public String waterTex() {
    return null;
  }

  abstract protected boolean build();

  abstract protected void decorate();

  abstract protected void createMobs();

  abstract protected void createItems();

  public boolean loadMapDataFromFile(String mapfile) {
    try {
      InputStream is = Game.instance.getAssets().open(mapfile);
      BufferedReader br = new BufferedReader(new InputStreamReader(is));

      String line;
      line = br.readLine();
      String[] wh = line.split(" ");
      int w = Integer.parseInt(wh[0]);
      int h = Integer.parseInt(wh[1]);

      // load map, assign necessary values
      for (int r = 0; r < h; ++r) {
        String[] eles = br.readLine().split(" ");
        for (int c = 0; c < w; ++c) {
          int t = Integer.parseInt(eles[c]);
          int pos = r * width() + c;

          map[pos] = t;
          switch (t) {
            case Terrain.ENTRANCE:
              entrance = pos;
              break;
            case Terrain.LOCKED_EXIT:
            case Terrain.UNLOCKED_EXIT:
            case Terrain.EXIT:
              exit = pos;
              break;
          }

        }
      }
    } catch (IOException e) {
      DarkestPixelDungeon.reportException(e);
      return false;
    }

    return true;
  }

  public void seal() {
    if (!locked) {
      locked = true;
      Buff.affect(Dungeon.hero, LockedFloor.class);
    }
  }

  public void unseal() {
    if (locked) {
      locked = false;
    }
  }

  public Group addVisuals() {
    if (visuals == null || visuals.parent == null) {
      visuals = new Group();
    } else {
      visuals.clear();
    }
    for (int i = 0; i < length(); i++) {
      if (pit[i]) {
        visuals.add(new WindParticle.Wind(i));
        if (i >= width() && water[i - width()]) {
          visuals.add(new FlowParticle.Flow(i - width()));
        }
      }
      if (luminary[i]) {
        addLightVisual(i);
      }
    }
    return visuals;
  }

  public void addLightVisual(int pos) {
    LightVisual e = lightVisual(pos);
    visuals.add(e);
    visualLights.add(e);
  }

  public void clearLightVisuals() {
    for (LightVisual e : visualLights)
      visuals.erase(e);

    visualLights.clear();
  }

  public void removeLightVisualAt(int pos) {
    for (LightVisual lv : visualLights) {
      if (lv.pos == pos) {
        visuals.erase(lv);
        visualLights.remove(lv);
        break;
      }
    }
  }

  protected LightVisual lightVisual(int pos) {
    return new TorchLight(pos);
  }

  public int nMobs() {
    return 0;
  }

  public Mob findMob(int pos) {
    for (Mob mob : mobs) {
      if (mob.pos == pos) {
        return mob;
      }
    }
    return null;
  }

  public Actor respawner() {
    return new Actor() {

      {
        actPriority = 1; //as if it were a buff.
      }

      @Override
      protected boolean act() {
        if (mobs.size() < nMobs()) {

          Mob mob = Bestiary.mutable(Dungeon.depth);
          mob.state = mob.WANDERING;
          mob.pos = randomRespawnCell();
          if (Dungeon.hero.isAlive() && mob.pos != -1 && distance(Dungeon
                  .hero.pos, mob.pos) >= 4) {
            GameScene.add(mob);
            if (Statistics.amuletObtained) {
              mob.beckon(Dungeon.hero.pos);
            }
          }
        }
        spend(Dungeon.level.feeling == Feeling.DARK || Statistics
                .amuletObtained ? TIME_TO_RESPAWN / 2 : TIME_TO_RESPAWN);
        return true;
      }
    };
  }

  public int randomRespawnCell() {
    int cell;
    do {
      cell = Random.Int(length());
    }
    while (!passable[cell] || Dungeon.visible[cell] || Actor.findChar(cell)
            != null);
    return cell;
  }

  public int randomDestination() {
    int cell;
    do {
      cell = Random.Int(length());
    } while (!passable[cell]);
    return cell;
  }

  public void addItemToSpawn(Item item) {
    if (item != null) {
      itemsToSpawn.add(item);
    }
  }

  public Item findPrizeItem() {
    return findPrizeItem(null);
  }

  public Item findPrizeItem(Class<? extends Item> match) {
    if (itemsToSpawn.size() == 0)
      return null;

    if (match == null) {
      Item item = Random.element(itemsToSpawn);
      itemsToSpawn.remove(item);
      return item;
    }

    for (Item item : itemsToSpawn) {
      if (match.isInstance(item)) {
        itemsToSpawn.remove(item);
        return item;
      }
    }

    return null;
  }

  protected void buildFlagMaps() {

    fieldOfView = new boolean[length()];

    passable = new boolean[length()];
    losBlocking = new boolean[length()];
    flamable = new boolean[length()];
    secret = new boolean[length()];
    solid = new boolean[length()];
    avoid = new boolean[length()];
    water = new boolean[length()];
    pit = new boolean[length()];

    luminary = new boolean[length()];
    lighted = new boolean[length()];

    for (int i = 0; i < length(); i++) {
      int flags = Terrain.flags[map[i]];
      passable[i] = (flags & Terrain.PASSABLE) != 0;
      losBlocking[i] = (flags & Terrain.LOS_BLOCKING) != 0;
      flamable[i] = (flags & Terrain.FLAMABLE) != 0;
      secret[i] = (flags & Terrain.SECRET) != 0;
      solid[i] = (flags & Terrain.SOLID) != 0;
      avoid[i] = (flags & Terrain.AVOID) != 0;
      water[i] = (flags & Terrain.LIQUID) != 0;
      pit[i] = (flags & Terrain.PIT) != 0;
      luminary[i] = (flags & Terrain.LUMINARY) != 0;
    }
    // update lights
    BArray.setFalse(lighted);
    int[] L1norm2 = new int[]{
            -width * 2,
            -width - 1, -width, -width + 1,
            -2, -1, 0, +1, +2,
            +width - 1, +width, +width + 1,
            +width * 2,
    };
    for (int i = 0; i < length(); ++i) {
      if (luminary[i]) {
        for (int np : L1norm2) {
          int pos = i + np;
          if (pos >= 0 && pos < length())
            lighted[pos] = true;
        }
      }
    }

    int lastRow = length() - width();
    for (int i = 0; i < width(); i++) {
      passable[i] = avoid[i] = false;
      passable[lastRow + i] = avoid[lastRow + i] = false;
    }
    for (int i = width(); i < lastRow; i += width()) {
      passable[i] = avoid[i] = false;
      passable[i + width() - 1] = avoid[i + width() - 1] = false;
    }

    for (int i = width(); i < length() - width(); i++) {

      if (water[i]) {
        map[i] = getWaterTile(i);
      }

      if (pit[i]) {
        if (!pit[i - width()]) {
          int c = map[i - width()];
          if (c == Terrain.EMPTY_SP || c == Terrain.STATUE_SP) {
            map[i] = Terrain.CHASM_FLOOR_SP;
          } else if (water[i - width()]) {
            map[i] = Terrain.CHASM_WATER;
          } else if ((Terrain.flags[c] & Terrain.UNSTITCHABLE) != 0) {
            map[i] = Terrain.CHASM_WALL;
          } else {
            map[i] = Terrain.CHASM_FLOOR;
          }
        }
      }
    }
  }

  //FIXME this is a temporary fix here to avoid changing the tiles texture
  //This logic will be changed in 0.4.3 anyway
  private static int[] N4Indicies = new int[]{0, 2, 3, 1};

  private int getWaterTile(int pos) {
    int t = Terrain.WATER_TILES;
    for (int j = 0; j < PathFinder.NEIGHBOURS4.length; j++) {
      if ((Terrain.flags[map[pos + PathFinder.NEIGHBOURS4[N4Indicies[j]]]] &
              Terrain.UNSTITCHABLE) != 0) {
        t += 1 << j;
      }
    }
    return t;
  }

  public void destroy(int pos) {
    if ((Terrain.flags[map[pos]] & Terrain.UNSTITCHABLE) == 0) {

      set(pos, Terrain.EMBERS);

    } else {
      boolean flood = false;
      for (int j = 0; j < PathFinder.NEIGHBOURS4.length; j++) {
        if (water[pos + PathFinder.NEIGHBOURS4[j]]) {
          flood = true;
          break;
        }
      }
      if (flood) {
        set(pos, getWaterTile(pos));
      } else {
        set(pos, Terrain.EMBERS);
      }
    }
  }

  protected void cleanWalls() {
    discoverable = new boolean[length()];
    if (DarkestPixelDungeon.debug())
      // can reveal all
      Arrays.fill(discoverable, true);
    else {
      for (int i = 0; i < length(); i++) {

        boolean d = false;

        for (int j = 0; j < PathFinder.NEIGHBOURS9.length; j++) {
          int n = i + PathFinder.NEIGHBOURS9[j];
          if (n >= 0 && n < length() &&
                  map[n] != Terrain.WALL && map[n] != Terrain.WALL_DECO &&
                  map[n] != Terrain.WALL_LIGHT_OFF && map[n] != Terrain
                  .WALL_LIGHT_ON) {
            d = true;
            break;
          }
        }

        if (d) {
          d = false;

          for (int j = 0; j < PathFinder.NEIGHBOURS9.length; j++) {
            int n = i + PathFinder.NEIGHBOURS9[j];
            if (n >= 0 && n < length() && !pit[n]) {
              d = true;
              break;
            }
          }
        }

        discoverable[i] = d;
      }
    }
  }

  public static void set(int cell, int terrain) {
    Painter.set(Dungeon.level, cell, terrain);

    if (terrain != Terrain.TRAP && terrain != Terrain.SECRET_TRAP && terrain
            != Terrain.INACTIVE_TRAP) {
      Dungeon.level.traps.remove(cell);
    }

    int flags = Terrain.flags[terrain];
    passable[cell] = (flags & Terrain.PASSABLE) != 0;
    losBlocking[cell] = (flags & Terrain.LOS_BLOCKING) != 0;
    flamable[cell] = (flags & Terrain.FLAMABLE) != 0;
    secret[cell] = (flags & Terrain.SECRET) != 0;
    solid[cell] = (flags & Terrain.SOLID) != 0;
    avoid[cell] = (flags & Terrain.AVOID) != 0;
    pit[cell] = (flags & Terrain.PIT) != 0;
    water[cell] = terrain == Terrain.WATER || terrain >= Terrain.WATER_TILES;
  }

  public Heap drop(Item item, int cell) {

    //This messy if statement deals will items which should not drop in 
    // challenges primarily.
    if ((Dungeon.isChallenged(Challenges.NO_FOOD) && (item instanceof Food ||
            item instanceof BlandfruitBush.Seed)) ||
            (Dungeon.isChallenged(Challenges.NO_ARMOR) && item instanceof
                    Armor) ||
            (Dungeon.isChallenged(Challenges.NO_HEALING) && item instanceof
                    PotionOfHealing) ||
            (Dungeon.isChallenged(Challenges.NO_HERBALISM) && (item
                    instanceof Plant.Seed || item instanceof Dewdrop || item
                    instanceof SeedPouch)) ||
            (Dungeon.isChallenged(Challenges.NO_SCROLLS) && ((item instanceof
                    Scroll && !(item instanceof ScrollOfUpgrade || item
                    instanceof ScrollOfMagicalInfusion)) || item instanceof
                    ScrollHolder)) ||
            item == null) {

      //create a dummy heap, give it a dummy sprite, don't add it to the 
      // game, and return it.
      //effectively nullifies whatever the logic calling this wants to do, 
      // including dropping items.
      Heap heap = new Heap();
      ItemSprite sprite = heap.sprite = new ItemSprite();
      sprite.link(heap);
      return heap;

    }

    if ((map[cell] == Terrain.ALCHEMY) && (
            !(item instanceof Plant.Seed || item instanceof Blandfruit) ||
                    item instanceof BlandfruitBush.Seed ||
                    (item instanceof Blandfruit && (((Blandfruit) item)
                            .potionAttrib != null || heaps.get(cell) != null)
                    ) ||
                    Dungeon.hero.buff(AlchemistsToolkit.alchemy.class) !=
                            null && Dungeon.hero.buff(AlchemistsToolkit
                            .alchemy.class).isCursed())) {
      int n;
      do {
        n = cell + PathFinder.NEIGHBOURS8[Random.Int(8)];
      } while (map[n] != Terrain.EMPTY_SP);
      cell = n;
    }

    Heap heap = heaps.get(cell);
    if (heap == null) {

      heap = new Heap();
      heap.seen = Dungeon.visible[cell];
      heap.pos = cell;
      if (map[cell] == Terrain.CHASM || (Dungeon.level != null && pit[cell])) {
        Dungeon.dropToChasm(item);
        GameScene.discard(heap);
      } else {
        heaps.put(cell, heap);
        GameScene.add(heap);
      }

    } else if (heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type
            .CRYSTAL_CHEST) {

      int n;
      do {
        n = cell + PathFinder.NEIGHBOURS8[Random.Int(8)];
      } while (!Level.passable[n] && !Level.avoid[n]);
      return drop(item, n);

    }
    heap.drop(item);

    if (Dungeon.level != null) {
      press(cell, null);
    }

    return heap;
  }

  public Plant plant(Plant.Seed seed, int pos) {

    Plant plant = plants.get(pos);
    if (plant != null) {
      plant.wither();
    }

    if (map[pos] == Terrain.HIGH_GRASS ||
            map[pos] == Terrain.EMPTY ||
            map[pos] == Terrain.EMBERS ||
            map[pos] == Terrain.EMPTY_DECO) {
      map[pos] = Terrain.GRASS;
      flamable[pos] = true;
      GameScene.updateMap(pos);
    }

    plant = seed.couch(pos);
    plants.put(pos, plant);

    GameScene.add(plant);

    return plant;
  }

  public void uproot(int pos) {
    plants.remove(pos);
  }

  public Trap setTrap(Trap trap, int pos) {
    Trap existingTrap = traps.get(pos);
    if (existingTrap != null) {
      traps.remove(pos);
      if (existingTrap.sprite != null) existingTrap.sprite.kill();
    }
    trap.set(pos);
    traps.put(pos, trap);
    GameScene.add(trap);
    return trap;
  }

  public void disarmTrap(int pos) {
    set(pos, Terrain.INACTIVE_TRAP);
    GameScene.updateMap(pos);
  }

  public void discover(int cell) {
    set(cell, Terrain.discover(map[cell]));
    Trap trap = traps.get(cell);
    if (trap != null)
      trap.reveal();
    GameScene.updateMap(cell);
  }

  public int pitCell() {
    return randomRespawnCell();
  }

  // hero press
  public void press(int cell, Char ch) {

    if (ch != null && pit[cell] && !ch.flying) {
      if (ch == Dungeon.hero) {
        Chasm.heroFall(cell);
      } else if (ch instanceof Mob) {
        Chasm.mobFall((Mob) ch);
      }
      return;
    }

    Trap trap = null;
    switch (map[cell]) {

      case Terrain.SECRET_TRAP:
        GLog.i(Messages.get(Level.class, "hidden_plate"));
      case Terrain.TRAP:
        trap = traps.get(cell);
        break;

      case Terrain.HIGH_GRASS:
      case Terrain.HIGH_GRASS_COLLECTED:
        HighGrass.trample(this, cell, ch);
        break;

      case Terrain.WELL:
        WellWater.affectCell(cell);
        break;

      case Terrain.ALCHEMY:
        if (ch == null) {
          Alchemy.transmute(cell);
        }
        break;

      case Terrain.DOOR:
        Door.enter(cell, ch);
        break;
    }

    TimekeepersHourglass.timeFreeze timeFreeze = Dungeon.hero.buff
            (TimekeepersHourglass.timeFreeze.class);

    if (trap != null) {
      if (timeFreeze == null) {

        if (ch == Dungeon.hero)
          Dungeon.hero.interrupt();

        trap.trigger();
      } else {

        Sample.INSTANCE.play(Assets.SND_TRAP);

        discover(cell);

        timeFreeze.setDelayedPress(cell);

      }
    }

    Plant plant = plants.get(cell);
    if (plant != null) {
      plant.trigger();
    }
  }

  // mob press
  public void mobPress(Mob mob) {

    int cell = mob.pos;

    if (pit[cell] && !mob.flying) {
      Chasm.mobFall(mob);
      return;
    }

    Trap trap = null;
    switch (map[cell]) {

      case Terrain.TRAP:
        trap = traps.get(cell);
        break;

      case Terrain.DOOR:
        Door.enter(cell, mob);
        break;
    }

    if (trap != null) {
      trap.trigger();
    }

    Plant plant = plants.get(cell);
    if (plant != null) {
      plant.trigger();
    }
  }

  public void updateFieldOfView(Char c, boolean[] fieldOfView) {

    int cx = c.pos % width();
    int cy = c.pos / width();

    boolean sighted = c.buff(Blindness.class) == null && c.buff(Shadows
            .class) == null
            && c.buff(TimekeepersHourglass.timeStasis.class) == null && c
            .isAlive();
    if (sighted) {
      ShadowCaster.castShadow(cx, cy, fieldOfView, c.viewDistance, c
              .seeDistance);
    } else {
      BArray.setFalse(fieldOfView);
    }

    int sense = 1;
    //Currently only the hero can get mind vision
    if (c.isAlive() && c == Dungeon.hero) {
      for (Buff b : c.buffs(MindVision.class)) {
        sense = Math.max(((MindVision) b).distance, sense);
      }
    }

    if ((sighted && sense > 1) || !sighted) {

      int ax = Math.max(0, cx - sense);
      int bx = Math.min(cx + sense, width() - 1);
      int ay = Math.max(0, cy - sense);
      int by = Math.min(cy + sense, height() - 1);

      int len = bx - ax + 1;
      int pos = ax + ay * width();
      for (int y = ay; y <= by; y++, pos += width()) {
        System.arraycopy(discoverable, pos, fieldOfView, pos, len);
      }
    }

    //Currently only the hero can get mind vision or awareness
    if (c.isAlive() && c == Dungeon.hero) {
      Dungeon.hero.mindVisionEnemies.clear();
      if (c.buff(MindVision.class) != null) {
        for (Mob mob : mobs) {
          int p = mob.pos;

          if (!fieldOfView[p]) {
            Dungeon.hero.mindVisionEnemies.add(mob);
          }
          for (int i : PathFinder.NEIGHBOURS9)
            fieldOfView[p + i] = true;

        }
      } else if (((Hero) c).heroClass == HeroClass.HUNTRESS) {
        for (Mob mob : mobs) {
          int p = mob.pos;
          if (distance(c.pos, p) == 2) {

            if (!fieldOfView[p]) {
              Dungeon.hero.mindVisionEnemies.add(mob);
            }
            for (int i : PathFinder.NEIGHBOURS9)
              fieldOfView[p + i] = true;
          }
        }
      }
      if (c.buff(Awareness.class) != null) {
        for (Heap heap : heaps.values()) {
          int p = heap.pos;
          for (int i : PathFinder.NEIGHBOURS9)
            fieldOfView[p + i] = true;
        }
      }
    }

    // view mark
    for (Mob mob : mobs) {
      ViewMark vm = mob.buff(ViewMark.class);
      if (vm != null && vm.observer == c.id()) {
        int p = mob.pos;
        for (int i : PathFinder.NEIGHBOURS9)
          fieldOfView[p + i] = true;
      }
    }

    if (c == Dungeon.hero) {
      for (Heap heap : heaps.values())
        if (!heap.seen && fieldOfView[heap.pos])
          heap.seen = true;
    }

  }

  public int distance(int a, int b) {
    int ax = a % width();
    int ay = a / width();
    int bx = b % width();
    int by = b / width();
    return Math.max(Math.abs(ax - bx), Math.abs(ay - by));
  }

  public boolean adjacent(int a, int b) {
    return distance(a, b) == 1;
  }

  //returns true if the input is a valid tile within the level
  public boolean insideMap(int tile) {
    //top and bottom row and beyond
    return !((tile < width || tile >= length - width) ||
            //left and right column
            (tile % width == 0 || tile % width == width - 1));
  }

  public Point cellToPoint(int cell) {
    return new Point(cell % width(), cell / width());
  }

  public int pointToCell(Point p) {
    return p.x + p.y * width();
  }

  public int xy2cell(int x, int y) {
    return x + y * width();
  }

  public String tileName(int tile) {

    if (tile >= Terrain.WATER_TILES) {
      return tileName(Terrain.WATER);
    }

    if (tile != Terrain.CHASM && (Terrain.flags[tile] & Terrain.PIT) != 0) {
      return tileName(Terrain.CHASM);
    }

    switch (tile) {
      case Terrain.CHASM:
        return Messages.get(Level.class, "chasm_name");
      case Terrain.EMPTY:
      case Terrain.EMPTY_SP:
      case Terrain.EMPTY_DECO:
      case Terrain.SECRET_TRAP:
        return Messages.get(Level.class, "floor_name");
      case Terrain.GRASS:
        return Messages.get(Level.class, "grass_name");
      case Terrain.WATER:
        return Messages.get(Level.class, "water_name");
      case Terrain.WALL:
      case Terrain.WALL_DECO:
      case Terrain.SECRET_DOOR:
        return Messages.get(Level.class, "wall_name");
      case Terrain.DOOR:
        return Messages.get(Level.class, "closed_door_name");
      case Terrain.OPEN_DOOR:
        return Messages.get(Level.class, "open_door_name");
      case Terrain.ENTRANCE:
        return Messages.get(Level.class, "entrace_name");
      case Terrain.EXIT:
        return Messages.get(Level.class, "exit_name");
      case Terrain.EMBERS:
        return Messages.get(Level.class, "embers_name");
      case Terrain.LOCKED_DOOR:
        return Messages.get(Level.class, "locked_door_name");
      case Terrain.PEDESTAL:
        return Messages.get(Level.class, "pedestal_name");
      case Terrain.BARRICADE:
        return Messages.get(Level.class, "barricade_name");
      case Terrain.HIGH_GRASS:
      case Terrain.HIGH_GRASS_COLLECTED:
        return Messages.get(Level.class, "high_grass_name");
      case Terrain.LOCKED_EXIT:
        return Messages.get(Level.class, "locked_exit_name");
      case Terrain.UNLOCKED_EXIT:
        return Messages.get(Level.class, "unlocked_exit_name");
      case Terrain.SIGN:
        return Messages.get(Level.class, "sign_name");
      case Terrain.WELL:
        return Messages.get(Level.class, "well_name");
      case Terrain.EMPTY_WELL:
        return Messages.get(Level.class, "empty_well_name");
      case Terrain.STATUE:
      case Terrain.STATUE_SP:
        return Messages.get(Level.class, "statue_name");
      case Terrain.INACTIVE_TRAP:
        return Messages.get(Level.class, "inactive_trap_name");
      case Terrain.BOOKSHELF:
        return Messages.get(Level.class, "bookshelf_name");
      case Terrain.ALCHEMY:
        return Messages.get(Level.class, "alchemy_name");
      case Terrain.WALL_LIGHT_ON:
        return Messages.get(Level.class, "lighton_name");
      case Terrain.WALL_LIGHT_OFF:
        return Messages.get(Level.class, "lightoff_name");
      case Terrain.ENCHANTING_STATION:
        return Messages.get(Level.class, "enchanting_station_name");


      default:
        return Messages.get(Level.class, "default_name");
    }
  }

  public String tileDesc(int tile) {

    switch (tile) {
      case Terrain.CHASM:
        return Messages.get(Level.class, "chasm_desc");
      case Terrain.WATER:
        return Messages.get(Level.class, "water_desc");
      case Terrain.ENTRANCE:
        return Messages.get(Level.class, "entrance_desc");
      case Terrain.EXIT:
      case Terrain.UNLOCKED_EXIT:
        return Messages.get(Level.class, "exit_desc");
      case Terrain.EMBERS:
        return Messages.get(Level.class, "embers_desc");
      case Terrain.HIGH_GRASS:
      case Terrain.HIGH_GRASS_COLLECTED:
        return Messages.get(Level.class, "high_grass_desc");
      case Terrain.LOCKED_DOOR:
        return Messages.get(Level.class, "locked_door_desc");
      case Terrain.LOCKED_EXIT:
        return Messages.get(Level.class, "locked_exit_desc");
      case Terrain.BARRICADE:
        return Messages.get(Level.class, "barricade_desc");
      case Terrain.SIGN:
        return Messages.get(Level.class, "sign_desc");
      case Terrain.INACTIVE_TRAP:
        return Messages.get(Level.class, "inactive_trap_desc");
      case Terrain.STATUE:
      case Terrain.STATUE_SP:
        return Messages.get(Level.class, "statue_desc");
      case Terrain.ALCHEMY:
        return Messages.get(Level.class, "alchemy_desc");
      case Terrain.EMPTY_WELL:
        return Messages.get(Level.class, "empty_well_desc");
      case Terrain.WALL_LIGHT_ON:
        return Messages.get(Level.class, "lighton_desc");
      case Terrain.WALL_LIGHT_OFF:
        return Messages.get(Level.class, "lightoff_desc");
      case Terrain.ENCHANTING_STATION:
        return Messages.get(Level.class, "enchanting_station_desc");

      default:
        if (tile >= Terrain.WATER_TILES) {
          return tileDesc(Terrain.WATER);
        }
        if ((Terrain.flags[tile] & Terrain.PIT) != 0) {
          return tileDesc(Terrain.CHASM);
        }
        return Messages.get(Level.class, "default_desc");
    }
  }

  // basic light visual
  public static class LightVisual extends Emitter {
    public int pos;

    public LightVisual(int pos) {
      this.pos = pos;
    }
  }

  public static class TorchLight extends LightVisual {
    public TorchLight(int pos) {
      super(pos);

      PointF p = DungeonTilemap.tileCenterToWorld(pos);
      pos(p.x - 1, p.y + 3, 2, 0);

      // pour(FlameParticle.FACTORY, 0.15f);

      // add(new Halo(16, 0xFFFFA0, 0.2f).point(p.x, p.y));
      add(new Halo(20, 0xFFFFCC, 0.2f).point(p.x, p.y));
    }

    @Override
    public void update() {
      if (visible = Dungeon.visible[pos]) {
        super.update();
      }
    }
  }
}
