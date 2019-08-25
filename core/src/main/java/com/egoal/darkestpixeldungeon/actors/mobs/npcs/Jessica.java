package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.books.textbook.CallysDiary;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.sprites.JessicaSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 5/5/2018.
 */

public class Jessica extends NPC {

  {
    spriteClass = JessicaSprite.class;
  }

  /// do something
  @Override
  public boolean interact() {
    //todo: solve the multiple book bug!!
    sprite.turnTo(pos, Dungeon.hero.pos);
    if (!Quest.completed_) {
      CallysDiary cd = Dungeon.hero.getBelongings().getItem(CallysDiary.class);
      if (cd == null) {
        tell(Messages.get(this, "please"));
        Quest.given_ = true;
      } else {
        cd.detach(Dungeon.hero.getBelongings().backpack);
        GLog.w(Messages.get(this, "return_book"));
        Quest.completed_ = true;
        tell(Messages.get(this, "thank_you"));
      }
    } else {
      tell(Messages.get(this, "farewell"));
    }

    return false;
  }

  // unbreakable
  @Override
  public boolean reset() {
    return true;
  }

  @Override
  protected boolean act() {
    throwItem();
    return super.act();
  }

  @Override
  public int defenseSkill(Char enemy) {
    return 1000;
  }

  @Override
  public int takeDamage(Damage dmg) {
    return 0;
  }

  @Override
  public void add(Buff buff) {
  }

  @Override
  public String description() {
    return Messages.get(this, Quest.completed_ ? "desc_2" : "desc");
  }

  public static class Quest {

    public static boolean spawned_;
    public static boolean given_;
    public static boolean completed_;

    public static void reset() {
      spawned_ = false;
      given_ = false;
      completed_ = false;
    }

    // bundle
    private static final String NODE = "jessica";

    private static final String SPAWNED = "spawned";
    private static final String GIVEN = "given";
    private static final String COMPLETED = "completed";

    public static void storeInBundle(Bundle bundle) {
      Bundle node = new Bundle();
      node.put(SPAWNED, spawned_);
      node.put(GIVEN, given_);
      node.put(COMPLETED, completed_);

      bundle.put(NODE, node);
    }

    public static void restoreFromBundle(Bundle bundle) {
      Bundle node = bundle.getBundle(NODE);
      if (!node.isNull()) {
        spawned_ = node.getBoolean(SPAWNED);
        given_ = node.getBoolean(GIVEN);
        completed_ = node.getBoolean(COMPLETED);
      } else {
        reset();
      }
    }

    // prison level indeed
    public static boolean spawnBook(Level level) {
      if (!given_ || spawned_)
        return true;

      if (Dungeon.depth > 5 && Random.Int(10 - Dungeon.depth) == 0) {
        Heap heap = new Heap();
        heap.type = Heap.Type.SKELETON;
        // heap.drop(new Book().setTitle(Book.Title.COLLIES_DIARY));
        heap.drop(new CallysDiary());
        heap.drop(Generator.RING.INSTANCE.generate());

        heap.pos = level.randomRespawnCell();
        level.heaps.put(heap.pos, heap);

//        level.heaps.put(level.randomRespawnCell(), heap);
        spawned_ = true;
      }

      return true;
    }
  }

}
