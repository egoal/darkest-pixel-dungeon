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
package com.egoal.darkestpixeldungeon;

import android.util.Log;

import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Resident;
import com.egoal.darkestpixeldungeon.actors.buffs.Amok;
import com.egoal.darkestpixeldungeon.actors.buffs.Awareness;
import com.egoal.darkestpixeldungeon.actors.buffs.MindVision;
import com.egoal.darkestpixeldungeon.actors.hero.Hero;
import com.egoal.darkestpixeldungeon.actors.hero.HeroClass;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.*;
import com.egoal.darkestpixeldungeon.items.unclassified.Ankh;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll;
import com.egoal.darkestpixeldungeon.levels.*;
import com.egoal.darkestpixeldungeon.levels.PrisonBossLevel;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.scenes.StartScene;
import com.egoal.darkestpixeldungeon.ui.QuickSlotButton;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndResurrect;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.items.rings.Ring;
import com.egoal.darkestpixeldungeon.utils.BArray;
import com.watabou.noosa.Game;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.SparseArray;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;

public class Dungeon {

  public static int initialDepth_ = -1;
  public static final String VERSION_STRING = "0.4.3-1-2";

  public static int transmutation;  // depth number for a well of transmutation

  //enum of items which have limited spawns, records how many have spawned
  //could all be their own separate numbers, but this allows iterating, much 
  // nicer for bundling/initializing.
  //TODO: this is fairly brittle when it comes to bundling, should look into 
  // a more flexible solution.
  public static enum limitedDrops {
    //limited world drops
    strengthPotions,
    upgradeScrolls,
    lullabyScrolls,
    arcaneStyli,
    wine,

    laboratories,
    archDemons,

    //all unlimited health potion sources (except guards, which are at the 
    // bottom.
    swarmHP,
    guardHP,
    batHP,
    warlockHP,
    scorpioHP,
    cookingHP,
    madManHumanity,
    //blandfruit, which can technically be an unlimited health potion source
    blandfruitSeed,

    //doesn't use Generator, so we have to enforce one armband drop here
    armband,
    chaliceOfBlood, // only the statuary drop this now
    demonicSkull,
    handOfElder,

    ceremonialDaggerUsed,
    ceremonialDagger,

    //containers
    dewVial,
    seedBag,
    scrollBag,
    potionBag,
    wandBag,;

    public int count = 0;

    //for items which can only be dropped once, should directly access count 
    // otherwise.
    public boolean dropped() {
      return count != 0;
    }

    public void drop() {
      count = 1;
    }
  }

  public static Hero hero;
  public static Level level;

  public static QuickSlot quickslot = new QuickSlot();

  public static int depth;
  public static int gold;

  public static HashSet<Integer> chapters;

  // Hero's field of view
  public static boolean[] visible;

  public static SparseArray<ArrayList<Item>> droppedItems;

  public static int version;

  public static void init() {

    version = Game.versionCode;

    Actor.clear();
    Actor.resetNextID();

    Scroll.initLabels();
    Potion.Companion.initColors();
    Ring.Companion.initGems();

    Statistics.INSTANCE.reset();
    Journal.INSTANCE.reset();

    quickslot.reset();
    QuickSlotButton.reset();

    depth = initialDepth_;
    gold = 0;

    droppedItems = new SparseArray<ArrayList<Item>>();

    for (limitedDrops a : limitedDrops.values())
      a.count = 0;

    transmutation = Random.IntRange(6, 14);

    chapters = new HashSet<Integer>();

    // quest init
    Ghost.Quest.INSTANCE.reset();
    Wandmaker.Quest.reset();
    Blacksmith.Quest.reset();
    Imp.Quest.reset();

    Alchemist.Quest.INSTANCE.reset();
    Statuary.Reset();
    Jessica.Quest.INSTANCE.reset();
    Yvette.Quest.INSTANCE.Reset();

    hero = new Hero();
    hero.live();

    Badges.INSTANCE.reset();

//    StartScene.curClass.initHero(hero);
    StartScene.Companion.getCurrentClass().initHero(hero);
  }

  public static boolean IsChallenged() {
    // fixme: this does not compatible with something
    return Dungeon.hero != null && Dungeon.hero.getChallenge() != null;
  }

