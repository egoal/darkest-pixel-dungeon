package com.egoal.darkestpixeldungeon.levels.diggers.specials;

import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfLevitation;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.diggers.DigResult;
import com.egoal.darkestpixeldungeon.levels.diggers.normal.RectDigger;
import com.egoal.darkestpixeldungeon.levels.diggers.XRect;
import com.egoal.darkestpixeldungeon.levels.diggers.XWall;
import com.egoal.darkestpixeldungeon.levels.traps.BlazingTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ConfusionTrap;
import com.egoal.darkestpixeldungeon.levels.traps.DisintegrationTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ExplosiveTrap;
import com.egoal.darkestpixeldungeon.levels.traps.FlockTrap;
import com.egoal.darkestpixeldungeon.levels.traps.GrimTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ParalyticTrap;
import com.egoal.darkestpixeldungeon.levels.traps.SpearTrap;
import com.egoal.darkestpixeldungeon.levels.traps.SummoningTrap;
import com.egoal.darkestpixeldungeon.levels.traps.TeleportationTrap;
import com.egoal.darkestpixeldungeon.levels.traps.ToxicTrap;
import com.egoal.darkestpixeldungeon.levels.traps.Trap;
import com.egoal.darkestpixeldungeon.levels.traps.VenomTrap;
import com.egoal.darkestpixeldungeon.levels.traps.WarpingTrap;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 2018/12/8.
 */

public class TrapsDigger extends RectDigger {
  protected Point chooseRoomSize(XWall wall) {
    if (Random.Int(3) == 0)
      return new Point(Random.IntRange(7, 9), Random.IntRange(7, 9));
    return super.chooseRoomSize(wall);
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    if (rect.w() >= 7 && rect.h() >= 7)
      return digBig(level, wall, rect);

    Class<? extends Trap> trapClass = chooseTrap();

    Fill(level, rect, trapClass == null ? Terrain.CHASM : Terrain.TRAP);
    Point door = overlapedWall(wall, rect).random();
    Set(level, door, Terrain.DOOR);

    int lastRowTile = trapClass == null ? Terrain.CHASM : Terrain.EMPTY;
    Point plat = rect.cen();
    switch (wall.direction) {
      case LEFT:
        LinkV(level, rect.x1, rect.y1, rect.y2, lastRowTile);
        plat.x = rect.x1;
        break;
      case RIGHT:
        LinkV(level, rect.x2, rect.y1, rect.y2, lastRowTile);
        plat.x = rect.x2;
        break;
      case UP:
        LinkH(level, rect.y1, rect.x1, rect.x2, lastRowTile);
        plat.y = rect.y1;
        break;
      case DOWN:
        LinkH(level, rect.y2, rect.x1, rect.x2, lastRowTile);
        plat.y = rect.y2;
        break;
    }

    putTraps(level, rect, trapClass);

    int pos = level.pointToCell(plat);
    if (Random.Int(3) == 0) {
      if (lastRowTile == Terrain.CHASM)
        Set(level, pos, Terrain.EMPTY);
      level.drop(Prize(level), pos).type = Heap.Type.CHEST;
    } else {
      Set(level, pos, Terrain.PEDESTAL);
      level.drop(Prize(level), pos);
    }

    level.addItemToSpawn(new PotionOfLevitation());

    return new DigResult(DigResult.Type.LOCKED);
  }

  public DigResult digBig(Level level, XWall wall, XRect rect) {
    Class<? extends Trap> trapClass = chooseTrap();

    Fill(level, rect, Terrain.EMPTY);
    Point door = overlapedWall(wall, rect).random();
    Set(level, door, Terrain.DOOR);

    Fill(level, rect.inner(1), trapClass == null ? Terrain.CHASM : Terrain
            .TRAP);
    int plat = level.pointToCell(rect.cen());
    Set(level, plat, Terrain.EMPTY);

    putTraps(level, rect.inner(1), trapClass);
    if (Random.Int(3) == 0) {
      level.drop(Prize(level), plat);
    } else {
      Set(level, plat, Terrain.PEDESTAL);
      level.drop(Prize(level), plat);
    }

    level.addItemToSpawn(new PotionOfLevitation());

    return new DigResult(DigResult.Type.SPECIAL).walls(
            wallsBut(rect, -wall.direction));
  }

  private Class<? extends Trap> chooseTrap() {
    Class<? extends Trap> trapClass = null;
    switch (Random.Int(5)) {
      case 0:
      default:
        trapClass = SpearTrap.class;
        break;
      case 1:
        trapClass = !Dungeon.bossLevel(Dungeon.depth + 1) ? null :
                SummoningTrap.class;
        break;
      case 2:
      case 3:
      case 4:
        trapClass = Random.oneOf(LevelTraps[Dungeon.depth / 5]);
        break;
    }

    return trapClass;
  }

  private void putTraps(Level level, XRect rect, Class<? extends Trap>
          trapClass) {
    for (Point p : rect.getAllPoints()) {
      int c = level.pointToCell(p);
      if (level.map[c] == Terrain.TRAP) {
        try {
          level.setTrap(((Trap) trapClass.newInstance()).reveal(), c);
        } catch (Exception e) {
          DarkestPixelDungeon.reportException(e);
        }
      }
    }
  }

  private static Item Prize(Level level) {

    Item prize;

    if (Random.Int(4) != 0) {
      prize = level.findPrizeItem();
      if (prize != null)
        return prize;
    }

    prize = Generator.random(Random.oneOf(
            Generator.Category.WEAPON,
            Generator.Category.ARMOR
    ));

    for (int i = 0; i < 3; i++) {
      Item another = Generator.random(Random.oneOf(
              Generator.Category.WEAPON,
              Generator.Category.ARMOR
      ));
      if (another.level() > prize.level()) {
        prize = another;
      }
    }

    return prize;
  }

  @SuppressWarnings("unchecked")
  private static Class<? extends Trap>[][] LevelTraps = new Class[][]{
          //sewers
          {ToxicTrap.class, TeleportationTrap.class, FlockTrap.class},
          //prison
          {ConfusionTrap.class, ExplosiveTrap.class, ParalyticTrap.class},
          //caves
          {BlazingTrap.class, VenomTrap.class, ExplosiveTrap.class},
          //city
          {WarpingTrap.class, VenomTrap.class, DisintegrationTrap.class},
          //halls, muahahahaha
          {GrimTrap.class}
  };
}
