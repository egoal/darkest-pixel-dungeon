package com.egoal.darkestpixeldungeon.levels;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.DungeonTilemap;
import com.egoal.darkestpixeldungeon.actors.mobs.npcs.Ghost;
import com.egoal.darkestpixeldungeon.effects.Ripple;
import com.egoal.darkestpixeldungeon.items.DewVial;
import com.egoal.darkestpixeldungeon.levels.traps.AlarmTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ChillingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FlockTrap;
import com.egoal.darkestpixeldungeon.levels.traps.OozeTrap;
import com.egoal.darkestpixeldungeon.levels.traps.SummoningTrap;
import com.egoal.darkestpixeldungeon.levels.traps.TeleportationTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ToxicTrap;
import com.egoal.darkestpixeldungeon.levels.traps.WornTrap;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.watabou.noosa.Game;
import com.watabou.noosa.Group;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/13.
 */

public class DPDSewerLevel extends DPDRegularLevel {
  {
    color1 = 0x48763c;
    color2 = 0x59994a;
    viewDistance = 4;
    seeDistance = 8;
  }

  @Override
  public String tilesTex() {
    return Assets.TILES_SEWERS;
  }

  @Override
  public String waterTex() {
    return Assets.WATER_SEWERS;
  }

  @Override
  protected boolean[] water() {
    return Patch.generate(this, feeling == Feeling.WATER ? 0.60f : 0.45f, 5);
  }

  @Override
  protected boolean[] grass() {
    return Patch.generate(this, feeling == Feeling.GRASS ? 0.60f : 0.40f, 4);
  }

  @Override
  protected Class<?>[] trapClasses() {
    return Dungeon.depth == 1 ? new Class<?>[]{WornTrap.class} :
            new Class<?>[]{ChillingTrap.class, ToxicTrap.class, WornTrap.class,
                    AlarmTrap.class, OozeTrap.class, FlockTrap.class,
                    SummoningTrap.class, TeleportationTrap.class};
  }

  @Override
  protected float[] trapChances() {
    return Dungeon.depth == 1 ? new float[]{1} :
            new float[]{4, 4, 4, 2, 2, 1, 1, 1};
  }

  @Override
  protected void decorate() {
  }

  @Override
  protected void createItems() {
    // dew vial
    if (Dungeon.depth == 1 && !Dungeon.limitedDrops.dewVial.dropped()) {
      addItemToSpawn(new DewVial());
      Dungeon.limitedDrops.dewVial.drop();
    }

    Ghost.Quest.Spawn(this);

    super.createItems();
  }

  @Override
  public Group addVisuals() {
    super.addVisuals();
    AddSewerVisuals(this, visuals);
    return visuals;
  }

  public static void AddSewerVisuals(Level level, Group group) {
    for (int i = 0; i < level.length(); ++i)
      if (level.map[i] == Terrain.WALL_DECO)
        group.add(new Sink(i));
  }

  //todo: tileName, tileDesc
  @Override
  public String tileName(int tile) {
    switch (tile) {
      case Terrain.WATER:
        return Messages.get(SewerLevel.class, "water_name");
      default:
        return super.tileName(tile);
    }
  }

  @Override
  public String tileDesc(int tile) {
    switch (tile) {
      case Terrain.EMPTY_DECO:
        return Messages.get(SewerLevel.class, "empty_deco_desc");
      case Terrain.BOOKSHELF:
        return Messages.get(SewerLevel.class, "bookshelf_desc");
      default:
        return super.tileDesc(tile);
    }
  }

  private static class Sink extends Emitter {

    private int pos;
    private float rippleDelay = 0;

    private static final Emitter.Factory factory = new Factory() {

      @Override
      public void emit(Emitter emitter, int index, float x, float y) {
        WaterParticle p = (WaterParticle) emitter.recycle(WaterParticle.class);
        p.reset(x, y);
      }
    };

    public Sink(int pos) {
      super();

      this.pos = pos;

      PointF p = DungeonTilemap.tileCenterToWorld(pos);
      pos(p.x - 2, p.y + 1, 4, 0);

      pour(factory, 0.1f);
    }

    @Override
    public void update() {
      if (visible = Dungeon.visible[pos]) {

        super.update();

        if ((rippleDelay -= Game.elapsed) <= 0) {
          Ripple ripple = GameScene.ripple(pos + Dungeon.level.width());
          if (ripple != null) {
            ripple.y -= DungeonTilemap.SIZE / 2;
            rippleDelay = Random.Float(0.4f, 0.6f);
          }
        }
      }
    }
  }

  public static final class WaterParticle extends PixelParticle {

    public WaterParticle() {
      super();

      acc.y = 50;
      am = 0.5f;

      color(ColorMath.random(0xb6ccc2, 0x3b6653));
      size(2);
    }

    public void reset(float x, float y) {
      revive();

      this.x = x;
      this.y = y;

      speed.set(Random.Float(-2, +2), 0);

      left = lifespan = 0.5f;
    }
  }

}