  public static Level newLevel() {

    Dungeon.level = null;
    Actor.clear();

    depth++;
    if (depth > Statistics.INSTANCE.getDeepestFloor()) {
      Statistics.INSTANCE.setDeepestFloor(depth);

      Statistics.INSTANCE.setCompletedWithNoKilling(depth > 1 &&
              Statistics.INSTANCE.getQualifiedForNoKilling());
    }

    Level level;
    switch (depth) {
      case 0:
        level = new VillageLevel();
        break;
      case 1:
      case 2:
      case 3:
      case 4:
        level = new SewerLevel();
        break;
      case 5:
        level = new SewerBossLevel();
        break;
      case 6:
      case 7:
      case 8:
      case 9:
        level = new PrisonLevel();
        break;
      case 10:
        level = new PrisonBossLevel();
        break;
      case 11:
      case 12:
      case 13:
      case 14:
        level = new CavesLevel();
        break;
      case 15:
        level = new CavesBossLevel();
        break;
      case 16:
      case 17:
      case 18:
      case 19:
        level = new CityLevel();
        break;
      case 20:
        level = new CityBossLevel();
        break;
      case 21:
        level = new LastShopLevel();
        break;
      case 22:
      case 23:
      case 24:
        level = new HallsLevel();
        break;
      case 25:
        level = new HallsBossLevel();
        break;
      case 26:
        level = new LastLevel();
        break;
      default:
        level = new DeadEndLevel();
        Statistics.INSTANCE.setDeepestFloor(Statistics.INSTANCE
                .getDeepestFloor() - 1);
    }

    visible = new boolean[level.length()];
    level.create();

    Statistics.INSTANCE.setQualifiedForNoKilling(!bossLevel());

    return level;
  }

  public static void resetLevel() {

    Actor.clear();

    level.reset();
    switchLevel(level, level.getEntrance());
  }

  public static boolean shopOnLevel() {
    return depth == 6 || depth == 11 || depth == 16;
  }

  public static boolean bossLevel() {
    return bossLevel(depth);
  }

  public static boolean bossLevel(int depth) {
    return depth == 5 || depth == 10 || depth == 15 || depth == 20 || depth
            == 25;
  }

  @SuppressWarnings("deprecation")
  public static void switchLevel(final Level level, int pos) {

    Dungeon.level = level;
    if (pos < 0 || pos >= level.length()) {
      pos = level.getExit();
    }
    Dungeon.level.onSwitchedIn();

    // add into level.mobs, then into actor.
    hero.restoreFollowers(level, pos);
    Actor.init();

    PathFinder.setMapSize(level.width(), level.height());
    visible = new boolean[level.length()];

    Actor respawner = level.respawner();
    if (respawner != null) {
      Actor.add(level.respawner());
    }
    Actor.add(new Resident());

    hero.pos = pos;

    observe();
    try {
      saveAll();
    } catch (IOException e) {
      DarkestPixelDungeon.reportException(e);
      /*This only catches IO errors. Yes, this means things can go wrong, and 
      they can go wrong catastrophically.
			But when they do the user will get a nice 'report this issue' dialogue, 
			and I can fix the bug.*/
    }
  }

  // drop items to the next level
  public static void dropToChasm(Item item) {
    int depth = Dungeon.depth + 1;
    ArrayList<Item> dropped = (ArrayList<Item>) Dungeon.droppedItems.get(depth);
    if (dropped == null) {
      Dungeon.droppedItems.put(depth, dropped = new ArrayList<Item>());
    }
    dropped.add(item);
  }

  // quotas
  public static boolean posNeeded() {
    //2 POS each floor set
    int posLeftThisSet = 2 - (limitedDrops.strengthPotions.count - (depth /
            5) * 2);
    if (posLeftThisSet <= 0) return false;

    int floorThisSet = (depth % 5);

    //pos drops every two floors, (numbers 1-2, and 3-4) with a 50% chance 
    // for the earlier one each time.
    int targetPOSLeft = 2 - floorThisSet / 2;
    if (floorThisSet % 2 == 1 && Random.Int(2) == 0) targetPOSLeft--;

    if (targetPOSLeft < posLeftThisSet) return true;
    else return false;

  }

