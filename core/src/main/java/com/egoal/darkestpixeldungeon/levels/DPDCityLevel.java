package com.egoal.darkestpixeldungeon.levels;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Imp;
import com.egoal.darkestpixeldungeon.levels.traps.BlazingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.CursingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.DisarmingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ExplosiveTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FlockTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FrostTrap;
import com.egoal.darkestpixeldungeon.levels.traps.GrippingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.GuardianTrap;
import com.egoal.darkestpixeldungeon.levels.traps.LightningTrap;
import com.egoal.darkestpixeldungeon.levels.traps.OozeTrap;
import com.egoal.darkestpixeldungeon.levels.traps.PitfallTrap;
import com.egoal.darkestpixeldungeon.levels.traps.RockfallTrap;
import com.egoal.darkestpixeldungeon.levels.traps.SpearTrap;
import com.egoal.darkestpixeldungeon.levels.traps.SummoningTrap;
import com.egoal.darkestpixeldungeon.levels.traps.TeleportationTrap;
import com.egoal.darkestpixeldungeon.levels.traps.VenomTrap;
import com.egoal.darkestpixeldungeon.levels.traps.WarpingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.WeakeningTrap;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/19.
 */

public class DPDCityLevel extends DPDRegularLevel {

  {
    color1 = 0x4b6636;
    color2 = 0xf2f2f2;

    viewDistance = 4;
  }

  @Override
  public String tilesTex() {
    return Assets.TILES_CITY;
  }

  @Override
  public String waterTex() {
    return Assets.WATER_CITY;
  }

  protected boolean[] water() {
    return Patch.generate(this, feeling == Feeling.WATER ? 0.55f : 0.45f, 4);
  }

  protected boolean[] grass() {
    return Patch.generate(this, feeling == Feeling.GRASS ? 0.55f : 0.40f, 3);
  }

  @Override
  protected Class<?>[] trapClasses() {
    return new Class[]{BlazingTrap.class, FrostTrap.class, SpearTrap.class,
            VenomTrap.class,
            ExplosiveTrap.class, GrippingTrap.class, LightningTrap.class,
            RockfallTrap.class, OozeTrap.class, WeakeningTrap.class,
            CursingTrap.class, FlockTrap.class, GuardianTrap.class,
            PitfallTrap.class, SummoningTrap.class, TeleportationTrap.class,
            DisarmingTrap.class, WarpingTrap.class};
  }

  @Override
  protected float[] trapChances() {
    return new float[]{8, 8, 8, 8,
            4, 4, 4, 4, 4, 4,
            2, 2, 2, 2, 2, 2,
            1, 1};
  }

  @Override
  protected void decorate() {

    for (int i = 0; i < length(); i++) {
      if (map[i] == Terrain.EMPTY && Random.Int(10) == 0) {
        map[i] = Terrain.EMPTY_DECO;
      } else if (map[i] == Terrain.WALL && Random.Int(8) == 0) {
        map[i] = Terrain.WALL_DECO;
      }
    }
  }

  @Override
  protected void createItems() {
    super.createItems();

    Imp.Quest.Spawn(this);
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
    AddCityVisuals(this, visuals);
    return visuals;
  }

  public static void AddCityVisuals(Level level, Group group) {
    for (int i = 0; i < level.length(); ++i)
      if (level.map[i] == Terrain.WALL_LIGHT_ON)
        group.add(new Smoke(i));
  }

  private static class Smoke extends Emitter {

    private int pos;

    private static final Emitter.Factory factory = new Factory() {

      @Override
      public void emit(Emitter emitter, int index, float x, float y) {
        SmokeParticle p = (SmokeParticle) emitter.recycle(SmokeParticle.class);
        p.reset(x, y);
      }
    };

    public Smoke(int pos) {
      super();

      this.pos = pos;

      PointF p = DungeonTilemap.tileCenterToWorld(pos);
      pos(p.x - 4, p.y - 2, 4, 0);

      pour(factory, 0.2f);
    }

    @Override
    public void update() {
      if (visible = Dungeon.visible[pos]) {
        super.update();
      }
    }
  }

  public static final class SmokeParticle extends PixelParticle {

    public SmokeParticle() {
      super();

      color(0x000000);
      speed.set(Random.Float(8), -Random.Float(8));
    }

    public void reset(float x, float y) {
      revive();

      this.x = x;
      this.y = y;

      left = lifespan = 2f;
    }

    @Override
    public void update() {
      super.update();
      float p = left / lifespan;
      am = p > 0.8f ? 1 - p : p * 0.25f;
      size(8 - p * 4);
    }
  }
}
