/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.egoal.darkestpixeldungeon.actors.mobs.npcs;

import com.egoal.darkestpixeldungeon.Journal;
import com.egoal.darkestpixeldungeon.DarkestPixelDungeon;
import com.egoal.darkestpixeldungeon.actors.Char;
import com.egoal.darkestpixeldungeon.actors.Damage;
import com.egoal.darkestpixeldungeon.actors.buffs.Buff;
import com.egoal.darkestpixeldungeon.actors.mobs.GreatCrab;
import com.egoal.darkestpixeldungeon.actors.mobs.Mob;
import com.egoal.darkestpixeldungeon.effects.CellEmitter;
import com.egoal.darkestpixeldungeon.items.Generator;
import com.egoal.darkestpixeldungeon.items.KGenerator;
import com.egoal.darkestpixeldungeon.items.armor.Armor;
import com.egoal.darkestpixeldungeon.items.artifacts.DriedRose;
import com.egoal.darkestpixeldungeon.items.weapon.Weapon;
import com.egoal.darkestpixeldungeon.levels.Level;
import com.egoal.darkestpixeldungeon.windows.WndSadGhost;
import com.egoal.darkestpixeldungeon.Assets;
import com.egoal.darkestpixeldungeon.Dungeon;
import com.egoal.darkestpixeldungeon.actors.buffs.Paralysis;
import com.egoal.darkestpixeldungeon.actors.buffs.Roots;
import com.egoal.darkestpixeldungeon.actors.mobs.FetidRat;
import com.egoal.darkestpixeldungeon.actors.mobs.GnollTrickster;
import com.egoal.darkestpixeldungeon.effects.Speck;
import com.egoal.darkestpixeldungeon.items.armor.LeatherArmor;
import com.egoal.darkestpixeldungeon.items.armor.MailArmor;
import com.egoal.darkestpixeldungeon.items.armor.PlateArmor;
import com.egoal.darkestpixeldungeon.items.armor.ScaleArmor;
import com.egoal.darkestpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.egoal.darkestpixeldungeon.items.weapon.melee.NewShortsword;
import com.egoal.darkestpixeldungeon.messages.Messages;
import com.egoal.darkestpixeldungeon.scenes.GameScene;
import com.egoal.darkestpixeldungeon.sprites.GhostSprite;
import com.egoal.darkestpixeldungeon.utils.GLog;
import com.egoal.darkestpixeldungeon.windows.WndQuest;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.HashSet;

public class Ghost extends NPC {

  {
    spriteClass = GhostSprite.class;

    flying = true;

    state = WANDERING;
  }

  public Ghost() {
    super();

    Sample.INSTANCE.load(Assets.SND_GHOST);
  }

  @Override
  protected boolean act() {
    if (Quest.completed())
      target = Dungeon.hero.pos;
    return super.act();
  }

  @Override
  public int defenseSkill(Char enemy) {
    return 1000;
  }

  @Override
  public float speed() {
    return Quest.completed() ? 2f : 0.5f;
  }

  @Override
  protected Char chooseEnemy() {
    return null;
  }

  @Override
  public int takeDamage(Damage dmg) {
    return 0;
  }

  @Override
  public void add(Buff buff) {
  }

  @Override
  public boolean reset() {
    return true;
  }

  @Override
  public boolean interact() {
    sprite.turnTo(pos, Dungeon.hero.pos);

    Sample.INSTANCE.play(Assets.SND_GHOST);

    if (Quest.given) {
      if (Quest.weapon != null) {
        if (Quest.processed) {
          GameScene.show(new WndSadGhost(this, Quest.type));
        } else {
          switch (Quest.type) {
            case 1:
            default:
              GameScene.show(new WndQuest(this, Messages.get(this, "rat_2")));
              break;
            case 2:
              GameScene.show(new WndQuest(this, Messages.get(this, "gnoll_2")));
              break;
            case 3:
              GameScene.show(new WndQuest(this, Messages.get(this, "crab_2")));
              break;
          }

          int newPos = -1;
          for (int i = 0; i < 10; i++) {
            newPos = Dungeon.level.randomRespawnCell();
            if (newPos != -1) {
              break;
            }
          }
          if (newPos != -1) {

            CellEmitter.get(pos).start(Speck.factory(Speck.LIGHT), 0.2f, 3);
            pos = newPos;
            sprite.place(pos);
            sprite.visible = Dungeon.visible[pos];
          }
        }
      }
    } else {
      Mob questBoss;
      String txt_quest;

      switch (Quest.type) {
        case 1:
        default:
          questBoss = new FetidRat();
          txt_quest = Messages.get(this, "rat_1", Dungeon.hero.givenName());
          break;
        case 2:
          questBoss = new GnollTrickster();
          txt_quest = Messages.get(this, "gnoll_1", Dungeon.hero.givenName());
          break;
        case 3:
          questBoss = new GreatCrab();
          txt_quest = Messages.get(this, "crab_1", Dungeon.hero.givenName());
          break;
      }

      questBoss.pos = Dungeon.level.randomRespawnCell();

      if (questBoss.pos != -1) {
        GameScene.add(questBoss);
        GameScene.show(new WndQuest(this, txt_quest));
        Quest.given = true;
        Journal.INSTANCE.add(name);
      }

    }

    return false;
  }