  public static boolean souNeeded() {
    final int SOU_PER_FLOORSET = 2;

    //3 SOU each floor set
    int souLeftThisSet = SOU_PER_FLOORSET - (limitedDrops.upgradeScrolls
            .count - (depth / 5)
            * SOU_PER_FLOORSET);
    if (souLeftThisSet <= 0) return false;

    int floorThisSet = (depth % 5);
    //chance is floors left / scrolls left
    return Random.Int(5 - floorThisSet) < souLeftThisSet;
  }

  public static boolean asNeeded() {
    //1 AS each floor set
    int asLeftThisSet = 1 - (limitedDrops.arcaneStyli.count - (depth / 5));
    if (asLeftThisSet <= 0) return false;

    int floorThisSet = (depth % 5);
    //chance is floors left / scrolls left
    return Random.Int(5 - floorThisSet) < asLeftThisSet;
  }

  public static boolean wineNeeded() {
    int wineLeftThisSet = 1 - (limitedDrops.wine.count - depth / 10);
    if (wineLeftThisSet <= 0) return false;

    int floorThisSet = depth % 5;
    return Random.Int(5 - floorThisSet) < wineLeftThisSet;
  }

  public static boolean daggerNeeded() {
    int wineLeftThisSet = 1 - (limitedDrops.ceremonialDagger.count - depth / 5);
    if (wineLeftThisSet <= 0) return false;

    int floorThisSet = depth % 5;
    return Random.Int(5 - floorThisSet) < wineLeftThisSet;
  }

  public static boolean scrollOfLullabyNeed() {
    // 1 per 10 floors
    int slLeft = (depth / 10 + 1) - limitedDrops.lullabyScrolls.count;

    return slLeft > 0 && Random.Int(10 - depth % 10) < slLeft;
  }

  public static boolean labNeed() {
    // 1 per 10 floors
    int labLeft = (depth / 10 + 1) - limitedDrops.laboratories.count;
    return labLeft > 0 && Random.Int(10 - depth % 10) < labLeft;
  }

  public static boolean demonNeed() {
    // from 12, 1 per 7 floors
    if (depth <= 12) return false;

    int demonLeft = ((depth - 12) / 6 + 1) - limitedDrops.archDemons.count;
    return demonLeft > 0 && Random.Int(6 - (depth - 12) % 6) < demonLeft;
  }

  // save
  private static final String RG_GAME_FILE = "game.dat";
  private static final String RG_DEPTH_FILE = "depth%d.dat";

  private static final String WR_GAME_FILE = "warrior.dat";
  private static final String WR_DEPTH_FILE = "warrior%d.dat";

  private static final String MG_GAME_FILE = "mage.dat";
  private static final String MG_DEPTH_FILE = "mage%d.dat";

  private static final String RN_GAME_FILE = "ranger.dat";
  private static final String RN_DEPTH_FILE = "ranger%d.dat";

  private static final String SC_GAME_FILE = "sorceress.dat";
  private static final String SC_DEPTH_FILE = "sorceress%d.data";

  private static final String VERSION = "version";
  private static final String CHALLENGES = "challenges";
  private static final String HERO = "hero";
  private static final String GOLD = "gold";
  private static final String DEPTH = "depth";
  private static final String DROPPED = "dropped%d";
  private static final String LEVEL = "level";
  private static final String LIMDROPS = "limiteddrops";
  private static final String DV = "dewVial";
  private static final String WT = "transmutation";
  private static final String CHAPTERS = "chapters";
  private static final String QUESTS = "quests";
  private static final String BADGES = "badges";

  public static String gameFile(HeroClass cl) {
    switch (cl) {
      case WARRIOR:
        return WR_GAME_FILE;
      case MAGE:
        return MG_GAME_FILE;
      case HUNTRESS:
        return RN_GAME_FILE;
      case SORCERESS:
        return SC_GAME_FILE;
      default:
        return RG_GAME_FILE;
    }
  }

  public static String backupGameFile(HeroClass cl) {
    return "backup_game_" + gameFile(cl);
  }

  public static String backupLevelFile(HeroClass cl) {
    return "backup_level_" + gameFile(cl);
  }

