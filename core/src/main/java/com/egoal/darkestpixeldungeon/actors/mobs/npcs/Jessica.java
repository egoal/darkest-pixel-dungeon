package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import android.hardware.camera2.DngCreator;
import android.telecom.Call;

import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.books.Book;
import com.egoal.darkestpixeldungeon.items.books.textbook.CallysDiary;
import com.egoal.darkestpixeldungeon.items.rings.Ring;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.PrisonLevel;
import com.egoal.darkestpixeldungeon.levels.Room;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.VillageLevel;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.plants.Sungrass;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.JessicaSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndQuest;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.security.cert.TrustAnchor;
import java.util.Collection;
import java.util.Collections;

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
      CallysDiary cd = Dungeon.hero.belongings.getItem(CallysDiary.class);
      if (cd == null) {
        tell(Messages.get(this, "please"));
        Quest.given_ = true;
      } else {
        cd.detach(Dungeon.hero.belongings.backpack);
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

  private void tell(String text) {
    GameScene.show(new WndQuest(this, text));
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

    public static boolean spawnJessica(VillageLevel level, Room villageRoom) {
      Jessica j = new Jessica();
      reset();  // reset quest
      do {
        j.pos = level.pointToCell(villageRoom.random(1));
      }
      while (level.findMob(j.pos) != null || !level.passable[j.pos] || level
              .map[j.pos] == Terrain.SIGN);
      level.mobs.add(j);

      return true;
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
        heap.drop(Generator.random(Generator.Category.RING).random());

        heap.pos = level.randomRespawnCell();
        level.heaps.put(heap.pos, heap);

//        level.heaps.put(level.randomRespawnCell(), heap);
        spawned_ = true;
      }

      return true;
    }
  }

}