  private static final HashSet<Class<?>> IMMUNITIES = new HashSet<>();

  static {
    IMMUNITIES.add(Paralysis.class);
    IMMUNITIES.add(Roots.class);
  }

  @Override
  public HashSet<Class<?>> immunizedBuffs() {
    return IMMUNITIES;
  }

  public static class Quest {

    private static boolean spawned;

    private static int type;

    private static boolean given;
    private static boolean processed;

    private static int depth;

    public static Weapon weapon;
    public static Armor armor;

    public static void reset() {
      spawned = false;

      weapon = null;
      armor = null;
    }

    private static final String NODE = "sadGhost";

    private static final String SPAWNED = "spawned";
    private static final String TYPE = "type";
    private static final String GIVEN = "given";
    private static final String PROCESSED = "processed";
    private static final String DEPTH = "depth";
    private static final String WEAPON = "weapon";
    private static final String ARMOR = "armor";

    public static void storeInBundle(Bundle bundle) {

      Bundle node = new Bundle();

      node.put(SPAWNED, spawned);

      if (spawned) {

        node.put(TYPE, type);

        node.put(GIVEN, given);
        node.put(DEPTH, depth);
        node.put(PROCESSED, processed);

        node.put(WEAPON, weapon);
        node.put(ARMOR, armor);
      }

      bundle.put(NODE, node);
    }

    public static void restoreFromBundle(Bundle bundle) {

      Bundle node = bundle.getBundle(NODE);

      if (!node.isNull() && (spawned = node.getBoolean(SPAWNED))) {

        type = node.getInt(TYPE);
        given = node.getBoolean(GIVEN);
        processed = node.getBoolean(PROCESSED);

        depth = node.getInt(DEPTH);

        weapon = (Weapon) node.get(WEAPON);
        armor = (Armor) node.get(ARMOR);
      } else {
        reset();
      }
    }

    public static void Spawn(Level level) {
      if (spawned || Dungeon.depth <= 1 || Random.Int(5 - Dungeon.depth) != 0)
        return;

      // spawn
      Ghost ghost = new Ghost();
      do {
        ghost.pos = level.randomRespawnCell();
      } while (ghost.pos == -1);
      level.mobs.add(ghost);

      spawned = true;
      // 2: fetid rat, 3: gnoll trickster, 4: great crab
      type = Dungeon.depth - 1;

      given = false;
      processed = false;
      depth = Dungeon.depth;

      PreparePrize();
    }

    public static void process() {
      if (spawned && given && !processed && (depth == Dungeon.depth)) {
        GLog.n(Messages.get(Ghost.class, "find_me"));
        Sample.INSTANCE.play(Assets.SND_GHOST);
        processed = true;
        // now the rose can spawn.
        KGenerator.ARTIFACT.INSTANCE.getProbMap().put(DriedRose.class, 1f);
      }
    }

    public static void complete() {
      weapon = null;
      armor = null;

      Journal.INSTANCE.remove(Messages.get(Ghost.class, "name"));
    }

    public static boolean completed() {
      return spawned && processed;
    }

    private static void PreparePrize() {
      //50%:tier2, 30%:tier3, 15%:tier4, 5%:tier5
      float itemTierRoll = Random.Float();
      int wepTier;

      if (itemTierRoll < 0.5f) {
        wepTier = 2;
        armor = new LeatherArmor();
      } else if (itemTierRoll < 0.8f) {
        wepTier = 3;
        armor = new MailArmor();
      } else if (itemTierRoll < 0.95f) {
        wepTier = 4;
        armor = new ScaleArmor();
      } else {
        wepTier = 5;
        armor = new PlateArmor();
      }

      do {
        weapon = (Weapon) KGenerator.WEAPON.MELEE.INSTANCE.tier(wepTier - 1).generate();
      } while (!(weapon instanceof MeleeWeapon));
      weapon.level(0);
      weapon.cursed = false;
      weapon.enchant(null);

      //50%:+0, 30%:+1, 15%:+2, 5%:+3
      float itemLevelRoll = Random.Float();
      int itemLevel;
      if (itemLevelRoll < 0.4f) {
        itemLevel = 0;
      } else if (itemLevelRoll < 0.8f) {
        itemLevel = 1;
      } else if (itemLevelRoll < 0.95f) {
        itemLevel = 2;
      } else {
        itemLevel = 3;
      }
      weapon.upgrade(itemLevel);
      armor.upgrade(itemLevel);

      //10% to be enchanted
      if (Random.Int(10) == 0) {
        weapon.enchant();
        armor.inscribe();
      }

      weapon.identify();
      armor.identify();
    }
  }
}