  private static String depthFile(HeroClass cl) {
    switch (cl) {
      case WARRIOR:
        return WR_DEPTH_FILE;
      case MAGE:
        return MG_DEPTH_FILE;
      case HUNTRESS:
        return RN_DEPTH_FILE;
      case SORCERESS:
        return SC_DEPTH_FILE;
      default:
        return RG_DEPTH_FILE;
    }
  }

  public static void saveAll() throws IOException {
    saveAll(false);
  }

  public static void saveAll(boolean doBackup) throws IOException {
    if (doBackup)
      Log.d("dpd", "saving with backup.");

    if (hero.isAlive()) {

      Actor.fixTime();
      saveGame(gameFile(hero.getHeroClass()), doBackup ? backupGameFile(hero
              .getHeroClass()) : null);
      saveLevel(doBackup ? backupLevelFile(hero.getHeroClass()) : null);

      GamesInProgress.INSTANCE.set(hero.getHeroClass(), depth, hero.getLvl(),
              hero.getChallenge());

    } else if (WndResurrect.instance != null) {

      WndResurrect.instance.hide();
      Hero.Companion.ReallyDie(WndResurrect.causeOfDeath);
    }
  }

  private static void saveGame(String fileName, String backupFile) throws
          IOException {
    try {
      Bundle bundle = new Bundle();

      version = Game.versionCode;
      bundle.put(VERSION, version);
      bundle.put(HERO, hero);
      bundle.put(GOLD, gold);
      bundle.put(DEPTH, depth);

      for (int d : droppedItems.keyArray()) {
        bundle.put(Messages.format(DROPPED, d), droppedItems.get(d));
      }

      quickslot.storePlaceholders(bundle);

      bundle.put(WT, transmutation);

      int[] dropValues = new int[limitedDrops.values().length];
      for (limitedDrops value : limitedDrops.values())
        dropValues[value.ordinal()] = value.count;
      bundle.put(LIMDROPS, dropValues);

      int count = 0;
      int ids[] = new int[chapters.size()];
      for (Integer id : chapters) {
        ids[count++] = id;
      }
      bundle.put(CHAPTERS, ids);

      Bundle quests = new Bundle();
      Ghost.Quest.INSTANCE.storeInBundle(quests);
      Wandmaker.Quest.storeInBundle(quests);
      Blacksmith.Quest.storeInBundle(quests);
      Imp.Quest.storeInBundle(quests);
      // dpd save
      Alchemist.Quest.INSTANCE.storeInBundle(quests);
      Jessica.Quest.INSTANCE.storeInBundle(quests);
      Yvette.Quest.INSTANCE.StoreInBundle(quests);
      Statuary.save(quests);

      bundle.put(QUESTS, quests);

      Statistics.INSTANCE.storeInBundle(bundle);
      Journal.INSTANCE.storeInBundle(bundle);
      Generator.INSTANCE.storeInBundle(bundle);

      Scroll.save(bundle);
      Potion.Companion.save(bundle);
      Ring.Companion.save(bundle);

      Actor.storeNextID(bundle);

      Bundle badges = new Bundle();
      Badges.INSTANCE.saveLocal(badges);
      bundle.put(BADGES, badges);

      OutputStream output = Game.instance.openFileOutput(fileName, Game
              .MODE_PRIVATE);
      Bundle.write(bundle, output);
      output.close();

      if (backupFile != null) {
        OutputStream os = Game.instance.openFileOutput(backupFile, Game
                .MODE_PRIVATE);
        Bundle.write(bundle, os);
        os.close();
      }

    } catch (IOException e) {
      GamesInProgress.INSTANCE.setUnknown(hero.getHeroClass());
      DarkestPixelDungeon.reportException(e);
    }
  }

  private static void saveLevel(String backupFile) throws IOException {
    Bundle bundle = new Bundle();
    bundle.put(LEVEL, level);

    OutputStream output = Game.instance.openFileOutput(
            Messages.format(depthFile(hero.getHeroClass()), depth), Game
                    .MODE_PRIVATE);
    Bundle.write(bundle, output);
    output.close();

    if (backupFile != null) {
      OutputStream os = Game.instance.openFileOutput(backupFile, Game
              .MODE_PRIVATE);
      Bundle.write(bundle, os);
      os.close();
    }
  }

