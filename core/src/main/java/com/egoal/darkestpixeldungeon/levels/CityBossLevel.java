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

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.mobs.King;
import com.egoal.darkestpixeldungeon.effects.particles.ShadowParticle;
import com.egoal.darkestpixeldungeon.items.keys.SkeletonKey;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Bones;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Actor;
import com.egoal.darkestpixeldungeon.actors.mobs.Bestiary;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.levels.painters.Painter;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.noosa.Group;
import com.watabou.noosa.audio.Music;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class CityBossLevel extends Level {

  {
    color1 = 0x4b6636;
    color2 = 0xf2f2f2;
  }

  private static final String MAP_FILE = "data/CityBossLevel.map";

  private int arenaDoor;
  private boolean enteredArena = false;
  private boolean keyDropped = false;
  private int remainStatuaries = 6;

  @Override
  public String tilesTex() {
    return Assets.TILES_CITY;
  }

  @Override
  public String waterTex() {
    return Assets.WATER_CITY;
  }

  @Override
  public String trackMusic() {
    return (enteredArena && !keyDropped) ? Assets.TRACK_BOSS_LOOP :
            Assets.TRACK_CHAPTER_4;
  }

  private static final String DOOR = "door";
  private static final String ENTERED = "entered";
  private static final String DROPPED = "droppped";
  private static final String STATUARIES = "statuaries";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(DOOR, arenaDoor);
    bundle.put(ENTERED, enteredArena);
    bundle.put(DROPPED, keyDropped);
    bundle.put(STATUARIES, remainStatuaries);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    arenaDoor = bundle.getInt(DOOR);
    enteredArena = bundle.getBoolean(ENTERED);
    keyDropped = bundle.getBoolean(DROPPED);
    remainStatuaries = bundle.getInt(STATUARIES);
  }

  @Override
  protected boolean build(int iterations) {
    loadMapDataFromFile(MAP_FILE);

    // entrance and exit already assigned in loading

    arenaDoor = xy2cell(17, 29);

    return true;
  }

  @Override
  protected void decorate() {
    // decoration is done by hand, in Tiled.
  }

  public int pedestal(boolean left) {
    return left ? xy2cell(15, 11) : xy2cell(19, 11);
  }

  @Override
  protected void createMobs() {
  }

  public Actor respawner() {
    return null;
  }

  @Override
  protected void createItems() {
    Item item = Bones.get();
    if (item != null) {
      // drop in entrance room
      //[14, 30]->[22, 33]
      int pos;
      do {
        pos = xy2cell(Random.Int(14, 23), Random.Int(30, 34));
      } while (pos == entrance || map[pos] == Terrain.SIGN);
      drop(item, pos).type = Heap.Type.REMAINS;
    }
  }

  @Override
  public int randomRespawnCell() {
    int cell = entrance + PathFinder.NEIGHBOURS8[Random.Int(8)];
    while (!passable[cell]) {
      cell = entrance + PathFinder.NEIGHBOURS8[Random.Int(8)];
    }
    return cell;
  }

  @Override
  public void press(int cell, Char hero) {

    super.press(cell, hero);

    // when walk near the statuary, active
    if (hero == Dungeon.hero && remainStatuaries > 0)
      activeNearbyStatuaries(cell);

    // create the king
    if (!enteredArena && isNearToHallCenter(cell) && hero == Dungeon.hero) {

      enteredArena = true;
      seal();

      Mob boss = Bestiary.mob(Dungeon.depth);
      boss.state = boss.WANDERING;
      boss.pos = xy2cell(17, 6);
      GameScene.add(boss);

      if (Dungeon.visible[boss.pos]) {
        boss.notice();
        boss.sprite.alpha(0);
        boss.sprite.parent.add(new AlphaTweener(boss.sprite, 1, 0.1f));
      }

      set(arenaDoor, Terrain.LOCKED_DOOR);
      GameScene.updateMap(arenaDoor);
      Dungeon.observe();

      boss.yell(Messages.get(boss, "greeting"));

      Music.INSTANCE.play(trackMusic(), true);
      Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f);
    }
  }

  private void activeNearbyStatuaries(int cell) {
    final int DISTANCE = 3;
    int x = cell % width();
    int y = cell / width();

    int actives = 0;
    // L1 distance
    for (int ix = x - DISTANCE; ix <= x + DISTANCE; ++ix) {
      for (int iy = y - DISTANCE; iy <= y + DISTANCE; ++iy) {
        int i = xy2cell(ix, iy);
        if (i < 0 || i >= length) continue;

        if (map[i] == Terrain.STATUE_SP) {
          // active
          map[i] = Terrain.EMPTY_SP;
          GameScene.updateMap(i);

          ++actives;

          King.Undead ku = new King.Undead();
          ku.state = ku.HUNTING;
          ku.pos = i;
          GameScene.add(ku, 1f);  // delay a turn

          ku.yell(Messages.get(ku, "awaken"));

          if (Dungeon.visible[i]) {
            ku.sprite.emitter().start(ShadowParticle.CURSE, .05f, 10);
            Sample.INSTANCE.play(Assets.SND_BONES);
          }
        }
      }
    }

    remainStatuaries -= actives;

    if (actives > 0) {
      buildFlagMaps();
      Dungeon.observe();
    }
  }

  @Override
  public Heap drop(Item item, int cell) {

    if (!keyDropped && item instanceof SkeletonKey) {

      keyDropped = true;
      unseal();

      set(arenaDoor, Terrain.DOOR);
      GameScene.updateMap(arenaDoor);
      Dungeon.observe();

      Music.INSTANCE.play(trackMusic(), true);
      Music.INSTANCE.volume(DarkestPixelDungeon.musicVol() / 10f);
    }

    return super.drop(item, cell);
  }

  private boolean isNearToHallCenter(int cell) {
    return cell / width() < 18;
  }

  @Override
  public String tileName(int tile) {
    switch (tile) {
      case Terrain.WATER:
        return Messages.get(CityLevel.class, "water_name");
      case Terrain.HIGH_GRASS:
        return Messages.get(CityLevel.class, "high_grass_name");
      default:
        return super.tileName(tile);
    }
  }

  @Override
  public String tileDesc(int tile) {
    switch (tile) {
      case Terrain.ENTRANCE:
        return Messages.get(CityLevel.class, "entrance_desc");
      case Terrain.EXIT:
        return Messages.get(CityLevel.class, "exit_desc");
      case Terrain.WALL_DECO:
      case Terrain.EMPTY_DECO:
        return Messages.get(CityLevel.class, "deco_desc");
      case Terrain.EMPTY_SP:
        return Messages.get(CityLevel.class, "sp_desc");
      case Terrain.STATUE:
      case Terrain.STATUE_SP:
        return Messages.get(CityLevel.class, "statue_desc");
      case Terrain.BOOKSHELF:
        return Messages.get(CityLevel.class, "bookshelf_desc");
      default:
        return super.tileDesc(tile);
    }
  }

  @Override
  public Group addVisuals() {
    super.addVisuals();
    CityLevel.addCityVisuals(this, visuals);
    return visuals;
  }
}
