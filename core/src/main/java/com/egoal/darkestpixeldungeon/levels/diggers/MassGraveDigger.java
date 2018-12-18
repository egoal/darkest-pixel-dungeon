package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.actors.mobs.Skeleton;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Gold;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.potions.PotionOfLiquidFlame;
import com.egoal.darkestpixeldungeon.items.quest.CorpseDust;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.ui.CustomTileVisual;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.util.ArrayList;

/**
 * Created by 93942 on 2018/12/18.
 */

public class MassGraveDigger extends RectDigger {
  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(5, 9), Random.IntRange(5, 9));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY_SP);

    Point door = overlapedWall(wall, rect).random();
    Set(level, door, Terrain.BARRICADE);
    level.addItemToSpawn(new PotionOfLiquidFlame());

    level.customTiles.addAll(Bones.CustomTilesForRect(rect, Bones.class));

    // 50% 1 skeleton, 50% 2 skeletons
    for (int i = 0; i <= Random.Int(2); ++i) {
      Skeleton s = new Skeleton();
      do {
        s.pos = level.pointToCell(rect.random());
      } while (level.map[s.pos] != Terrain.EMPTY_SP ||
              level.findMob(s.pos) != null);
      level.mobs.add(s);
    }

    ArrayList<Item> items = new ArrayList<>();
    //100% corpse dust, 2x100% 1 coin, 2x30% coins, 1x60% random item, 1x30% 
    // armor
    items.add(new CorpseDust());
    items.add(new Gold(1));
    items.add(new Gold(1));
    if (Random.Float() <= 0.3f) items.add(new Gold());
    if (Random.Float() <= 0.3f) items.add(new Gold());
    if (Random.Float() <= 0.6f) items.add(Generator.random());
    if (Random.Float() <= 0.3f) items.add(Generator.randomArmor());

    for (Item i : items) {
      int pos;
      do {
        pos = level.pointToCell(rect.random());
      } while (level.map[pos] != Terrain.EMPTY_SP ||
              level.heaps.get(pos) != null);
      Heap h = level.drop(i, pos);
      h.type = Heap.Type.SKELETON;
    }

    return new DigResult(DigResult.Type.LOCKED);
  }

  public static class Bones extends CustomTileVisual {
    {
      name = Messages.get(this, "name");

      tx = Assets.PRISON_QUEST;
      txX = 3;
      txY = 0;
    }

    @Override
    public String desc() {
      if (ofsX == 1 && ofsY == 1) {
        return Messages.get(this, "desc");
      } else {
        return null;
      }
    }
  }
}