  public static void loadGame(HeroClass cl) throws IOException {
    loadGame(gameFile(cl), true);
  }

  public static void loadBackupGame(HeroClass cl) throws IOException {
    loadGame(backupGameFile(cl), true);
  }

  public static void loadGame(String fileName, boolean fullLoad) throws
          IOException {

    Bundle bundle = gameBundle(fileName);

    version = bundle.getInt(VERSION);

    Generator.INSTANCE.reset();

    Actor.restoreNextID(bundle);

    quickslot.reset();
    QuickSlotButton.reset();

    Dungeon.level = null;
    Dungeon.depth = -1;

    Scroll.restore(bundle);
    Potion.Companion.restore(bundle);
    Ring.Companion.restore(bundle);

    quickslot.restorePlaceholders(bundle);

    if (fullLoad) {
      transmutation = bundle.getInt(WT);

      int[] dropValues = bundle.getIntArray(LIMDROPS);
      for (limitedDrops value : limitedDrops.values())
        value.count = value.ordinal() < dropValues.length ?
                dropValues[value.ordinal()] : 0;

      chapters = new HashSet<Integer>();
      int ids[] = bundle.getIntArray(CHAPTERS);
      if (ids != null) {
        for (int id : ids) {
          chapters.add(id);
        }
      }

      Bundle quests = bundle.getBundle(QUESTS);
      if (!quests.isNull()) {
        Ghost.Quest.INSTANCE.restoreFromBundle(quests);
        Wandmaker.Quest.restoreFromBundle(quests);
        Blacksmith.Quest.restoreFromBundle(quests);
        Imp.Quest.restoreFromBundle(quests);

        // dpd, restore quests
        Alchemist.Quest.INSTANCE.restoreFromBundle(quests);
        Jessica.Quest.INSTANCE.restoreFromBundle(quests);
        Statuary.load(quests);
        Yvette.Quest.INSTANCE.RestoreFromBundle(quests);
      } else {
        Ghost.Quest.INSTANCE.reset();
        Wandmaker.Quest.reset();
        Blacksmith.Quest.reset();
        Imp.Quest.reset();

        // dpd
        Alchemist.Quest.INSTANCE.reset();
        Jessica.Quest.INSTANCE.reset();
        Yvette.Quest.INSTANCE.Reset();
      }
    }

    Bundle badges = bundle.getBundle(BADGES);
    if (!badges.isNull()) {
      Badges.INSTANCE.loadLocal(badges);
    } else {
      Badges.INSTANCE.reset();
    }

    hero = null;
    hero = (Hero) bundle.get(HERO);

    Dungeon.gold = bundle.getInt(GOLD);
    Dungeon.depth = bundle.getInt(DEPTH);

    Statistics.INSTANCE.restoreFromBundle(bundle);
    Journal.INSTANCE.restoreFromBundle(bundle);
    Generator.INSTANCE.restoreFromBundle(bundle);

    droppedItems = new SparseArray<ArrayList<Item>>();
    for (int i = 2; i <= Statistics.INSTANCE.getDeepestFloor() + 1; i++) {
      ArrayList<Item> dropped = new ArrayList<Item>();
      for (Bundlable b : bundle.getCollection(Messages.format(DROPPED, i))) {
        dropped.add((Item) b);
      }
      if (!dropped.isEmpty()) {
        droppedItems.put(i, dropped);
      }
    }
  }

  public static Level loadBackupLevel(HeroClass cl) throws IOException {
    return loadLevelFromFile(backupLevelFile(cl));
  }

  public static Level loadLevel(HeroClass cl) throws IOException {
    return loadLevelFromFile(Messages.format(depthFile(cl), depth));
  }

  private static Level loadLevelFromFile(String filename) throws IOException {
    Dungeon.level = null;
    Actor.clear();

    InputStream is = Game.instance.openFileInput(filename);
    Bundle bundle = Bundle.read(is);
    is.close();

    return (Level) bundle.get("level");
  }

  public static void deleteGame(HeroClass cl, boolean deleteLevels, boolean
          deleteBackup) {

    Game.instance.deleteFile(gameFile(cl));

    if (deleteLevels) {
      int depth = 1;
      while (Game.instance.deleteFile(Messages.format(depthFile(cl), depth))) {
        depth++;
      }
    }

    if (deleteBackup) {
      Game.instance.deleteFile(backupGameFile(cl));
    }

    GamesInProgress.INSTANCE.delete(cl);
  }

