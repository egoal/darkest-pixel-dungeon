package com.egoal.darkestpixeldungeon.levels;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Jessica;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Wandmaker;
import com.egoal.darkestpixeldungeon.effects.Halo;
import com.egoal.darkestpixeldungeon.effects.particles.FlameParticle;
import com.egoal.darkestpixeldungeon.levels.diggers.Digger;
import com.egoal.darkestpixeldungeon.levels.traps.AlarmTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ChillingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ConfusionTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FireTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FlashingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FlockTrap;
import com.egoal.darkestpixeldungeon.levels.traps.GrippingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.LightningTrap;
import com.egoal.darkestpixeldungeon.levels.traps.OozeTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ParalyticTrap;
import com.egoal.darkestpixeldungeon.levels.traps.PoisonTrap;
import com.egoal.darkestpixeldungeon.levels.traps.SpearTrap;
import com.egoal.darkestpixeldungeon.levels.traps.SummoningTrap;
import com.egoal.darkestpixeldungeon.levels.traps.TeleportationTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ToxicTrap;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.watabou.noosa.Group;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 2018/12/17.
 */

public class DPDPrisonLevel extends DPDRegularLevel {
  {
    color1 = 0x6a723d;
    color2 = 0x88924c;

    viewDistance = 4;
  }

  // temp variable
  private boolean shouldAddWandmaker = false;

  @Override
  public String tilesTex() {
    return Assets.TILES_PRISON;
  }

  @Override
  public String waterTex() {
    return Assets.WATER_PRISON;
  }

  protected boolean[] water() {
    return Patch.generate(this, feeling == Feeling.WATER ? 0.65f : 0.45f, 4);
  }

  protected boolean[] grass() {
    return Patch.generate(this, feeling == Feeling.GRASS ? 0.60f : 0.40f, 3);
  }

  @Override
  protected Class<?>[] trapClasses() {
    return new Class[]{ChillingTrap.class, FireTrap.class, PoisonTrap.class,
            SpearTrap.class, ToxicTrap.class,
            AlarmTrap.class, FlashingTrap.class, GrippingTrap.class,
            ParalyticTrap.class, LightningTrap.class, OozeTrap.class,
            ConfusionTrap.class, FlockTrap.class, SummoningTrap.class,
            TeleportationTrap.class,};
  }

  @Override
  protected float[] trapChances() {
    return new float[]{4, 4, 4, 4,
            2, 2, 2, 2, 2, 2,
            1, 1, 1, 1};
  }

  protected ArrayList<Digger> chooseDiggers() {
    ArrayList<Digger> diggers = super.chooseDiggers();

    // wand maker
    Digger digger = Wandmaker.Quest.GiveDigger();
    if (digger != null) {
      shouldAddWandmaker = true;
      diggers.add(digger);
    }

    return diggers;
  }

  @Override
  protected void decorate() {
    for (int i = width() + 1; i < length() - width() - 1; i++) {
      if (map[i] == Terrain.EMPTY) {

        float c = 0.05f;
        if (map[i + 1] == Terrain.WALL && map[i + width()] == Terrain.WALL) {
          c += 0.2f;
        }
        if (map[i - 1] == Terrain.WALL && map[i + width()] == Terrain.WALL) {
          c += 0.2f;
        }
        if (map[i + 1] == Terrain.WALL && map[i - width()] == Terrain.WALL) {
          c += 0.2f;
        }
        if (map[i - 1] == Terrain.WALL && map[i - width()] == Terrain.WALL) {
          c += 0.2f;
        }

        if (Random.Float() < c) {
          map[i] = Terrain.EMPTY_DECO;
        }
      }
    }

    for (int i = 0; i < width(); i++) {
      if (map[i] == Terrain.WALL &&
              (map[i + width()] == Terrain.EMPTY || map[i + width()] ==
                      Terrain.EMPTY_SP) &&
              Random.Int(6) == 0) {

        map[i] = Terrain.WALL_DECO;
      }
    }

    for (int i = width(); i < length() - width(); i++) {
      if (map[i] == Terrain.WALL &&
              map[i - width()] == Terrain.WALL &&
              (map[i + width()] == Terrain.EMPTY || map[i + width()] ==
                      Terrain.EMPTY_SP) &&
              Random.Int(3) == 0) {

        map[i] = Terrain.WALL_DECO;
      }
    }
  }

  @Override
  public void createItems() {
    Jessica.Quest.spawnBook(this);

    super.createItems();
  }

  @Override
  public void createMobs() {
    if (shouldAddWandmaker) {
      for (Space s : spaces)
        if (s.type == Digger.DigResult.Type.ENTRANCE) {
          Wandmaker.Quest.Spawn(this, s.rect);
          break;
        }
    }

    super.createMobs();
  }

  @Override
  public String tileName(int tile) {
    switch (tile) {
      case Terrain.WATER:
        return Messages.get(PrisonLevel.class, "water_name");
      default:
        return super.tileName(tile);
    }
  }

  @Override
  public String tileDesc(int tile) {
    switch (tile) {
      case Terrain.EMPTY_DECO:
        return Messages.get(PrisonLevel.class, "empty_deco_desc");
      case Terrain.BOOKSHELF:
        return Messages.get(PrisonLevel.class, "bookshelf_desc");
      default:
        return super.tileDesc(tile);
    }
  }

  @Override
  protected LightVisual lightVisual(int pos) {
    return new PrisonLevel.Torch(pos);
  }

  // not used now
  public static void AddPrisonVisuals(Level level, Group group) {
    for (int i = 0; i < level.length(); i++) {
      if (level.map[i] == Terrain.WALL_DECO) {
        group.add(new PrisonLevel.Torch(i));
      }
    }
  }

  public static class Torch extends LightVisual {

    public Torch(int pos) {
      super(pos);

      PointF p = DungeonTilemap.tileCenterToWorld(pos);
      pos(p.x - 1, p.y + 3, 2, 0);

      pour(FlameParticle.FACTORY, 0.15f);

      add(new Halo(16, 0xFFFFCC, 0.2f).point(p.x, p.y));
    }

    @Override
    public void update() {
      if (visible = Dungeon.visible[pos]) {
        super.update();
      }
    }
  }
}
