package com.egoal.darkestpixeldungeon.levels.diggers;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.items.keys.IronKey;
import com.egoal.darkestpixeldungeon.items.potions.Potion;
import com.egoal.darkestpixeldungeon.items.scrolls.Scroll;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

import java.io.FileInputStream;

/**
 * Created by 93942 on 2018/12/5.
 */

public class LibraryDigger extends RectDigger {
  protected Point chooseRoomSize(XWall wall) {
    return new Point(Random.IntRange(3, 6), Random.IntRange(3, 6));
  }

  @Override
  public DigResult dig(Level level, XWall wall, XRect rect) {
    Fill(level, rect, Terrain.EMPTY);

    Point door = overlapedWall(wall, rect).random();
    Set(level, door, Terrain.LOCKED_DOOR);

    // book shelf
    Point sa = null, sb = null;
    switch (wall.direction) {
      case LEFT:
        LinkV(level, rect.x1, rect.y1, rect.y2, Terrain.BOOKSHELF);
        sa = new Point(door.x - 1, door.y - 1);
        sb = new Point(door.x - 1, door.y + 1);
        break;
      case RIGHT:
        LinkV(level, rect.x2, rect.y1, rect.y2, Terrain.BOOKSHELF);
        sa = new Point(door.x + 1, door.y - 1);
        sb = new Point(door.x + 1, door.y + 1);
        break;
      case UP:
        LinkH(level, rect.y1, rect.x1, rect.x2, Terrain.BOOKSHELF);
        sa = new Point(door.x - 1, door.y - 1);
        sb = new Point(door.x + 1, door.y - 1);
        break;
      case DOWN:
        LinkH(level, rect.y2, rect.x1, rect.x2, Terrain.BOOKSHELF);
        sa = new Point(door.x - 1, door.y + 1);
        sb = new Point(door.x + 1, door.y + 1);
        break;
    }

    // statuary
    if (sa != null && level.map[level.pointToCell(sa)] == Terrain.EMPTY)
      Set(level, sa, Terrain.STATUE);
    if (sb != null && level.map[level.pointToCell(sb)] == Terrain.EMPTY)
      Set(level, sb, Terrain.STATUE);

    // items
    int n = Random.IntRange(2, 3);
    for (int i = 0; i < n; ++i) {
      int pos;
      do {
        pos = level.pointToCell(rect.random());
      }
      while (level.map[pos] != Terrain.EMPTY || level.heaps.get(pos) != null);

      level.drop(prize(level), pos);
    }

    level.addItemToSpawn(new IronKey(Dungeon.depth));

    return new DigResult(DigResult.Type.LOCKED);
  }

  private static Item prize(Level level) {
    Item prize = level.findPrizeItem(Scroll.class);
    if (prize == null)
      prize = Generator.random(Generator.Category.SCROLL);

    return prize;
  }
}