  public static Bundle gameBundle(String fileName) throws IOException {

    InputStream input = Game.instance.openFileInput(fileName);
    Bundle bundle = Bundle.read(input);
    input.close();

    return bundle;
  }

  public static void preview(GamesInProgress.Info info, Bundle bundle) {
    info.setDepth(bundle.getInt(DEPTH));
    if (info.getDepth() == -1) {
      info.setDepth(bundle.getInt("maxDepth"));  // FIXME
    }
    Hero.Companion.Preview(info, bundle.getBundle(HERO));
  }

  public static void fail(Class cause) {
    if (hero.getBelongings().getItem(Ankh.class) == null) {
      Rankings.INSTANCE.Submit(false, cause);
    }
  }

  public static void win(Class cause) {

    hero.getBelongings().identify();

    if (IsChallenged()) Badges.INSTANCE.validateChampion();

    Rankings.INSTANCE.Submit(true, cause);
  }

  public static void observe() {
    if (hero == null)
      return;
    observe(hero.seeDistance() + 1);
  }

  public static void observe(int dist) {

    if (level == null) {
      return;
    }

    level.updateFieldOfView(hero, visible);

    int cx = hero.pos % level.width();
    int cy = hero.pos / level.width();

    int ax = Math.max(0, cx - dist);
    int bx = Math.min(cx + dist, level.width() - 1);
    int ay = Math.max(0, cy - dist);
    int by = Math.min(cy + dist, level.height() - 1);

    int len = bx - ax + 1;
    int pos = ax + ay * level.width();
    for (int y = ay; y <= by; y++, pos += level.width()) {
      BArray.or(level.getVisited(), visible, pos, len, level.getVisited());
    }

    if (hero.buff(MindVision.class) != null || hero.buff(Awareness.class) !=
            null)
      GameScene.updateFog();
    else
      GameScene.updateFog(ax, ay, len, by - ay);

    GameScene.afterObserve();
  }

  //we store this to avoid having to re-allocate the array with each pathfind
  private static boolean[] passable;

  private static void setupPassable() {
    if (passable == null || passable.length != Dungeon.level.length())
      passable = new boolean[Dungeon.level.length()];
    else
      BArray.setFalse(passable);
  }

  public static PathFinder.Path findPath(Char ch, int from, int to, boolean
          pass[], boolean[] visible) {

    setupPassable();
    if (ch.flying || ch.buff(Amok.class) != null) {
      BArray.or(pass, Level.Companion.getAvoid(), passable);
    } else {
      System.arraycopy(pass, 0, passable, 0, Dungeon.level.length());
    }

    for (Char c : Actor.chars()) {
      if (visible[c.pos]) {
        passable[c.pos] = false;
      }
    }

    return PathFinder.find(from, to, passable);

  }

  public static int findStep(Char ch, int from, int to, boolean pass[],
                             boolean[] visible) {

    if (level.adjacent(from, to)) {
      return Actor.findChar(to) == null && (pass[to] || Level.Companion
              .getAvoid()[to]) ? to
              : -1;
    }

    setupPassable();
    if (ch.flying || ch.buff(Amok.class) != null) {
      BArray.or(pass, Level.Companion.getAvoid(), passable);
    } else {
      System.arraycopy(pass, 0, passable, 0, Dungeon.level.length());
    }

    for (Char c : Actor.chars()) {
      if (visible[c.pos]) {
        passable[c.pos] = false;
      }
    }

    return PathFinder.getStep(from, to, passable);

  }

  public static int flee(Char ch, int cur, int from, boolean pass[],
                         boolean[] visible) {

    setupPassable();
    if (ch.flying) {
      BArray.or(pass, Level.Companion.getAvoid(), passable);
    } else {
      System.arraycopy(pass, 0, passable, 0, Dungeon.level.length());
    }

    for (Char c : Actor.chars()) {
      if (visible[c.pos]) {
        passable[c.pos] = false;
      }
    }
    passable[cur] = true;

    return PathFinder.getStepBack(cur, from, passable);

  }

}
