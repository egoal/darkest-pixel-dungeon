package com.egoal.darkestpixeldungeon.levels;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.egoal.darkestpixeldungeon.levels.diggers.Digger;
import com.egoal.darkestpixeldungeon.levels.traps.ConfusionTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ExplosiveTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FireTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FlashingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FlockTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FrostTrap;
import com.egoal.darkestpixeldungeon.levels.traps.GrippingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.GuardianTrap;
import com.egoal.darkestpixeldungeon.levels.traps.LightningTrap;
import com.egoal.darkestpixeldungeon.levels.traps.OozeTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ParalyticTrap;
import com.egoal.darkestpixeldungeon.levels.traps.PitfallTrap;
import com.egoal.darkestpixeldungeon.levels.traps.PoisonTrap;
import com.egoal.darkestpixeldungeon.levels.traps.RockfallTrap;
import com.egoal.darkestpixeldungeon.levels.traps.SpearTrap;
import com.egoal.darkestpixeldungeon.levels.traps.SummoningTrap;
import com.egoal.darkestpixeldungeon.levels.traps.TeleportationTrap;
import com.egoal.darkestpixeldungeon.levels.traps.VenomTrap;
import com.egoal.darkestpixeldungeon.levels.traps.WarpingTrap;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 2018/12/17.
 */

public class DPDCavesLevel extends DPDRegularLevel {
  {
    color1 = 0x534f3e;
    color2 = 0xb9d661;
    
    viewDistance = 4;
  }

  private boolean shouldAddBlackSmith = false;
  
  @Override
  public String tilesTex() {
    return Assets.TILES_CAVES;
  }

  @Override
  public String waterTex() {
    return Assets.WATER_CAVES;
  }

  protected boolean[] water() {
    return Patch.generate(this, feeling == Feeling.WATER ? 0.60f : 0.45f, 6);
  }

  protected boolean[] grass() {
    return Patch.generate(this, feeling == Feeling.GRASS ? 0.55f : 0.35f, 3);
  }

  @Override
  protected Class<?>[] trapClasses() {
    return new Class[]{FireTrap.class, FrostTrap.class, PoisonTrap.class,
            SpearTrap.class, VenomTrap.class,
            ExplosiveTrap.class, FlashingTrap.class, GrippingTrap.class,
            ParalyticTrap.class, LightningTrap.class, RockfallTrap.class,
            OozeTrap.class,
            ConfusionTrap.class, FlockTrap.class, GuardianTrap.class,
            PitfallTrap.class, SummoningTrap.class, TeleportationTrap.class,
            WarpingTrap.class};
  }

  @Override
  protected float[] trapChances() {
    return new float[]{8, 8, 8, 8, 8,
            4, 4, 4, 4, 4, 4, 4,
            2, 2, 2, 2, 2, 2,
            1};
  }

  //todo: spawn blacksmith, affect the diggers.
  protected ArrayList<Digger> chooseDiggers() {
    ArrayList<Digger> diggers = super.chooseDiggers();

    // wand maker
    Digger digger = Blacksmith.Quest.GiveDigger();
    if (digger != null) {
      shouldAddBlackSmith = true;
      diggers.add(digger);
    }

    return diggers;
  }
  
  @Override
  protected void decorate() {
    //todo: rework this.
  }

  @Override
  public void createMobs() {
    if (shouldAddBlackSmith) 
      Blacksmith.Quest.Spawn();

    super.createMobs();
  }
  
  @Override
  public String tileName(int tile) {
    switch (tile) {
      case Terrain.GRASS:
        return Messages.get(CavesLevel.class, "grass_name");
      case Terrain.HIGH_GRASS:
        return Messages.get(CavesLevel.class, "high_grass_name");
      case Terrain.WATER:
        return Messages.get(CavesLevel.class, "water_name");
      default:
        return super.tileName(tile);
    }
  }

  @Override
  public String tileDesc(int tile) {
    switch (tile) {
      case Terrain.ENTRANCE:
        return Messages.get(CavesLevel.class, "entrance_desc");
      case Terrain.EXIT:
        return Messages.get(CavesLevel.class, "exit_desc");
      case Terrain.HIGH_GRASS:
        return Messages.get(CavesLevel.class, "high_grass_desc");
      case Terrain.WALL_DECO:
        return Messages.get(CavesLevel.class, "wall_deco_desc");
      case Terrain.BOOKSHELF:
        return Messages.get(CavesLevel.class, "bookshelf_desc");
      default:
        return super.tileDesc(tile);
    }
  }

  @Override
  public Group addVisuals() {
    super.addVisuals();
    AddCavesVisuals(this, visuals);
    return visuals;
  }

  public static void AddCavesVisuals(Level level, Group group) {
    for (int i = 0; i < level.length(); i++) {
      if (level.map[i] == Terrain.WALL_DECO) {
        group.add(new Vein(i));
      }
    }
  }

  private static class Vein extends Group {

    private int pos;

    private float delay;

    public Vein(int pos) {
      super();

      this.pos = pos;

      delay = Random.Float(2);
    }

    @Override
    public void update() {

      if (visible = Dungeon.visible[pos]) {

        super.update();

        if ((delay -= Game.elapsed) <= 0) {

          //pickaxe can remove the ore, should remove the sparkling too.
          if (Dungeon.level.map[pos] != Terrain.WALL_DECO) {
            kill();
            return;
          }

          delay = Random.Float();

          PointF p = DungeonTilemap.tileToWorld(pos);
          ((Sparkle) recycle(Sparkle.class)).reset(
                  p.x + Random.Float(DungeonTilemap.SIZE),
                  p.y + Random.Float(DungeonTilemap.SIZE));
        }
      }
    }
  }

  public static final class Sparkle extends PixelParticle {

    public void reset(float x, float y) {
      revive();

      this.x = x;
      this.y = y;

      left = lifespan = 0.5f;
    }

    @Override
    public void update() {
      super.update();

      float p = left / lifespan;
      size((am = p < 0.5f ? p * 2 : (1 - p) * 2) * 2);
    }
  }

}
