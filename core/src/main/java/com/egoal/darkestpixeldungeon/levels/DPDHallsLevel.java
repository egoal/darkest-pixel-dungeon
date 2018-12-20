package com.egoal.darkestpixeldungeon.levels;

import android.opengl.GLES20;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.levels.diggers.Digger;
import com.egoal.darkestpixeldungeon.levels.traps.BlazingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.CursingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.DisarmingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.DisintegrationTrap;
import com.egoal.darkestpixeldungeon.levels.traps.DistortionTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ExplosiveTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FlockTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FrostTrap;
import com.egoal.darkestpixeldungeon.levels.traps.GrimTrap;
import com.egoal.darkestpixeldungeon.levels.traps.GrippingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.GuardianTrap;
import com.egoal.darkestpixeldungeon.levels.traps.LightningTrap;
import com.egoal.darkestpixeldungeon.levels.traps.OozeTrap;
import com.egoal.darkestpixeldungeon.levels.traps.SpearTrap;
import com.egoal.darkestpixeldungeon.levels.traps.SummoningTrap;
import com.egoal.darkestpixeldungeon.levels.traps.TeleportationTrap;
import com.egoal.darkestpixeldungeon.levels.traps.VenomTrap;
import com.egoal.darkestpixeldungeon.levels.traps.WarpingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.WeakeningTrap;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by 93942 on 2018/12/20.
 */

public class DPDHallsLevel extends DPDRegularLevel {
  {
    color1 = 0x801500;
    color2 = 0xa68521;

    viewDistance = Math.max(25 - Dungeon.depth, 1);
  }

  // no extra torch for you again.

  // smaller size
  protected ArrayList<Digger> chooseDiggers() {
    return selectDiggers(Random.NormalIntRange(3, 5), 14);
  }

  @Override
  public String tilesTex() {
    return Assets.TILES_HALLS;
  }

  @Override
  public String waterTex() {
    return Assets.WATER_HALLS;
  }

  protected boolean[] water() {
    return Patch.generate(this, feeling == Feeling.WATER ? 0.55f : 0.40f, 6);
  }

  protected boolean[] grass() {
    return Patch.generate(this, feeling == Feeling.GRASS ? 0.55f : 0.30f, 3);
  }

  @Override
  protected Class<?>[] trapClasses() {
    return new Class[]{BlazingTrap.class, DisintegrationTrap.class, FrostTrap
            .class, SpearTrap.class, VenomTrap.class,
            ExplosiveTrap.class, GrippingTrap.class, LightningTrap.class,
            OozeTrap.class, WeakeningTrap.class,
            CursingTrap.class, FlockTrap.class, GrimTrap.class, GuardianTrap
            .class, SummoningTrap.class, TeleportationTrap.class,
            DisarmingTrap.class, DistortionTrap.class, WarpingTrap.class};
  }

  @Override
  protected float[] trapChances() {
    return new float[]{8, 8, 8, 8, 8,
            4, 4, 4, 4, 4,
            2, 2, 2, 2, 2, 2,
            1, 1, 1};
  }

  @Override
  protected void decorate() {

    for (int i = width() + 1; i < length() - width() - 1; i++) {
      if (map[i] == Terrain.EMPTY) {

        int count = 0;
        for (int j = 0; j < PathFinder.NEIGHBOURS8.length; j++) {
          if ((Terrain.flags[map[i + PathFinder.NEIGHBOURS8[j]]] & Terrain
                  .PASSABLE) > 0) {
            count++;
          }
        }

        if (Random.Int(80) < count) {
          map[i] = Terrain.EMPTY_DECO;
        }

      } else if (map[i] == Terrain.WALL &&
              map[i - 1] != Terrain.WALL_DECO && map[i - width()] != Terrain
              .WALL_DECO &&
              Random.Int(20) == 0) {

        map[i] = Terrain.WALL_DECO;

      }
    }
  }

  @Override
  public String tileName(int tile) {
    switch (tile) {
      case Terrain.WATER:
        return Messages.get(HallsLevel.class, "water_name");
      case Terrain.GRASS:
        return Messages.get(HallsLevel.class, "grass_name");
      case Terrain.HIGH_GRASS:
        return Messages.get(HallsLevel.class, "high_grass_name");
      case Terrain.STATUE:
      case Terrain.STATUE_SP:
        return Messages.get(HallsLevel.class, "statue_name");
      default:
        return super.tileName(tile);
    }
  }

  @Override
  public String tileDesc(int tile) {
    switch (tile) {
      case Terrain.WATER:
        return Messages.get(HallsLevel.class, "water_desc");
      case Terrain.STATUE:
      case Terrain.STATUE_SP:
        return Messages.get(HallsLevel.class, "statue_desc");
      case Terrain.BOOKSHELF:
        return Messages.get(HallsLevel.class, "bookshelf_desc");
      default:
        return super.tileDesc(tile);
    }
  }

  @Override
  public Group addVisuals() {
    super.addVisuals();
    AddHallsVisuals(this, visuals);
    
    return visuals;
  }

  public static void AddHallsVisuals(Level level, Group group) {
    for (int i = 0; i < level.length(); ++i)
      if (level.map[i] == Terrain.WATER)
        group.add(new Stream(i));
  }

  private static class Stream extends Group {

    private int pos;

    private float delay;

    public Stream(int pos) {
      super();

      this.pos = pos;

      delay = Random.Float(2);
    }

    @Override
    public void update() {

      if (visible = Dungeon.visible[pos]) {

        super.update();

        if ((delay -= Game.elapsed) <= 0) {

          delay = Random.Float(2);

          PointF p = DungeonTilemap.tileToWorld(pos);
          ((FireParticle) recycle(FireParticle.class)).reset(
                  p.x + Random.Float(DungeonTilemap.SIZE),
                  p.y + Random.Float(DungeonTilemap.SIZE));
        }
      }
    }

    @Override
    public void draw() {
      GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
      super.draw();
      GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    }
  }

  public static class FireParticle extends PixelParticle.Shrinking {

    public FireParticle() {
      super();

      color(0xEE7722);
      lifespan = 1f;

      acc.set(0, +80);
    }

    public void reset(float x, float y) {
      revive();

      this.x = x;
      this.y = y;

      left = lifespan;

      speed.set(0, -40);
      size = 4;
    }

    @Override
    public void update() {
      super.update();
      float p = left / lifespan;
      am = p > 0.8f ? (1 - p) * 5 : 1;
    }
  }
}
