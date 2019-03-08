package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.effects.particles.ElmoParticle;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.KGenerator;
import com.egoal.darkestpixeldungeon.items.unclassified.Gold;
import com.egoal.darkestpixeldungeon.items.Heap;
import com.egoal.darkestpixeldungeon.items.Item;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.levels.Terrain;
import com.egoal.darkestpixeldungeon.levels.diggers.Rect;
import com.egoal.darkestpixeldungeon.levels.traps.RockfallTrap;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.MobSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndOptions;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

/**
 * Created by 93942 on 10/13/2018.
 */

public class Questioner extends NPC {
  {
    spriteClass = Sprite.class;

    properties.add(Property.IMMOVABLE);
  }

  private String question = "pain";
  private Rect heldRect = null;

  public Questioner hold(Rect rect) {
    heldRect = rect;
    return this;
  }

  @Override
  public boolean interact() {
    String content = Messages.get(this, question);
    String[] answers = new String[]{
            Messages.get(this, question + "_0"),
            Messages.get(this, question + "_1"),
            Messages.get(this, "attack")
    };
    GameScene.show(new WndOptions(new Sprite(), name, content, answers) {
      @Override
      protected void onSelect(int index) {
        onAnswered(index);
      }
    });

    return true;
  }

  public Questioner random() {
    question = Random.oneOf("pain", "goal", "honour", "fear");
    return this;
  }

  private void onAnswered(int index) {
    if (index == 2) {
      yell(Messages.get(this, "silly"));
      RockfallTrap.fallRocks(pos);
    } else {
      yell(Messages.get(this, question + "_pass"));
      Dungeon.hero.takeDamage(new Damage(Random.Int(2, 6), this, Dungeon
              .hero).type(Damage.Type.MENTAL));
      GLog.n(Messages.get(this, "tough"));
    }

    // paint the room
    if (heldRect != null) {
      float mimicratio = index == 2 ? .35f : .2f;
      randomPlaceItem(new Gold().random(), Random.Float() < mimicratio);

      Item sp = null;
      switch (index) {
        case 0:
          sp = Random.oneOf(KGenerator.POTION.INSTANCE,
                  KGenerator.SCROLL.INSTANCE).generate();
          break;
        case 1:
          sp = Random.oneOf(KGenerator.WEAPON.INSTANCE,
                  KGenerator.ARMOR.INSTANCE).generate();
          break;
        case 2:
          sp = new Gold().random();
          break;
      }
      randomPlaceItem(sp, Random.Float() < mimicratio);

      if (index == 2 && Random.Float() < .6f) {
        randomPlaceItem(Random.oneOf(KGenerator.HELMET.INSTANCE, 
                KGenerator.WAND.INSTANCE, 
                KGenerator.ARTIFACT.INSTANCE).generate(), true);
      }
    }

    open();
  }

  private void randomPlaceItem(Item item, boolean mimic) {
    Heap heap = new Heap();
    heap.type = mimic ? Heap.Type.MIMIC : Heap.Type.CHEST;
    heap.drop(item);

    do {
      heap.pos = Dungeon.level.pointToCell(heldRect.random(0));
    } while (!Level.passable[heap.pos] || Dungeon.level.heaps.get(heap.pos) !=
            null);

    Dungeon.level.heaps.put(heap.pos, heap);
    GameScene.add(heap);
  }

  private void open() {
    // destroy wall
    Level.set(pos, Terrain.EMBERS);
    GameScene.updateMap(pos);
    Dungeon.observe();

    // die
    die(null);
  }

  private static final String THE_ROOM = "room";
  private static final String QUESTION = "question";

  @Override
  public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);

    bundle.put(QUESTION, question);
    if (heldRect != null)
      bundle.put(THE_ROOM, heldRect);
  }

  @Override
  public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);

    question = bundle.getString(QUESTION);
    Bundlable b = bundle.get(THE_ROOM);
    if (b != null)
      heldRect = (Rect) b;
  }

  // unbreakable
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

  public static class Sprite extends MobSprite {
    public Sprite() {
      super();

      texture(Assets.QUESTIONER);

      // animations
      TextureFilm frames = new TextureFilm(texture, 16, 16);
      idle = new Animation(2, true);
      idle.frames(frames, 0, 1, 2, 3);

      run = new MovieClip.Animation(1, true);
      run.frames(frames, 0);

      die = new MovieClip.Animation(1, false);
      die.frames(frames, 0);

      play(idle);
    }

    @Override
    public void die() {
      super.die();
      emitter().burst(ElmoParticle.FACTORY, 4);

      if (visible)
        Sample.INSTANCE.play(Assets.SND_BURNING);
    }
  }
}
